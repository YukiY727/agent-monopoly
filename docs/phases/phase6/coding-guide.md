# Phase 6 コーディングガイド

**Phase 6**: 基本統計収集機能の実装

---

## 1. 基本方針

### 1.1 既存コードを保護する

- Phase 5までの機能は変更しない
- 新しいパッケージ `com.monopoly.statistics` と `com.monopoly.export` を追加
- Main.kt で統計処理を追加

### 1.2 シンプルな実装

Phase 6では以下を避ける：

- 外部ライブラリへの依存（JSON/CSVは手動生成）
- 複雑な統計分析（Phase 9で実装）
- データベース保存（将来的な拡張として検討）

---

## 2. パッケージ構成

### 2.1 新しいパッケージ

```
src/main/kotlin/com/monopoly/
├── domain/              # 既存
├── cli/                 # 既存
├── simulation/          # 既存（Phase 5）
├── statistics/          # 新規
│   ├── GameStatistics.kt
│   ├── PlayerStatistics.kt
│   ├── TurnStatistics.kt
│   └── StatisticsCalculator.kt
└── export/              # 新規
    ├── JsonExporter.kt
    └── CsvExporter.kt
```

---

## 3. 統計計算パターン

### 3.1 StatisticsCalculator の実装

```kotlin
class StatisticsCalculator {
    fun calculate(result: MultiGameResult): GameStatistics {
        val playerStats = calculatePlayerStatistics(result)
        val turnStats = calculateTurnStatistics(result)

        return GameStatistics(
            totalGames = result.totalGames,
            playerStats = playerStats,
            turnStats = turnStats,
        )
    }

    private fun calculatePlayerStatistics(
        result: MultiGameResult,
    ): Map<String, PlayerStatistics> {
        // 全プレイヤー名を取得
        val allPlayerNames = result.gameResults
            .flatMap { it.finalState.players.map { player -> player.name } }
            .distinct()

        return allPlayerNames.associateWith { playerName ->
            calculateSinglePlayerStatistics(playerName, result)
        }
    }

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

    private fun calculateTurnStatistics(result: MultiGameResult): TurnStatistics {
        val turns = result.gameResults.map { it.totalTurns }
        val averageTurns = turns.average()

        return TurnStatistics(
            averageTurns = averageTurns,
            minTurns = result.getMinTurns(),
            maxTurns = result.getMaxTurns(),
            standardDeviation = TurnStatistics.calculateStandardDeviation(
                turns,
                averageTurns,
            ),
        )
    }
}
```

---

## 4. 標準偏差の計算パターン

### 4.1 TurnStatistics の companion object

```kotlin
data class TurnStatistics(
    val averageTurns: Double,
    val minTurns: Int,
    val maxTurns: Int,
    val standardDeviation: Double,
) {
    companion object {
        /**
         * 標準偏差を計算
         *
         * σ = sqrt(Σ(x - μ)² / N)
         */
        fun calculateStandardDeviation(values: List<Int>, average: Double): Double {
            if (values.isEmpty()) return 0.0

            val variance = values
                .map { (it - average) * (it - average) }
                .average()

            return sqrt(variance)
        }
    }
}
```

---

## 5. JSON生成パターン

### 5.1 手動でJSON文字列を構築

外部ライブラリを使わず、Kotlinの文字列操作でJSON生成：

```kotlin
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

            // 最後の要素以外はカンマを付ける
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
```

### 5.2 注意点

- 数値はそのまま出力（クォートなし）
- 文字列はダブルクォートで囲む
- 最後の要素にはカンマを付けない
- インデントは2スペース

---

## 6. CSV生成パターン

### 6.1 ヘッダーとデータ行の生成

```kotlin
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

private fun generateDataRow(gameResult: SingleGameResult): String {
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
```

### 6.2 注意点

- カンマ区切り
- 数値も文字列として扱う（クォートなし）
- プレイヤー名に特殊文字がある場合は考慮不要（Phase 6では単純な名前のみ）

---

## 7. Main.kt の統合パターン

### 7.1 GameConfig の拡張

```kotlin
data class GameConfig(
    val strategy: BuyStrategy,
    val numberOfGames: Int = 1,
    val generateReport: Boolean = true,
    val exportJson: Boolean = true,
    val exportCsv: Boolean = true,
)
```

### 7.2 引数解析の拡張

```kotlin
fun parseArgs(args: Array<String>): GameConfig {
    // ... 既存の処理 ...

    var exportJson = true
    var exportCsv = true

    while (i < args.size) {
        when (args[i]) {
            // ... 既存のオプション ...
            "--export-json" -> {
                exportJson = true
                exportCsv = false
            }
            "--export-csv" -> {
                exportJson = false
                exportCsv = true
            }
            "--no-export" -> {
                exportJson = false
                exportCsv = false
            }
        }
        i++
    }

    return GameConfig(strategy, numberOfGames, generateReport, exportJson, exportCsv)
}
```

### 7.3 複数ゲーム実行後のエクスポート

```kotlin
private fun runMultipleGames(config: GameConfig) {
    // ... ゲーム実行 ...

    val result = gameRunner.runMultipleGames(...)

    // 結果サマリー表示
    val summaryPrinter = ResultSummaryPrinter()
    summaryPrinter.print(result)

    // 統計エクスポート（複数ゲーム実行時のみ）
    if (config.numberOfGames > 1) {
        exportStatistics(result, config)
    }

    // ... レポート生成 ...
}

private fun exportStatistics(result: MultiGameResult, config: GameConfig) {
    // 統計計算
    val calculator = StatisticsCalculator()
    val statistics = calculator.calculate(result)

    // JSON エクスポート
    if (config.exportJson) {
        val jsonExporter = JsonExporter()
        val jsonFile = jsonExporter.export(statistics)
        println("Statistics exported to JSON: ${jsonFile.absolutePath}")
    }

    // CSV エクスポート
    if (config.exportCsv) {
        val csvExporter = CsvExporter()
        val csvFile = csvExporter.export(result)
        println("Results exported to CSV: ${csvFile.absolutePath}")
    }

    println()
}
```

---

## 8. エラーハンドリングパターン

### 8.1 ファイル書き込みエラー

```kotlin
fun export(statistics: GameStatistics, filename: String = generateFilename()): File {
    return try {
        val json = toJson(statistics)
        val file = File(filename)
        file.writeText(json)
        file
    } catch (e: IOException) {
        println("Error: Failed to write JSON file: ${e.message}")
        throw e
    }
}
```

### 8.2 0除算対策

```kotlin
// average() は空リストに対してNaNを返すので、チェックする
val averageFinalAssets = if (playerFinalStates.isNotEmpty()) {
    playerFinalStates.map { it.getTotalAssets() }.average()
} else {
    0.0
}
```

---

## 9. ヘルプメッセージの更新

```kotlin
private fun printHelp() {
    println(
        """
        Monopoly Game Simulator - Phase 6

        Usage: ./gradlew run --args="[options]"

        Options:
          --strategy <name>  戦略を指定
                             always-buy: 常に購入する（Phase 1の戦略）
                             random: ランダムに購入する
                             conservative: 一定額以上の現金を保持
                             デフォルト: always-buy
          --games <N>        実行するゲーム数を指定（デフォルト: 1）
          --no-report        HTMLレポート生成を抑制
          --export-json      JSON形式のみでエクスポート
          --export-csv       CSV形式のみでエクスポート
          --no-export        統計エクスポートを抑制
          --help             ヘルプを表示

        Examples:
          # 単一ゲーム実行
          ./gradlew run --args="--strategy always-buy"

          # 100ゲーム実行（JSON/CSV両方エクスポート）
          ./gradlew run --args="--strategy random --games 100"

          # JSON形式のみエクスポート
          ./gradlew run --args="--strategy random --games 100 --export-json"

          # エクスポートなし
          ./gradlew run --args="--strategy random --games 100 --no-export"
        """.trimIndent(),
    )
}
```

---

## 10. テストパターン

### 10.1 StatisticsCalculator のテスト

```kotlin
class StatisticsCalculatorTest : StringSpec({
    "calculate should compute correct player statistics" {
        val calculator = StatisticsCalculator()
        val mockResults = createMockMultiGameResult(
            wins = mapOf("Alice" to 3, "Bob" to 1),
            totalGames = 4,
        )

        val statistics = calculator.calculate(mockResults)

        statistics.totalGames shouldBe 4
        statistics.playerStats["Alice"]?.wins shouldBe 3
        statistics.playerStats["Alice"]?.winRate shouldBe 0.75
        statistics.playerStats["Bob"]?.wins shouldBe 1
        statistics.playerStats["Bob"]?.winRate shouldBe 0.25
    }

    "calculate should compute correct turn statistics" {
        val calculator = StatisticsCalculator()
        val mockResults = createMockMultiGameResultWithTurns(
            turns = listOf(40, 50, 60),
        )

        val statistics = calculator.calculate(mockResults)

        statistics.turnStats.averageTurns shouldBe 50.0
        statistics.turnStats.minTurns shouldBe 40
        statistics.turnStats.maxTurns shouldBe 60
        statistics.turnStats.standardDeviation shouldBeGreaterThan 0.0
    }
})
```

### 10.2 JsonExporter のテスト

```kotlin
class JsonExporterTest : StringSpec({
    "export should create valid JSON file" {
        val exporter = JsonExporter()
        val statistics = GameStatistics(
            totalGames = 10,
            playerStats = mapOf(
                "Alice" to PlayerStatistics("Alice", 6, 0.6, 1200.0, 600.0, 3.0),
                "Bob" to PlayerStatistics("Bob", 4, 0.4, 1000.0, 500.0, 2.5),
            ),
            turnStats = TurnStatistics(50.0, 40, 60, 5.0),
        )

        val file = exporter.export(statistics, "test-stats.json")

        file.exists() shouldBe true
        val content = file.readText()
        content shouldContain "\"totalGames\": 10"
        content shouldContain "\"Alice\""

        file.delete()
    }
})
```

---

## 11. Phase 5との対比

| 要素 | Phase 5 | Phase 6 |
|------|---------|---------|
| 結果表示 | ターミナルのみ | ターミナル + ファイル保存 |
| データ保存 | なし | JSON/CSV形式 |
| 統計計算 | 基本的（勝率、平均） | 詳細（標準偏差含む） |
| パッケージ | simulation, cli | simulation, cli, statistics, export |

---

**作成日**: 2025-11-16
