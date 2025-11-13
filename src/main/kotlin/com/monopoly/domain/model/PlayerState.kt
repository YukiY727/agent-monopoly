package com.monopoly.domain.model

data class PlayerState(
    val money: Money,
    val position: BoardPosition,
    val isBankrupt: Boolean = false,
    val ownedProperties: PropertyCollection = PropertyCollection.EMPTY,
) {
    fun withMoney(newMoney: Money): PlayerState = copy(money = newMoney)

    fun withPosition(newPosition: BoardPosition): PlayerState = copy(position = newPosition)

    fun withBankruptcy(): PlayerState = copy(isBankrupt = true, ownedProperties = PropertyCollection.EMPTY)

    fun withProperty(property: Property): PlayerState = copy(ownedProperties = ownedProperties.add(property))

    fun calculateTotalAssets(): Money = money.plus(ownedProperties.calculateTotalValue())

    companion object {
        fun initial(): PlayerState =
            PlayerState(
                money = Money.INITIAL_AMOUNT,
                position = BoardPosition.GO,
                isBankrupt = false,
                ownedProperties = PropertyCollection.EMPTY,
            )
    }
}
