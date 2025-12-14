package com.monopoly.domain.service

import com.monopoly.domain.model.player.Player
import com.monopoly.domain.model.property.ColorGroup
import com.monopoly.domain.model.property.StreetProperty
import com.monopoly.domain.model.trade.TradeOffer

/**
 * トレード戦略を支援するサービス
 *
 * モノポリー完成を目指したトレード提案・評価ロジックを提供する
 */
object TradeHelperService {
    // 各カラーグループに含まれるプロパティ数
    private val COLOR_GROUP_SIZES: Map<ColorGroup, Int> =
        mapOf(
            ColorGroup.BROWN to 2,
            ColorGroup.LIGHT_BLUE to 3,
            ColorGroup.PINK to 3,
            ColorGroup.ORANGE to 3,
            ColorGroup.RED to 3,
            ColorGroup.YELLOW to 3,
            ColorGroup.GREEN to 3,
            ColorGroup.DARK_BLUE to 2,
        )

    /**
     * プレイヤーの各カラーグループの所有状況を取得
     *
     * @param player プレイヤー
     * @return カラーグループ -> 所有数のマップ
     */
    fun getColorGroupOwnership(player: Player): Map<ColorGroup, Int> {
        return player.ownedProperties
            .filterIsInstance<StreetProperty>()
            .groupBy { it.colorGroup }
            .mapValues { it.value.size }
    }

    /**
     * モノポリー完成に近いカラーグループを見つける（1つ足りない）
     *
     * @param player プレイヤー
     * @return 1つ足りないカラーグループのリスト
     */
    fun findNearMonopolyGroups(player: Player): List<ColorGroup> {
        val ownership: Map<ColorGroup, Int> = getColorGroupOwnership(player)
        return COLOR_GROUP_SIZES.filter { (colorGroup, required) ->
            val owned: Int = ownership[colorGroup] ?: 0
            owned == required - 1 // 1つ足りない
        }.keys.toList()
    }

    /**
     * 不要なカラーグループ（1つしか持っていない3物件グループ）を見つける
     *
     * @param player プレイヤー
     * @return 不要なカラーグループのリスト
     */
    fun findLowValueGroups(player: Player): List<ColorGroup> {
        val ownership: Map<ColorGroup, Int> = getColorGroupOwnership(player)
        return COLOR_GROUP_SIZES.filter { (colorGroup, required) ->
            val owned: Int = ownership[colorGroup] ?: 0
            owned == 1 && required >= 3 // 3物件グループで1つしか持っていない
        }.keys.toList()
    }

    /**
     * トレード提案を作成する
     *
     * 戦略:
     * 1. 自分が1つ足りないカラーグループを特定
     * 2. その足りない物件を持っている相手を探す
     * 3. 自分が1つしか持っていないカラーグループの物件を提供
     * 4. 必要に応じて現金を追加
     *
     * @param currentPlayer 現在のプレイヤー
     * @param otherPlayers 他のプレイヤーリスト
     * @param maxMoneyOffer 提供する最大金額
     * @return トレード提案（null = 適切なトレードがない）
     */
    fun createTradeOffer(
        currentPlayer: Player,
        otherPlayers: List<Player>,
        maxMoneyOffer: Int,
    ): TradeOffer? {
        val nearMonopolyGroups: List<ColorGroup> = findNearMonopolyGroups(currentPlayer)
        val lowValueGroups: List<ColorGroup> = findLowValueGroups(currentPlayer)

        if (nearMonopolyGroups.isEmpty()) {
            return null // モノポリーに近いグループがない
        }

        // 各near-monopolyグループについて、足りない物件を持っている相手を探す
        for (targetGroup in nearMonopolyGroups) {
            for (otherPlayer in otherPlayers) {
                if (otherPlayer.isBankrupt) continue

                // 相手が持っている、自分が欲しい物件
                val wantedProperty: StreetProperty? =
                    otherPlayer.ownedProperties
                        .filterIsInstance<StreetProperty>()
                        .filter { it.colorGroup == targetGroup }
                        .filter { it.buildings.houseCount == 0 && !it.buildings.hasHotel }
                        .firstOrNull()

                if (wantedProperty == null) continue

                // 自分が提供できる物件（相手がモノポリー完成に近づけるもの、または不要なもの）
                val offerProperty: StreetProperty? = findPropertyToOffer(currentPlayer, otherPlayer, lowValueGroups)

                if (offerProperty != null) {
                    // 物件交換
                    return try {
                        TradeOffer(
                            proposer = currentPlayer,
                            target = otherPlayer,
                            offeredProperties = listOf(offerProperty),
                            requestedProperties = listOf(wantedProperty),
                        )
                    } catch (e: IllegalArgumentException) {
                        null // 無効なトレード
                    }
                } else if (maxMoneyOffer > 0) {
                    // 現金で購入
                    val offerAmount: Int = (wantedProperty.price * 1.5).toInt().coerceAtMost(maxMoneyOffer)
                    if (offerAmount > 0 && currentPlayer.money >= offerAmount) {
                        return try {
                            TradeOffer(
                                proposer = currentPlayer,
                                target = otherPlayer,
                                offeredMoney = offerAmount,
                                requestedProperties = listOf(wantedProperty),
                            )
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    }
                }
            }
        }

        return null
    }

    /**
     * 相手に提供する物件を見つける
     *
     * 優先順位:
     * 1. 相手がモノポリー完成に近いグループで自分が持っている物件
     * 2. 自分が1つしか持っていないカラーグループの物件
     */
    private fun findPropertyToOffer(
        currentPlayer: Player,
        otherPlayer: Player,
        lowValueGroups: List<ColorGroup>,
    ): StreetProperty? {
        val otherNearMonopoly: List<ColorGroup> = findNearMonopolyGroups(otherPlayer)

        // 相手のnear-monopolyに貢献できる物件
        for (group in otherNearMonopoly) {
            val property: StreetProperty? =
                currentPlayer.ownedProperties
                    .filterIsInstance<StreetProperty>()
                    .filter { it.colorGroup == group }
                    .filter { it.buildings.houseCount == 0 && !it.buildings.hasHotel }
                    .firstOrNull()
            if (property != null) return property
        }

        // 自分にとって価値の低い物件
        for (group in lowValueGroups) {
            val property: StreetProperty? =
                currentPlayer.ownedProperties
                    .filterIsInstance<StreetProperty>()
                    .filter { it.colorGroup == group }
                    .filter { it.buildings.houseCount == 0 && !it.buildings.hasHotel }
                    .firstOrNull()
            if (property != null) return property
        }

        return null
    }

    /**
     * トレード提案を評価する
     *
     * 受け入れ条件:
     * 1. モノポリー完成に近づく
     * 2. 相手にモノポリーを与えない（または自分もモノポリーを得る）
     * 3. 金銭的に大きく損しない
     *
     * @param offer トレード提案
     * @param currentPlayer 現在のプレイヤー（受け取り側）
     * @return 受け入れる場合true
     */
    fun evaluateTradeOffer(
        offer: TradeOffer,
        currentPlayer: Player,
    ): Boolean {
        // 受け取る物件でモノポリーが完成するか
        val wouldCompleteMonopoly: Boolean = wouldCompleteMonopoly(offer, currentPlayer)

        // 相手にモノポリーを与えるか
        val wouldGiveOpponentMonopoly: Boolean = wouldGiveOpponentMonopoly(offer)

        // 金銭的な評価
        val monetaryValue: Int = calculateMonetaryValue(offer, currentPlayer)

        // 受け入れ判断
        return when {
            // 自分がモノポリーを完成できる場合は受け入れ
            wouldCompleteMonopoly -> true

            // 相手にモノポリーを与えて自分は得しない場合は拒否
            wouldGiveOpponentMonopoly && !wouldCompleteMonopoly -> false

            // 金銭的に得する場合は受け入れ
            monetaryValue > 0 -> true

            else -> false
        }
    }

    /**
     * トレードでモノポリーが完成するかチェック
     */
    private fun wouldCompleteMonopoly(
        offer: TradeOffer,
        currentPlayer: Player,
    ): Boolean {
        val currentOwnership: Map<ColorGroup, Int> = getColorGroupOwnership(currentPlayer)

        for (property in offer.offeredProperties) {
            if (property is StreetProperty) {
                val group: ColorGroup = property.colorGroup
                val currentCount: Int = currentOwnership[group] ?: 0
                val required: Int = COLOR_GROUP_SIZES[group] ?: 0
                if (currentCount + 1 == required) {
                    return true // このプロパティでモノポリー完成
                }
            }
        }
        return false
    }

    /**
     * トレードで相手にモノポリーを与えるかチェック
     */
    private fun wouldGiveOpponentMonopoly(offer: TradeOffer): Boolean {
        val proposer: Player = offer.proposer
        val proposerOwnership: Map<ColorGroup, Int> = getColorGroupOwnership(proposer)

        for (property in offer.requestedProperties) {
            if (property is StreetProperty) {
                val group: ColorGroup = property.colorGroup
                val proposerCount: Int = proposerOwnership[group] ?: 0
                val required: Int = COLOR_GROUP_SIZES[group] ?: 0
                if (proposerCount + 1 == required) {
                    return true // 提案者がモノポリーを完成する
                }
            }
        }
        return false
    }

    /**
     * トレードの金銭的価値を計算（正 = 得、負 = 損）
     */
    private fun calculateMonetaryValue(
        offer: TradeOffer,
        currentPlayer: Player,
    ): Int {
        // 受け取る物件の価値
        val receivedPropertyValue: Int = offer.offeredProperties.sumOf { it.price }

        // 渡す物件の価値
        val givenPropertyValue: Int = offer.requestedProperties.sumOf { it.price }

        // 現金の授受
        val cashFlow: Int = offer.offeredMoney - offer.requestedMoney

        return receivedPropertyValue - givenPropertyValue + cashFlow
    }
}
