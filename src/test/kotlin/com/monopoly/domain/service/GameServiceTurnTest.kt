package com.monopoly.domain.service

import com.monopoly.domain.model.Board
import com.monopoly.domain.model.Dice
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Player
import com.monopoly.domain.strategy.AlwaysBuyStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import kotlin.random.Random

class GameServiceTurnTest : StringSpec({
    val gameService = GameService()

    // TC-160: 1ターンの流れ
    // Given: GameState
    // When: executeTurn(gameState)
    // Then: サイコロが振られ、プレイヤーが移動し、マス目処理が実行され、ターン番号が増加
    "should execute one turn with dice roll, movement, space processing, and turn increment" {
        val player1 = Player("Alice", AlwaysBuyStrategy())
        val player2 = Player("Bob", AlwaysBuyStrategy())
        val gameState = GameState(
            players = listOf(player1, player2),
            board = Board(),
        )

        val initialTurnNumber = gameState.turnNumber
        val initialPosition = player1.position
        val dice = Dice(Random(42)) // 固定シードで再現性を確保

        gameService.executeTurn(gameState, dice)

        // プレイヤーが移動した
        player1.position shouldBeGreaterThan initialPosition
        // ターン番号が増加した
        gameState.turnNumber shouldBe initialTurnNumber + 1
    }

    // TC-161: ターン後のプレイヤー交代
    // Given: GameStateでcurrentPlayerIndex=0、2人プレイヤー
    // When: executeTurn(gameState)
    // Then: currentPlayerIndexが1
    "should switch to next player after turn execution" {
        val player1 = Player("Alice", AlwaysBuyStrategy())
        val player2 = Player("Bob", AlwaysBuyStrategy())
        val gameState = GameState(
            players = listOf(player1, player2),
            board = Board(),
        )

        val initialPlayer = gameState.currentPlayer
        initialPlayer shouldBe player1

        val dice = Dice(Random(42))
        gameService.executeTurn(gameState, dice)

        gameState.currentPlayer shouldBe player2
    }
})
