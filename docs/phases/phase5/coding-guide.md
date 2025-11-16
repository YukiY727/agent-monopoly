# Phase 5 コーディングガイド

**Phase 5**: 複数ゲーム実行機能の実装

---

## 1. 基本方針

### 1.1 既存コードを保護する

- Phase 4までの単一ゲーム実行機能は変更しない
- `Main.kt` で単一/複数実行を分岐
- 新しいパッケージ `com.monopoly.simulation` を追加

### 1.2 シンプルな実装

Phase 5では以下を避ける：

- 並列実行（Phase 8で実装）
- 複雑な統計分析（Phase 6で実装）
- ファイル保存（Phase 6で実装）

---

## 2. パッケージ構成

### 2.1 新しいパッケージ

```
src/main/kotlin/com/monopoly/
├── domain/              # 既存
├── cli/                 # 既存
└── simulation/          # 新規
    ├── GameRunner.kt
    ├── MultiGameResult.kt
    └── SingleGameResult.kt
```

### 2.2 既存パッケージの拡張

```
src/main/kotlin/com/monopoly/cli/
├── Main.kt                    # 更新: 複数ゲーム実行に対応
├── ConsoleLogger.kt           # 既存（変更なし）
├── HtmlReportGenerator.kt     # 既存（変更なし）
├── SummaryReportGenerator.kt  # 既存（変更なし）
├── ProgressDisplay.kt         # 新規
└── ResultSummaryPrinter.kt    # 新規
```

---

## 3. GameRunner の実装パターン

### 3.1 基本構造

```kotlin
class GameRunner(
    private val gameService: GameService,
    private val dice: Dice,
) {
    fun runMultipleGames(
        numberOfGames: Int,
        playerStrategies: List<Pair<String, BuyStrategy>>,
        board: Board,
        showProgress: Boolean = true,
    ): MultiGameResult {
        val results = mutableListOf<SingleGameResult>()
        val progressDisplay = createProgressDisplay(numberOfGames, showProgress)

        progressDisplay?.start()

        repeat(numberOfGames) { gameIndex ->
            val result = runSingleGame(gameIndex, playerStrategies, board)
            results.add(result)
            progressDisplay?.update(gameIndex + 1)
        }

        progressDisplay?.finish()

        return MultiGameResult(results, numberOfGames)
    }

    private fun runSingleGame(
        gameIndex: Int,
        playerStrategies: List<Pair<String, BuyStrategy>>,
        board: Board,
    ): SingleGameResult {
        // 新しいGameStateを作成（完全に独立）
        val gameState = createInitialGameState(playerStrategies, board)

        // ゲーム実行
        gameService.runGame(gameState, dice)

        // 結果を返す
        return SingleGameResult(
            gameNumber = gameIndex + 1,
            winner = determineWinner(gameState),
            totalTurns = gameState.turnNumber,
            finalState = gameState,
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

    private fun createProgressDisplay(
        numberOfGames: Int,
        showProgress: Boolean,
    ): ProgressDisplay? {
        return if (showProgress) ProgressDisplay(numberOfGames) else null
    }
}
```

---

## 4. 進捗表示パターン

### 4.1 ProgressDisplay の実装

```kotlin
class ProgressDisplay(
    private val totalGames: Int,
) {
    private var startTime: Long = 0
    private val barLength = 40

    fun start() {
        startTime = System.currentTimeMillis()
        println("Running $totalGames games...")
        println()
    }

    fun update(currentGame: Int) {
        val progress = (currentGame.toDouble() / totalGames * 100).toInt()
        val filledLength = (barLength * currentGame / totalGames)
        val bar = buildProgressBar(filledLength)

        // \r でカーソルを行頭に戻して上書き
        print("\rProgress: [$bar] $currentGame/$totalGames ($progress%)")
        System.out.flush()  // バッファをフラッシュ
    }

    fun finish() {
        val elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0
        println()
        println()
        println("Completed $totalGames games in %.2f seconds".format(elapsedTime))
        println()
    }

    private fun buildProgressBar(filledLength: Int): String {
        return "=".repeat(filledLength) + " ".repeat(barLength - filledLength)
    }
}
```

### 4.2 注意点

- `\r` を使って同じ行を上書き
- `System.out.flush()` でバッファをフラッシュ
- 最後に改行して次の出力に備える

---

## 5. 結果サマリー表示パターン

### 5.1 ResultSummaryPrinter の実装

```kotlin
class ResultSummaryPrinter {
    fun print(result: MultiGameResult) {
        printHeader()
        printWinnerSummary(result)
        println()
        printTurnStatistics(result)
        printFooter()
    }

    private fun printHeader() {
        println("=".repeat(60))
        println("SIMULATION RESULTS")
        println("=".repeat(60))
        println()
    }

    private fun printWinnerSummary(result: MultiGameResult) {
        println("Winner Summary:")
        println("-".repeat(40))

        val winsByPlayer = result.getWinsByPlayer()
            .toList()
            .sortedByDescending { (_, wins) -> wins }

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

    private fun printFooter() {
        println()
        println("=".repeat(60))
    }
}
```

---

## 6. MultiGameResult の拡張メソッド

### 6.1 集計メソッドの実装

```kotlin
data class MultiGameResult(
    val gameResults: List<SingleGameResult>,
    val totalGames: Int,
) {
    fun getWinsByPlayer(): Map<String, Int> {
        return gameResults
            .groupBy { it.winner }
            .mapValues { (_, results) -> results.size }
    }

    fun getAverageTurns(): Double {
        if (gameResults.isEmpty()) return 0.0
        return gameResults.map { it.totalTurns }.average()
    }

    fun getMinTurns(): Int {
        return gameResults.minOfOrNull { it.totalTurns } ?: 0
    }

    fun getMaxTurns(): Int {
        return gameResults.maxOfOrNull { it.totalTurns } ?: 0
    }

    fun getWinRate(playerName: String): Double {
        val wins = gameResults.count { it.winner == playerName }
        return wins.toDouble() / totalGames
    }
}
```

---

## 7. Main.kt の統合パターン

### 7.1 引数解析の拡張

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
                    if (numberOfGames <= 0) {
                        println("Error: --games must be a positive integer")
                        exitProcess(1)
                    }
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

### 7.2 main() の分岐

```kotlin
fun main(args: Array<String>) {
    val config = parseArgs(args)

    if (config.numberOfGames == 1) {
        runSingleGame(config)
    } else {
        runMultipleGames(config)
    }
}

private fun runSingleGame(config: GameConfig) {
    // 既存のPhase 4までの実装
    // ...
}

private fun runMultipleGames(config: GameConfig) {
    println("Starting multiple game simulation...")
    println()

    val board = Board()
    val playerStrategies = listOf(
        "Alice" to config.strategy,
        "Bob" to config.strategy,
    )

    val gameService = GameService()
    val dice = Dice()
    val gameRunner = GameRunner(gameService, dice)

    val result = gameRunner.runMultipleGames(
        numberOfGames = config.numberOfGames,
        playerStrategies = playerStrategies,
        board = board,
        showProgress = true,
    )

    // 結果表示
    val summaryPrinter = ResultSummaryPrinter()
    summaryPrinter.print(result)

    // オプション: 最後のゲームのみレポート生成
    if (config.generateReport && config.numberOfGames > 1) {
        generateLastGameReport(result)
    }
}

private fun generateLastGameReport(result: MultiGameResult) {
    val lastGameState = result.gameResults.last().finalState
    val summaryReportGenerator = SummaryReportGenerator()
    val reportFile = summaryReportGenerator.saveToFile(lastGameState)
    println("Last game summary report: ${reportFile.absolutePath}")
}
```

---

## 8. ヘルプメッセージの更新

```kotlin
private fun printHelp() {
    println(
        """
        Monopoly Game Simulator

        Usage:
          gradle run --args="[options]"

        Options:
          --strategy <name>    Select buy strategy (default: always-buy)
                               Available: always-buy, random, conservative
          --games <N>          Run N games (default: 1)
          --no-report          Disable HTML report generation
          --help               Show this help message

        Examples:
          # Run single game with always-buy strategy
          gradle run --args="--strategy always-buy"

          # Run 100 games with random strategy
          gradle run --args="--strategy random --games 100"

          # Run 10 games without generating reports
          gradle run --args="--strategy conservative --games 10 --no-report"

        """.trimIndent(),
    )
}
```

---

## 9. エラーハンドリング

### 9.1 不正な引数

```kotlin
// 負の数や0の場合
if (numberOfGames <= 0) {
    println("Error: --games must be a positive integer")
    exitProcess(1)
}

// 数値でない場合
val numberOfGames = args[i + 1].toIntOrNull() ?: run {
    println("Error: --games requires a valid integer")
    exitProcess(1)
}
```

### 9.2 ゲーム実行エラー

```kotlin
try {
    gameService.runGame(gameState, dice)
} catch (e: Exception) {
    println("Error in game ${gameIndex + 1}: ${e.message}")
    // Phase 5では例外を記録して次のゲームへ継続
    // Phase 6以降で詳細なエラーレポートを追加
}
```

---

## 10. テストパターン

### 10.1 GameRunner のテスト

```kotlin
class GameRunnerTest : StringSpec({
    "runMultipleGames should execute specified number of games" {
        val gameService = GameService()
        val dice = Dice()
        val gameRunner = GameRunner(gameService, dice)

        val result = gameRunner.runMultipleGames(
            numberOfGames = 5,
            playerStrategies = listOf(
                "Alice" to AlwaysBuyStrategy(),
                "Bob" to AlwaysBuyStrategy(),
            ),
            board = Board(),
            showProgress = false,  // テスト時は進捗表示OFF
        )

        result.totalGames shouldBe 5
        result.gameResults.size shouldBe 5
        result.gameResults.forEachIndexed { index, gameResult ->
            gameResult.gameNumber shouldBe index + 1
        }
    }

    "each game should be independent" {
        val gameService = GameService()
        val dice = Dice()
        val gameRunner = GameRunner(gameService, dice)

        val result = gameRunner.runMultipleGames(
            numberOfGames = 3,
            playerStrategies = listOf(
                "Alice" to AlwaysBuyStrategy(),
                "Bob" to AlwaysBuyStrategy(),
            ),
            board = Board(),
            showProgress = false,
        )

        // 各ゲームの初期状態が同じであることを検証
        result.gameResults.forEach { gameResult ->
            gameResult.finalState.players.size shouldBe 2
            gameResult.totalTurns shouldBeGreaterThan 0
        }
    }
})
```

### 10.2 MultiGameResult のテスト

```kotlin
class MultiGameResultTest : StringSpec({
    "getWinsByPlayer should count wins correctly" {
        val mockState = mockk<GameState>()
        val results = listOf(
            SingleGameResult(1, "Alice", 50, mockState),
            SingleGameResult(2, "Bob", 45, mockState),
            SingleGameResult(3, "Alice", 55, mockState),
            SingleGameResult(4, "Alice", 60, mockState),
        )
        val multiResult = MultiGameResult(results, 4)

        val winsByPlayer = multiResult.getWinsByPlayer()
        winsByPlayer["Alice"] shouldBe 3
        winsByPlayer["Bob"] shouldBe 1
    }

    "getAverageTurns should calculate correctly" {
        val mockState = mockk<GameState>()
        val results = listOf(
            SingleGameResult(1, "Alice", 40, mockState),
            SingleGameResult(2, "Bob", 50, mockState),
            SingleGameResult(3, "Alice", 60, mockState),
        )
        val multiResult = MultiGameResult(results, 3)

        multiResult.getAverageTurns() shouldBe 50.0
    }
})
```

---

## 11. パフォーマンスの考慮

### 11.1 メモリ使用量

Phase 5では全ゲームの `finalState` を保持しますが、以下を念頭に置く：

```kotlin
// 将来的な最適化の余地を残す
data class SingleGameResult(
    val gameNumber: Int,
    val winner: String,
    val totalTurns: Int,
    val finalState: GameState,  // Phase 6以降で削減検討
)
```

### 11.2 実行時間の測定

```kotlin
// ProgressDisplay で実行時間を測定
private var startTime: Long = 0

fun start() {
    startTime = System.currentTimeMillis()
    // ...
}

fun finish() {
    val elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0
    println("Completed $totalGames games in %.2f seconds".format(elapsedTime))
}
```

---

## 12. Phase 4との対比

| 要素 | Phase 4 | Phase 5 |
|------|---------|---------|
| 実行回数 | 1回 | N回 |
| レポート生成 | 常に生成 | オプション |
| 進捗表示 | なし | プログレスバー |
| 結果集計 | なし | 勝率、平均ターン数など |
| パッケージ | cli のみ | cli + simulation |

---

**作成日**: 2025-11-16
