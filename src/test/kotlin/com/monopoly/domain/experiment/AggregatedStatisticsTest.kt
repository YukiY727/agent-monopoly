package com.monopoly.domain.experiment

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe

class AggregatedStatisticsTest : StringSpec({
    // TC-EXP-016: AggregatedStatisticsが総ゲーム数を正しく集計する
    "should calculate total game count correctly" {
        // Given
        val statistics: List<GameStatistics> =
            listOf(
                GameStatistics(
                    gameId = "game_001",
                    timestamp = 1000L,
                    turnCount = 50,
                    winner = "Player1",
                    finalAssets = mapOf("Player1" to 2000, "Player2" to -100),
                    bankruptcyOrder = listOf("Player2"),
                ),
                GameStatistics(
                    gameId = "game_002",
                    timestamp = 2000L,
                    turnCount = 60,
                    winner = "Player2",
                    finalAssets = mapOf("Player1" to -50, "Player2" to 1800),
                    bankruptcyOrder = listOf("Player1"),
                ),
            )

        // When
        val aggregated = AggregatedStatistics.from(statistics)

        // Then
        aggregated.totalGames shouldBe 2
    }

    // TC-EXP-017: AggregatedStatisticsが勝率を正しく計算する
    "should calculate win rates correctly" {
        // Given
        val statistics: List<GameStatistics> =
            listOf(
                GameStatistics(
                    gameId = "game_001",
                    timestamp = 1000L,
                    turnCount = 50,
                    winner = "Player1",
                    finalAssets = mapOf("Player1" to 2000, "Player2" to -100),
                    bankruptcyOrder = listOf("Player2"),
                ),
                GameStatistics(
                    gameId = "game_002",
                    timestamp = 2000L,
                    turnCount = 60,
                    winner = "Player2",
                    finalAssets = mapOf("Player1" to -50, "Player2" to 1800),
                    bankruptcyOrder = listOf("Player1"),
                ),
                GameStatistics(
                    gameId = "game_003",
                    timestamp = 3000L,
                    turnCount = 55,
                    winner = "Player1",
                    finalAssets = mapOf("Player1" to 2500, "Player2" to -200),
                    bankruptcyOrder = listOf("Player2"),
                ),
            )

        // When
        val aggregated = AggregatedStatistics.from(statistics)

        // Then
        aggregated.winRates["Player1"] shouldBe (2.0 / 3.0)
        aggregated.winRates["Player2"] shouldBe (1.0 / 3.0)
    }

    // TC-EXP-018: AggregatedStatisticsが平均ターン数を正しく計算する
    "should calculate average turn count correctly" {
        // Given
        val statistics: List<GameStatistics> =
            listOf(
                GameStatistics(
                    gameId = "game_001",
                    timestamp = 1000L,
                    turnCount = 50,
                    winner = "Player1",
                    finalAssets = mapOf("Player1" to 2000, "Player2" to -100),
                    bankruptcyOrder = listOf("Player2"),
                ),
                GameStatistics(
                    gameId = "game_002",
                    timestamp = 2000L,
                    turnCount = 60,
                    winner = "Player2",
                    finalAssets = mapOf("Player1" to -50, "Player2" to 1800),
                    bankruptcyOrder = listOf("Player1"),
                ),
                GameStatistics(
                    gameId = "game_003",
                    timestamp = 3000L,
                    turnCount = 80,
                    winner = "Player1",
                    finalAssets = mapOf("Player1" to 2500, "Player2" to -200),
                    bankruptcyOrder = listOf("Player2"),
                ),
            )

        // When
        val aggregated = AggregatedStatistics.from(statistics)

        // Then
        // (50 + 60 + 80) / 3 = 63.33...
        aggregated.averageTurnCount shouldBe (190.0 / 3.0)
    }

    // TC-EXP-019: AggregatedStatisticsが平均最終資産を正しく計算する（プレイヤーごと）
    "should calculate average final assets per player correctly" {
        // Given
        val statistics: List<GameStatistics> =
            listOf(
                GameStatistics(
                    gameId = "game_001",
                    timestamp = 1000L,
                    turnCount = 50,
                    winner = "Player1",
                    finalAssets = mapOf("Player1" to 2000, "Player2" to -100),
                    bankruptcyOrder = listOf("Player2"),
                ),
                GameStatistics(
                    gameId = "game_002",
                    timestamp = 2000L,
                    turnCount = 60,
                    winner = "Player2",
                    finalAssets = mapOf("Player1" to -50, "Player2" to 1800),
                    bankruptcyOrder = listOf("Player1"),
                ),
            )

        // When
        val aggregated = AggregatedStatistics.from(statistics)

        // Then
        // Player1: (2000 + (-50)) / 2 = 975.0
        // Player2: ((-100) + 1800) / 2 = 850.0
        aggregated.averageFinalAssets["Player1"] shouldBe 975.0
        aggregated.averageFinalAssets["Player2"] shouldBe 850.0
    }

    // TC-EXP-020: AggregatedStatisticsが空のリストで初期化される
    "should handle empty statistics list" {
        // Given
        val statistics: List<GameStatistics> = emptyList()

        // When
        val aggregated = AggregatedStatistics.from(statistics)

        // Then
        aggregated.totalGames shouldBe 0
        aggregated.winRates shouldBe emptyMap()
        aggregated.averageTurnCount shouldBe 0.0
        aggregated.averageFinalAssets shouldBe emptyMap()
    }

    // TC-EXP-021: AggregatedStatisticsが実行時間を記録する
    "should record total duration" {
        // Given
        val statistics: List<GameStatistics> =
            listOf(
                GameStatistics(
                    gameId = "game_001",
                    timestamp = 1000L,
                    turnCount = 50,
                    winner = "Player1",
                    finalAssets = mapOf("Player1" to 2000, "Player2" to -100),
                    bankruptcyOrder = listOf("Player2"),
                ),
            )

        // When
        val aggregated = AggregatedStatistics.from(statistics)

        // Then
        aggregated.totalDuration shouldBeGreaterThanOrEqual 0L
    }
})
