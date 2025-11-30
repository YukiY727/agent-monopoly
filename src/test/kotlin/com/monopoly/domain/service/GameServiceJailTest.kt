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
        val board = Board(emptyList()) // Empty board for simplicity
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
})
