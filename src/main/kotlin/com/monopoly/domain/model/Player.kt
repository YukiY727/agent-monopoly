package com.monopoly.domain.model

import com.monopoly.domain.strategy.BuyStrategy

@Suppress("TooManyFunctions") // Compatibility methods for existing tests will be removed
class Player(
    val name: String,
    val strategy: BuyStrategy,
    initialState: PlayerState? = null,
) {
    var state: PlayerState = initialState ?: PlayerState.initial()
        private set

    // Expose primitive Int for backward compatibility with existing tests
    val money: Int
        get() = state.money.amount

    val position: Int
        get() = state.position.value

    val isBankrupt: Boolean
        get() = state.isBankrupt

    val ownedProperties: List<Property>
        get() = state.ownedProperties.toList()

    // Phase 16: Jail関連のプロパティ
    val inJail: Boolean
        get() = state.inJail

    val jailTurns: Int
        get() = state.jailTurns

    val getOutOfJailFreeCards: Int
        get() = state.getOutOfJailFreeCards

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

    /**
     * プロパティを更新（家・ホテル建設などで使用）
     *
     * Phase 14: 家・ホテルシステム用
     */
    fun updateProperty(property: Property) {
        state = state.withUpdatedProperty(property)
    }

    /**
     * プロパティを削除（取引で使用）
     *
     * Phase 19: プレイヤー間取引用
     */
    fun removeProperty(property: Property) {
        state = state.withRemovedProperty(property)
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

    // Phase 16: Jail関連のメソッド
    fun sendToJail() {
        state = state.withJail(inJail = true, jailTurns = 0)
        moveTo(BoardPosition(10)) // Jail position
    }

    fun releaseFromJail() {
        state = state.withJail(inJail = false, jailTurns = 0)
    }

    fun incrementJailTurns() {
        state = state.withJailTurns(state.jailTurns + 1)
    }

    fun addGetOutOfJailFreeCard() {
        state = state.withGetOutOfJailFreeCards(state.getOutOfJailFreeCards + 1)
    }

    fun useGetOutOfJailFreeCard() {
        require(state.getOutOfJailFreeCards > 0) {
            "No Get Out of Jail Free cards available"
        }
        state = state.withGetOutOfJailFreeCards(state.getOutOfJailFreeCards - 1)
    }
}
