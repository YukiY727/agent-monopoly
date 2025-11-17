package com.monopoly.domain.strategy

import com.monopoly.domain.model.Property

/**
 * 一定額以上の現金を保持する保守的な戦略
 *
 * 購入後の所持金が閾値以上になる場合のみプロパティを購入します。
 * リスクを抑えた戦略で、破産しにくい傾向があります。
 *
 * @property minimumCashReserve 保持する最小現金額（デフォルト: $500）
 */
class ConservativeStrategy(
    private val minimumCashReserve: Int = 500,
) : BuyStrategy {
    override fun shouldBuy(
        property: Property,
        playerMoney: Int,
    ): Boolean {
        // 購入後の所持金が閾値以上なら購入
        val moneyAfterPurchase: Int = playerMoney - property.price
        return moneyAfterPurchase >= minimumCashReserve
    }
}
