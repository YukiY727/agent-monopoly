package com.monopoly.domain.strategy

import com.monopoly.domain.model.Property

/**
 * バランス型の戦略
 *
 * 複数の要因（カラーグループ、ROI、価格、レント）を総合的に判断します。
 *
 * @property threshold 購入スコア閾値
 * @property minCashReserve 最低現金残高
 */
class BalancedStrategy(
    private val threshold: Int = 80,
    private val minCashReserve: Int = 400,
) : BuyStrategy {
    override fun shouldBuy(context: BuyDecisionContext): Boolean {
        if (context.moneyAfterPurchase < minCashReserve) {
            return false
        }

        val score = calculateScore(context)
        return score >= threshold
    }

    override fun shouldBuy(property: Property, currentMoney: Int): Boolean {
        throw NotImplementedError(
            "BalancedStrategy はコンテキスト版のshouldBuyを使用してください"
        )
    }

    private fun calculateScore(context: BuyDecisionContext): Int {
        var score = 0

        // カラーグループスコア
        val ownedCount = context.countOwnedInColorGroup(context.property.colorGroup)
        score += ownedCount * 20

        // ROIスコア
        val roi = context.property.baseRent.toDouble() / context.property.price.toDouble()
        score += (roi * 100).toInt()

        // 価格スコア（安いほど高スコア）
        score += (500 - context.property.price) / 10

        // レントスコア
        score += context.property.baseRent

        return score
    }
}
