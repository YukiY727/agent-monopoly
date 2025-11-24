package com.monopoly.domain.strategy

import com.monopoly.domain.model.Property
import com.monopoly.domain.model.BuyStrategy

class AlwaysBuyStrategy : BuyStrategy {
    override fun shouldBuy(
        property: Property,
        currentMoney: Int,
    ): Boolean {
        return currentMoney >= property.price
    }
}
