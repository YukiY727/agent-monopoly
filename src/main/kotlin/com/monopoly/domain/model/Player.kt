package com.monopoly.domain.model

import com.monopoly.domain.strategy.BuyStrategy

class Player(
    val name: String,
    val strategy: BuyStrategy,
) {
    private var _money: Int = 1500
    private var _position: Int = 0
    private var _isBankrupt: Boolean = false
    private val _ownedProperties: MutableList<Property> = mutableListOf()

    companion object {
        private const val MAX_POSITION = 39
    }

    val money: Int
        get() = _money

    val position: Int
        get() = _position

    val isBankrupt: Boolean
        get() = _isBankrupt

    val ownedProperties: List<Property>
        get() = _ownedProperties.toList()

    fun addMoney(amount: Int) {
        require(amount >= 0) { "Amount must be non-negative" }
        _money += amount
    }

    fun subtractMoney(amount: Int) {
        require(amount >= 0) { "Amount must be non-negative" }
        _money -= amount
    }

    fun addProperty(property: Property) {
        _ownedProperties.add(property)
    }

    fun getTotalAssets(): Int {
        val propertiesValue = _ownedProperties.sumOf { it.price }
        return _money + propertiesValue
    }

    fun markAsBankrupt() {
        _isBankrupt = true
    }

    fun setPosition(newPosition: Int) {
        require(newPosition in 0..MAX_POSITION) { "Position must be between 0 and $MAX_POSITION" }
        _position = newPosition
    }
}
