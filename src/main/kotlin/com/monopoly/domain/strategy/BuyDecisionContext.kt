package com.monopoly.domain.strategy

import com.monopoly.domain.model.Board
import com.monopoly.domain.model.Player
import com.monopoly.domain.model.Property

/**
 * 購入判断に必要なコンテキスト情報
 *
 * 高度な戦略が購入判断を行う際に必要な情報を集約したデータクラス。
 * シンプルな戦略はこのコンテキストを使用せず、property と currentMoney のみで判断可能。
 */
data class BuyDecisionContext(
    /** 購入対象のプロパティ */
    val property: Property,

    /** プレイヤーの現在の所持金 */
    val playerMoney: Int,

    /** プレイヤーが所有しているプロパティ一覧 */
    val ownedProperties: List<Property>,

    /** ゲームボード（全プロパティ情報） */
    val board: Board,

    /** 全プレイヤー情報（自分を含む） */
    val allPlayers: List<Player>,

    /** 現在のターン数 */
    val currentTurn: Int,
) {
    /**
     * 他のプレイヤー一覧（自分以外）
     */
    val otherPlayers: List<Player>
        get() = allPlayers.filter { player ->
            // 所有プロパティリストが一致しないプレイヤー = 他人
            player.properties != ownedProperties
        }

    /**
     * 指定したカラーグループで自分が所有しているプロパティ数
     */
    fun countOwnedInColorGroup(colorGroup: String): Int {
        return ownedProperties.count { it.colorGroup == colorGroup }
    }

    /**
     * 指定したカラーグループで他プレイヤーが所有している最大プロパティ数
     */
    fun maxOtherPlayerCountInColorGroup(colorGroup: String): Int {
        return otherPlayers.maxOfOrNull { player ->
            player.properties.count { it.colorGroup == colorGroup }
        } ?: 0
    }

    /**
     * 購入後の所持金
     */
    val moneyAfterPurchase: Int
        get() = playerMoney - property.price
}
