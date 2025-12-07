package com.monopoly.domain.service

import com.monopoly.domain.event.GameEvent
import com.monopoly.domain.model.game.Board
import com.monopoly.domain.model.game.Dice
import com.monopoly.domain.model.game.DiceRoll
import com.monopoly.domain.model.game.GameState
import com.monopoly.domain.model.player.Player
import com.monopoly.domain.model.game.Space
import com.monopoly.domain.model.game.SpaceType
import com.monopoly.domain.strategy.AlwaysPlayerStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TaxSpaceTest : StringSpec({

    class MockDice(private val rolls: List<DiceRoll>) : Dice {
        private var index = 0
        override fun roll(): DiceRoll {
            return rolls[index++ % rolls.size]
        }
    }

    fun createBoardWithTaxSpaces(): Board {
        val spaces = List(40) { index ->
            when (index) {
                0 -> Space.Go(0)
                4 -> Space.Other(4, SpaceType.TAX) // Income Tax
                38 -> Space.Other(38, SpaceType.TAX) // Luxury Tax
                else -> Space.Other(index, SpaceType.FREE_PARKING)
            }
        }
        return Board(spaces)
    }

    // TC-TAX-001: Player lands on income tax and pays $200
    "Player landing on income tax should pay $200" {
        val player = Player("Alice", AlwaysPlayerStrategy())
        val initialMoney = player.money
        
        val board = createBoardWithTaxSpaces()
        val gameState = GameState(listOf(player), board)
        val gameService = GameService(BuildingService(MonopolyCheckerService()))
        
        // Move player to income tax space (position 4)
        player.setPosition(4)
        
        // Process the tax space
        gameService.processSpace(player, gameState)
        
        // Player should have paid $200
        player.money shouldBe initialMoney - 200
        
        // Check for MoneyPaid event
        val taxEvents = gameState.events.filterIsInstance<GameEvent.MoneyPaid>()
        taxEvents.size shouldBe 1
        taxEvents[0].amount shouldBe 200
        taxEvents[0].reason shouldBe "Income Tax"
    }

    // TC-TAX-002: Player lands on luxury tax and pays $100
    "Player landing on luxury tax should pay $100" {
        val player = Player("Bob", AlwaysPlayerStrategy())
        val initialMoney = player.money
        
        val board = createBoardWithTaxSpaces()
        val gameState = GameState(listOf(player), board)
        val gameService = GameService(BuildingService(MonopolyCheckerService()))
        
        // Move player to luxury tax space (position 38)
        player.setPosition(38)
        
        // Process the tax space
        gameService.processSpace(player, gameState)
        
        // Player should have paid $100
        player.money shouldBe initialMoney - 100
        
        // Check for MoneyPaid event
        val taxEvents = gameState.events.filterIsInstance<GameEvent.MoneyPaid>()
        taxEvents.size shouldBe 1
        taxEvents[0].amount shouldBe 100
        taxEvents[0].reason shouldBe "Luxury Tax"
    }

    // TC-TAX-003: Player with insufficient funds goes bankrupt on tax
    "Player with insufficient funds should go bankrupt on tax payment" {
        val player = Player("Charlie", AlwaysPlayerStrategy())
        player.subtractMoney(1400) // Leave only $100
        
        val board = createBoardWithTaxSpaces()
        val gameState = GameState(listOf(player), board)
        val gameService = GameService(BuildingService(MonopolyCheckerService()))
        
        player.isBankrupt shouldBe false
        player.money shouldBe 100
        
        // Move player to income tax space (position 4) - needs $200 but has $100
        player.setPosition(4)
        
        // Process the tax space
        gameService.processSpace(player, gameState)
        
        // Player should be bankrupt
        player.isBankrupt shouldBe true
        
        // Check for bankruptcy event
        val bankruptEvents = gameState.events.filterIsInstance<GameEvent.PlayerBankrupted>()
        bankruptEvents.size shouldBe 1
    }
})
