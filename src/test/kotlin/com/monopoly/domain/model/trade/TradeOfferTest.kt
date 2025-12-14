package com.monopoly.domain.model.trade

import com.monopoly.domain.model.player.Player
import com.monopoly.domain.model.property.ColorGroup
import com.monopoly.domain.model.property.PropertyRent
import com.monopoly.domain.model.property.StreetProperty
import com.monopoly.domain.strategy.AlwaysPlayerStrategy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TradeOfferTest : StringSpec({

    "TradeOffer should be created with valid property exchange" {
        val player1 = Player("Alice", AlwaysPlayerStrategy())
        val player2 = Player("Bob", AlwaysPlayerStrategy())
        val property1 = createTestProperty("Mediterranean Avenue", 1, ColorGroup.BROWN)
        val property2 = createTestProperty("Baltic Avenue", 3, ColorGroup.BROWN)

        val offer = TradeOffer(
            proposer = player1,
            target = player2,
            offeredProperties = listOf(property1),
            requestedProperties = listOf(property2),
        )

        offer.proposer shouldBe player1
        offer.target shouldBe player2
        offer.offeredProperties shouldBe listOf(property1)
        offer.requestedProperties shouldBe listOf(property2)
        offer.offeredMoney shouldBe 0
        offer.requestedMoney shouldBe 0
    }

    "TradeOffer should be created with money exchange" {
        val player1 = Player("Alice", AlwaysPlayerStrategy())
        val player2 = Player("Bob", AlwaysPlayerStrategy())
        val property = createTestProperty("Mediterranean Avenue", 1, ColorGroup.BROWN)

        val offer = TradeOffer(
            proposer = player1,
            target = player2,
            offeredMoney = 100,
            requestedProperties = listOf(property),
        )

        offer.offeredMoney shouldBe 100
        offer.requestedMoney shouldBe 0
        offer.netMoneyForProposer shouldBe -100
        offer.netMoneyForTarget shouldBe 100
    }

    "TradeOffer should calculate net money correctly" {
        val player1 = Player("Alice", AlwaysPlayerStrategy())
        val player2 = Player("Bob", AlwaysPlayerStrategy())
        val property = createTestProperty("Mediterranean Avenue", 1, ColorGroup.BROWN)

        val offer = TradeOffer(
            proposer = player1,
            target = player2,
            offeredProperties = listOf(property),
            offeredMoney = 50,
            requestedMoney = 200,
        )

        offer.netMoneyForProposer shouldBe 150 // receives 200, pays 50
        offer.netMoneyForTarget shouldBe -150 // pays 200, receives 50
    }

    "TradeOffer should fail if proposer and target are the same" {
        val player = Player("Alice", AlwaysPlayerStrategy())
        val property = createTestProperty("Mediterranean Avenue", 1, ColorGroup.BROWN)

        shouldThrow<IllegalArgumentException> {
            TradeOffer(
                proposer = player,
                target = player,
                offeredProperties = listOf(property),
            )
        }
    }

    "TradeOffer should fail if empty" {
        val player1 = Player("Alice", AlwaysPlayerStrategy())
        val player2 = Player("Bob", AlwaysPlayerStrategy())

        shouldThrow<IllegalArgumentException> {
            TradeOffer(
                proposer = player1,
                target = player2,
            )
        }
    }

    "TradeOffer should fail if offered money is negative" {
        val player1 = Player("Alice", AlwaysPlayerStrategy())
        val player2 = Player("Bob", AlwaysPlayerStrategy())
        val property = createTestProperty("Mediterranean Avenue", 1, ColorGroup.BROWN)

        shouldThrow<IllegalArgumentException> {
            TradeOffer(
                proposer = player1,
                target = player2,
                offeredMoney = -100,
                requestedProperties = listOf(property),
            )
        }
    }

    "TradeOffer should be created with jail cards" {
        val player1 = Player("Alice", AlwaysPlayerStrategy())
        val player2 = Player("Bob", AlwaysPlayerStrategy())
        val property = createTestProperty("Mediterranean Avenue", 1, ColorGroup.BROWN)

        val offer = TradeOffer(
            proposer = player1,
            target = player2,
            offeredJailCards = 1,
            requestedProperties = listOf(property),
        )

        offer.offeredJailCards shouldBe 1
        offer.requestedJailCards shouldBe 0
    }
})

private fun createTestProperty(
    name: String,
    position: Int,
    colorGroup: ColorGroup,
): StreetProperty =
    StreetProperty(
        name = name,
        position = position,
        price = 60,
        rent = PropertyRent(2, 10, 30, 90, 160, 250),
        houseCost = 50,
        hotelCost = 50,
        colorGroup = colorGroup,
    )
