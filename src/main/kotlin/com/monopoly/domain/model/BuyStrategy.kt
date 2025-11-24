package com.monopoly.domain.model

interface BuyStrategy {
    fun shouldBuy(
        property: Property,
        currentMoney: Int,
    ): Boolean
}
