package com.monopoly.domain.service

import com.monopoly.domain.model.BoardFixtures
import com.monopoly.domain.model.Dice
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Player
import com.monopoly.domain.strategy.AlwaysBuyStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import kotlin.random.Random

class GameServiceRunGameTest : StringSpec({
    val gameService = GameService()

    // TC-170: ゲーム完走
    // Given: GameState（2プレイヤー）
    // When: runGame(gameState)
    // Then: ゲームが終了し、勝者が返される
    "should run game until completion and return winner" {
        val player1 = Player("Alice", AlwaysBuyStrategy())
        val player2 = Player("Bob", AlwaysBuyStrategy())
        val gameState =
            GameState(
                players = listOf(player1, player2),
                board = BoardFixtures.createStandardBoard(),
            )

        val dice = Dice(Random(42))
        val winner = gameService.runGame(gameState, dice)

        // ゲームが終了している
        gameState.isGameOver shouldBe true
        // 勝者が存在する
        listOf(player1, player2) shouldContain winner
    }

    // TC-171: 勝者が破産していない
    // Given: GameState
    // When: runGame(gameState)
    // Then: 返された勝者のisBankrupt()がfalse
    "should return winner who is not bankrupt" {
        val player1 = Player("Alice", AlwaysBuyStrategy())
        val player2 = Player("Bob", AlwaysBuyStrategy())
        val gameState =
            GameState(
                players = listOf(player1, player2),
                board = BoardFixtures.createStandardBoard(),
            )

        val dice = Dice(Random(123))
        val winner = gameService.runGame(gameState, dice)

        winner.isBankrupt.shouldBeFalse()
    }

    // TC-172: 複数回実行でランダム性確認
    // Given: 同じ初期条件のGameState
    // When: runGame()を3回実行（異なるシード）
    // Then: 少なくとも1回は異なる結果が出る（ランダム性が機能）
    "should produce different results with different random seeds" {
        val results = mutableListOf<String>()

        // 異なるシードで3回実行
        for (seed in listOf(100L, 200L, 300L, 400L, 500L)) {
            val player1 = Player("Alice", AlwaysBuyStrategy())
            val player2 = Player("Bob", AlwaysBuyStrategy())
            val gameState =
                GameState(
                    players = listOf(player1, player2),
                    board = BoardFixtures.createStandardBoard(),
                )

            val dice = Dice(Random(seed))
            val winner = gameService.runGame(gameState, dice)
            results.add(winner.name)
        }

        // すべて同じ勝者だとランダム性が機能していない可能性がある
        // （理論的には可能だが、5回実行して全部同じ結果になる確率は低い）
        val allSame = results.all { it == results[0] }
        allSame.shouldBeFalse()
    }
})
