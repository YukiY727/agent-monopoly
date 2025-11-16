# Phase 8 ドメイン用語集

**Phase 8**: 並列実行

---

## 1. 並列処理関連用語

### 1.1 Parallelism（並列性）

**定義**: 複数のタスクを同時に実行する能力

**並列度（Degree of Parallelism）**: 同時に実行するタスクの数

**Phase 8での設定**:
- デフォルト: CPU コア数
- CLI オプションで変更可能（`--parallel <N>`）

### 1.2 Concurrency（並行性）

**定義**: 複数のタスクを交互に実行する能力

**並列性との違い**:
- 並行性: タスクの構造（同時進行可能な設計）
- 並列性: タスクの実行（実際に同時実行）

### 1.3 Thread Safety（スレッドセーフティ）

**定義**: 複数のスレッドから同時にアクセスしても安全な状態

**Phase 8での対策**:
- AtomicInteger による安全なカウント
- 不変データ構造の使用
- 各ゲームでの独立したリソース

---

## 2. Kotlin Coroutines 用語

### 2.1 Coroutine（コルーチン）

**定義**: 軽量なスレッド。中断可能な計算単位

**特徴**:
- スレッドより軽量（メモリ効率が良い）
- 中断・再開が可能
- 構造化並行性をサポート

### 2.2 Suspend Function（サスペンド関数）

**定義**: 中断可能な関数

**宣言**:
```kotlin
suspend fun runMultipleGames(...): MultiGameResult
```

**特徴**:
- 他のサスペンド関数やコルーチンから呼び出し可能
- 非ブロッキングで実行

### 2.3 CoroutineScope（コルーチンスコープ）

**定義**: コルーチンのライフサイクルを管理する範囲

**Phase 8での使用**:
```kotlin
coroutineScope {
    // この中でコルーチンを起動
}
```

### 2.4 Structured Concurrency（構造化並行性）

**定義**: コルーチンの親子関係を明確にし、ライフサイクルを管理する設計原則

**利点**:
- 親がキャンセルされると子も自動的にキャンセル
- 子の例外は親に伝播
- リソースリークを防ぐ

---

## 3. Dispatcher（ディスパッチャー）

### 3.1 Dispatcher

**定義**: コルーチンがどのスレッドで実行されるかを決定するもの

### 3.2 Dispatchers.Default

**定義**: CPU 集約的な処理用のディスパッチャー

**特徴**:
- スレッドプールサイズ: CPU コア数
- ゲームシミュレーションに最適

**Phase 8での使用**:
```kotlin
async(Dispatchers.Default) {
    runSingleGame(...)
}
```

### 3.3 Dispatchers.IO

**定義**: I/O 処理用のディスパッチャー

**Phase 8では使用しない**: ゲームシミュレーションは CPU 集約的なため

---

## 4. Coroutine Builders（コルーチンビルダー）

### 4.1 async

**定義**: 非同期処理を開始し、Deferred を返すビルダー

**Phase 8での使用**:
```kotlin
val deferred = async(Dispatchers.Default) {
    runSingleGame(...)
}
```

### 4.2 await / awaitAll

**定義**: Deferred の結果を待機する

**Phase 8での使用**:
```kotlin
val results = deferredResults.awaitAll()
```

### 4.3 runBlocking

**定義**: コルーチンをブロッキングで実行（テストで使用）

**Phase 8での使用**:
```kotlin
@Test
fun test() = runBlocking {
    val result = parallelGameRunner.runMultipleGames(...)
}
```

---

## 5. 並列実行の設計パターン

### 5.1 Task Parallelism（タスク並列）

**定義**: 異なるタスクを並列実行

**Phase 8でのアプローチ**: 各ゲームを独立したタスクとして並列実行

### 5.2 Data Parallelism（データ並列）

**定義**: 同じ処理を異なるデータに対して並列実行

**Phase 8での例**: N 個のゲームに対して同じシミュレーション処理

---

## 6. パフォーマンス用語

### 6.1 Throughput（スループット）

**定義**: 単位時間あたりの処理量

**Phase 8の目標**: 10,000ゲーム/分以上

### 6.2 Speedup（高速化率）

**定義**: 並列実行による性能向上の度合い

**計算式**:
```
Speedup = 逐次実行時間 / 並列実行時間
```

**理想値**: 並列度と同じ（例: 4コアなら4倍）
**現実値**: オーバーヘッドにより理想値より低い

### 6.3 Overhead（オーバーヘッド）

**定義**: 並列化に伴う追加コスト

**Phase 8でのオーバーヘッド**:
- コルーチンの起動コスト
- スレッド間の同期コスト
- 結果の集約コスト

---

## 7. スレッドセーフティパターン

### 7.1 Atomic Operations（アトミック操作）

**定義**: 分割不可能な操作（中断されない）

**Phase 8での使用**:
```kotlin
val completed = AtomicInteger(0)
completed.incrementAndGet() // アトミックに +1
```

### 7.2 Immutability（不変性）

**定義**: 作成後に変更できないデータ

**Phase 8での使用**:
```kotlin
val results = deferredResults.awaitAll() // 不変リスト
MultiGameResult(results, numberOfGames)
```

---

## 8. CLI オプション

### 8.1 --parallel <N>

**定義**: 並列度を指定するオプション

**デフォルト**: CPU コア数

**使用例**:
```bash
./gradlew run --args="--games 1000 --parallel 4"
```

### 8.2 --sequential

**定義**: 逐次実行を強制するオプション（デバッグ用）

**使用例**:
```bash
./gradlew run --args="--games 1000 --sequential"
```

---

## 9. クラス・コンポーネント

### 9.1 ParallelGameRunner

**定義**: 並列実行エンジン

**責務**:
- Kotlin Coroutines を使った並列実行
- 結果の集約

### 9.2 AtomicProgressTracker

**定義**: スレッドセーフな進捗トラッカー

**責務**:
- 複数のコルーチンから安全に進捗を更新
- リアルタイムで進捗バーを表示

### 9.3 GameRunner (既存)

**定義**: 逐次実行エンジン

**Phase 8での位置づけ**: デバッグ用に残す（--sequential オプション）

---

**作成日**: 2025-11-16
