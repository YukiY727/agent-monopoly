package com.monopoly.integration

import com.monopoly.domain.model.BoardFixtures
import com.monopoly.domain.model.Dice
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Player
import com.monopoly.domain.service.GameService
import com.monopoly.domain.strategy.AlwaysBuyStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import kotlin.random.Random

class GameIntegrationTest : StringSpec({
    val gameService = GameService()

    // TC-300: 2プレイヤーでゲーム完走
    // Given: 2人のPlayer、Board、GameState
    // When: runGame(gameState)
    // Then: ゲームが最後まで実行され、勝者が決定される
    "should run complete game with 2 players and determine winner" {
        val player1 = Player("Alice", AlwaysBuyStrategy())
        val player2 = Player("Bob", AlwaysBuyStrategy())
        val board = BoardFixtures.createStandardBoard()
        val gameState =
            GameState(
                players = listOf(player1, player2),
                board = board,
            )

        val dice = Dice(Random(42))
        val winner = gameService.runGame(gameState, dice, maxTurns = 10000)

        // ゲームが終了している
        gameState.isGameOver shouldBe true
        // 勝者が決定されている
        listOf(player1, player2) shouldContain winner
        // 勝者が破産していない
        winner.isBankrupt.shouldBeFalse()
    }

    // TC-301: 勝者の妥当性
    // Given: GameState
    // When: runGame(gameState)
    // Then: 勝者は破産しておらず、アクティブプレイヤーの1人
    "should return valid winner who is not bankrupt and is an active player" {
        val player1 = Player("Alice", AlwaysBuyStrategy())
        val player2 = Player("Bob", AlwaysBuyStrategy())
        val gameState =
            GameState(
                players = listOf(player1, player2),
                board = BoardFixtures.createStandardBoard(),
            )

        val dice = Dice(Random(123))
        val winner = gameService.runGame(gameState, dice, maxTurns = 10000)

        // 勝者は破産していない
        winner.isBankrupt.shouldBeFalse()
        // 勝者はプレイヤーの1人
        listOf(player1, player2) shouldContain winner
        // アクティブプレイヤーが少なくとも1人いる（ターン数上限に達した場合は複数残る可能性がある）
        (gameState.getActivePlayerCount() >= 1) shouldBe true
    }

    // TC-302: 複数回実行でランダム性確認
    // Given: 同じ初期条件のGameState
    // When: runGame()を複数回実行（異なるシード）
    // Then: 勝者が異なる場合がある（ランダム性が機能）
    "should produce different outcomes with different random seeds" {
        val winners = mutableListOf<String>()

        // 異なるシードで複数回実行
        // Phase 2: ゾロ目ロジック追加により、より多様なシードで確認
        for (seed in listOf(1L, 2L, 3L, 42L, 123L, 999L)) {
            val player1 = Player("Alice", AlwaysBuyStrategy())
            val player2 = Player("Bob", AlwaysBuyStrategy())
            val gameState =
                GameState(
                    players = listOf(player1, player2),
                    board = BoardFixtures.createStandardBoard(),
                )

            val dice = Dice(Random(seed))
            val winner = gameService.runGame(gameState, dice, maxTurns = 10000)
            winners.add(winner.name)
        }

        // すべて同じ勝者だとランダム性が機能していない
        // 6回実行して全部同じ結果になる確率は統計的に非常に低い
        val allSame = winners.all { it == winners[0] }
        allSame.shouldBeFalse()
    }
})
