package com.monopoly.domain.model

interface BuyStrategy {
    fun shouldBuy(
        property: Property,
        currentMoney: Int,
    ): Boolean

    fun shouldBuildHouse(
        property: StreetProperty,
        currentMoney: Int,
    ): Boolean

    fun shouldBuildHotel(
        property: StreetProperty,
        currentMoney: Int,
    ): Boolean
}
