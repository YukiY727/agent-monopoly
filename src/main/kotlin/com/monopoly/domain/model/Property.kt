package com.monopoly.domain.model

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
}
