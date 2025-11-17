package com.monopoly.domain.strategy

import com.monopoly.domain.model.Property

/**
 * 高収益のプロパティを優先する戦略
 *
 * レントが高いプロパティを優先的に購入します。
 *
 * @property minRent 購入する最低レント額
 * @property minCashReserve 最低現金残高（低め設定）
 */
class HighValueStrategy(
    private val minRent: Int = 20,
    private val minCashReserve: Int = 100,
) : BuyStrategy {
    override fun shouldBuy(context: BuyDecisionContext): Boolean {
        return context.property.rent >= minRent &&
            context.moneyAfterPurchase >= minCashReserve
    }

    override fun shouldBuy(property: Property, currentMoney: Int): Boolean {
        return property.rent >= minRent &&
            (currentMoney - property.price) >= minCashReserve
    }
}
