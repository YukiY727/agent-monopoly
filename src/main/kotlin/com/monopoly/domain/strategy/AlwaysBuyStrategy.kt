package com.monopoly.domain.strategy

import com.monopoly.domain.model.Property

class AlwaysBuyStrategy : BuyStrategy {
    override fun shouldBuy(
        property: Property,
        currentMoney: Int,
    ): Boolean {
        return currentMoney >= property.price
    }
}
