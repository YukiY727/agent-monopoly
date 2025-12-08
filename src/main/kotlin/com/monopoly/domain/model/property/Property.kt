package com.monopoly.domain.model.property

import com.monopoly.domain.model.core.Money
import com.monopoly.domain.model.player.Player

interface Property {
    val name: String
    val position: Int
    val price: Int
    val priceValue: Money
    val ownership: PropertyOwnership
    val rentValue: Money

    fun withOwner(newOwner: Player): Property
    fun withoutOwner(): Property
    fun isOwned(): Boolean

    // Mortgage functionality
    val mortgageValue: Money
        get() = Money(price / 2)

    val unmortgageValue: Money
        get() = Money((price / 2.0 * 1.1).toInt())

    fun mortgage(): Property
    fun unmortgage(): Property
    fun isMortgaged(): Boolean
}
