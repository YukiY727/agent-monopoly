package com.monopoly.domain.strategy

import com.monopoly.domain.model.Property

interface BuyStrategy {
    fun shouldBuy(
        property: Property,
        currentMoney: Int,
    ): Boolean
}
