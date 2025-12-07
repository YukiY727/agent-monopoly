package com.monopoly.domain.model.player

import com.monopoly.domain.model.property.Property
import com.monopoly.domain.model.property.StreetProperty

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
