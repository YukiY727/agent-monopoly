package com.monopoly.domain.experiment

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.shouldBeBetween
import io.kotest.matchers.shouldBe

class StatisticalAnalysisTest : StringSpec({

    "should calculate confidence interval for win rate" {
        // Given
        val wins = 70
        val total = 100
        val confidenceLevel = 0.95

        // When
        val result: ConfidenceInterval = StatisticalAnalysis.confidenceInterval(wins, total, confidenceLevel)

        // Then
        // 70% win rate with 100 samples, 95% CI should be approximately [60%, 79%]
        result.lower.shouldBeBetween(0.58, 0.62, 0.0)
        result.upper.shouldBeBetween(0.78, 0.82, 0.0)
        result.point shouldBe 0.7
    }

    "should calculate Cohen's d effect size" {
        // Given
        // Two groups with different win rates
        val winRate1 = 0.70
        val winRate2 = 0.50
        val sampleSize = 100

        // When
        val effectSize: Double = StatisticalAnalysis.cohensD(winRate1, winRate2, sampleSize)

        // Then
        // Effect size should be positive (group1 > group2)
        // Cohen's d: 0.2 = small, 0.5 = medium, 0.8 = large
        effectSize.shouldBeBetween(0.3, 0.5, 0.0) // Medium effect
    }

    "should perform two-proportion z-test" {
        // Given
        val wins1 = 70
        val total1 = 100
        val wins2 = 50
        val total2 = 100

        // When
        val result: HypothesisTestResult = StatisticalAnalysis.twoProportionZTest(wins1, total1, wins2, total2)

        // Then
        // 70% vs 50% with n=100 each should be highly significant
        result.pValue.shouldBeBetween(0.0, 0.01, 0.0)
        result.significant shouldBe true
        result.zScore.shouldBeBetween(2.5, 3.5, 0.0)
    }

    "should not find significance when win rates are similar" {
        // Given
        val wins1 = 52
        val total1 = 100
        val wins2 = 48
        val total2 = 100

        // When
        val result: HypothesisTestResult = StatisticalAnalysis.twoProportionZTest(wins1, total1, wins2, total2)

        // Then
        // 52% vs 48% is not significant at p < 0.05
        result.significant shouldBe false
    }

    "should apply Bonferroni correction" {
        // Given
        val pValues: List<Double> = listOf(0.01, 0.03, 0.05, 0.10)
        val numComparisons = 4

        // When
        val adjusted: List<Double> = StatisticalAnalysis.bonferroniCorrection(pValues, numComparisons)

        // Then
        // Bonferroni multiplies p-values by number of comparisons
        adjusted[0].shouldBeBetween(0.03, 0.05, 0.0) // 0.01 * 4 = 0.04
        adjusted[1].shouldBeBetween(0.11, 0.13, 0.0) // 0.03 * 4 = 0.12
        adjusted[2].shouldBeBetween(0.19, 0.21, 0.0) // 0.05 * 4 = 0.20
        adjusted[3].shouldBeBetween(0.39, 0.41, 0.0) // 0.10 * 4 = 0.40
    }

    "should detect strict dominance" {
        // Given
        val matrix = ComparisonMatrix(
            strategies = listOf("A", "B", "C"),
            matchups = listOf(
                StrategyMatchup("A", "B", 80, 20, 100),
                StrategyMatchup("A", "C", 70, 30, 100),
                StrategyMatchup("B", "C", 60, 40, 100),
            ),
        )

        // When
        val dominance: DominanceAnalysis = StatisticalAnalysis.analyzeDominance(matrix)

        // Then
        // A beats both B and C (>50%), so A strictly dominates
        dominance.strictlyDominant shouldBe "A"
        // C loses to both A and B (<50%), so C is strictly dominated
        dominance.strictlyDominated shouldBe listOf("C")
    }

    "should detect weak dominance" {
        // Given
        val matrix = ComparisonMatrix(
            strategies = listOf("A", "B", "C"),
            matchups = listOf(
                StrategyMatchup("A", "B", 50, 50, 100), // Tie
                StrategyMatchup("A", "C", 60, 40, 100), // A wins
                StrategyMatchup("B", "C", 70, 30, 100), // B wins
            ),
        )

        // When
        val dominance: DominanceAnalysis = StatisticalAnalysis.analyzeDominance(matrix)

        // Then
        // No strict dominance (A ties with B)
        dominance.strictlyDominant shouldBe null
        // A and B both weakly dominate C
        dominance.weaklyDominant shouldBe listOf("A", "B")
    }
})
