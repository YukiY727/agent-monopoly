package com.monopoly.domain.experiment

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.io.File

class HtmlReportWriterTest : StringSpec({
    // TC-EXP-037: HTMLレポートがファイルに書き込まれる
    "should write HTML report to file" {
        // Given
        val writer = HtmlReportWriter(outputDir = "test-output")
        val statistics: List<GameStatistics> =
            listOf(
                GameStatistics(
                    gameId = "game_001",
                    timestamp = 1000L,
                    turnCount = 50,
                    winner = "Player1",
                    finalAssets = mapOf("Player1" to 2000, "Player2" to 0),
                    bankruptcyOrder = listOf("Player2"),
                ),
                GameStatistics(
                    gameId = "game_002",
                    timestamp = 2000L,
                    turnCount = 60,
                    winner = "Player2",
                    finalAssets = mapOf("Player1" to 0, "Player2" to 2500),
                    bankruptcyOrder = listOf("Player1"),
                ),
            )
        val aggregated: AggregatedStatistics = AggregatedStatistics.from(statistics)

        // When
        val htmlPath: String = writer.write(statistics, aggregated)

        // Then
        val file = File(htmlPath)
        file.exists() shouldBe true

        val content: String = file.readText()
        content shouldContain "<!DOCTYPE html>"
        content shouldContain "Monopoly Experiment Report"
        content shouldContain "Player1"
        content shouldContain "Player2"

        // クリーンアップ
        file.delete()
        File("test-output").deleteRecursively()
    }

    // TC-EXP-038: HTMLレポートにサマリー統計が含まれる
    "should include summary statistics in HTML report" {
        // Given
        val writer = HtmlReportWriter(outputDir = "test-output")
        val statistics: List<GameStatistics> =
            listOf(
                GameStatistics(
                    gameId = "game_001",
                    timestamp = 1000L,
                    turnCount = 50,
                    winner = "Player1",
                    finalAssets = mapOf("Player1" to 2000, "Player2" to 0),
                    bankruptcyOrder = listOf("Player2"),
                ),
                GameStatistics(
                    gameId = "game_002",
                    timestamp = 2000L,
                    turnCount = 60,
                    winner = "Player2",
                    finalAssets = mapOf("Player1" to 0, "Player2" to 2500),
                    bankruptcyOrder = listOf("Player1"),
                ),
            )
        val aggregated: AggregatedStatistics = AggregatedStatistics.from(statistics)

        // When
        val htmlPath: String = writer.write(statistics, aggregated)

        // Then
        val content: String = File(htmlPath).readText()

        // サマリー統計が含まれている
        content shouldContain "Total Games"
        content shouldContain "2" // 総ゲーム数
        content shouldContain "Average Turn Count"
        content shouldContain "55" // (50 + 60) / 2

        // クリーンアップ
        File(htmlPath).delete()
        File("test-output").deleteRecursively()
    }

    // TC-EXP-039: HTMLレポートに勝率が含まれる
    "should include win rates in HTML report" {
        // Given
        val writer = HtmlReportWriter(outputDir = "test-output")
        val statistics: List<GameStatistics> =
            listOf(
                GameStatistics(
                    gameId = "game_001",
                    timestamp = 1000L,
                    turnCount = 50,
                    winner = "Player1",
                    finalAssets = mapOf("Player1" to 2000, "Player2" to 0),
                    bankruptcyOrder = listOf("Player2"),
                ),
                GameStatistics(
                    gameId = "game_002",
                    timestamp = 2000L,
                    turnCount = 60,
                    winner = "Player1",
                    finalAssets = mapOf("Player1" to 2500, "Player2" to 0),
                    bankruptcyOrder = listOf("Player2"),
                ),
            )
        val aggregated: AggregatedStatistics = AggregatedStatistics.from(statistics)

        // When
        val htmlPath: String = writer.write(statistics, aggregated)

        // Then
        val content: String = File(htmlPath).readText()

        // 勝率が含まれている
        content shouldContain "Win Rates"
        content shouldContain "100%" // Player1の勝率
        content shouldContain "0%" // Player2の勝率

        // クリーンアップ
        File(htmlPath).delete()
        File("test-output").deleteRecursively()
    }

    // TC-EXP-040: HTMLレポートにゲーム詳細が含まれる
    "should include game details in HTML report" {
        // Given
        val writer = HtmlReportWriter(outputDir = "test-output")
        val statistics: List<GameStatistics> =
            listOf(
                GameStatistics(
                    gameId = "game_001",
                    timestamp = 1000L,
                    turnCount = 50,
                    winner = "Player1",
                    finalAssets = mapOf("Player1" to 2000, "Player2" to 0),
                    bankruptcyOrder = listOf("Player2"),
                ),
            )
        val aggregated: AggregatedStatistics = AggregatedStatistics.from(statistics)

        // When
        val htmlPath: String = writer.write(statistics, aggregated)

        // Then
        val content: String = File(htmlPath).readText()

        // ゲーム詳細が含まれている
        content shouldContain "Game Details"
        content shouldContain "game_001"
        content shouldContain "50" // ターン数

        // クリーンアップ
        File(htmlPath).delete()
        File("test-output").deleteRecursively()
    }
})
