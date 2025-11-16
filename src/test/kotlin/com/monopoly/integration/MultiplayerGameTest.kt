package com.monopoly.integration
import com.monopoly.domain.strategy.AlwaysBuyStrategy

import com.monopoly.domain.model.Board
import com.monopoly.domain.model.Dice
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Player
import com.monopoly.domain.model.StandardBoard
import com.monopoly.domain.service.GameService
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe

/**
 * マルチプレイヤーゲームの統合テスト
 *
 * Phase 20: 3人以上のマルチプレイヤー対応
 */
class MultiplayerGameTest : DescribeSpec({
    describe("3人プレイヤーのゲーム") {
        it("should run game with 3 players") {
            val board: Board = StandardBoard.create()
            val player1 = Player("Alice", AlwaysBuyStrategy())
            val player2 = Player("Bob", AlwaysBuyStrategy())
            val player3 = Player("Charlie", AlwaysBuyStrategy())

            val gameState = GameState(
                players = listOf(player1, player2, player3),
                board = board,
            )

            val dice = Dice()
            val gameService = GameService()

            val winner = gameService.runGame(gameState, dice, maxTurns = 100)

            // ゲームが完了し、勝者が決定されている
            listOf(player1, player2, player3).any { it.name == winner.name } shouldBe true
            gameState.getActivePlayerCount() shouldBeGreaterThanOrEqual 1
        }
    }

    describe("4人プレイヤーのゲーム") {
        it("should run game with 4 players") {
            val board: Board = StandardBoard.create()
            val player1 = Player("Alice", AlwaysBuyStrategy())
            val player2 = Player("Bob", AlwaysBuyStrategy())
            val player3 = Player("Charlie", AlwaysBuyStrategy())
            val player4 = Player("David", AlwaysBuyStrategy())

            val gameState = GameState(
                players = listOf(player1, player2, player3, player4),
                board = board,
            )

            val dice = Dice()
            val gameService = GameService()

            val winner = gameService.runGame(gameState, dice, maxTurns = 100)

            // ゲームが完了し、勝者が決定されている
            listOf(player1, player2, player3, player4).any { it.name == winner.name } shouldBe true
            gameState.getActivePlayerCount() shouldBeGreaterThanOrEqual 1
        }
    }

    describe("8人プレイヤーのゲーム") {
        it("should run game with 8 players") {
            val board: Board = StandardBoard.create()
            val players = (1..8).map { i ->
                Player("Player$i", AlwaysBuyStrategy())
            }

            val gameState = GameState(
                players = players,
                board = board,
            )

            val dice = Dice()
            val gameService = GameService()

            val winner = gameService.runGame(gameState, dice, maxTurns = 100)

            // ゲームが完了し、勝者が決定されている
            players.any { it.name == winner.name } shouldBe true
            gameState.getActivePlayerCount() shouldBeGreaterThanOrEqual 1
        }
    }
})
