package com.monopoly.domain.experiment

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain

class StatisticsDisplayTest : StringSpec({
    // TC-EXP-026: StatisticsDisplayが集計統計をフォーマットする
    "should format aggregated statistics for display" {
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
        val aggregated = AggregatedStatistics.from(statistics)
        val display = StatisticsDisplay()

        // When
        val output: String = display.format(aggregated)

        // Then
        output shouldContain "Total Games:"
        output shouldContain "2"
        output shouldContain "Win Rates:"
        output shouldContain "Player1"
        output shouldContain "Player2"
        output shouldContain "Average Turn Count:"
        output shouldContain "Average Final Assets:"
    }

    // TC-EXP-027: StatisticsDisplayが空の統計を処理する
    "should handle empty statistics" {
        // Given
        val statistics: List<GameStatistics> = emptyList()
        val aggregated = AggregatedStatistics.from(statistics)
        val display = StatisticsDisplay()

        // When
        val output: String = display.format(aggregated)

        // Then
        output shouldContain "Total Games:"
        output shouldContain "0"
    }

    // TC-EXP-028: StatisticsDisplayがパーセント表示で勝率を表示する
    "should display win rates as percentages" {
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
        val aggregated = AggregatedStatistics.from(statistics)
        val display = StatisticsDisplay()

        // When
        val output: String = display.format(aggregated)

        // Then
        output shouldContain "%"
    }
})
