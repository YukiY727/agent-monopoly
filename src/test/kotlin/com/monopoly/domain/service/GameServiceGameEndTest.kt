package com.monopoly.domain.service

import com.monopoly.domain.model.Board
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Player
import com.monopoly.domain.strategy.AlwaysBuyStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class GameServiceGameEndTest : StringSpec({
    val gameService = GameService()

    // TC-150: ゲーム終了（1人残り）
    // Given: GameStateで3人中2人が破産
    // When: checkGameEnd(gameState)
    // Then: true
    "should end game when only one player remains" {
        val player1 = Player("Alice", AlwaysBuyStrategy())
        val player2 = Player("Bob", AlwaysBuyStrategy())
        val player3 = Player("Charlie", AlwaysBuyStrategy())

        player2.markAsBankrupt()
        player3.markAsBankrupt()

        val gameState = GameState(
            players = listOf(player1, player2, player3),
            board = Board(),
        )

        val result = gameService.checkGameEnd(gameState)

        result shouldBe true
    }

    // TC-151: ゲーム継続中（複数人アクティブ）
    // Given: GameStateで3人中1人が破産
    // When: checkGameEnd(gameState)
    // Then: false
    "should continue game when multiple players are active" {
        val player1 = Player("Alice", AlwaysBuyStrategy())
        val player2 = Player("Bob", AlwaysBuyStrategy())
        val player3 = Player("Charlie", AlwaysBuyStrategy())

        player2.markAsBankrupt()

        val gameState = GameState(
            players = listOf(player1, player2, player3),
            board = Board(),
        )

        val result = gameService.checkGameEnd(gameState)

        result shouldBe false
    }
})
