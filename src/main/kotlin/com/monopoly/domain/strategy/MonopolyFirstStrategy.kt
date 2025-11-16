package com.monopoly.domain.strategy

import com.monopoly.domain.model.Property

/**
 * カラーグループの独占を優先する戦略
 *
 * 既に所有しているカラーグループのプロパティを優先的に購入し、
 * 独占を完成させることを目指します。
 *
 * @property blockOpponentMonopoly 相手の独占を阻止するか
 * @property minCashReserve 最低現金残高
 */
class MonopolyFirstStrategy(
    private val blockOpponentMonopoly: Boolean = true,
    private val minCashReserve: Int = 300,
) : BuyStrategy {
    override fun shouldBuy(context: BuyDecisionContext): Boolean {
        val colorGroup = context.property.colorGroup

        // スコアリング
        var score = 0

        // 自分が同じカラーグループを所有している数
        val ownedCount = context.countOwnedInColorGroup(colorGroup)
        when (ownedCount) {
            1 -> score += 50
            2 -> score += 100  // 独占完成間近
        }

        // 購入で独占が完成する場合
        val totalInGroup = context.board.getPropertiesByColorGroup(colorGroup).size
        if (ownedCount + 1 == totalInGroup) {
            score += 200  // 独占完成は最優先
        }

        // 他プレイヤーの独占を阻止
        if (blockOpponentMonopoly) {
            val maxOtherCount = context.maxOtherPlayerCountInColorGroup(colorGroup)
            if (maxOtherCount >= 2) {
                score += 80  // 相手の独占を阻止
            }
        }

        // 現金が足りない場合はスコアを0に
        if (context.moneyAfterPurchase < minCashReserve) {
            score = 0
        }

        return score >= 50
    }

    override fun shouldBuy(property: Property, currentMoney: Int): Boolean {
        throw NotImplementedError(
            "MonopolyFirstStrategy はコンテキスト版のshouldBuyを使用してください"
        )
    }
}
