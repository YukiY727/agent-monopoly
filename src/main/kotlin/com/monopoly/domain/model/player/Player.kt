package com.monopoly.domain.model.player

import com.monopoly.domain.model.card.Card
import com.monopoly.domain.model.core.BoardPosition
import com.monopoly.domain.model.core.Money
import com.monopoly.domain.model.jail.JailStatus
import com.monopoly.domain.model.property.Property
import com.monopoly.domain.model.property.RailroadProperty
import com.monopoly.domain.model.property.UtilityProperty

@Suppress("TooManyFunctions") // Compatibility methods for existing tests will be removed
class Player(
    val name: String,
    val strategy: PlayerStrategy,
) {
    // GameService needs to update player state (e.g. for consecutive doubles tracking)
    // This should be restricted to the domain service layer
    internal var state: PlayerState = PlayerState.initial()

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

    fun removeProperty(property: Property) {
        state = state.withoutProperty(property)
    }

    fun goBankrupt() {
        state = state.withBankruptcy()
    }

    fun calculateTotalAssets(): Money = state.calculateTotalAssets()

    /**
     * Calculate rent for a property owned by this player
     * Handles special cases for railroads and utilities
     * Returns 0 if property is mortgaged
     *
     * @param property The property to calculate rent for
     * @param diceRoll The dice roll (used for utility rent calculation)
     * @return The rent amount to be paid
     */
    fun calculateRentFor(property: Property, diceRoll: Int = 0): Int {
        // Mortgaged properties have no rent
        if (property.isMortgaged()) {
            return 0
        }

        return when (property) {
            is RailroadProperty -> {
                val railroadCount: Int = ownedProperties.filterIsInstance<RailroadProperty>().count()
                property.calculateRentWithCount(railroadCount)
            }
            is UtilityProperty -> {
                val utilityCount: Int = ownedProperties.filterIsInstance<UtilityProperty>().count()
                property.calculateRentWithDice(utilityCount, diceRoll)
            }
            else -> property.rentValue.amount
        }
    }

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

    fun sendToJail() {
        state = state.copy(
            jailStatus = JailStatus.Jailed,
            turnsInJail = 0,
        )
    }

    fun escapeJailByPayment() {
        pay(Money(50))
        state = state.copy(
            jailStatus = JailStatus.Free,
            turnsInJail = 0,
        )
    }

    fun incrementJailTurn() {
        state = state.copy(
            turnsInJail = state.turnsInJail + 1,
        )
    }

    fun escapeJailByDoubles() {
        state = state.copy(
            jailStatus = JailStatus.Free,
            turnsInJail = 0,
        )
    }

    fun forceEscapeJail() {
        pay(Money(50))
        state = state.copy(
            jailStatus = JailStatus.Free,
            turnsInJail = 0,
        )
    }

    fun addCard(card: Card) {
        state = state.copy(heldCards = state.heldCards + card)
    }

    fun removeCard(card: Card) {
        state = state.copy(heldCards = state.heldCards - card)
    }

    fun hasGetOutOfJailFreeCard(): Boolean {
        return state.heldCards.any { it is Card.GetOutOfJailFree }
    }

    fun useGetOutOfJailFreeCard(): Card.GetOutOfJailFree? {
        val card = state.heldCards.find { it is Card.GetOutOfJailFree } as? Card.GetOutOfJailFree
        if (card != null) {
            removeCard(card)
        }
        return card
    }
}
