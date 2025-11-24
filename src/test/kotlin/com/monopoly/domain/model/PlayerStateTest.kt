package com.monopoly.domain.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PlayerStateTest : StringSpec({
    "should create initial state" {
        val state = PlayerState.initial()

        state.money shouldBe Money.INITIAL_AMOUNT
        state.position shouldBe BoardPosition.GO
        state.isBankrupt shouldBe false
        state.ownedProperties.isEmpty shouldBe true
    }

    "should create state with custom values" {
        val state =
            PlayerState(
                money = Money(1000),
                position = BoardPosition(5),
                isBankrupt = false,
                ownedProperties = PropertyCollection.EMPTY,
            )

        state.money shouldBe Money(1000)
        state.position shouldBe BoardPosition(5)
        state.isBankrupt shouldBe false
    }

    "should update money" {
        val state = PlayerState.initial()

        val newState = state.withMoney(Money(2000))

        newState.money shouldBe Money(2000)
        newState.position shouldBe state.position
        newState.isBankrupt shouldBe state.isBankrupt
    }

    "should update position" {
        val state = PlayerState.initial()

        val newState = state.withPosition(BoardPosition(10))

        newState.position shouldBe BoardPosition(10)
        newState.money shouldBe state.money
        newState.isBankrupt shouldBe state.isBankrupt
    }

    "should mark as bankrupt and clear properties" {
        val property: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                baseRent = 2,
                colorGroup = ColorGroup.BROWN,
            )
        val state = PlayerState.initial().withProperty(property)

        val bankruptState = state.withBankruptcy()

        bankruptState.isBankrupt shouldBe true
        bankruptState.ownedProperties.isEmpty shouldBe true
    }

    "should add property" {
        val state = PlayerState.initial()
        val property: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                baseRent = 2,
                colorGroup = ColorGroup.BROWN,
            )

        val newState = state.withProperty(property)

        newState.ownedProperties.size shouldBe 1
        newState.ownedProperties.contains(property) shouldBe true
    }

    "should calculate total assets" {
        val property1: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                baseRent = 2,
                colorGroup = ColorGroup.BROWN,
            )
        val property2: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Baltic Avenue",
                position = 3,
                price = 60,
                baseRent = 4,
                colorGroup = ColorGroup.BROWN,
            )
        val state =
            PlayerState
                .initial()
                .withProperty(property1)
                .withProperty(property2)

        val totalAssets = state.calculateTotalAssets()

        totalAssets shouldBe Money(1500 + 60 + 60)
    }

    "should calculate total assets with no properties" {
        val state = PlayerState.initial()

        val totalAssets = state.calculateTotalAssets()

        totalAssets shouldBe Money.INITIAL_AMOUNT
    }
})
