package com.monopoly.domain.strategy

import com.monopoly.domain.model.Property
import com.monopoly.domain.model.StreetProperty
import com.monopoly.domain.model.BuyStrategy

class AlwaysBuyStrategy : BuyStrategy {
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
}
