package com.monopoly.domain.service

import com.monopoly.domain.model.game.Board
import com.monopoly.domain.model.game.Dice
import com.monopoly.domain.model.game.DiceRoll
import com.monopoly.domain.model.game.GameState
import com.monopoly.domain.model.game.Space
import com.monopoly.domain.model.game.SpaceType
import com.monopoly.domain.model.player.Player
import com.monopoly.domain.model.property.Property
import com.monopoly.domain.model.property.PropertyOwnership
import com.monopoly.domain.model.property.RailroadProperty
import com.monopoly.domain.model.property.UtilityProperty
import com.monopoly.domain.strategy.AlwaysPlayerStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SpecialPropertyRentTest : StringSpec({

    class MockDice(private val rolls: List<DiceRoll>) : Dice {
        private var index = 0

        override fun roll(): DiceRoll {
            return rolls[index++ % rolls.size]
        }
    }

    fun createBoardWithProperties(
        railroads: List<RailroadProperty>,
        utilities: List<UtilityProperty>,
    ): Board {
        // Create a map of position to property
        val propertyMap = mutableMapOf<Int, Property>()
        railroads.forEach { propertyMap[it.position] = it }
        utilities.forEach { propertyMap[it.position] = it }

        val spaces =
            List(40) { index ->
                when {
                    propertyMap.containsKey(index) -> Space.PropertySpace(index, propertyMap[index]!!)
                    index == 0 -> Space.Go(0)
                    else -> Space.Other(index, SpaceType.FREE_PARKING)
                }
            }
        return Board(spaces)
    }

    // TC-RAILROAD-RENT-001: Pay rent with 1 railroad owned
    "Player landing on railroad with 1 railroad owned should pay $25" {
        val player1 = Player("Alice", AlwaysPlayerStrategy())
        val player2 = Player("Bob", AlwaysPlayerStrategy())

        val railroad =
            RailroadProperty(
                name = "Reading Railroad",
                position = 5,
                price = 200,
                ownership = PropertyOwnership.OwnedByPlayer(player1),
            )

        // Add property to player's owned properties
        player1.acquireProperty(railroad)

        val board = createBoardWithProperties(listOf(railroad), emptyList())
        val gameState = GameState(listOf(player1, player2), board)
        val gameService = GameService(BuildingService(MonopolyCheckerService()))

        val initialMoney: Int = player2.money

        // Player2 lands on railroad
        player2.setPosition(5)

        // Process the space
        gameService.processSpace(player2, gameState)

        // Player2 should have paid $25
        player2.money shouldBe initialMoney - 25
    }

    // TC-RAILROAD-RENT-002: Pay rent with 2 railroads owned
    "Player landing on railroad with 2 railroads owned should pay $50" {
        val player1 = Player("Alice", AlwaysPlayerStrategy())
        val player2 = Player("Bob", AlwaysPlayerStrategy())

        val railroad1 =
            RailroadProperty(
                name = "Reading Railroad",
                position = 5,
                price = 200,
                ownership = PropertyOwnership.OwnedByPlayer(player1),
            )

        val railroad2 =
            RailroadProperty(
                name = "Pennsylvania Railroad",
                position = 15,
                price = 200,
                ownership = PropertyOwnership.OwnedByPlayer(player1),
            )

        // Add properties to player's owned properties
        player1.acquireProperty(railroad1)
        player1.acquireProperty(railroad2)

        val board = createBoardWithProperties(listOf(railroad1, railroad2), emptyList())
        val gameState = GameState(listOf(player1, player2), board)
        val gameService = GameService(BuildingService(MonopolyCheckerService()))

        val initialMoney: Int = player2.money

        // Player2 lands on railroad
        player2.setPosition(5)

        // Process the space
        gameService.processSpace(player2, gameState)

        // Player2 should have paid $50
        player2.money shouldBe initialMoney - 50
    }

    // TC-RAILROAD-RENT-003: Pay rent with 4 railroads owned
    "Player landing on railroad with 4 railroads owned should pay $200" {
        val player1 = Player("Alice", AlwaysPlayerStrategy())
        val player2 = Player("Bob", AlwaysPlayerStrategy())

        val railroads =
            listOf(
                RailroadProperty("Reading Railroad", 5, 200, PropertyOwnership.OwnedByPlayer(player1)),
                RailroadProperty("Pennsylvania Railroad", 15, 200, PropertyOwnership.OwnedByPlayer(player1)),
                RailroadProperty("B&O Railroad", 25, 200, PropertyOwnership.OwnedByPlayer(player1)),
                RailroadProperty("Short Line", 35, 200, PropertyOwnership.OwnedByPlayer(player1)),
            )

        // Add all properties to player's owned properties
        railroads.forEach { player1.acquireProperty(it) }

        val board = createBoardWithProperties(railroads, emptyList())
        val gameState = GameState(listOf(player1, player2), board)
        val gameService = GameService(BuildingService(MonopolyCheckerService()))

        val initialMoney: Int = player2.money

        // Player2 lands on railroad
        player2.setPosition(5)

        // Process the space
        gameService.processSpace(player2, gameState)

        // Player2 should have paid $200
        player2.money shouldBe initialMoney - 200
    }

    // TC-UTILITY-RENT-001: Pay rent with 1 utility owned and dice roll 6
    "Player landing on utility with 1 utility owned and dice roll 6 should pay $24" {
        val player1 = Player("Alice", AlwaysPlayerStrategy())
        val player2 = Player("Bob", AlwaysPlayerStrategy())

        val utility =
            UtilityProperty(
                name = "Electric Company",
                position = 12,
                price = 150,
                ownership = PropertyOwnership.OwnedByPlayer(player1),
            )

        // Add property to player's owned properties
        player1.acquireProperty(utility)

        val board = createBoardWithProperties(emptyList(), listOf(utility))
        val gameState = GameState(listOf(player1, player2), board)
        val gameService = GameService(BuildingService(MonopolyCheckerService()))

        val initialMoney: Int = player2.money

        // Player2 lands on utility
        player2.setPosition(12)

        // Process the space (dice roll should be saved from player's movement)
        // For this test, we need to simulate that the player rolled to land here
        gameState.lastDiceRoll = 6
        gameService.processSpace(player2, gameState)

        // Player2 should have paid $24 (6 × 4)
        player2.money shouldBe initialMoney - 24
    }

    // TC-UTILITY-RENT-002: Pay rent with 2 utilities owned and dice roll 8
    "Player landing on utility with 2 utilities owned and dice roll 8 should pay $80" {
        val player1 = Player("Alice", AlwaysPlayerStrategy())
        val player2 = Player("Bob", AlwaysPlayerStrategy())

        val utilities =
            listOf(
                UtilityProperty("Electric Company", 12, 150, PropertyOwnership.OwnedByPlayer(player1)),
                UtilityProperty("Water Works", 28, 150, PropertyOwnership.OwnedByPlayer(player1)),
            )

        // Add all properties to player's owned properties
        utilities.forEach { player1.acquireProperty(it) }

        val board = createBoardWithProperties(emptyList(), utilities)
        val gameState = GameState(listOf(player1, player2), board)
        val gameService = GameService(BuildingService(MonopolyCheckerService()))

        val initialMoney: Int = player2.money

        // Player2 lands on utility
        player2.setPosition(12)

        // Process the space
        gameState.lastDiceRoll = 8
        gameService.processSpace(player2, gameState)

        // Player2 should have paid $80 (8 × 10)
        player2.money shouldBe initialMoney - 80
    }

    // TC-RAILROAD-RENT-004: No rent paid if landing on own railroad
    "Player landing on own railroad should not pay rent" {
        val player1 = Player("Alice", AlwaysPlayerStrategy())

        val railroad =
            RailroadProperty(
                name = "Reading Railroad",
                position = 5,
                price = 200,
                ownership = PropertyOwnership.OwnedByPlayer(player1),
            )

        val board = createBoardWithProperties(listOf(railroad), emptyList())
        val gameState = GameState(listOf(player1), board)
        val gameService = GameService(BuildingService(MonopolyCheckerService()))

        val initialMoney: Int = player1.money

        // Player1 lands on their own railroad
        player1.setPosition(5)

        // Process the space
        gameService.processSpace(player1, gameState)

        // Player1 should not have paid rent
        player1.money shouldBe initialMoney
    }

    // TC-UTILITY-RENT-003: No rent paid if landing on own utility
    "Player landing on own utility should not pay rent" {
        val player1 = Player("Alice", AlwaysPlayerStrategy())

        val utility =
            UtilityProperty(
                name = "Electric Company",
                position = 12,
                price = 150,
                ownership = PropertyOwnership.OwnedByPlayer(player1),
            )

        val board = createBoardWithProperties(emptyList(), listOf(utility))
        val gameState = GameState(listOf(player1), board)
        val gameService = GameService(BuildingService(MonopolyCheckerService()))

        val initialMoney: Int = player1.money

        // Player1 lands on their own utility
        player1.setPosition(12)

        // Process the space
        gameService.processSpace(player1, gameState)

        // Player1 should not have paid rent
        player1.money shouldBe initialMoney
    }
})
