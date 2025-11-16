# Phase 8: 並列実行 - 設計詳細

---

## アーキテクチャ概要

```
┌─────────────────────────────────────────────┐
│            Main (CLI)                        │
│  --parallel <N>  / --sequential             │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│       ParallelGameRunner                     │
│  - Coroutine Dispatcher設定                  │
│  - async/await による並列実行                │
│  - 結果の集約                                 │
└──────────────────┬──────────────────────────┘
                   │
         ┌─────────┴─────────┐
         ▼                   ▼
    ┌─────────┐         ┌─────────┐
    │ Game 1  │   ...   │ Game N  │
    │ (独立)  │         │ (独立)  │
    └─────────┘         └─────────┘
         │                   │
         └─────────┬─────────┘
                   ▼
         ┌──────────────────┐
         │  MultiGameResult │
         └──────────────────┘
```

---

## クラス設計

### 1. ParallelGameRunner

```kotlin
class ParallelGameRunner(
    private val gameService: GameService,
    private val dice: Dice,
    private val parallelism: Int = Runtime.getRuntime().availableProcessors()
) {
    suspend fun runMultipleGames(
        numberOfGames: Int,
        playerStrategies: List<Pair<String, BuyStrategy>>,
        board: Board,
        showProgress: Boolean = true
    ): MultiGameResult
}
```

**責務**:
- Kotlin Coroutinesを使った並列実行
- 各ゲームの独立実行
- 結果の集約

**並列度の決定**:
- デフォルト: `Runtime.getRuntime().availableProcessors()`
- CLIオプションで上書き可能
- 1 の場合は逐次実行と同等

### 2. AtomicProgressTracker

```kotlin
class AtomicProgressTracker(private val totalGames: Int) {
    private val completed = AtomicInteger(0)

    fun start()
    fun incrementAndDisplay()
    fun finish()
}
```

**責務**:
- スレッドセーフな進捗カウント
- リアルタイム進捗表示

---

## データフロー

### 並列実行のフロー

```
1. 並列度を決定 (parallelism)
   ↓
2. CoroutineScope を作成
   ↓
3. numberOfGames を parallelism で分割
   ↓
4. 各チャンクを async で並列実行
   ↓
5. 各 async ブロック内で複数ゲームを実行
   ↓
6. awaitAll で全結果を収集
   ↓
7. MultiGameResult に集約
```

### 実装例

```kotlin
suspend fun runMultipleGames(...): MultiGameResult = coroutineScope {
    val deferredResults = (1..numberOfGames).map { gameNumber ->
        async(Dispatchers.Default) {
            val gameState = createInitialGameState(...)
            val winner = gameService.runGame(gameState, dice)

            if (showProgress) {
                progressTracker.incrementAndDisplay()
            }

            SingleGameResult(
                gameNumber = gameNumber,
                winner = winner.name,
                totalTurns = gameState.turnNumber,
                finalState = gameState
            )
        }
    }

    val results = deferredResults.awaitAll()
    MultiGameResult(results, numberOfGames)
}
```

---

## 並列実行の詳細設計

### Dispatcher の選択

**Dispatchers.Default**:
- CPU 集約的なタスクに最適
- スレッドプール: CPU コア数
- ゲームシミュレーションに適している

**代替案**:
- `Dispatchers.IO`: I/O 処理用（このケースには不適）
- `Dispatchers.Unconfined`: テスト用（本番には不適）

### バッチ処理

大量のゲーム（例: 100,000ゲーム）の場合、メモリ効率のためバッチ処理を行う：

```kotlin
fun runMultipleGamesBatched(...): MultiGameResult {
    val batchSize = 10000
    val allResults = mutableListOf<SingleGameResult>()

    for (batchStart in 0 until numberOfGames step batchSize) {
        val batchEnd = min(batchStart + batchSize, numberOfGames)
        val batchResult = runBatch(batchStart, batchEnd, ...)
        allResults.addAll(batchResult.gameResults)
    }

    return MultiGameResult(allResults, numberOfGames)
}
```

---

## スレッドセーフティ

### 1. 乱数生成

**問題**: 複数のコルーチンで同じ Dice インスタンスを共有すると競合

**解決策**: 各ゲームで独立した Dice インスタンスを生成

```kotlin
async(Dispatchers.Default) {
    val gameDice = Dice() // 各ゲーム専用
    gameService.runGame(gameState, gameDice)
}
```

### 2. 進捗表示

**問題**: 複数のコルーチンから同時に進捗を更新

**解決策**: `AtomicInteger` で安全にカウント

```kotlin
private val completed = AtomicInteger(0)

fun incrementAndDisplay() {
    val current = completed.incrementAndGet()
    // 表示処理
}
```

### 3. 結果の集約

**問題**: 結果リストへの同時書き込み

**解決策**: `awaitAll` で全結果を収集してから集約（不変リスト）

```kotlin
val results = deferredResults.awaitAll() // 安全
MultiGameResult(results, numberOfGames)
```

---

## パフォーマンス最適化

### 1. メモリ管理

**戦略**:
- 各ゲーム完了後、不要なデータを破棄
- バッチ処理で大量ゲームに対応
- GC フレンドリーなデータ構造

### 2. CPU 効率

**戦略**:
- 並列度を CPU コア数に合わせる
- オーバーサブスクリプションを避ける
- 軽量なコルーチンで効率化

### 3. ベンチマーク

**測定項目**:
- 実行時間（逐次 vs 並列）
- CPU 使用率
- メモリ使用量
- スループット（ゲーム/秒）

---

## エラーハンドリング

### Coroutine の例外処理

```kotlin
coroutineScope {
    try {
        val deferredResults = (1..numberOfGames).map { ... }
        deferredResults.awaitAll()
    } catch (e: Exception) {
        // 1つのゲームでエラーが発生した場合
        // 全てのコルーチンがキャンセルされる
        println("Error during parallel execution: ${e.message}")
        throw e
    }
}
```

**構造化並行性**:
- 親スコープの例外は子コルーチンをキャンセル
- 子の例外は親に伝播
- 全てのコルーチンが確実にクリーンアップされる

---

## テスト戦略

### 1. 正確性テスト

```kotlin
@Test
fun `並列実行と逐次実行で同じ結果が得られる`() {
    val seed = 12345L

    val sequentialResult = runSequential(seed)
    val parallelResult = runParallel(seed)

    sequentialResult.getWinsByPlayer() shouldBe parallelResult.getWinsByPlayer()
}
```

### 2. パフォーマンステスト

```kotlin
@Test
fun `並列実行が逐次実行より高速`() {
    val startSequential = System.currentTimeMillis()
    runSequential(100)
    val sequentialTime = System.currentTimeMillis() - startSequential

    val startParallel = System.currentTimeMillis()
    runParallel(100)
    val parallelTime = System.currentTimeMillis() - startParallel

    parallelTime shouldBeLessThan (sequentialTime * 0.7) // 30%以上高速化
}
```

---

## CLI インターフェース

### 新しいオプション

```bash
# デフォルト（並列実行、CPU コア数）
./gradlew run --args="--games 1000"

# 並列度を指定
./gradlew run --args="--games 1000 --parallel 4"

# 逐次実行（デバッグ用）
./gradlew run --args="--games 1000 --sequential"
```

---

## 依存関係

### build.gradle.kts への追加

```kotlin
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}
```

Kotlin Coroutinesは標準ライブラリではないため、依存関係の追加が必要です。

---

**作成日**: 2025-11-16
