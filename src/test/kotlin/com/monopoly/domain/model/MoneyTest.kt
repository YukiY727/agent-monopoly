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
})
