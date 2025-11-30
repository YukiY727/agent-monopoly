package com.monopoly.domain.model

interface PlayerStrategy {
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

    // Phase 3: 刑務所関連
    fun shouldPayToEscapeJail(currentMoney: Int): Boolean
}
