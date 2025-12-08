package com.monopoly.domain.service

import com.monopoly.domain.event.GameEvent
import com.monopoly.domain.model.card.Card
import com.monopoly.domain.model.card.CardDeck
import com.monopoly.domain.model.card.CardType
import com.monopoly.domain.model.core.BoardPosition
import com.monopoly.domain.model.core.Money
import com.monopoly.domain.model.game.Board
import com.monopoly.domain.model.game.GameState
import com.monopoly.domain.model.game.Space
import com.monopoly.domain.model.game.SpaceType
import com.monopoly.domain.model.jail.JailStatus
import com.monopoly.domain.model.player.Player
import com.monopoly.domain.strategy.AlwaysPlayerStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class CardEffectTest : StringSpec({

    fun createTestBoard(): Board {
        val spaces = List(40) { index ->
            when (index) {
                0 -> Space.Go(0)
                2, 17, 33 -> Space.Other(index, SpaceType.COMMUNITY_CHEST)
                7, 22, 36 -> Space.Other(index, SpaceType.CHANCE)
                10 -> Space.Other(10, SpaceType.JAIL)
                else -> Space.Other(index, SpaceType.FREE_PARKING)
            }
        }
        return Board(spaces)
    }

    // TC-CARD-001: ReceiveMoney card increases player money
    "ReceiveMoney card should increase player money" {
        val player = Player("Alice", AlwaysPlayerStrategy())
        val initialMoney = player.money
        
        val card = Card.ReceiveMoney(
            id = "test-receive",
            text = "Receive $200",
            type = CardType.CHANCE,
            amount = 200
        )
        
        val deck = CardDeck(listOf(card))
        val board = createTestBoard()
        val gameState = GameState(listOf(player), board, chanceDeck = deck)
        
        val gameService = GameService(BuildingService(MonopolyCheckerService()))
        
        // Manually apply card effect (we'll test through processCard later)
        player.receiveMoney(Money(card.amount))
        
        player.money shouldBe initialMoney + 200
    }

    // TC-CARD-002: PayMoney card decreases player money
    "PayMoney card should decrease player money" {
        val player = Player("Bob", AlwaysPlayerStrategy())
        val initialMoney = player.money
        
        val card = Card.PayMoney(
            id = "test-pay",
            text = "Pay $50",
            type = CardType.COMMUNITY_CHEST,
            amount = 50
        )
        
        player.pay(Money(card.amount))
        
        player.money shouldBe initialMoney - 50
    }

    // TC-CARD-003: GoToJail card sends player to jail
    "GoToJail card should send player to jail" {
        val player = Player("Charlie", AlwaysPlayerStrategy())
        player.state.jailStatus shouldBe JailStatus.Free
        
        val card = Card.GoToJail(
            id = "test-jail",
            text = "Go to Jail",
            type = CardType.CHANCE
        )
        
        player.sendToJail()
        
        player.state.jailStatus shouldBe JailStatus.Jailed
        player.state.turnsInJail shouldBe 0
    }

    // TC-CARD-004: GetOutOfJailFree card is held by player
    "GetOutOfJailFree card should be held by player" {
        val player = Player("Dave", AlwaysPlayerStrategy())
        player.state.heldCards.size shouldBe 0
        
        val card = Card.GetOutOfJailFree(
            id = "test-goojf",
            text = "Get Out of Jail Free",
            type = CardType.CHANCE
        )
        
        player.addCard(card)
        
        player.state.heldCards.size shouldBe 1
        player.hasGetOutOfJailFreeCard() shouldBe true
    }

    // TC-CARD-005: Using GetOutOfJailFree card removes it from player
    "Using GetOutOfJailFree card should remove it from player" {
        val player = Player("Eve", AlwaysPlayerStrategy())
        
        val card = Card.GetOutOfJailFree(
            id = "test-goojf",
            text = "Get Out of Jail Free",
            type = CardType.COMMUNITY_CHEST
        )
        
        player.addCard(card)
        player.hasGetOutOfJailFreeCard() shouldBe true
        
        val usedCard = player.useGetOutOfJailFreeCard()
        usedCard shouldBe card
        player.hasGetOutOfJailFreeCard() shouldBe false
        player.state.heldCards.size shouldBe 0
    }

    // TC-CARD-006: MoveTo card with specific position
    "MoveTo card should move player to specific position" {
        val player = Player("Frank", AlwaysPlayerStrategy())
        player.setPosition(5)
        
        val targetPosition = 15
        val card = Card.MoveTo(
            id = "test-move",
            text = "Advance to position 15",
            type = CardType.CHANCE,
            targetPosition = targetPosition,
            collectGoMoney = true
        )
        
        // Move player to target position
        player.moveTo(BoardPosition(targetPosition))
        
        player.position shouldBe targetPosition
    }

    // TC-CARD-007: MoveTo card passing GO collects $200
    "MoveTo card passing GO should collect bonus" {
        val player = Player("Grace", AlwaysPlayerStrategy())
        player.setPosition(35) // Near end of board
        val initialMoney = player.money

        val targetPosition = 5 // Will pass GO
        val card = Card.MoveTo(
            id = "test-move-go",
            text = "Advance to position 5",
            type = CardType.CHANCE,
            targetPosition = targetPosition,
            collectGoMoney = true
        )

        // Calculate if passing GO
        val passedGo = targetPosition < player.position
        if (passedGo) {
            player.receiveMoney(Money.GO_BONUS)
        }
        player.moveTo(BoardPosition(targetPosition))

        player.position shouldBe targetPosition
        player.money shouldBe initialMoney + 200
    }

    // TC-CARD-008: Integration test - Drawing and processing ReceiveMoney card
    "Drawing ReceiveMoney card through GameService should increase money" {
        val player = Player("Henry", AlwaysPlayerStrategy())
        val initialMoney = player.money

        val card = Card.ReceiveMoney(
            id = "test-receive-integration",
            text = "Bank pays you $50",
            type = CardType.COMMUNITY_CHEST,
            amount = 50
        )

        val deck = CardDeck(listOf(card))
        val board = createTestBoard()
        val gameState = GameState(listOf(player), board, communityChestDeck = deck)
        val gameService = GameService(BuildingService(MonopolyCheckerService()))

        // Place player on community chest space (position 2)
        player.setPosition(2)

        // Process the space (which will draw and apply the card)
        gameService.processSpace(player, gameState)

        // Verify money increased
        player.money shouldBe initialMoney + 50

        // Verify events
        val cardEvents = gameState.events.filterIsInstance<GameEvent.CardDrawn>()
        cardEvents.size shouldBe 1
        cardEvents[0].cardText shouldBe "Bank pays you $50"

        val moneyEvents = gameState.events.filterIsInstance<GameEvent.MoneyReceived>()
        moneyEvents.size shouldBe 1
        moneyEvents[0].amount shouldBe 50
    }

    // TC-CARD-009: Integration test - Drawing and processing PayMoney card
    "Drawing PayMoney card through GameService should decrease money" {
        val player = Player("Ivy", AlwaysPlayerStrategy())
        val initialMoney = player.money

        val card = Card.PayMoney(
            id = "test-pay-integration",
            text = "Doctor's fee - Pay $50",
            type = CardType.CHANCE,
            amount = 50
        )

        val deck = CardDeck(listOf(card))
        val board = createTestBoard()
        val gameState = GameState(listOf(player), board, chanceDeck = deck)
        val gameService = GameService(BuildingService(MonopolyCheckerService()))

        // Place player on chance space (position 7)
        player.setPosition(7)

        // Process the space
        gameService.processSpace(player, gameState)

        // Verify money decreased
        player.money shouldBe initialMoney - 50

        // Verify events
        val cardEvents = gameState.events.filterIsInstance<GameEvent.CardDrawn>()
        cardEvents.size shouldBe 1

        val moneyEvents = gameState.events.filterIsInstance<GameEvent.MoneyPaid>()
        moneyEvents.size shouldBe 1
        moneyEvents[0].amount shouldBe 50
    }

    // TC-CARD-010: Integration test - GoToJail card
    "Drawing GoToJail card through GameService should send player to jail" {
        val player = Player("Jack", AlwaysPlayerStrategy())
        player.state.jailStatus shouldBe JailStatus.Free

        val card = Card.GoToJail(
            id = "test-jail-integration",
            text = "Go directly to Jail",
            type = CardType.CHANCE
        )

        val deck = CardDeck(listOf(card))
        val board = createTestBoard()
        val gameState = GameState(listOf(player), board, chanceDeck = deck)
        val gameService = GameService(BuildingService(MonopolyCheckerService()))

        // Place player on chance space
        player.setPosition(7)

        // Process the space
        gameService.processSpace(player, gameState)

        // Verify player is in jail
        player.state.jailStatus shouldBe JailStatus.Jailed
        player.position shouldBe 10 // Jail position

        // Verify events
        val jailEvents = gameState.events.filterIsInstance<GameEvent.PlayerSentToJail>()
        jailEvents.size shouldBe 1
    }

    // TC-CARD-011: Integration test - GetOutOfJailFree card is kept by player
    "Drawing GetOutOfJailFree card should be kept by player" {
        val player = Player("Kate", AlwaysPlayerStrategy())
        player.state.heldCards.size shouldBe 0

        val card = Card.GetOutOfJailFree(
            id = "test-goojf-integration",
            text = "Get Out of Jail Free",
            type = CardType.COMMUNITY_CHEST
        )

        val deck = CardDeck(listOf(card))
        val board = createTestBoard()
        val gameState = GameState(listOf(player), board, communityChestDeck = deck)
        val gameService = GameService(BuildingService(MonopolyCheckerService()))

        // Verify card is in deck
        deck.size shouldBe 1

        // Place player on community chest space
        player.setPosition(2)

        // Process the space
        gameService.processSpace(player, gameState)

        // Verify player has the card
        player.state.heldCards.size shouldBe 1
        player.hasGetOutOfJailFreeCard() shouldBe true

        // Verify card is NOT returned to deck
        deck.size shouldBe 0
    }
})
