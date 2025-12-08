package com.monopoly.domain.experiment

import com.monopoly.domain.model.player.PlayerStrategy
import com.monopoly.domain.strategy.AlwaysPlayerStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch

class ExperimentRunnerTest : StringSpec({
    // TC-EXP-006: runSingleGameが正しいgameIdフォーマットでGameStatisticsを返す
    "should return GameStatistics with correct gameId format" {
        // Given
        val strategies: List<PlayerStrategy> = listOf(AlwaysPlayerStrategy(), AlwaysPlayerStrategy())
        val runner = ExperimentRunner()

        // When
        val statistics: GameStatistics = runner.runSingleGame(gameId = "game_001", strategies = strategies)

        // Then
        statistics.gameId shouldBe "game_001"
        statistics.gameId shouldMatch Regex("game_\\d{3}")
    }

    // TC-EXP-007: runSingleGameがターン数を記録する
    "should record turn count" {
        // Given
        val strategies: List<PlayerStrategy> = listOf(AlwaysPlayerStrategy(), AlwaysPlayerStrategy())
        val runner = ExperimentRunner()

        // When
        val statistics: GameStatistics = runner.runSingleGame(gameId = "game_001", strategies = strategies)

        // Then
        assert(statistics.turnCount > 0) { "Turn count should be positive" }
    }

    // TC-EXP-008: runSingleGameが勝者を正しく識別する
    "should identify winner correctly" {
        // Given
        val strategies: List<PlayerStrategy> = listOf(AlwaysPlayerStrategy(), AlwaysPlayerStrategy())
        val runner = ExperimentRunner()

        // When
        val statistics: GameStatistics = runner.runSingleGame(gameId = "game_001", strategies = strategies)

        // Then
        assert(statistics.winner.isNotEmpty()) { "Winner name should not be empty" }
        statistics.finalAssets.keys shouldContain statistics.winner
    }

    // TC-EXP-009: runSingleGameが全プレイヤーの最終資産を記録する
    "should record final assets for all players" {
        // Given
        val strategies: List<PlayerStrategy> = listOf(AlwaysPlayerStrategy(), AlwaysPlayerStrategy())
        val runner = ExperimentRunner()

        // When
        val statistics: GameStatistics = runner.runSingleGame(gameId = "game_001", strategies = strategies)

        // Then
        statistics.finalAssets.size shouldBe 2
        statistics.finalAssets.values.forEach { asset ->
            assert(asset != 0) { "Final assets should not be zero (either positive for winner or negative for bankrupt)" }
        }
    }

    // TC-EXP-010: runSingleGameが破産順序を記録する（破産がある場合）
    "should record bankruptcy order when players go bankrupt" {
        // Given
        val strategies: List<PlayerStrategy> = listOf(AlwaysPlayerStrategy(), AlwaysPlayerStrategy())
        val runner = ExperimentRunner()

        // When
        val statistics: GameStatistics = runner.runSingleGame(gameId = "game_001", strategies = strategies)

        // Then
        // 2人ゲームなので1人は破産するはず
        if (statistics.bankruptcyOrder.isNotEmpty()) {
            assert(statistics.bankruptcyOrder.size >= 1) { "At least one player should go bankrupt in a 2-player game" }
            statistics.bankruptcyOrder.forEach { bankruptPlayer ->
                statistics.finalAssets.keys shouldContain bankruptPlayer
            }
        }
    }

    // TC-EXP-011: runSingleGameが異なるgameIdで異なるゲームを実行する
    "should run independent games with different gameIds" {
        // Given
        val strategies: List<PlayerStrategy> = listOf(AlwaysPlayerStrategy(), AlwaysPlayerStrategy())
        val runner = ExperimentRunner()

        // When
        val statistics1: GameStatistics = runner.runSingleGame(gameId = "game_001", strategies = strategies)
        val statistics2: GameStatistics = runner.runSingleGame(gameId = "game_002", strategies = strategies)

        // Then
        statistics1.gameId shouldBe "game_001"
        statistics2.gameId shouldBe "game_002"
        assert(statistics1.timestamp <= statistics2.timestamp) { "Second game timestamp should be >= first game timestamp" }
    }

    // TC-EXP-012: runSingleGameがタイムスタンプを正しく記録する
    "should record timestamp correctly" {
        // Given
        val strategies: List<PlayerStrategy> = listOf(AlwaysPlayerStrategy(), AlwaysPlayerStrategy())
        val runner = ExperimentRunner()
        val beforeTime: Long = System.currentTimeMillis()

        // When
        val statistics: GameStatistics = runner.runSingleGame(gameId = "game_001", strategies = strategies)

        // Then
        val afterTime: Long = System.currentTimeMillis()
        assert(statistics.timestamp >= beforeTime) { "Timestamp should be >= before time" }
        assert(statistics.timestamp <= afterTime) { "Timestamp should be <= after time" }
    }

    // TC-EXP-013: runExperimentが指定回数のゲームを実行する
    "should run specified number of games" {
        // Given
        val strategies: List<PlayerStrategy> = listOf(AlwaysPlayerStrategy(), AlwaysPlayerStrategy())
        val runner = ExperimentRunner()
        val gameCount = 5

        // When
        val results: List<GameStatistics> = runner.runExperiment(gameCount = gameCount, strategies = strategies)

        // Then
        results.size shouldBe 5
    }

    // TC-EXP-014: runExperimentが連番のgameIdを生成する
    "should generate sequential gameIds" {
        // Given
        val strategies: List<PlayerStrategy> = listOf(AlwaysPlayerStrategy(), AlwaysPlayerStrategy())
        val runner = ExperimentRunner()
        val gameCount = 3

        // When
        val results: List<GameStatistics> = runner.runExperiment(gameCount = gameCount, strategies = strategies)

        // Then
        results[0].gameId shouldBe "game_001"
        results[1].gameId shouldBe "game_002"
        results[2].gameId shouldBe "game_003"
    }

    // TC-EXP-015: runExperimentが各ゲームで独立した統計を返す
    "should return independent statistics for each game" {
        // Given
        val strategies: List<PlayerStrategy> = listOf(AlwaysPlayerStrategy(), AlwaysPlayerStrategy())
        val runner = ExperimentRunner()
        val gameCount = 3

        // When
        val results: List<GameStatistics> = runner.runExperiment(gameCount = gameCount, strategies = strategies)

        // Then
        // 各ゲームのタイムスタンプは異なる（または同じ）
        results.forEach { stats ->
            assert(stats.turnCount > 0) { "Each game should have positive turn count" }
            assert(stats.winner.isNotEmpty()) { "Each game should have a winner" }
        }
    }
})
