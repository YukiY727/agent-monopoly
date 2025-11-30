package com.monopoly.domain.service

import com.monopoly.domain.event.GameEvent
import com.monopoly.domain.model.Board
import com.monopoly.domain.model.Dice
import com.monopoly.domain.model.DiceRoll
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.JailStatus
import com.monopoly.domain.model.Player
import com.monopoly.domain.model.PlayerStrategy
import com.monopoly.domain.model.Property
import com.monopoly.domain.model.Space
import com.monopoly.domain.model.StreetProperty
import com.monopoly.domain.strategy.AlwaysPlayerStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class GameServiceJailTest : StringSpec({
    
    // Mock classes
    class MockDice(private val rolls: List<DiceRoll>) : Dice {
        private var index = 0
        override fun roll(): DiceRoll {
            return rolls[index++ % rolls.size]
        }
    }

    class PayStrategy(private val shouldPay: Boolean) : PlayerStrategy {
        override fun shouldBuy(property: Property, currentMoney: Int): Boolean = true
        override fun shouldBuildHouse(property: StreetProperty, currentMoney: Int): Boolean = false
        override fun shouldBuildHotel(property: StreetProperty, currentMoney: Int): Boolean = false
        override fun shouldPayToEscapeJail(currentMoney: Int): Boolean = shouldPay
    }

    // TC-JAIL-INTEG-001: 刑務所にいて脱出に失敗（支払い拒否、ゾロ目なし）
    "player in jail fails to escape (no pay, no doubles) -> stays in jail, turn increments" {
        // Setup
        val player = Player("Jailbird", PayStrategy(shouldPay = false))
        player.sendToJail() // JailStatus.Jailed, turnsInJail = 0
        
        val players = listOf(player)
        // Create a valid board with 40 spaces (all Other for simplicity)
        val spaces = List(40) { Space.Other(it, com.monopoly.domain.model.SpaceType.FREE_PARKING) }
        val board = Board(spaces)
        val gameState = GameState(players, board)
        
        // Mock dice: Not doubles (2, 3)
        val dice = MockDice(listOf(DiceRoll(2, 3)))
        
        val buildingService = BuildingService(MonopolyCheckerService())
        val gameService = GameService(buildingService)

        // Execute
        gameService.executeTurn(gameState, dice)

        // Verify
        player.state.jailStatus shouldBe JailStatus.Jailed
        player.state.turnsInJail shouldBe 1
        
        // Verify events
        // Should have TurnStarted, DiceRolled, (maybe JailTurnFailed?), TurnEnded
        // Should NOT have PlayerMoved
        gameState.events.any { it is GameEvent.PlayerMoved } shouldBe false
    }

    // TC-JAIL-INTEG-002: 3回連続ゾロ目で刑務所送り
    "player rolling 3 consecutive doubles goes to jail" {
        // Setup
        val player = Player("Unlucky", AlwaysPlayerStrategy())
        
        val players = listOf(player)
        val spaces = List(40) { Space.Other(it, com.monopoly.domain.model.SpaceType.FREE_PARKING) }
        val board = Board(spaces)
        val gameState = GameState(players, board)
        
        // Mock dice: Always doubles (3, 3)
        val dice = MockDice(listOf(DiceRoll(3, 3)))
        
        val buildingService = BuildingService(MonopolyCheckerService())
        val gameService = GameService(buildingService)

        // Execute 3 turns with doubles
        gameService.executeTurn(gameState, dice) // 1st doubles
        player.state.consecutiveDoubles shouldBe 1
        player.state.jailStatus shouldBe com.monopoly.domain.model.JailStatus.Free
        
        gameService.executeTurn(gameState, dice) // 2nd doubles
        player.state.consecutiveDoubles shouldBe 2
        player.state.jailStatus shouldBe com.monopoly.domain.model.JailStatus.Free
        
        gameService.executeTurn(gameState, dice) // 3rd doubles -> jail
        
        // Verify
        player.state.jailStatus shouldBe com.monopoly.domain.model.JailStatus.Jailed
        player.state.turnsInJail shouldBe 0
        player.state.consecutiveDoubles shouldBe 0 // Reset
        
        // Verify ThreeConsecutiveDoubles event
        gameState.events.any { it is GameEvent.ThreeConsecutiveDoubles } shouldBe true
    }
})
