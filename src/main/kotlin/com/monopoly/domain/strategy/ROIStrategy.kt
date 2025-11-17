package com.monopoly.domain.strategy

import com.monopoly.domain.model.Property

/**
 * 投資収益率（ROI）を重視する戦略
 *
 * プロパティの価格に対してレントが高い物件を優先的に購入します。
 *
 * @property minROI 最低ROI閾値（デフォルト: 0.15 = 15%）
 * @property minCashReserve 最低現金残高
 */
class ROIStrategy(
    private val minROI: Double = 0.15,
    private val minCashReserve: Int = 300,
) : BuyStrategy {
    override fun shouldBuy(context: BuyDecisionContext): Boolean {
        val roi = calculateROI(context.property)
        return roi >= minROI && context.moneyAfterPurchase >= minCashReserve
    }

    override fun shouldBuy(property: Property, currentMoney: Int): Boolean {
        throw NotImplementedError(
            "ROIStrategy はコンテキスト版のshouldBuyを使用してください"
        )
    }

    /**
     * ROI（投資収益率）を計算
     *
     * @param property プロパティ
     * @return ROI（基本レント ÷ 価格）
     */
    private fun calculateROI(property: Property): Double {
        return property.rent.toDouble() / property.price.toDouble()
    }
}
