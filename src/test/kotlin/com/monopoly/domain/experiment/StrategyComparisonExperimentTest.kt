package com.monopoly.domain.experiment

import com.monopoly.domain.strategy.AggressiveStrategy
import com.monopoly.domain.strategy.AlwaysPlayerStrategy
import com.monopoly.domain.strategy.ConservativeStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.shouldBeBetween
import io.kotest.matchers.shouldBe

class StrategyComparisonExperimentTest : StringSpec({

    "should run comparison between two strategies" {
        // Given
        val experiment = StrategyComparisonExperiment(gamesPerMatchup = 10)
        val strategies: List<StrategyInfo> = listOf(
            StrategyInfo("Always", AlwaysPlayerStrategy()),
            StrategyInfo("Conservative", ConservativeStrategy()),
        )

        // When
        val result: ComparisonMatrix = experiment.run(strategies)

        // Then
        result.strategies shouldHaveSize 2
        result.matchups shouldHaveSize 1 // 2C2 = 1 matchup

        // Verify matchup exists
        val matchup: StrategyMatchup = result.matchups.first()
        matchup.strategy1 shouldBe "Always"
        matchup.strategy2 shouldBe "Conservative"
        matchup.gamesPlayed shouldBe 10

        // Win rate should sum to approximately 1.0
        val totalWinRate: Double = matchup.strategy1WinRate + matchup.strategy2WinRate
        totalWinRate.shouldBeBetween(0.9, 1.1, 0.0) // Allow for draws
    }

    "should run comparison among three strategies" {
        // Given
        val experiment = StrategyComparisonExperiment(gamesPerMatchup = 5)
        val strategies: List<StrategyInfo> = listOf(
            StrategyInfo("Always", AlwaysPlayerStrategy()),
            StrategyInfo("Conservative", ConservativeStrategy()),
            StrategyInfo("Aggressive", AggressiveStrategy()),
        )

        // When
        val result: ComparisonMatrix = experiment.run(strategies)

        // Then
        result.strategies shouldHaveSize 3
        result.matchups shouldHaveSize 3 // 3C2 = 3 matchups

        // Verify all matchups exist
        val matchupNames: List<Pair<String, String>> = result.matchups.map {
            it.strategy1 to it.strategy2
        }
        matchupNames shouldBe listOf(
            "Always" to "Conservative",
            "Always" to "Aggressive",
            "Conservative" to "Aggressive",
        )
    }

    "should calculate win rates correctly" {
        // Given
        val experiment = StrategyComparisonExperiment(gamesPerMatchup = 20)
        val strategies: List<StrategyInfo> = listOf(
            StrategyInfo("Always", AlwaysPlayerStrategy()),
            StrategyInfo("Conservative", ConservativeStrategy()),
        )

        // When
        val result: ComparisonMatrix = experiment.run(strategies)

        // Then
        val matchup: StrategyMatchup = result.matchups.first()

        // Win rates should be between 0 and 1
        matchup.strategy1WinRate.shouldBeBetween(0.0, 1.0, 0.0)
        matchup.strategy2WinRate.shouldBeBetween(0.0, 1.0, 0.0)

        // Win counts should sum to games played
        matchup.strategy1Wins + matchup.strategy2Wins shouldBe matchup.gamesPlayed
    }

    "should format matrix as table" {
        // Given
        val matchups: List<StrategyMatchup> = listOf(
            StrategyMatchup(
                strategy1 = "A",
                strategy2 = "B",
                strategy1Wins = 7,
                strategy2Wins = 3,
                gamesPlayed = 10,
            ),
        )
        val matrix = ComparisonMatrix(
            strategies = listOf("A", "B"),
            matchups = matchups,
        )

        // When
        val table: String = matrix.toTable()

        // Then
        table.contains("A") shouldBe true
        table.contains("B") shouldBe true
        table.contains("70.0%") shouldBe true
        table.contains("30.0%") shouldBe true
    }
})
