package com.monopoly.domain.strategy

import com.monopoly.domain.model.ColorGroup
import com.monopoly.domain.model.Property
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class AlwaysBuyStrategyTest : StringSpec({
    "should return true when player has enough money" {
        val strategy = AlwaysBuyStrategy()
        val property =
            Property(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                baseRent = 2,
                colorGroup = ColorGroup.BROWN,
            )
        val currentMoney = 100

        val result = strategy.shouldBuy(property, currentMoney)

        result shouldBe true
    }

    "should return false when player does not have enough money" {
        val strategy = AlwaysBuyStrategy()
        val property =
            Property(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                baseRent = 2,
                colorGroup = ColorGroup.BROWN,
            )
        val currentMoney = 50

        val result = strategy.shouldBuy(property, currentMoney)

        result shouldBe false
    }

    "should return true when player has exactly enough money" {
        val strategy = AlwaysBuyStrategy()
        val property =
            Property(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                baseRent = 2,
                colorGroup = ColorGroup.BROWN,
            )
        val currentMoney = 60

        val result = strategy.shouldBuy(property, currentMoney)

        result shouldBe true
    }
})
