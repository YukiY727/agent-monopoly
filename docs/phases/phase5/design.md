# Phase 5 詳細設計

**Phase 5**: 複数ゲーム実行機能の設計

---

## 1. アーキテクチャ概要

```
┌─────────────────────────────────────────────────┐
│                   Main.kt                       │
│  - CLI引数解析                                   │
│  - GameRunner/GameServiceの呼び分け              │
└────────────┬────────────────────────────────────┘
             │
             ├─ 単一ゲーム実行 (--games 1 or 未指定)
             │   └→ GameService → HtmlReportGenerator + SummaryReportGenerator
             │
             └─ 複数ゲーム実行 (--games N)
                 └→ GameRunner
                     ├→ GameService (N回呼び出し)
                     ├→ ProgressDisplay (進捗表示)
                     └→ ResultSummaryPrinter (結果サマリー)
```

---

## 2. クラス設計

### 2.1 GameRunner

**責務**: 複数ゲームの実行とオーケストレーション

```kotlin
package com.monopoly.simulation

class GameRunner(
    private val gameService: GameService,
    private val dice: Dice,
) {
    /**
     * 複数ゲームを実行
     *
     * @param numberOfGames 実行するゲーム数
     * @param playerStrategies プレイヤー名と戦略のペアリスト
     * @param board ゲームボード
     * @param showProgress 進捗表示するか（デフォルト: true）
     * @return 複数ゲーム実行結果
     */
    fun runMultipleGames(
        numberOfGames: Int,
        playerStrategies: List<Pair<String, BuyStrategy>>,
        board: Board,
        showProgress: Boolean = true,
    ): MultiGameResult {
        val results = mutableListOf<SingleGameResult>()
        val progressDisplay = if (showProgress) ProgressDisplay(numberOfGames) else null

        progressDisplay?.start()

        repeat(numberOfGames) { gameIndex ->
            // 各ゲームは完全に独立した新しいGameStateで実行
            val gameState = createInitialGameState(playerStrategies, board)

            // ゲーム実行
            gameService.runGame(gameState, dice)

            // 結果を記録
            val result = SingleGameResult(
                gameNumber = gameIndex + 1,
                winner = determineWinner(gameState),
                totalTurns = gameState.turnNumber,
                finalState = gameState,
            )
            results.add(result)

            // 進捗更新
            progressDisplay?.update(gameIndex + 1)
        }

        progressDisplay?.finish()

        return MultiGameResult(
            gameResults = results,
            totalGames = numberOfGames,
        )
    }

    private fun createInitialGameState(
        playerStrategies: List<Pair<String, BuyStrategy>>,
        board: Board,
    ): GameState {
        val players = playerStrategies.map { (name, strategy) ->
            Player(name, strategy)
        }
        return GameState(players = players, board = board)
    }

    private fun determineWinner(gameState: GameState): String {
        return gameState.players
            .maxByOrNull { it.getTotalAssets() }
            ?.name
            ?: "Unknown"
    }
}
```

---

### 2.2 データクラス

#### SingleGameResult

```kotlin
package com.monopoly.simulation

import com.monopoly.domain.model.GameState

/**
 * 1ゲームの実行結果
 */
data class SingleGameResult(
    val gameNumber: Int,        // ゲーム番号（1から開始）
    val winner: String,          // 勝者名
    val totalTurns: Int,        // 総ターン数
    val finalState: GameState,  // 最終状態（詳細分析用）
)
```

#### MultiGameResult

```kotlin
package com.monopoly.simulation

/**
 * 複数ゲーム実行の結果
 */
data class MultiGameResult(
    val gameResults: List<SingleGameResult>,  // 各ゲームの結果
    val totalGames: Int,                      // 総ゲーム数
) {
    /**
     * プレイヤー別の勝利数を取得
     */
    fun getWinsByPlayer(): Map<String, Int> {
        return gameResults
            .groupBy { it.winner }
            .mapValues { it.value.size }
    }

    /**
     * 平均ターン数を取得
     */
    fun getAverageTurns(): Double {
        return gameResults.map { it.totalTurns }.average()
    }

    /**
     * 最短ゲームのターン数を取得
     */
    fun getMinTurns(): Int {
        return gameResults.minOfOrNull { it.totalTurns } ?: 0
    }

    /**
     * 最長ゲームのターン数を取得
     */
    fun getMaxTurns(): Int {
        return gameResults.maxOfOrNull { it.totalTurns } ?: 0
    }
}
```

---

### 2.3 ProgressDisplay

**責務**: ターミナルへの進捗表示

```kotlin
package com.monopoly.cli

/**
 * 複数ゲーム実行の進捗を表示
 */
class ProgressDisplay(
    private val totalGames: Int,
) {
    private var startTime: Long = 0

    fun start() {
        startTime = System.currentTimeMillis()
        println("Running $totalGames games...")
        println()
    }

    fun update(currentGame: Int) {
        val progress = (currentGame.toDouble() / totalGames * 100).toInt()
        val barLength = 40
        val filledLength = (barLength * currentGame / totalGames)
        val bar = "=".repeat(filledLength) + " ".repeat(barLength - filledLength)

        // カーソルを行頭に戻して上書き（\r）
        print("\rProgress: [$bar] $currentGame/$totalGames ($progress%)")
    }

    fun finish() {
        val elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0
        println()
        println()
        println("Completed $totalGames games in %.2f seconds".format(elapsedTime))
        println()
    }
}
```

---

### 2.4 ResultSummaryPrinter

**責務**: 実行結果サマリーの表示

```kotlin
package com.monopoly.cli

import com.monopoly.simulation.MultiGameResult

/**
 * 複数ゲーム実行結果のサマリーを表示
 */
class ResultSummaryPrinter {
    fun print(result: MultiGameResult) {
        println("=" .repeat(60))
        println("SIMULATION RESULTS")
        println("=".repeat(60))
        println()

        printWinnerSummary(result)
        println()
        printTurnStatistics(result)
        println()

        println("=".repeat(60))
    }

    private fun printWinnerSummary(result: MultiGameResult) {
        println("Winner Summary:")
        println("-".repeat(40))

        val winsByPlayer = result.getWinsByPlayer()
            .toList()
            .sortedByDescending { it.second }

        winsByPlayer.forEach { (player, wins) ->
            val winRate = (wins.toDouble() / result.totalGames * 100)
            println("  %-20s: %4d wins (%.1f%%)".format(player, wins, winRate))
        }
    }

    private fun printTurnStatistics(result: MultiGameResult) {
        println("Turn Statistics:")
        println("-".repeat(40))
        println("  Average turns: %.1f".format(result.getAverageTurns()))
        println("  Min turns:     %d".format(result.getMinTurns()))
        println("  Max turns:     %d".format(result.getMaxTurns()))
    }
}
```

---

## 3. CLI引数設計

### 3.1 新しいオプション

```bash
--games N          # 実行するゲーム数（デフォルト: 1）
--no-report        # HTMLレポート生成を抑制
```

### 3.2 使用例

```bash
# 単一ゲーム実行（既存の動作）
./gradlew run --args="--strategy always-buy"

# 100ゲーム実行（レポートなし）
./gradlew run --args="--strategy always-buy --games 100"

# 10ゲーム実行（最後のゲームのみレポート生成）
./gradlew run --args="--strategy random --games 10"

# 異なる戦略で対戦（将来的な拡張）
./gradlew run --args="--player1 always-buy --player2 conservative --games 100"
```

### 3.3 引数解析関数の拡張

```kotlin
data class GameConfig(
    val strategy: BuyStrategy,
    val numberOfGames: Int = 1,
    val generateReport: Boolean = true,
)

fun parseArgs(args: Array<String>): GameConfig {
    var strategyName = "always-buy"
    var numberOfGames = 1
    var generateReport = true

    var i = 0
    while (i < args.size) {
        when (args[i]) {
            "--strategy" -> {
                if (i + 1 < args.size) {
                    strategyName = args[i + 1]
                    i++
                }
            }
            "--games" -> {
                if (i + 1 < args.size) {
                    numberOfGames = args[i + 1].toIntOrNull() ?: 1
                    i++
                }
            }
            "--no-report" -> {
                generateReport = false
            }
            "--help" -> {
                printHelp()
                exitProcess(0)
            }
        }
        i++
    }

    val strategy = createStrategy(strategyName)
    return GameConfig(strategy, numberOfGames, generateReport)
}
```

---

## 4. Main.kt の制御フロー

```kotlin
fun main(args: Array<String>) {
    val config = parseArgs(args)

    if (config.numberOfGames == 1) {
        // 単一ゲーム実行（既存のフロー）
        runSingleGame(config.strategy)
    } else {
        // 複数ゲーム実行（新しいフロー）
        runMultipleGames(config)
    }
}

private fun runSingleGame(strategy: BuyStrategy) {
    // 既存の実装（Phase 4まで）
    val gameState = createInitialGameState(strategy)
    val gameService = GameService()
    val dice = Dice()
    val consoleLogger = ConsoleLogger()
    val htmlReportGenerator = HtmlReportGenerator()
    val summaryReportGenerator = SummaryReportGenerator()

    gameService.runGame(gameState, dice)

    // レポート生成
    htmlReportGenerator.saveToFile(gameState)
    summaryReportGenerator.saveToFile(gameState)

    // ... 以下略
}

private fun runMultipleGames(config: GameConfig) {
    val board = Board()
    val playerStrategies = listOf(
        "Alice" to config.strategy,
        "Bob" to config.strategy,
    )

    val gameService = GameService()
    val dice = Dice()
    val gameRunner = GameRunner(gameService, dice)

    // 複数ゲーム実行
    val result = gameRunner.runMultipleGames(
        numberOfGames = config.numberOfGames,
        playerStrategies = playerStrategies,
        board = board,
        showProgress = true,
    )

    // 結果サマリー表示
    val summaryPrinter = ResultSummaryPrinter()
    summaryPrinter.print(result)

    // オプション: 最後のゲームのレポート生成
    if (config.generateReport) {
        val lastGameState = result.gameResults.last().finalState
        val summaryReportGenerator = SummaryReportGenerator()
        summaryReportGenerator.saveToFile(lastGameState)
        println("Last game summary report generated.")
    }
}
```

---

## 5. メモリ管理

### 5.1 問題

大量のゲーム（例: 10,000回）を実行すると、各ゲームの `GameState` を保持し続けるとメモリ不足になる可能性があります。

### 5.2 対策

**Phase 5では**: 各ゲームの `finalState` を保持

**Phase 6以降**: 必要な統計情報のみ抽出し、GameStateは破棄

```kotlin
// Phase 6での改善案
data class GameStatistics(
    val gameNumber: Int,
    val winner: String,
    val totalTurns: Int,
    val finalAssets: Map<String, Int>,  // GameStateではなく必要な情報のみ
)
```

Phase 5では `finalState` を保持しますが、将来的なメモリ最適化の余地を残します。

---

## 6. パフォーマンス目標

| ゲーム数 | 目標実行時間 | 備考 |
|---------|------------|-----|
| 10 | < 1秒 | 開発中の動作確認 |
| 100 | < 10秒 | Phase 5の目標 |
| 1,000 | < 2分 | Phase 5で許容範囲 |
| 10,000 | < 20分 | Phase 8で並列化予定 |

---

## 7. エラーハンドリング

### 7.1 不正な引数

```kotlin
// --games に負の数や0が指定された場合
if (numberOfGames <= 0) {
    println("Error: --games must be a positive integer")
    exitProcess(1)
}
```

### 7.2 ゲーム実行中のエラー

```kotlin
try {
    gameService.runGame(gameState, dice)
} catch (e: Exception) {
    println("Error in game ${gameIndex + 1}: ${e.message}")
    // エラーを記録して次のゲームへ継続
}
```

---

## 8. テスト設計

### 8.1 GameRunner のテスト

```kotlin
class GameRunnerTest : StringSpec({
    "runMultipleGames should execute specified number of games" {
        val gameService = GameService()
        val dice = Dice()
        val gameRunner = GameRunner(gameService, dice)

        val result = gameRunner.runMultipleGames(
            numberOfGames = 10,
            playerStrategies = listOf(
                "Alice" to AlwaysBuyStrategy(),
                "Bob" to AlwaysBuyStrategy(),
            ),
            board = Board(),
            showProgress = false,
        )

        result.totalGames shouldBe 10
        result.gameResults.size shouldBe 10
    }

    "each game should be independent" {
        // 各ゲームの初期状態が同じであることを検証
        // ...
    }
})
```

### 8.2 MultiGameResult のテスト

```kotlin
class MultiGameResultTest : StringSpec({
    "getWinsByPlayer should count wins correctly" {
        val results = listOf(
            SingleGameResult(1, "Alice", 50, mockGameState),
            SingleGameResult(2, "Bob", 45, mockGameState),
            SingleGameResult(3, "Alice", 55, mockGameState),
        )
        val multiResult = MultiGameResult(results, 3)

        multiResult.getWinsByPlayer() shouldBe mapOf(
            "Alice" to 2,
            "Bob" to 1,
        )
    }
})
```

---

**作成日**: 2025-11-16
