package com.monopoly.domain.model

/**
 * プレイヤーの状態
 *
 * Phase 16: Jail関連のフィールドを追加
 */
data class PlayerState(
    val money: Money,
    val position: BoardPosition,
    val isBankrupt: Boolean = false,
    val ownedProperties: PropertyCollection = PropertyCollection.EMPTY,
    val inJail: Boolean = false,
    val jailTurns: Int = 0,
    val getOutOfJailFreeCards: Int = 0,
) {
    fun withMoney(newMoney: Money): PlayerState = copy(money = newMoney)

    fun withPosition(newPosition: BoardPosition): PlayerState = copy(position = newPosition)

    fun withBankruptcy(): PlayerState = copy(isBankrupt = true, ownedProperties = PropertyCollection.EMPTY)

    fun withProperty(property: Property): PlayerState = copy(ownedProperties = ownedProperties.add(property))

    fun withJail(inJail: Boolean, jailTurns: Int = 0): PlayerState = copy(inJail = inJail, jailTurns = jailTurns)

    fun withJailTurns(turns: Int): PlayerState = copy(jailTurns = turns)

    fun withGetOutOfJailFreeCards(cards: Int): PlayerState = copy(getOutOfJailFreeCards = cards)

    fun calculateTotalAssets(): Money = money.plus(ownedProperties.calculateTotalValue())

    companion object {
        fun initial(): PlayerState =
            PlayerState(
                money = Money.INITIAL_AMOUNT,
                position = BoardPosition.GO,
                isBankrupt = false,
                ownedProperties = PropertyCollection.EMPTY,
                inJail = false,
                jailTurns = 0,
                getOutOfJailFreeCards = 0,
            )
    }
}
