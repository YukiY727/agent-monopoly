package com.monopoly.domain.experiment

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import java.io.File

class StatisticsWriterTest : StringSpec({
    val testOutputDir = "build/test-output/experiment"

    beforeEach {
        // テストディレクトリをクリーンアップ
        File(testOutputDir).deleteRecursively()
    }

    afterEach {
        // テスト後にクリーンアップ
        File(testOutputDir).deleteRecursively()
    }

    // TC-EXP-022: StatisticsWriterがJSONファイルに書き込む
    "should write JSON file with all game statistics" {
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
        val aggregated = AggregatedStatistics.from(statistics)
        val writer = StatisticsWriter(outputDir = testOutputDir)

        // When
        val result: WrittenFiles = writer.write(statistics, aggregated)

        // Then
        val jsonFile = File(result.jsonPath)
        jsonFile.exists() shouldBe true
        val jsonContent: String = jsonFile.readText()
        jsonContent shouldContain "game_001"
        jsonContent shouldContain "Player1"
        jsonContent shouldContain "Player2"
    }

    // TC-EXP-023: StatisticsWriterがCSVファイルに書き込む
    "should write CSV file with game-by-game data" {
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
        val writer = StatisticsWriter(outputDir = testOutputDir)

        // When
        val result: WrittenFiles = writer.write(statistics, aggregated)

        // Then
        val csvFile = File(result.csvPath)
        csvFile.exists() shouldBe true
        val csvContent: String = csvFile.readText()
        csvContent shouldStartWith "gameId,timestamp,turnCount,winner"
        csvContent shouldContain "game_001"
        csvContent shouldContain "game_002"
    }

    // TC-EXP-024: StatisticsWriterがタイムスタンプ付きファイル名を生成する
    "should generate timestamped filenames" {
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
        val aggregated = AggregatedStatistics.from(statistics)
        val writer = StatisticsWriter(outputDir = testOutputDir)

        // When
        val result: WrittenFiles = writer.write(statistics, aggregated)

        // Then
        result.jsonPath shouldContain testOutputDir
        result.jsonPath shouldContain ".json"
        result.csvPath shouldContain testOutputDir
        result.csvPath shouldContain ".csv"
    }

    // TC-EXP-025: StatisticsWriterがディレクトリを自動作成する
    "should create output directory if it does not exist" {
        // Given
        val nonExistentDir = "$testOutputDir/nested/path"
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
        val aggregated = AggregatedStatistics.from(statistics)
        val writer = StatisticsWriter(outputDir = nonExistentDir)

        // When
        val result: WrittenFiles = writer.write(statistics, aggregated)

        // Then
        File(nonExistentDir).exists() shouldBe true
        File(result.jsonPath).exists() shouldBe true
        File(result.csvPath).exists() shouldBe true
    }
})
