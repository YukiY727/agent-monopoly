package com.monopoly.domain.service

import com.monopoly.domain.model.card.Card
import com.monopoly.domain.model.card.CardType
import com.monopoly.domain.model.player.Player
import com.monopoly.domain.model.property.ColorGroup
import com.monopoly.domain.model.property.PropertyBuildings
import com.monopoly.domain.model.property.PropertyRent
import com.monopoly.domain.model.property.StreetProperty
import com.monopoly.domain.model.trade.TradeOffer
import com.monopoly.domain.strategy.AlwaysPlayerStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TradeServiceTest : StringSpec({
    val tradeService = TradeService()

    "validateOffer should return true for valid property exchange" {
        val player1 = Player("Alice", AlwaysPlayerStrategy())
        val player2 = Player("Bob", AlwaysPlayerStrategy())

        val property1 = createTestProperty("Mediterranean Avenue", 1, ColorGroup.BROWN)
        val property2 = createTestProperty("Baltic Avenue", 3, ColorGroup.BROWN)

        player1.acquireProperty(property1.withOwner(player1))
        player2.acquireProperty(property2.withOwner(player2))

        val offer = TradeOffer(
            proposer = player1,
            target = player2,
            offeredProperties = listOf(property1.withOwner(player1)),
            requestedProperties = listOf(property2.withOwner(player2)),
        )

        tradeService.validateOffer(offer) shouldBe true
    }

    "validateOffer should return false if proposer does not own offered property" {
        val player1 = Player("Alice", AlwaysPlayerStrategy())
        val player2 = Player("Bob", AlwaysPlayerStrategy())

        val property1 = createTestProperty("Mediterranean Avenue", 1, ColorGroup.BROWN)
        val property2 = createTestProperty("Baltic Avenue", 3, ColorGroup.BROWN)

        // player1 does NOT own property1
        player2.acquireProperty(property2.withOwner(player2))

        val offer = TradeOffer(
            proposer = player1,
            target = player2,
            offeredProperties = listOf(property1),
            requestedProperties = listOf(property2.withOwner(player2)),
        )

        tradeService.validateOffer(offer) shouldBe false
    }

    "validateOffer should return false if offered property has buildings" {
        val player1 = Player("Alice", AlwaysPlayerStrategy())
        val player2 = Player("Bob", AlwaysPlayerStrategy())

        val propertyWithHouse = createTestProperty("Mediterranean Avenue", 1, ColorGroup.BROWN)
            .copy(buildings = PropertyBuildings(houseCount = 1))
            .withOwner(player1)
        val property2 = createTestProperty("Baltic Avenue", 3, ColorGroup.BROWN).withOwner(player2)

        player1.acquireProperty(propertyWithHouse)
        player2.acquireProperty(property2)

        val offer = TradeOffer(
            proposer = player1,
            target = player2,
            offeredProperties = listOf(propertyWithHouse),
            requestedProperties = listOf(property2),
        )

        tradeService.validateOffer(offer) shouldBe false
    }

    "validateOffer should return false if proposer lacks money" {
        val player1 = Player("Alice", AlwaysPlayerStrategy())
        val player2 = Player("Bob", AlwaysPlayerStrategy())

        val property = createTestProperty("Baltic Avenue", 3, ColorGroup.BROWN).withOwner(player2)
        player2.acquireProperty(property)

        // player1 has $1500 by default, trying to offer $2000
        val offer = TradeOffer(
            proposer = player1,
            target = player2,
            offeredMoney = 2000,
            requestedProperties = listOf(property),
        )

        tradeService.validateOffer(offer) shouldBe false
    }

    "validateOffer should return true for valid money exchange" {
        val player1 = Player("Alice", AlwaysPlayerStrategy())
        val player2 = Player("Bob", AlwaysPlayerStrategy())

        val property = createTestProperty("Baltic Avenue", 3, ColorGroup.BROWN).withOwner(player2)
        player2.acquireProperty(property)

        val offer = TradeOffer(
            proposer = player1,
            target = player2,
            offeredMoney = 100,
            requestedProperties = listOf(property),
        )

        tradeService.validateOffer(offer) shouldBe true
    }

    "executeTrade should transfer properties correctly" {
        val player1 = Player("Alice", AlwaysPlayerStrategy())
        val player2 = Player("Bob", AlwaysPlayerStrategy())

        val property1 = createTestProperty("Mediterranean Avenue", 1, ColorGroup.BROWN).withOwner(player1)
        val property2 = createTestProperty("Baltic Avenue", 3, ColorGroup.BROWN).withOwner(player2)

        player1.acquireProperty(property1)
        player2.acquireProperty(property2)

        val offer = TradeOffer(
            proposer = player1,
            target = player2,
            offeredProperties = listOf(property1),
            requestedProperties = listOf(property2),
        )

        val result = tradeService.executeTrade(offer)

        result shouldBe true

        // Player1 should no longer have property1
        player1.ownedProperties.none { it.name == "Mediterranean Avenue" } shouldBe true
        // Player1 should now have property2
        player1.ownedProperties.any { it.name == "Baltic Avenue" } shouldBe true

        // Player2 should no longer have property2
        player2.ownedProperties.none { it.name == "Baltic Avenue" } shouldBe true
        // Player2 should now have property1
        player2.ownedProperties.any { it.name == "Mediterranean Avenue" } shouldBe true
    }

    "executeTrade should transfer money correctly" {
        val player1 = Player("Alice", AlwaysPlayerStrategy())
        val player2 = Player("Bob", AlwaysPlayerStrategy())

        val property = createTestProperty("Baltic Avenue", 3, ColorGroup.BROWN).withOwner(player2)
        player2.acquireProperty(property)

        val initialMoney1 = player1.money
        val initialMoney2 = player2.money

        val offer = TradeOffer(
            proposer = player1,
            target = player2,
            offeredMoney = 100,
            requestedProperties = listOf(property),
        )

        val result = tradeService.executeTrade(offer)

        result shouldBe true
        player1.money shouldBe initialMoney1 - 100
        player2.money shouldBe initialMoney2 + 100
    }

    "executeTrade should handle bidirectional money exchange" {
        val player1 = Player("Alice", AlwaysPlayerStrategy())
        val player2 = Player("Bob", AlwaysPlayerStrategy())

        val property1 = createTestProperty("Mediterranean Avenue", 1, ColorGroup.BROWN).withOwner(player1)
        val property2 = createTestProperty("Boardwalk", 39, ColorGroup.DARK_BLUE).withOwner(player2)

        player1.acquireProperty(property1)
        player2.acquireProperty(property2)

        val initialMoney1 = player1.money
        val initialMoney2 = player2.money

        // player1 offers property1 + $200 for property2 (more valuable)
        val offer = TradeOffer(
            proposer = player1,
            target = player2,
            offeredProperties = listOf(property1),
            offeredMoney = 200,
            requestedProperties = listOf(property2),
        )

        val result = tradeService.executeTrade(offer)

        result shouldBe true
        player1.money shouldBe initialMoney1 - 200
        player2.money shouldBe initialMoney2 + 200
    }

    "executeTrade should transfer jail cards correctly" {
        val player1 = Player("Alice", AlwaysPlayerStrategy())
        val player2 = Player("Bob", AlwaysPlayerStrategy())

        val jailCard = Card.GetOutOfJailFree(
            id = "chance_get_out_of_jail",
            text = "Get Out of Jail Free",
            type = CardType.CHANCE,
        )
        player1.addCard(jailCard)

        val property = createTestProperty("Baltic Avenue", 3, ColorGroup.BROWN).withOwner(player2)
        player2.acquireProperty(property)

        val offer = TradeOffer(
            proposer = player1,
            target = player2,
            offeredJailCards = 1,
            requestedProperties = listOf(property),
        )

        val result = tradeService.executeTrade(offer)

        result shouldBe true
        player1.hasGetOutOfJailFreeCard() shouldBe false
        player2.hasGetOutOfJailFreeCard() shouldBe true
    }

    "validateOffer should return false if proposer lacks jail cards" {
        val player1 = Player("Alice", AlwaysPlayerStrategy())
        val player2 = Player("Bob", AlwaysPlayerStrategy())

        // player1 has NO jail cards
        val property = createTestProperty("Baltic Avenue", 3, ColorGroup.BROWN).withOwner(player2)
        player2.acquireProperty(property)

        val offer = TradeOffer(
            proposer = player1,
            target = player2,
            offeredJailCards = 1,
            requestedProperties = listOf(property),
        )

        tradeService.validateOffer(offer) shouldBe false
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
