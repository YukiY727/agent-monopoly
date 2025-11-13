package com.monopoly.domain.model

import com.monopoly.domain.strategy.BuyStrategy

@Suppress("TooManyFunctions") // Compatibility methods for existing tests will be removed
class Player(
    val name: String,
    val strategy: BuyStrategy,
) {
    private var state: PlayerState = PlayerState.initial()

    // Expose primitive Int for backward compatibility with existing tests
    val money: Int
        get() = state.money.amount

    val position: Int
        get() = state.position.value

    val isBankrupt: Boolean
        get() = state.isBankrupt

    val ownedProperties: List<Property>
        get() = state.ownedProperties.toList()

    // New value object accessors
    val moneyValue: Money
        get() = state.money

    val positionValue: BoardPosition
        get() = state.position

    fun receiveMoney(amount: Money) {
        state = state.withMoney(state.money.plus(amount))
    }

    fun pay(amount: Money) {
        val newMoney = state.money.minus(amount)
        state = state.withMoney(newMoney)

        if (newMoney.amount < 0) {
            goBankrupt()
        }
    }

    fun moveTo(newPosition: BoardPosition) {
        state = state.withPosition(newPosition)
    }

    fun advance(steps: Int): Boolean {
        val result = state.position.advance(steps)
        state = state.withPosition(result.newPosition)

        if (result.passedGo) {
            receiveMoney(Money.GO_BONUS)
        }

        return result.passedGo
    }

    fun acquireProperty(property: Property) {
        state = state.withProperty(property)
    }

    fun goBankrupt() {
        state = state.withBankruptcy()
    }

    fun calculateTotalAssets(): Money = state.calculateTotalAssets()

    // Compatibility methods for existing tests
    fun addMoney(amount: Int) {
        receiveMoney(Money(amount))
    }

    fun subtractMoney(amount: Int) {
        pay(Money(amount))
    }

    fun addProperty(property: Property) {
        acquireProperty(property)
    }

    fun markAsBankrupt() {
        goBankrupt()
    }

    fun setPosition(newPosition: Int) {
        moveTo(BoardPosition(newPosition))
    }

    fun getTotalAssets(): Int = calculateTotalAssets().amount
}
