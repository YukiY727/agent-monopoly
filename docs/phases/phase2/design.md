# Phase 2 詳細設計（TDD版）

**フェーズ目標**: ゲーム進行を最初から最後まで追跡できるようにする

このドキュメントは、TDD（Test-Driven Development）でPhase 2を実装するための設計指針です。

**重要**: Phase 2の実装にあたり、**Phase 1のコードも柔軟に書き換えます**。後方互換性よりも、正しい設計を優先します。

---

## ⚠️ この設計書の位置づけ

**このドキュメントに書かれている設計は「出発点」であり、「確定仕様」ではありません。**

### TDDでの柔軟な設計変更

このドキュメントには詳細な設計例（クラス構造、メソッドシグネチャ、コード例）が記載されていますが、これらは：

- ✅ **参考例・出発点**として活用する
- ✅ **実装中に違和感があれば変更する**
- ✅ **テストを書きながら最適な設計を見つける**
- ❌ **この通りに実装しなければならない**わけではない

### 実装中の判断基準

TDD実装中に以下を積極的に行います：

1. **クラス設計の変更**
   - GameEventの型定義も変更可能
   - 新しいイベント型の追加、既存イベント型の統合など

2. **メソッドシグネチャの変更**
   - ドキュメントの例はあくまで参考
   - 実装してみて使いにくければ変更

3. **コード例の無視**
   - HtmlReportGeneratorの実装例などは参考程度
   - より良い実装方法が見つかればそちらを採用

4. **Phase 1コードの変更**
   - ドキュメント記載の変更内容も柔軟に
   - 実装中に別のアプローチが見つかれば変更

### 設計変更の例

```kotlin
// ドキュメント例: GameEvent.DiceRolled
data class DiceRolled(
    val turnNumber: Int,
    val timestamp: Long,
    val playerName: String,
    val die1: Int,
    val die2: Int,
    val total: Int
) : GameEvent()

// 実装中の判断: timestampは不要かも？
// → テストを書いて、実際に使ってみて判断
// → 必要なければフィールドを削除
```

**実装してみないと最適解は分からない。TDDのリファクタリングフェーズで設計を進化させる。**

---

## TDD開発の流れ

各機能について以下のサイクルを回します：

1. **Red**: テストを書く（失敗する）
2. **Green**: テストが通る最小限の実装
3. **Refactor**: コードをきれいにする（**Phase 1のコードも含む**、**設計も柔軟に変更**）

---

## Phase 2で追加するパッケージ構造（暫定版）

**重要**: このパッケージ構造は**暫定版**です。Phase 2実装中に最適な配置を見つけ、柔軟に変更します。

```
com.monopoly
├── domain
│   ├── model
│   │   ├── (既存) Player, Property, Board, GameState
│   │   └── (新規) GameEvent (sealed class)
│   ├── service
│   │   ├── (既存・改修) GameService, Dice
│   │   └── (新規) EventRecorder
│   └── strategy
│       └── (既存) Strategy, AlwaysBuyStrategy
├── infrastructure       # 新規パッケージ（暫定）
│   ├── logging          # または output/presentation
│   │   ├── ConsoleLogger
│   │   └── EventLogger
│   └── reporting        # 配置は実装中に再検討
│       └── HtmlReportGenerator
└── cli
    └── (既存・改修) MonopolyGame
```

### パッケージ配置の判断基準（実装しながら決定）

**HtmlReportGeneratorの配置について**:
- 現時点では`infrastructure.reporting`に配置
- 実装中に以下の選択肢を検討：
  1. **infrastructure.output**: レポート生成を出力処理として扱う
  2. **presentation.html**: CLI/HTMLを表示層として分離
  3. **application.reporting**: レポート生成をユースケース層として扱う

**判断タイミング**:
- Phase 2実装中に、HtmlReportGeneratorとConsoleLoggerの関連性を評価
- 責務の境界が明確になった時点で、最適なパッケージ構造にリファクタリング
- **実装してみないと最適解は分からない**ため、柔軟に対応

---

## クラス設計と責務

### 新規追加クラス

#### 1. GameEvent (sealed class)
**責務**: ゲーム内のすべてのイベントを型安全に表現

**イベント型**:
```kotlin
sealed class GameEvent {
    abstract val turnNumber: Int
    abstract val timestamp: Long  // System.currentTimeMillis()

    data class GameStarted(
        override val turnNumber: Int = 0,
        override val timestamp: Long,
        val playerNames: List<String>
    ) : GameEvent()

    data class GameEnded(
        override val turnNumber: Int,
        override val timestamp: Long,
        val winner: String?,
        val totalTurns: Int
    ) : GameEvent()

    data class TurnStarted(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String
    ) : GameEvent()

    data class TurnEnded(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String
    ) : GameEvent()

    data class DiceRolled(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String,
        val die1: Int,
        val die2: Int,
        val total: Int
    ) : GameEvent()

    data class PlayerMoved(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String,
        val fromPosition: Int,
        val toPosition: Int,
        val passedGo: Boolean
    ) : GameEvent()

    data class PropertyPurchased(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String,
        val propertyName: String,
        val price: Int
    ) : GameEvent()

    data class RentPaid(
        override val turnNumber: Int,
        override val timestamp: Long,
        val payerName: String,
        val receiverName: String,
        val propertyName: String,
        val amount: Int
    ) : GameEvent()

    data class PlayerBankrupted(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String,
        val finalMoney: Int
    ) : GameEvent()
}
```

---

#### 2. EventRecorder
**責務**: イベントをGameStateに記録

**主要なメソッド**:
```kotlin
class EventRecorder {
    fun record(gameState: GameState, event: GameEvent) {
        gameState.events.add(event)
    }

    fun getEvents(gameState: GameState): List<GameEvent> {
        return gameState.events.toList()  // 防御的コピー
    }
}
```

**設計判断**:
- Phase 2の初期段階ではシンプルなEventRecorderを作成
- 必要に応じてGameServiceに直接組み込むか、別クラスにするか判断
- TDD中に設計を進化させる

---

#### 3. ConsoleLogger
**責務**: ゲームの進行をCLIに表示

**主要なメソッド**:
```kotlin
class ConsoleLogger {
    fun logEvent(event: GameEvent) {
        when (event) {
            is GameEvent.GameStarted -> logGameStarted(event)
            is GameEvent.TurnStarted -> logTurnStarted(event)
            is GameEvent.DiceRolled -> logDiceRolled(event)
            is GameEvent.PlayerMoved -> logPlayerMoved(event)
            is GameEvent.PropertyPurchased -> logPropertyPurchased(event)
            is GameEvent.RentPaid -> logRentPaid(event)
            is GameEvent.PlayerBankrupted -> logPlayerBankrupted(event)
            is GameEvent.TurnEnded -> logTurnEnded(event)
            is GameEvent.GameEnded -> logGameEnded(event)
        }
    }

    private fun logDiceRolled(event: GameEvent.DiceRolled) {
        println("  🎲 ${event.playerName} rolled ${event.die1} + ${event.die2} = ${event.total}")
    }

    private fun logPropertyPurchased(event: GameEvent.PropertyPurchased) {
        println("  💰 ${event.playerName} purchased ${event.propertyName} for $${event.price}")
    }

    // ... 他のイベントログ
}
```

---

#### 4. EventLogger
**責務**: イベントログをJSON形式で保存

**主要なメソッド**:
```kotlin
class EventLogger {
    fun saveToJson(events: List<GameEvent>, filePath: String) {
        // kotlinx.serialization を使用
        val json = Json.encodeToString(events)
        File(filePath).writeText(json)
    }

    fun loadFromJson(filePath: String): List<GameEvent> {
        val json = File(filePath).readText()
        return Json.decodeFromString(json)
    }
}
```

**必要な依存関係**:
- `org.jetbrains.kotlinx:kotlinx-serialization-json` を追加

---

#### 5. HtmlReportGenerator
**責務**: イベントログからHTML形式のレポートを生成

**主要なメソッド**:
```kotlin
class HtmlReportGenerator(private val gameState: GameState) {
    fun generate(): String {
        return buildString {
            appendHtmlHeader()
            appendGameSummary()
            appendPlayerStatesTable()
            appendEventTimeline()
            appendHtmlFooter()
        }
    }

    fun saveToFile(filePath: String) {
        val html = generate()
        File(filePath).writeText(html)
    }

    private fun StringBuilder.appendHtmlHeader() {
        append("""
            <!DOCTYPE html>
            <html>
            <head>
                <title>Monopoly Game Report</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; }
                    h1 { color: #2c3e50; }
                    table { border-collapse: collapse; width: 100%; margin: 20px 0; }
                    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                    th { background-color: #3498db; color: white; }
                    .event { margin: 10px 0; padding: 10px; border-left: 4px solid #3498db; }
                    .bankrupt { background-color: #f8d7da; }
                    .purchase { background-color: #d4edda; }
                    .rent { background-color: #fff3cd; }
                </style>
            </head>
            <body>
        """.trimIndent())
    }

    private fun StringBuilder.appendGameSummary() {
        val gameEndedEvent = gameState.events.filterIsInstance<GameEvent.GameEnded>().firstOrNull()
        append("""
            <h1>Monopoly Game Report</h1>
            <h2>Game Summary</h2>
            <p><strong>Winner:</strong> ${gameEndedEvent?.winner ?: "N/A"}</p>
            <p><strong>Total Turns:</strong> ${gameEndedEvent?.totalTurns ?: 0}</p>
        """.trimIndent())
    }

    private fun StringBuilder.appendEventTimeline() {
        append("<h2>Event Timeline</h2>")
        gameState.events.forEach { event ->
            when (event) {
                is GameEvent.PropertyPurchased -> {
                    append("""<div class="event purchase">Turn ${event.turnNumber}: ${event.playerName} purchased ${event.propertyName} for $${event.price}</div>""")
                }
                is GameEvent.RentPaid -> {
                    append("""<div class="event rent">Turn ${event.turnNumber}: ${event.payerName} paid $${event.amount} rent to ${event.receiverName} for ${event.propertyName}</div>""")
                }
                is GameEvent.PlayerBankrupted -> {
                    append("""<div class="event bankrupt">Turn ${event.turnNumber}: ${event.playerName} went bankrupt</div>""")
                }
                // ... 他のイベント
                else -> {}
            }
        }
    }

    // ... 他のメソッド
}
```

---

### Phase 1クラスの改修

#### 1. GameState（改修）
**変更内容**: イベントログを保持するフィールドを追加

**改修前**:
```kotlin
data class GameState(
    val board: Board,
    val players: List<Player>,
    var currentPlayerIndex: Int = 0,
    var turnNumber: Int = 0,
    var gameOver: Boolean = false
)
```

**改修後**:
```kotlin
data class GameState(
    val board: Board,
    val players: List<Player>,
    var currentPlayerIndex: Int = 0,
    var turnNumber: Int = 0,
    var gameOver: Boolean = false,
    val events: MutableList<GameEvent> = mutableListOf()  // 新規追加
)
```

**影響範囲**:
- 既存のコンストラクタ呼び出しは、デフォルト引数により影響なし
- テストコードでGameStateを生成している箇所は修正不要（デフォルト引数で対応）

---

#### 2. GameService（改修）
**変更内容**: 各メソッドにイベント記録処理を追加

**改修箇所**:

**movePlayer（改修）**:
```kotlin
// 改修前
fun movePlayer(player: Player, diceValue: Int, gameState: GameState) {
    val oldPosition = player.position
    player.position = (player.position + diceValue) % 40

    if (player.position < oldPosition) {
        player.addMoney(200)
    }
}

// 改修後
fun movePlayer(player: Player, diceValue: Int, gameState: GameState) {
    val oldPosition = player.position
    val passedGo = (player.position + diceValue) >= 40
    player.position = (player.position + diceValue) % 40

    if (passedGo) {
        player.addMoney(200)
    }

    // イベント記録を追加
    gameState.events.add(
        GameEvent.PlayerMoved(
            turnNumber = gameState.turnNumber,
            timestamp = System.currentTimeMillis(),
            playerName = player.name,
            fromPosition = oldPosition,
            toPosition = player.position,
            passedGo = passedGo
        )
    )
}
```

**buyProperty（改修）**:
```kotlin
fun buyProperty(player: Player, property: Property) {
    player.subtractMoney(property.price)
    property.owner = player
    player.ownedProperties.add(property)

    // イベント記録を追加（GameStateを引数に追加する必要がある）
}
```

**設計判断**:
- `buyProperty`などのメソッドシグネチャに`gameState: GameState`を追加する必要がある
- これはPhase 1のメソッドシグネチャの変更だが、柔軟に対応する
- テストコードも併せて更新する

**改修後のbuyProperty**:
```kotlin
// シグネチャを変更
fun buyProperty(player: Player, property: Property, gameState: GameState) {
    player.subtractMoney(property.price)
    property.owner = player
    player.ownedProperties.add(property)

    // イベント記録を追加
    gameState.events.add(
        GameEvent.PropertyPurchased(
            turnNumber = gameState.turnNumber,
            timestamp = System.currentTimeMillis(),
            playerName = player.name,
            propertyName = property.name,
            price = property.price
        )
    )
}
```

**同様の改修が必要なメソッド**:
- `payRent(payer, receiver, rent)` → `payRent(payer, receiver, rent, gameState)`
- `bankruptPlayer(player)` → `bankruptPlayer(player, gameState)`
- `processSpace(player, gameState)` → イベント記録を追加（シグネチャ変更不要）
- `executeTurn(gameState)` → イベント記録を追加（シグネチャ変更不要）
- `runGame(gameState)` → イベント記録を追加（シグネチャ変更不要）

---

#### 3. Dice（改修）
**変更内容**: サイコロ結果をイベントとして記録できるようにする

**設計判断**:
- Diceクラス自体はシンプルに保つ
- `executeTurn`でサイコロ結果を記録する方針を採用

**executeTurn内でのイベント記録**:
```kotlin
fun executeTurn(gameState: GameState) {
    val currentPlayer = gameState.getCurrentPlayer()

    // ターン開始イベント
    gameState.events.add(
        GameEvent.TurnStarted(
            turnNumber = gameState.turnNumber,
            timestamp = System.currentTimeMillis(),
            playerName = currentPlayer.name
        )
    )

    // サイコロを振る
    val diceRoll = dice.roll()
    val (die1, die2) = dice.getLastRoll()  // Diceクラスに追加する必要あり

    // サイコロイベント
    gameState.events.add(
        GameEvent.DiceRolled(
            turnNumber = gameState.turnNumber,
            timestamp = System.currentTimeMillis(),
            playerName = currentPlayer.name,
            die1 = die1,
            die2 = die2,
            total = diceRoll
        )
    )

    // 移動とマス目処理
    movePlayer(currentPlayer, diceRoll, gameState)
    processSpace(currentPlayer, gameState)

    // ターン終了イベント
    gameState.events.add(
        GameEvent.TurnEnded(
            turnNumber = gameState.turnNumber,
            timestamp = System.currentTimeMillis(),
            playerName = currentPlayer.name
        )
    )

    gameState.turnNumber++
    gameState.nextPlayer()
}
```

**Diceクラスの拡張**:
```kotlin
class Dice(private val random: Random = Random.Default) {
    private var lastDie1: Int = 0
    private var lastDie2: Int = 0

    constructor(seed: Long) : this(Random(seed))

    fun roll(): Int {
        lastDie1 = random.nextInt(1, 7)
        lastDie2 = random.nextInt(1, 7)
        return lastDie1 + lastDie2
    }

    // 新規追加
    fun getLastRoll(): Pair<Int, Int> = Pair(lastDie1, lastDie2)
}
```

---

#### 4. MonopolyGame（改修）
**変更内容**: CLIログとHTML出力を追加

**改修後のmain**:
```kotlin
fun main() {
    // ゲーム初期化
    val board = Board()
    val strategy = AlwaysBuyStrategy()
    val player1 = Player("Alice", strategy)
    val player2 = Player("Bob", strategy)
    val gameState = GameState(board, listOf(player1, player2))

    // ロガー初期化
    val consoleLogger = ConsoleLogger()

    // ゲーム開始イベント記録
    gameState.events.add(
        GameEvent.GameStarted(
            turnNumber = 0,
            timestamp = System.currentTimeMillis(),
            playerNames = listOf(player1.name, player2.name)
        )
    )

    // ゲーム実行
    val gameService = GameService()

    // イベントをリアルタイム表示（オプション）
    // または後でまとめて表示
    val winner = gameService.runGame(gameState)

    // ゲーム終了後、すべてのイベントをCLI表示
    gameState.events.forEach { event ->
        consoleLogger.logEvent(event)
    }

    // 結果表示
    displayResult(winner, gameState)

    // HTML生成
    val htmlGenerator = HtmlReportGenerator(gameState)
    val timestamp = System.currentTimeMillis()
    htmlGenerator.saveToFile("game-report-$timestamp.html")
    println("\nHTML report saved to: game-report-$timestamp.html")

    // JSON保存
    val eventLogger = EventLogger()
    eventLogger.saveToJson(gameState.events, "game-log-$timestamp.json")
    println("Event log saved to: game-log-$timestamp.json")
}
```

---

## テストケースリスト

### 新規テストケース（Phase 2）

#### GameEventのテスト
- [ ] GameEvent.GameStartedが正しく初期化されること
- [ ] GameEvent.DiceRolledが正しい情報を保持すること
- [ ] すべてのイベント型が必要なフィールドを持つこと

#### EventRecorderのテスト
- [ ] イベントを記録できること
- [ ] イベントが正しい順序で記録されること
- [ ] 防御的コピーが機能すること

#### GameStateのテスト（改修後）
- [ ] eventsフィールドが初期化されること
- [ ] イベントを追加できること
- [ ] Phase 1の既存テストがすべてパスすること

#### GameServiceのテスト（改修後）
- [ ] movePlayer実行後、PlayerMovedイベントが記録されること
- [ ] buyProperty実行後、PropertyPurchasedイベントが記録されること
- [ ] payRent実行後、RentPaidイベントが記録されること
- [ ] bankruptPlayer実行後、PlayerBankruptedイベントが記録されること
- [ ] executeTurn実行後、TurnStarted, DiceRolled, TurnEndedイベントが記録されること
- [ ] runGame実行後、GameStarted, GameEndedイベントが記録されること
- [ ] Phase 1の既存テストがすべてパスすること

#### ConsoleLoggerのテスト
- [ ] 各イベント型を正しくフォーマットして表示すること
- [ ] 手動テスト: 表示が見やすいこと

#### EventLoggerのテスト
- [ ] イベントリストをJSON形式で保存できること
- [ ] 保存したJSONを読み込めること
- [ ] JSONフォーマットが正しいこと

#### HtmlReportGeneratorのテスト
- [ ] HTMLが生成されること
- [ ] 必要な要素（title, table, event listなど）が含まれること
- [ ] ファイルに保存できること
- [ ] 手動テスト: ブラウザで表示して確認

#### 統合テスト（Phase 2）
- [ ] ゲームを実行し、イベントログが記録されること
- [ ] JSON/HTMLファイルが出力されること
- [ ] HTMLをブラウザで開き、ゲームの流れを確認できること

---

## TDD実装順序

### ステップ1: イベント型の定義
1. **GameEvent sealed class**
   - テスト: 各イベント型が正しく初期化されること
   - 実装: sealed classと各イベント型

### ステップ2: GameStateの拡張
2. **GameStateにeventsフィールド追加**
   - テスト: eventsフィールドが初期化されること、イベントを追加できること
   - 実装: `val events: MutableList<GameEvent> = mutableListOf()`
   - Phase 1のテストがパスすることを確認

### ステップ3: GameServiceの改修
3. **movePlayerにイベント記録追加**
   - テスト: PlayerMovedイベントが記録されること
   - 実装: イベント記録処理を追加

4. **buyPropertyにイベント記録追加**
   - テスト: PropertyPurchasedイベントが記録されること
   - 実装: シグネチャ変更 + イベント記録処理
   - Phase 1のテストを更新

5. **payRent, bankruptPlayerにイベント記録追加**
   - 同様にテストと実装

6. **executeTurn, runGameにイベント記録追加**
   - テスト: TurnStarted, DiceRolled, TurnEnded, GameStarted, GameEndedイベントが記録されること
   - 実装: イベント記録処理を追加

### ステップ4: Diceの拡張
7. **Diceにlastロール記録追加**
   - テスト: getLastRoll()が正しい値を返すこと
   - 実装: lastDie1, lastDie2フィールドとgetLastRoll()メソッド

### ステップ5: ロガー実装
8. **ConsoleLogger**
   - テスト: 各イベントが正しくフォーマットされること
   - 実装: logEvent()メソッドと各イベント用のprivateメソッド

9. **EventLogger**
   - テスト: JSON保存/読み込みが正しく動作すること
   - 実装: saveToJson(), loadFromJson()メソッド

### ステップ6: HTML生成
10. **HtmlReportGenerator**
    - テスト: HTML生成、ファイル保存
    - 実装: generate(), saveToFile()メソッド

### ステップ7: CLI統合
11. **MonopolyGameの改修**
    - テスト: 手動テスト（実行してログ表示、HTML生成確認）
    - 実装: main()の更新

### ステップ8: 統合テスト
12. **Phase 2統合テスト**
    - テスト: ゲーム全体実行、イベントログ記録、ファイル出力確認
    - 実装: 統合テストコード

---

## Phase 1コードの改修に関する具体的方針

### メソッドシグネチャの変更

以下のメソッドはシグネチャを変更します：

| メソッド | 変更前 | 変更後 |
|---------|-------|-------|
| buyProperty | `(player, property)` | `(player, property, gameState)` |
| payRent | `(payer, receiver, rent)` | `(payer, receiver, rent, gameState)` |
| bankruptPlayer | `(player)` | `(player, gameState)` |

### テストコードの更新

Phase 1のテストコードで以下の箇所を更新します：

- `GameServiceTest.kt`: 上記メソッドの呼び出しに`gameState`引数を追加
- `GameStateTest.kt`: 特に変更なし（デフォルト引数で対応）
- `PlayerTest.kt`: 特に変更なし

### リファクタリングの自由度

Phase 2実装中に以下の改善を積極的に行います：

1. **コードの重複削除**
   - イベント記録処理が重複する場合、ヘルパーメソッドに抽出

2. **責務の明確化**
   - EventRecorderが必要であれば導入
   - GameServiceが肥大化する場合は分割

3. **Null安全性の改善**
   - Phase 1でnullableにした箇所を見直し、sealed classなどで置き換え

4. **Immutabilityの見直し**
   - イベントログは追記型なので`MutableList`が自然
   - 他の部分は可能な限りimmutableに保つ

---

## Phase 2完了の定義

以下がすべて満たされたらPhase 2完了：

- ✅ すべてのPhase 2テストケースがパス
- ✅ Phase 1のテストケースがすべてパス（改修後）
- ✅ CLIでゲームログが詳細に表示される
- ✅ HTML形式のレポートが生成される
- ✅ JSONログが保存される
- ✅ HTMLをブラウザで開き、ゲームの流れを確認できる
- ✅ コードがリファクタリングされている

---

**作成日**: 2025-11-15
**最終更新**: 2025-11-15
