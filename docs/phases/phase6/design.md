# Phase 6 詳細設計

**Phase 6**: 基本統計収集機能の設計

---

## 1. アーキテクチャ概要

```
┌─────────────────────────────────────────────────┐
│                   Main.kt                       │
│  - 複数ゲーム実行後に統計処理                      │
└────────────┬────────────────────────────────────┘
             │
             ├→ GameRunner → MultiGameResult
             │
             ├→ StatisticsCalculator → GameStatistics
             │
             └→ Exporter
                 ├→ JsonExporter → .json
                 └→ CsvExporter  → .csv
```

---

## 2. クラス設計

### 2.1 データモデル

#### GameStatistics

**責務**: 全体の統計データを保持

```kotlin
package com.monopoly.statistics

data class GameStatistics(
    val totalGames: Int,
    val playerStats: Map<String, PlayerStatistics>,
    val turnStats: TurnStatistics,
    val timestamp: Long = System.currentTimeMillis(),
)
```

#### PlayerStatistics

**責務**: プレイヤー別の統計データを保持

```kotlin
package com.monopoly.statistics

data class PlayerStatistics(
    val playerName: String,
    val wins: Int,
    val winRate: Double,
    val averageFinalAssets: Double,
    val averageFinalCash: Double,
    val averagePropertiesOwned: Double,
)
```

#### TurnStatistics

**責務**: ターン数に関する統計データを保持

```kotlin
package com.monopoly.statistics

import kotlin.math.sqrt

data class TurnStatistics(
    val averageTurns: Double,
    val minTurns: Int,
    val maxTurns: Int,
    val standardDeviation: Double,
) {
    companion object {
        /**
         * ターン数のリストから標準偏差を計算
         */
        fun calculateStandardDeviation(turns: List<Int>, average: Double): Double {
            if (turns.isEmpty()) return 0.0

            val variance = turns.map { (it - average) * (it - average) }.average()
            return sqrt(variance)
        }
    }
}
```

---

### 2.2 StatisticsCalculator

**責務**: MultiGameResult から統計データを計算

```kotlin
package com.monopoly.statistics

import com.monopoly.simulation.MultiGameResult

class StatisticsCalculator {
    /**
     * 統計データを計算
     *
     * @param result 複数ゲーム実行結果
     * @return 統計データ
     */
    fun calculate(result: MultiGameResult): GameStatistics {
        val playerStats = calculatePlayerStatistics(result)
        val turnStats = calculateTurnStatistics(result)

        return GameStatistics(
            totalGames = result.totalGames,
            playerStats = playerStats,
            turnStats = turnStats,
        )
    }

    /**
     * プレイヤー別統計を計算
     */
    private fun calculatePlayerStatistics(result: MultiGameResult): Map<String, PlayerStatistics> {
        // 全プレイヤー名を取得
        val allPlayerNames = result.gameResults
            .flatMap { it.finalState.players.map { player -> player.name } }
            .distinct()

        return allPlayerNames.associateWith { playerName ->
            calculateSinglePlayerStatistics(playerName, result)
        }
    }

    /**
     * 単一プレイヤーの統計を計算
     */
    private fun calculateSinglePlayerStatistics(
        playerName: String,
        result: MultiGameResult,
    ): PlayerStatistics {
        val wins = result.gameResults.count { it.winner == playerName }
        val winRate = wins.toDouble() / result.totalGames

        // 各ゲームでのプレイヤーの最終状態を取得
        val playerFinalStates = result.gameResults.mapNotNull { gameResult ->
            gameResult.finalState.players.find { it.name == playerName }
        }

        val averageFinalAssets = playerFinalStates
            .map { it.getTotalAssets() }
            .average()

        val averageFinalCash = playerFinalStates
            .map { it.money }
            .average()

        val averagePropertiesOwned = playerFinalStates
            .map { it.ownedProperties.size }
            .average()

        return PlayerStatistics(
            playerName = playerName,
            wins = wins,
            winRate = winRate,
            averageFinalAssets = averageFinalAssets,
            averageFinalCash = averageFinalCash,
            averagePropertiesOwned = averagePropertiesOwned,
        )
    }

    /**
     * ターン統計を計算
     */
    private fun calculateTurnStatistics(result: MultiGameResult): TurnStatistics {
        val turns = result.gameResults.map { it.totalTurns }
        val averageTurns = turns.average()

        return TurnStatistics(
            averageTurns = averageTurns,
            minTurns = result.getMinTurns(),
            maxTurns = result.getMaxTurns(),
            standardDeviation = TurnStatistics.calculateStandardDeviation(turns, averageTurns),
        )
    }
}
```

---

### 2.3 JsonExporter

**責務**: 統計データをJSON形式でエクスポート

```kotlin
package com.monopoly.export

import com.monopoly.statistics.GameStatistics
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class JsonExporter {
    /**
     * 統計データをJSON形式でエクスポート
     *
     * @param statistics 統計データ
     * @param filename ファイル名（省略時は自動生成）
     * @return 保存したファイル
     */
    fun export(statistics: GameStatistics, filename: String = generateFilename()): File {
        val json = toJson(statistics)
        val file = File(filename)
        file.writeText(json)
        return file
    }

    /**
     * 統計データをJSON文字列に変換
     */
    private fun toJson(statistics: GameStatistics): String {
        return buildString {
            appendLine("{")
            appendLine("  \"totalGames\": ${statistics.totalGames},")
            appendLine("  \"timestamp\": ${statistics.timestamp},")
            appendLine("  \"playerStats\": {")

            val playerEntries = statistics.playerStats.entries.toList()
            playerEntries.forEachIndexed { index, (playerName, stats) ->
                appendLine("    \"$playerName\": {")
                appendLine("      \"playerName\": \"$playerName\",")
                appendLine("      \"wins\": ${stats.wins},")
                appendLine("      \"winRate\": ${stats.winRate},")
                appendLine("      \"averageFinalAssets\": ${stats.averageFinalAssets},")
                appendLine("      \"averageFinalCash\": ${stats.averageFinalCash},")
                appendLine("      \"averagePropertiesOwned\": ${stats.averagePropertiesOwned}")
                val comma = if (index < playerEntries.size - 1) "," else ""
                appendLine("    }$comma")
            }

            appendLine("  },")
            appendLine("  \"turnStats\": {")
            appendLine("    \"averageTurns\": ${statistics.turnStats.averageTurns},")
            appendLine("    \"minTurns\": ${statistics.turnStats.minTurns},")
            appendLine("    \"maxTurns\": ${statistics.turnStats.maxTurns},")
            appendLine("    \"standardDeviation\": ${statistics.turnStats.standardDeviation}")
            appendLine("  }")
            append("}")
        }
    }

    /**
     * ファイル名を生成
     */
    private fun generateFilename(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return "simulation-stats-$timestamp.json"
    }
}
```

---

### 2.4 CsvExporter

**責務**: ゲーム結果をCSV形式でエクスポート

```kotlin
package com.monopoly.export

import com.monopoly.simulation.MultiGameResult
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class CsvExporter {
    /**
     * ゲーム結果をCSV形式でエクスポート
     *
     * @param result 複数ゲーム実行結果
     * @param filename ファイル名（省略時は自動生成）
     * @return 保存したファイル
     */
    fun export(result: MultiGameResult, filename: String = generateFilename()): File {
        val csv = toCsv(result)
        val file = File(filename)
        file.writeText(csv)
        return file
    }

    /**
     * MultiGameResultをCSV文字列に変換
     */
    private fun toCsv(result: MultiGameResult): String {
        return buildString {
            // ヘッダー行
            appendLine(generateHeader(result))

            // データ行
            result.gameResults.forEach { gameResult ->
                appendLine(generateDataRow(gameResult))
            }
        }
    }

    /**
     * CSVヘッダーを生成
     */
    private fun generateHeader(result: MultiGameResult): String {
        val playerNames = result.gameResults.first().finalState.players.map { it.name }

        val columns = mutableListOf("GameNumber", "Winner", "TotalTurns")

        playerNames.forEach { playerName ->
            columns.add("${playerName}FinalAssets")
            columns.add("${playerName}FinalCash")
            columns.add("${playerName}Properties")
        }

        return columns.joinToString(",")
    }

    /**
     * CSVデータ行を生成
     */
    private fun generateDataRow(gameResult: com.monopoly.simulation.SingleGameResult): String {
        val values = mutableListOf<String>()

        values.add(gameResult.gameNumber.toString())
        values.add(gameResult.winner)
        values.add(gameResult.totalTurns.toString())

        gameResult.finalState.players.forEach { player ->
            values.add(player.getTotalAssets().toString())
            values.add(player.money.toString())
            values.add(player.ownedProperties.size.toString())
        }

        return values.joinToString(",")
    }

    /**
     * ファイル名を生成
     */
    private fun generateFilename(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return "simulation-results-$timestamp.csv"
    }
}
```

---

## 3. CLI引数設計

### 3.1 新しいオプション

```bash
--export-json      # JSON形式でエクスポート
--export-csv       # CSV形式でエクスポート
--no-export        # エクスポートしない
```

### 3.2 使用例

```bash
# 複数ゲーム実行（デフォルト: 両方エクスポート）
./gradlew run --args="--strategy random --games 100"

# JSON形式のみエクスポート
./gradlew run --args="--strategy random --games 100 --export-json"

# CSV形式のみエクスポート
./gradlew run --args="--strategy random --games 100 --export-csv"

# エクスポートなし
./gradlew run --args="--strategy random --games 100 --no-export"
```

### 3.3 GameConfig の拡張

```kotlin
data class GameConfig(
    val strategy: BuyStrategy,
    val numberOfGames: Int = 1,
    val generateReport: Boolean = true,
    val exportJson: Boolean = true,      // 新規追加
    val exportCsv: Boolean = true,       // 新規追加
)
```

---

## 4. Main.kt の統合

### 4.1 複数ゲーム実行後のエクスポート処理

```kotlin
private fun runMultipleGames(config: GameConfig) {
    // ... ゲーム実行 ...

    val result = gameRunner.runMultipleGames(...)

    // 結果サマリー表示
    val summaryPrinter = ResultSummaryPrinter()
    summaryPrinter.print(result)

    // 統計計算
    val calculator = StatisticsCalculator()
    val statistics = calculator.calculate(result)

    // エクスポート
    if (config.exportJson) {
        val jsonExporter = JsonExporter()
        val jsonFile = jsonExporter.export(statistics)
        println("Statistics exported to JSON: ${jsonFile.absolutePath}")
    }

    if (config.exportCsv) {
        val csvExporter = CsvExporter()
        val csvFile = csvExporter.export(result)
        println("Results exported to CSV: ${csvFile.absolutePath}")
    }

    // ... レポート生成 ...
}
```

---

## 5. エラーハンドリング

### 5.1 ファイル書き込みエラー

```kotlin
try {
    file.writeText(json)
} catch (e: IOException) {
    println("Error: Failed to write file: ${e.message}")
    // エラーを記録するがプログラムは継続
}
```

### 5.2 統計計算エラー

```kotlin
// 0除算を避ける
val average = if (values.isNotEmpty()) values.average() else 0.0
```

---

## 6. パフォーマンス設計

### 6.1 メモリ効率

Phase 6では全データをメモリ上に保持：
- 10,000ゲーム程度なら問題なし
- 100,000ゲーム以上はPhase 9でストリーミング処理を検討

### 6.2 計算効率

統計計算の計算量：
- プレイヤー統計: O(N × P) (N: ゲーム数, P: プレイヤー数)
- ターン統計: O(N)
- 10,000ゲームで1秒以内を目標

---

## 7. テスト設計

### 7.1 StatisticsCalculator のテスト

```kotlin
class StatisticsCalculatorTest : StringSpec({
    "calculate should compute correct win rates" {
        val mockResults = createMockResults(
            listOf("Alice", "Bob", "Alice", "Alice")
        )
        val calculator = StatisticsCalculator()

        val statistics = calculator.calculate(mockResults)

        statistics.playerStats["Alice"]?.winRate shouldBe 0.75
        statistics.playerStats["Bob"]?.winRate shouldBe 0.25
    }

    "calculate should compute correct average turns" {
        val mockResults = createMockResultsWithTurns(
            listOf(40, 50, 60)
        )
        val calculator = StatisticsCalculator()

        val statistics = calculator.calculate(mockResults)

        statistics.turnStats.averageTurns shouldBe 50.0
    }
})
```

### 7.2 JsonExporter のテスト

```kotlin
class JsonExporterTest : StringSpec({
    "export should create valid JSON file" {
        val exporter = JsonExporter()
        val statistics = createMockStatistics()

        val file = exporter.export(statistics, "test-stats.json")

        file.exists() shouldBe true
        val content = file.readText()
        content shouldContain "\"totalGames\":"

        file.delete()
    }
})
```

---

## 8. ファイル出力例

### 8.1 JSON出力例

```json
{
  "totalGames": 100,
  "timestamp": 1700000000000,
  "playerStats": {
    "Alice": {
      "playerName": "Alice",
      "wins": 58,
      "winRate": 0.58,
      "averageFinalAssets": 1234.5,
      "averageFinalCash": 678.9,
      "averagePropertiesOwned": 3.2
    },
    "Bob": {
      "playerName": "Bob",
      "wins": 42,
      "winRate": 0.42,
      "averageFinalAssets": 1100.2,
      "averageFinalCash": 550.3,
      "averagePropertiesOwned": 2.8
    }
  },
  "turnStats": {
    "averageTurns": 52.3,
    "minTurns": 35,
    "maxTurns": 78,
    "standardDeviation": 8.5
  }
}
```

### 8.2 CSV出力例

```csv
GameNumber,Winner,TotalTurns,AliceFinalAssets,AliceFinalCash,AliceProperties,BobFinalAssets,BobFinalCash,BobProperties
1,Alice,50,1500,800,4,1200,600,3
2,Bob,45,1100,500,3,1400,700,4
3,Alice,55,1600,900,5,1000,400,2
```

---

**作成日**: 2025-11-16
