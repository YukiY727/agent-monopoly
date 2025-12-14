package com.monopoly.domain.service

import com.monopoly.domain.model.card.Card
import com.monopoly.domain.model.property.Property
import com.monopoly.domain.model.property.StreetProperty
import com.monopoly.domain.model.trade.TradeOffer

/**
 * トレード（プレイヤー間取引）を管理するサービス
 *
 * 主な責務:
 * - トレード提案のバリデーション
 * - トレードの実行（物件・現金・カードの移動）
 */
class TradeService {
    /**
     * トレード提案が有効かどうかを検証する
     *
     * 検証項目:
     * 1. 提案者が提供物件を所有している
     * 2. 提供物件に建物がない
     * 3. 提案者が提供金額を持っている
     * 4. 提案者が提供カード数を持っている
     * 5. ターゲットが要求物件を所有している
     * 6. 要求物件に建物がない
     * 7. ターゲットが要求金額を持っている
     * 8. ターゲットが要求カード数を持っている
     *
     * @param offer トレード提案
     * @return 有効な場合true
     */
    fun validateOffer(offer: TradeOffer): Boolean {
        // 提案者のバリデーション
        if (!validateProposerAssets(offer)) {
            return false
        }

        // ターゲットのバリデーション
        if (!validateTargetAssets(offer)) {
            return false
        }

        return true
    }

    /**
     * 提案者の資産を検証
     */
    private fun validateProposerAssets(offer: TradeOffer): Boolean {
        val proposer = offer.proposer

        // 提供物件を所有しているか
        for (property in offer.offeredProperties) {
            if (property !in proposer.ownedProperties) {
                return false
            }
            // 建物がないか
            if (property is StreetProperty && property.buildings.houseCount > 0) {
                return false
            }
            if (property is StreetProperty && property.buildings.hasHotel) {
                return false
            }
        }

        // 提供金額を持っているか
        if (proposer.money < offer.offeredMoney) {
            return false
        }

        // 提供カード数を持っているか
        val proposerJailCards: Int = proposer.state.heldCards.count { it is Card.GetOutOfJailFree }
        if (proposerJailCards < offer.offeredJailCards) {
            return false
        }

        return true
    }

    /**
     * ターゲットの資産を検証
     */
    private fun validateTargetAssets(offer: TradeOffer): Boolean {
        val target = offer.target

        // 要求物件を所有しているか
        for (property in offer.requestedProperties) {
            if (property !in target.ownedProperties) {
                return false
            }
            // 建物がないか
            if (property is StreetProperty && property.buildings.houseCount > 0) {
                return false
            }
            if (property is StreetProperty && property.buildings.hasHotel) {
                return false
            }
        }

        // 要求金額を持っているか
        if (target.money < offer.requestedMoney) {
            return false
        }

        // 要求カード数を持っているか
        val targetJailCards: Int = target.state.heldCards.count { it is Card.GetOutOfJailFree }
        if (targetJailCards < offer.requestedJailCards) {
            return false
        }

        return true
    }

    /**
     * トレードを実行する
     *
     * 物件・現金・カードの移動を行う
     *
     * @param offer トレード提案（事前にvalidateOfferで検証済みであること）
     * @return 成功した場合true
     */
    fun executeTrade(offer: TradeOffer): Boolean {
        // バリデーション（念のため再検証）
        if (!validateOffer(offer)) {
            return false
        }

        val proposer = offer.proposer
        val target = offer.target

        // 物件の移動: 提案者 -> ターゲット
        for (property in offer.offeredProperties) {
            transferProperty(proposer, target, property)
        }

        // 物件の移動: ターゲット -> 提案者
        for (property in offer.requestedProperties) {
            transferProperty(target, proposer, property)
        }

        // 現金の移動
        if (offer.offeredMoney > 0) {
            proposer.subtractMoney(offer.offeredMoney)
            target.addMoney(offer.offeredMoney)
        }
        if (offer.requestedMoney > 0) {
            target.subtractMoney(offer.requestedMoney)
            proposer.addMoney(offer.requestedMoney)
        }

        // カードの移動: 提案者 -> ターゲット
        repeat(offer.offeredJailCards) {
            transferJailCard(proposer, target)
        }

        // カードの移動: ターゲット -> 提案者
        repeat(offer.requestedJailCards) {
            transferJailCard(target, proposer)
        }

        return true
    }

    /**
     * 物件を移動する
     */
    private fun transferProperty(
        from: com.monopoly.domain.model.player.Player,
        to: com.monopoly.domain.model.player.Player,
        property: Property,
    ) {
        from.removeProperty(property)
        val newProperty: Property = property.withOwner(to)
        to.acquireProperty(newProperty)
    }

    /**
     * 刑務所脱出カードを移動する
     */
    private fun transferJailCard(
        from: com.monopoly.domain.model.player.Player,
        to: com.monopoly.domain.model.player.Player,
    ) {
        val card: Card.GetOutOfJailFree? = from.useGetOutOfJailFreeCard()
        if (card != null) {
            to.addCard(card)
        }
    }
}
