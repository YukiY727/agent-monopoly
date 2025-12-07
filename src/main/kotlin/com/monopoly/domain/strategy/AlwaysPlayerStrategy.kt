package com.monopoly.domain.strategy

import com.monopoly.domain.model.player.PlayerStrategy
import com.monopoly.domain.model.property.Property
import com.monopoly.domain.model.property.StreetProperty

class AlwaysPlayerStrategy : PlayerStrategy {
    override fun shouldBuy(
        property: Property,
        currentMoney: Int,
    ): Boolean {
        return currentMoney >= property.price
    }

    override fun shouldBuildHouse(
        property: StreetProperty,
        currentMoney: Int,
    ): Boolean {
        return currentMoney >= property.houseCost
    }

    override fun shouldBuildHotel(
        property: StreetProperty,
        currentMoney: Int,
    ): Boolean {
        return currentMoney >= property.hotelCost
    }

    override fun shouldPayToEscapeJail(currentMoney: Int): Boolean {
        return currentMoney >= 50
    }
}
