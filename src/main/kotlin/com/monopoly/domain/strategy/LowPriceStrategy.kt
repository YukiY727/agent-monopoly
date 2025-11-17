package com.monopoly.domain.strategy

import com.monopoly.domain.model.Property

/**
 * 低価格のプロパティを優先する戦略
 *
 * 安いプロパティから購入し、早期に多くのプロパティを確保します。
 *
 * @property maxPrice 購入する最大価格
 * @property minCashReserve 最低現金残高
 */
class LowPriceStrategy(
    private val maxPrice: Int = 200,
    private val minCashReserve: Int = 200,
) : BuyStrategy {
    override fun shouldBuy(context: BuyDecisionContext): Boolean {
        return context.property.price <= maxPrice &&
            context.moneyAfterPurchase >= minCashReserve
    }

    override fun shouldBuy(property: Property, currentMoney: Int): Boolean {
        // シンプルな戦略なので、旧インターフェースでも実装可能
        return property.price <= maxPrice &&
            (currentMoney - property.price) >= minCashReserve
    }
}
