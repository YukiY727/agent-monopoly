package com.monopoly.domain.model

@JvmInline
value class Money(val amount: Int) {
    operator fun plus(other: Money): Money = Money(amount + other.amount)

    operator fun minus(other: Money): Money = Money(amount - other.amount)

    fun isEnough(required: Money): Boolean = amount >= required.amount

    fun canAfford(required: Money): Boolean = amount >= required.amount

    companion object {
        val ZERO = Money(0)
        val INITIAL_AMOUNT = Money(1500)
        val GO_BONUS = Money(200)
    }
}
