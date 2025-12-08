package com.monopoly.domain.model.player

import com.monopoly.domain.model.card.Card
import com.monopoly.domain.model.core.BoardPosition
import com.monopoly.domain.model.core.Money
import com.monopoly.domain.model.jail.JailStatus
import com.monopoly.domain.model.property.Property
import com.monopoly.domain.model.property.PropertyCollection

data class PlayerState(
    val money: Money,
    val position: BoardPosition,
    val isBankrupt: Boolean = false,
    val ownedProperties: PropertyCollection = PropertyCollection.EMPTY,
    val consecutiveDoubles: Int = 0,
    val jailStatus: JailStatus = JailStatus.Free,
    val turnsInJail: Int = 0,
    val heldCards: List<Card> = emptyList(),
) {
    fun withMoney(newMoney: Money): PlayerState = copy(money = newMoney)

    fun withPosition(newPosition: BoardPosition): PlayerState = copy(position = newPosition)

    fun withBankruptcy(): PlayerState =
        copy(
            isBankrupt = true,
            ownedProperties = PropertyCollection.EMPTY,
            consecutiveDoubles = 0,
        )

    fun withProperty(property: Property): PlayerState = copy(ownedProperties = ownedProperties.add(property))

    fun withoutProperty(property: Property): PlayerState = copy(ownedProperties = ownedProperties.remove(property))

    fun calculateTotalAssets(): Money = money.plus(ownedProperties.calculateTotalValue())

    fun withConsecutiveDoubles(count: Int): PlayerState = copy(consecutiveDoubles = count)

    fun resetConsecutiveDoubles(): PlayerState = copy(consecutiveDoubles = 0)

    companion object {
        fun initial(): PlayerState =
            PlayerState(
                money = Money.INITIAL_AMOUNT,
                position = BoardPosition.GO,
                isBankrupt = false,
                ownedProperties = PropertyCollection.EMPTY,
                consecutiveDoubles = 0,
            )
    }
}
