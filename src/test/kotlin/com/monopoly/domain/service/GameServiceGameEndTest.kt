package com.monopoly.domain.service

import com.monopoly.domain.model.BoardFixtures
import com.monopoly.domain.model.Dice
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Player
import com.monopoly.domain.model.impl.StandardDice
import com.monopoly.domain.strategy.AlwaysBuyStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class GameServiceGameEndTest : StringSpec({
    val gameService = GameService(BuildingService(MonopolyCheckerService()))

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

        val gameState =
            GameState(
                players = listOf(player1, player2, player3),
                board = BoardFixtures.createStandardBoard(),
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

        val gameState =
            GameState(
                players = listOf(player1, player2, player3),
                board = BoardFixtures.createStandardBoard(),
            )

        val result = gameService.checkGameEnd(gameState)

        result shouldBe false
    }

    // TC-152: runGame - 最大ターン数到達時の勝者決定（資産比較）
    // Given: 複数のアクティブプレイヤー、maxTurns=1
    // When: runGame(gameState, dice, maxTurns=1)
    // Then: 最も資産の多いプレイヤーが勝者として返される
    "should determine winner by total assets when max turns reached" {
        val player1 = Player("Alice", AlwaysBuyStrategy())
        val player2 = Player("Bob", AlwaysBuyStrategy())

        // Player2により多くの所持金を与える
        player2.receiveMoney(com.monopoly.domain.model.Money(1000))

        val gameState =
            GameState(
                players = listOf(player1, player2),
                board = BoardFixtures.createStandardBoard(),
            )

        val dice = com.monopoly.domain.model.impl.StandardDice()
        val winner: Player = gameService.runGame(gameState, dice, maxTurns = 1)

        // Player2の方が資産が多いので勝者となる
        winner shouldBe player2
    }

    // TC-153: runGame - 1人の勝者がいる場合
    // Given: 1人のアクティブプレイヤー、他は破産
    // When: runGame(gameState, dice, maxTurns=10)
    // Then: アクティブなプレイヤーが勝者として返される
    "should return the only active player as winner" {
        val player1 = Player("Alice", AlwaysBuyStrategy())
        val player2 = Player("Bob", AlwaysBuyStrategy())
        player2.markAsBankrupt()

        val gameState =
            GameState(
                players = listOf(player1, player2),
                board = BoardFixtures.createStandardBoard(),
            )

        val dice = com.monopoly.domain.model.impl.StandardDice()
        val winner: Player = gameService.runGame(gameState, dice, maxTurns = 10)

        winner shouldBe player1
    }
})
