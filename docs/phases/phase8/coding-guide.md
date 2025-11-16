# Phase 8: 並列実行 - コーディングガイド

---

## ディレクトリ構造

```
src/main/kotlin/com/monopoly/
├── simulation/
│   ├── GameRunner.kt          (既存: 逐次実行)
│   ├── ParallelGameRunner.kt  (新規: 並列実行)
│   ├── SingleGameResult.kt    (既存)
│   └── MultiGameResult.kt     (既存)
├── cli/
│   ├── ProgressDisplay.kt     (既存)
│   ├── AtomicProgressTracker.kt (新規: スレッドセーフ進捗)
│   └── Main.kt                (更新: 並列オプション追加)
└── ...
```

---

## 実装順序

### Step 1: AtomicProgressTracker 実装

**目的**: スレッドセーフな進捗表示

```kotlin
package com.monopoly.cli

import java.util.concurrent.atomic.AtomicInteger

/**
 * スレッドセーフな進捗トラッカー
 * 並列実行時に複数のコルーチンから安全に進捗を更新できる
 */
class AtomicProgressTracker(private val totalGames: Int) {
    private val completed = AtomicInteger(0)
    private var startTime: Long = 0

    /**
     * 進捗表示を開始
     */
    fun start() {
        startTime = System.currentTimeMillis()
        println("Starting $totalGames games...")
        displayProgress(0)
    }

    /**
     * 完了数をインクリメントして進捗を表示
     */
    fun incrementAndDisplay() {
        val current = completed.incrementAndGet()
        displayProgress(current)
    }

    /**
     * 進捗表示を完了
     */
    fun finish() {
        val elapsed = System.currentTimeMillis() - startTime
        println() // 改行
        println("Completed $totalGames games in ${elapsed}ms")
    }

    /**
     * 進捗バーを表示
     */
    private fun displayProgress(current: Int) {
        val percentage = (current * 100) / totalGames
        val barLength = 50
        val filledLength = (barLength * current) / totalGames
        val bar = "=".repeat(filledLength) + " ".repeat(barLength - filledLength)

        // \r で行頭に戻って上書き
        print("\r[$bar] $current/$totalGames ($percentage%)")
    }
}
```

**テストポイント**:
- 複数スレッドから同時に `incrementAndDisplay()` を呼んでも正確にカウント
- 進捗バーが正しく表示される

---

### Step 2: ParallelGameRunner 実装

**目的**: Kotlin Coroutines を使った並列実行

```kotlin
package com.monopoly.simulation

import com.monopoly.cli.AtomicProgressTracker
import com.monopoly.domain.model.Board
import com.monopoly.domain.model.Dice
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Player
import com.monopoly.domain.service.GameService
import com.monopoly.domain.strategy.BuyStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * 並列でゲームを実行するクラス
 *
 * Kotlin Coroutines を使用して複数のゲームを並列実行し、
 * パフォーマンスを向上させる
 */
class ParallelGameRunner(
    private val gameService: GameService,
    private val parallelism: Int = Runtime.getRuntime().availableProcessors(),
) {
    /**
     * 複数のゲームを並列実行
     *
     * @param numberOfGames 実行するゲーム数
     * @param playerStrategies プレイヤー名と戦略のペアのリスト
     * @param board ゲームボード
     * @param showProgress 進捗を表示するか
     * @return 複数ゲームの結果
     */
    suspend fun runMultipleGames(
        numberOfGames: Int,
        playerStrategies: List<Pair<String, BuyStrategy>>,
        board: Board,
        showProgress: Boolean = true,
    ): MultiGameResult = coroutineScope {
        val progressTracker = if (showProgress) {
            AtomicProgressTracker(numberOfGames).also { it.start() }
        } else {
            null
        }

        // 各ゲームを並列実行
        val deferredResults = (1..numberOfGames).map { gameNumber ->
            async(Dispatchers.Default) {
                runSingleGame(gameNumber, playerStrategies, board, progressTracker)
            }
        }

        // 全ての結果を待機
        val results = deferredResults.awaitAll()

        progressTracker?.finish()

        MultiGameResult(results, numberOfGames)
    }

    /**
     * 単一のゲームを実行
     */
    private fun runSingleGame(
        gameNumber: Int,
        playerStrategies: List<Pair<String, BuyStrategy>>,
        board: Board,
        progressTracker: AtomicProgressTracker?,
    ): SingleGameResult {
        // 各ゲームで独立した Dice インスタンスを作成
        val gameDice = Dice()

        // プレイヤーを作成
        val players = playerStrategies.map { (name, strategy) ->
            Player(name, strategy)
        }

        // ゲーム状態を初期化
        val gameState = GameState(
            players = players,
            board = board,
        )

        // ゲームを実行
        val winner = gameService.runGame(gameState, gameDice)

        // 進捗を更新
        progressTracker?.incrementAndDisplay()

        return SingleGameResult(
            gameNumber = gameNumber,
            winner = winner.name,
            totalTurns = gameState.turnNumber,
            finalState = gameState,
        )
    }
}
```

**実装のポイント**:
1. `coroutineScope` で構造化並行性を確保
2. `async(Dispatchers.Default)` で各ゲームを並列実行
3. 各ゲームで独立した `Dice` インスタンスを作成
4. `awaitAll()` で全結果を安全に収集

---

### Step 3: Main.kt の更新

**変更内容**:
- `--parallel <N>` オプション追加
- `--sequential` オプション追加
- ParallelGameRunner の使用

```kotlin
// GameConfig に追加
data class GameConfig(
    // ... 既存フィールド
    val parallel: Int? = null, // null の場合は CPU コア数
    val sequential: Boolean = false,
)

// parseArgs に追加
"--parallel" -> {
    if (i + 1 < args.size) {
        parallel = args[i + 1].toIntOrNull() ?: run {
            println("Error: --parallel requires a valid integer")
            exitProcess(1)
        }
        i++
    }
}
"--sequential" -> {
    sequential = true
}

// runMultipleGames を更新
private suspend fun runMultipleGames(config: GameConfig) {
    // ...

    val result = if (config.sequential) {
        // 逐次実行（既存の GameRunner）
        val gameRunner = GameRunner(gameService, dice)
        gameRunner.runMultipleGames(
            numberOfGames = config.numberOfGames,
            playerStrategies = playerStrategies,
            board = board,
            showProgress = true,
        )
    } else {
        // 並列実行（新しい ParallelGameRunner）
        val parallelism = config.parallel ?: Runtime.getRuntime().availableProcessors()
        val parallelGameRunner = ParallelGameRunner(gameService, parallelism)
        parallelGameRunner.runMultipleGames(
            numberOfGames = config.numberOfGames,
            playerStrategies = playerStrategies,
            board = board,
            showProgress = true,
        )
    }

    // ...
}

// main 関数を suspend に変更
suspend fun main(args: Array<String>) {
    // ...
}
```

---

### Step 4: build.gradle.kts の更新

**Kotlin Coroutines 依存関係を追加**:

```kotlin
dependencies {
    // 既存の依存関係...

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // テスト用
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}
```

---

## コーディング規約

### 1. Coroutine のスコープ

**Good**:
```kotlin
suspend fun runMultipleGames(...) = coroutineScope {
    // 構造化並行性を確保
}
```

**Bad**:
```kotlin
fun runMultipleGames(...) = GlobalScope.launch {
    // グローバルスコープは避ける
}
```

### 2. Dispatcher の選択

**CPU 集約的な処理**:
```kotlin
async(Dispatchers.Default) {
    // ゲームシミュレーション
}
```

**I/O 処理**:
```kotlin
async(Dispatchers.IO) {
    // ファイル読み書き（このプロジェクトでは使用しない）
}
```

### 3. リソース管理

**各ゲームで独立したリソースを作成**:
```kotlin
async(Dispatchers.Default) {
    val gameDice = Dice() // ✓ OK: 独立
    // gameService.runGame(gameState, gameDice)
}
```

**共有リソースは避ける**:
```kotlin
val sharedDice = Dice() // ✗ NG: 共有はスレッドセーフでない
async(Dispatchers.Default) {
    gameService.runGame(gameState, sharedDice)
}
```

---

## テストコード例

### 並列実行の正確性テスト

```kotlin
@Test
fun `並列実行でも結果が正確`() = runBlocking {
    val gameService = GameService()
    val parallelGameRunner = ParallelGameRunner(gameService, parallelism = 4)

    val board = createStandardBoard()
    val playerStrategies = listOf(
        "Alice" to AlwaysBuyStrategy(),
        "Bob" to AlwaysBuyStrategy(),
    )

    val result = parallelGameRunner.runMultipleGames(
        numberOfGames = 100,
        playerStrategies = playerStrategies,
        board = board,
        showProgress = false,
    )

    result.totalGames shouldBe 100
    result.gameResults.size shouldBe 100
    result.getWinsByPlayer().values.sum() shouldBe 100
}
```

---

## デバッグのヒント

### 1. 逐次実行で問題を再現

```bash
# 並列実行で問題が発生
./gradlew run --args="--games 1000"

# 逐次実行で再現
./gradlew run --args="--games 1000 --sequential"
```

### 2. 並列度を下げる

```bash
# 並列度を下げて問題を特定
./gradlew run --args="--games 1000 --parallel 1"
```

### 3. ログ出力

```kotlin
async(Dispatchers.Default) {
    println("[Game $gameNumber] Started on ${Thread.currentThread().name}")
    // ...
}
```

---

**作成日**: 2025-11-16
