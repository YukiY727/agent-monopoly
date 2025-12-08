package com.monopoly.domain.experiment

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch

class GameStatisticsTest : StringSpec({
    // TC-EXP-001: GameStatisticsが正しく初期化される
    "should create GameStatistics with all fields" {
        // Given
        val gameId = "game_001"
        val timestamp: Long = 1733654400000L // 2024-12-08 12:00:00 UTC
        val turnCount = 45
        val winner = "Alice"
        val finalAssets: Map<String, Int> = mapOf("Alice" to 2500, "Bob" to -100)
        val bankruptcyOrder: List<String> = listOf("Bob")

        // When
        val statistics =
            GameStatistics(
                gameId = gameId,
                timestamp = timestamp,
                turnCount = turnCount,
                winner = winner,
                finalAssets = finalAssets,
                bankruptcyOrder = bankruptcyOrder,
            )

        // Then
        statistics.gameId shouldBe "game_001"
        statistics.timestamp shouldBe 1733654400000L
        statistics.turnCount shouldBe 45
        statistics.winner shouldBe "Alice"
        statistics.finalAssets shouldBe mapOf("Alice" to 2500, "Bob" to -100)
        statistics.bankruptcyOrder shouldBe listOf("Bob")
    }

    // TC-EXP-002: GameIDのフォーマットが正しい（game_XXX形式）
    "should validate gameId format as game_XXX" {
        // Given
        val statistics =
            GameStatistics(
                gameId = "game_001",
                timestamp = System.currentTimeMillis(),
                turnCount = 10,
                winner = "Alice",
                finalAssets = mapOf("Alice" to 1500),
                bankruptcyOrder = emptyList(),
            )

        // Then
        statistics.gameId shouldMatch Regex("game_\\d{3}")
    }

    // TC-EXP-003: 破産順序が空リスト（誰も破産していない）
    "should allow empty bankruptcy order when no one went bankrupt" {
        // Given & When
        val statistics =
            GameStatistics(
                gameId = "game_001",
                timestamp = System.currentTimeMillis(),
                turnCount = 100,
                winner = "Alice",
                finalAssets = mapOf("Alice" to 3000, "Bob" to 500),
                bankruptcyOrder = emptyList(),
            )

        // Then
        statistics.bankruptcyOrder shouldBe emptyList()
    }

    // TC-EXP-004: 複数プレイヤーが破産した場合の順序
    "should record bankruptcy order for multiple players" {
        // Given & When
        val statistics =
            GameStatistics(
                gameId = "game_005",
                timestamp = System.currentTimeMillis(),
                turnCount = 60,
                winner = "Alice",
                finalAssets = mapOf("Alice" to 2000, "Bob" to -50, "Carol" to -100),
                bankruptcyOrder = listOf("Carol", "Bob"), // Carolが先に破産
            )

        // Then
        statistics.bankruptcyOrder shouldBe listOf("Carol", "Bob")
        statistics.winner shouldBe "Alice"
    }

    // TC-EXP-005: タイムスタンプが正の値
    "should have positive timestamp" {
        // Given & When
        val currentTime: Long = System.currentTimeMillis()
        val statistics =
            GameStatistics(
                gameId = "game_001",
                timestamp = currentTime,
                turnCount = 20,
                winner = "Bob",
                finalAssets = mapOf("Alice" to 0, "Bob" to 1800),
                bankruptcyOrder = listOf("Alice"),
            )

        // Then
        statistics.timestamp shouldBe currentTime
        assert(statistics.timestamp > 0) { "Timestamp should be positive" }
    }
})
