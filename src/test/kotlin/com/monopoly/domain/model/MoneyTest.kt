package com.monopoly.domain.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class MoneyTest : StringSpec({
    "should add two money amounts" {
        val money1 = Money(100)
        val money2 = Money(50)

        val result = money1 + money2

        result shouldBe Money(150)
    }

    "should subtract two money amounts" {
        val money1 = Money(100)
        val money2 = Money(30)

        val result = money1 - money2

        result shouldBe Money(70)
    }

    "should check if amount is enough" {
        val money = Money(100)
        val required = Money(80)

        val result = money.isEnough(required)

        result shouldBe true
    }

    "should return false when amount is not enough" {
        val money = Money(50)
        val required = Money(100)

        val result = money.isEnough(required)

        result shouldBe false
    }

    "should check if can afford" {
        val money = Money(200)
        val required = Money(150)

        val result = money.canAfford(required)

        result shouldBe true
    }

    "should return false when cannot afford" {
        val money = Money(100)
        val required = Money(150)

        val result = money.canAfford(required)

        result shouldBe false
    }

    "should have ZERO constant" {
        Money.ZERO shouldBe Money(0)
    }

    "should have INITIAL_AMOUNT constant" {
        Money.INITIAL_AMOUNT shouldBe Money(1500)
    }

    "should have GO_BONUS constant" {
        Money.GO_BONUS shouldBe Money(200)
    }

    "should allow negative amounts for bankruptcy" {
        val money = Money(-100)

        money.amount shouldBe -100
    }

    "should handle subtraction resulting in negative amount" {
        val money = Money(50)
        val amount = Money(100)

        val result = money - amount

        result shouldBe Money(-50)
    }

    "isEnough should return true when amounts are equal" {
        val money = Money(100)
        val required = Money(100)

        val result = money.isEnough(required)

        result shouldBe true
    }

    "canAfford should return true when amounts are equal" {
        val money = Money(100)
        val required = Money(100)

        val result = money.canAfford(required)

        result shouldBe true
    }

    // TC-Money-001: Adding negative amounts
    // Given: 負の金額を持つMoney
    // When: 負の金額を加算
    // Then: より負の金額になる
    "should handle adding negative amounts" {
        val money = Money(-50)
        val amount = Money(-30)

        val result = money + amount

        result shouldBe Money(-80)
    }

    // TC-Money-002: Subtracting negative amounts
    // Given: 正の金額を持つMoney
    // When: 負の金額を減算
    // Then: 金額が増加する
    "should increase when subtracting negative amount" {
        val money = Money(100)
        val amount = Money(-50)

        val result = money - amount

        result shouldBe Money(150)
    }

    // TC-Money-003: Adding with zero
    // Given: 金額を持つMoney
    // When: ZEROを加算
    // Then: 金額が変わらない
    "should remain same when adding ZERO" {
        val money = Money(100)

        val result = money + Money.ZERO

        result shouldBe Money(100)
    }

    // TC-Money-004: Subtracting zero
    // Given: 金額を持つMoney
    // When: ZEROを減算
    // Then: 金額が変わらない
    "should remain same when subtracting ZERO" {
        val money = Money(100)

        val result = money - Money.ZERO

        result shouldBe Money(100)
    }

    // TC-Money-005: Large amounts
    // Given: 非常に大きな金額
    // When: 加算と減算を行う
    // Then: 正しく計算される
    "should handle large amounts" {
        val money1 = Money(1_000_000)
        val money2 = Money(500_000)

        val added = money1 + money2
        val subtracted = money1 - money2

        added shouldBe Money(1_500_000)
        subtracted shouldBe Money(500_000)
    }

    // TC-Money-006: isEnough with negative balance
    // Given: 負の金額を持つMoney
    // When: isEnough()で正の金額をチェック
    // Then: falseが返される
    "should return false for isEnough when balance is negative" {
        val money = Money(-100)
        val required = Money(50)

        val result = money.isEnough(required)

        result shouldBe false
    }

    // TC-Money-007: canAfford with negative balance
    // Given: 負の金額を持つMoney
    // When: canAfford()で正の金額をチェック
    // Then: falseが返される
    "should return false for canAfford when balance is negative" {
        val money = Money(-50)
        val required = Money(100)

        val result = money.canAfford(required)

        result shouldBe false
    }

    // TC-Money-008: Zero amount operations
    // Given: ZERO
    // When: ZEROと加算・減算
    // Then: ZEROになる
    "should handle ZERO operations correctly" {
        val zero = Money.ZERO

        val addZero = zero + Money.ZERO
        val subtractZero = zero - Money.ZERO

        addZero shouldBe Money.ZERO
        subtractZero shouldBe Money.ZERO
    }

    // TC-Money-009: isEnough with ZERO
    // Given: ZERO
    // When: isEnough(ZERO)
    // Then: trueが返される
    "should return true for isEnough when checking ZERO with ZERO" {
        val result = Money.ZERO.isEnough(Money.ZERO)

        result shouldBe true
    }

    // TC-Money-010: Multiple operations
    // Given: 初期金額
    // When: 複数の加算・減算を連続で行う
    // Then: 正しく累積計算される
    "should handle multiple consecutive operations" {
        var money = Money.INITIAL_AMOUNT

        money = money + Money.GO_BONUS // 1500 + 200 = 1700
        money = money - Money(500) // 1700 - 500 = 1200
        money = money + Money(300) // 1200 + 300 = 1500
        money = money - Money(2000) // 1500 - 2000 = -500

        money shouldBe Money(-500)
    }
})
