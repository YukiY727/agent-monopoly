# Phase 2: コーディングガイド

このドキュメントは、Phase 2実装時のコーディング規約を定義します。

**Phase 1のコーディングガイドを継承しつつ、Phase 2特有の方針を追加します。**

---

## 最重要方針: 柔軟性を最優先

### Phase 1コードは柔軟に書き換える

Phase 2では、**Phase 1のコードも柔軟に書き換えます**。以下の原則を守ります：

1. **後方互換性よりも正しい設計を優先**
   - Phase 1のAPIを変更する必要があれば、躊躇なく変更する
   - メソッドシグネチャ、クラス構造、パッケージ構成など、すべて変更可能

2. **"動作するコード"よりも"保守しやすいコード"を優先**
   - Phase 1で妥協した設計があれば、Phase 2で改善する
   - リファクタリングを恐れない

3. **技術的負債を残さない**
   - Phase 2実装中に発見した問題は、その場で修正する
   - "後で直す"は禁止

4. **テストコードも積極的に更新**
   - Phase 1のテストがPhase 2の実装を妨げる場合は、テストを修正する
   - テストは設計の変更に追従する

---

## Phase 2での具体的な柔軟性の実践

### 1. メソッドシグネチャの変更

**変更が必要な理由**: イベント記録のために`GameState`が必要

**変更例**:
```kotlin
// ❌ Phase 1（変更前）
fun buyProperty(player: Player, property: Property) {
    player.subtractMoney(property.price)
    property.owner = player
    player.ownedProperties.add(property)
}

// ✅ Phase 2（変更後）
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

**影響範囲の対処**:
- `buyProperty`を呼び出しているすべての箇所を修正
- テストコードも併せて修正
- IDEのリファクタリング機能を活用

---

### 2. データ構造の拡張

**変更が必要な理由**: イベントログを保持するため

**変更例**:
```kotlin
// ❌ Phase 1（変更前）
data class GameState(
    val board: Board,
    val players: List<Player>,
    var currentPlayerIndex: Int = 0,
    var turnNumber: Int = 0,
    var gameOver: Boolean = false
)

// ✅ Phase 2（変更後）
data class GameState(
    val board: Board,
    val players: List<Player>,
    var currentPlayerIndex: Int = 0,
    var turnNumber: Int = 0,
    var gameOver: Boolean = false,
    val events: MutableList<GameEvent> = mutableListOf()  // 新規追加
)
```

**影響範囲の対処**:
- デフォルト引数により、既存のコンストラクタ呼び出しは影響なし
- ただし、明示的に`events`を渡したい箇所は自由に追加

---

### 3. 責務の再分配

**Phase 2実装中に、以下の判断を柔軟に行う**:

#### 例1: EventRecorderの導入判断
```kotlin
// パターンA: GameServiceに直接組み込む（シンプル）
class GameService {
    fun buyProperty(player: Player, property: Property, gameState: GameState) {
        // ... 購入処理
        gameState.events.add(PropertyPurchased(...))  // 直接追加
    }
}

// パターンB: EventRecorderを分離（責務の明確化）
class GameService(private val eventRecorder: EventRecorder) {
    fun buyProperty(player: Player, property: Property, gameState: GameState) {
        // ... 購入処理
        eventRecorder.record(gameState, PropertyPurchased(...))  // 委譲
    }
}
```

**判断基準**:
- 初期段階ではパターンAで進める（シンプル）
- イベント記録処理が複雑になったら、パターンBにリファクタリング
- **どちらが正しいかは実装しながら判断**

#### 例2: ConsoleLoggerの責務
```kotlin
// パターンA: MonopolyGameで直接ログ出力
fun main() {
    // ... ゲーム実行
    gameState.events.forEach { event ->
        when (event) {
            is GameEvent.DiceRolled -> println("Rolled ${event.total}")
            // ...
        }
    }
}

// パターンB: ConsoleLoggerに委譲（推奨）
fun main() {
    val consoleLogger = ConsoleLogger()
    // ... ゲーム実行
    gameState.events.forEach { event ->
        consoleLogger.logEvent(event)
    }
}
```

**判断基準**:
- パターンBを推奨（責務の分離）
- ただし、初期段階ではパターンAでプロトタイプを作成してもOK
- **実装しながら適切な設計に進化させる**

---

### 4. Immutabilityの柔軟な運用

**Phase 1では可能な限りimmutableを推奨したが、Phase 2では柔軟に**

```kotlin
// ✅ イベントログはMutableListが自然
data class GameState(
    val events: MutableList<GameEvent> = mutableListOf()
)

// ✅ 防御的コピーは必要な箇所のみ
fun getEvents(gameState: GameState): List<GameEvent> {
    return gameState.events.toList()  // 外部に返す時は防御的コピー
}

// ✅ 内部では直接追加してOK
gameState.events.add(event)
```

**判断基準**:
- イベントログは追記型なので、`MutableList`が自然
- 他の部分は可能な限りimmutableに保つ
- **実用性と安全性のバランスを取る**

---

### 5. パッケージ構成の柔軟性

**重要**: Phase 2のパッケージ構成は**暫定版**です。実装しながら最適な配置を見つけます。

#### 初期配置（暫定）
```
com.monopoly
├── domain
├── infrastructure
│   ├── logging (ConsoleLogger, EventLogger)
│   └── reporting (HtmlReportGenerator)
└── cli
```

#### 実装中の判断ポイント

**HtmlReportGeneratorの配置**:
- 現時点では`infrastructure.reporting`に配置
- 実装中に以下を検討：
  1. `infrastructure.output`に変更（出力処理として統一）
  2. `presentation.html`に移動（表示層として分離）
  3. `application.reporting`に移動（ユースケース層として扱う）

**判断タイミング**:
- HtmlReportGeneratorとConsoleLoggerの実装が進んだ時点
- 責務の境界が明確になったら、適切にリファクタリング
- **実装してみないと最適解は分からない**

#### リファクタリングの自由度

Phase 2実装中に以下を積極的に行います：

- ✅ パッケージ名の変更（`reporting` → `output`など）
- ✅ パッケージ構造の再編成（`presentation`層の導入など）
- ✅ クラスの移動（ConsoleLoggerとHtmlReportGeneratorを同じ層に配置など）
- ✅ テストコードのパッケージも併せて更新

**例: リファクタリング後の構成**
```kotlin
// 初期: infrastructure.reporting.HtmlReportGenerator
package com.monopoly.infrastructure.reporting

// リファクタリング後: presentation.html.HtmlReportGenerator
package com.monopoly.presentation.html

// テストコードも追従
// test/.../infrastructure/reporting/HtmlReportGeneratorTest.kt
// → test/.../presentation/html/HtmlReportGeneratorTest.kt
```

---

## Phase 2で追加する規約

### イベント記録の一貫性

**すべてのゲームアクションでイベントを記録**

```kotlin
// ✅ 推奨: アクション実行後、必ずイベント記録
fun movePlayer(player: Player, diceValue: Int, gameState: GameState) {
    // アクション実行
    val oldPosition = player.position
    player.position = (player.position + diceValue) % 40

    // イベント記録（必須）
    gameState.events.add(PlayerMoved(...))
}

// ❌ 避ける: イベント記録を忘れる
fun movePlayer(player: Player, diceValue: Int, gameState: GameState) {
    player.position = (player.position + diceValue) % 40
    // イベント記録なし → バグ
}
```

**チェックリスト**:
- [ ] movePlayer → PlayerMovedイベント
- [ ] buyProperty → PropertyPurchasedイベント
- [ ] payRent → RentPaidイベント
- [ ] bankruptPlayer → PlayerBankruptedイベント
- [ ] executeTurn → TurnStarted, DiceRolled, TurnEndedイベント
- [ ] runGame → GameStarted, GameEndedイベント

---

### イベントの命名規則

**過去形で命名**

```kotlin
// ✅ 推奨: 過去形（イベントは「起こったこと」）
data class DiceRolled(...)
data class PlayerMoved(...)
data class PropertyPurchased(...)

// ❌ 避ける: 現在形や名詞形
data class DiceRoll(...)
data class MovePlayer(...)
data class BuyProperty(...)
```

---

### イベントの必須フィールド

**すべてのイベントに以下を含める**

```kotlin
sealed class GameEvent {
    abstract val turnNumber: Int      // 必須: どのターンで発生したか
    abstract val timestamp: Long      // 必須: いつ発生したか（ミリ秒）

    data class PropertyPurchased(
        override val turnNumber: Int,
        override val timestamp: Long,
        val playerName: String,       // イベント特有の情報
        val propertyName: String,
        val price: Int
    ) : GameEvent()
}
```

---

### HTML生成の規約

**シンプルさを優先、デザインは凝らない**

```kotlin
// ✅ 推奨: シンプルなHTML、インラインCSS
fun generateHtml(): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body { font-family: Arial, sans-serif; }
                table { border-collapse: collapse; }
            </style>
        </head>
        <body>
            <h1>Game Report</h1>
            <!-- ... -->
        </body>
        </html>
    """.trimIndent()
}

// ❌ 避ける: 複雑なCSS、外部ライブラリ（Phase 2では不要）
// Phase 12（高度な可視化）まで凝ったデザインは不要
```

---

## Phase 1規約の継承

Phase 2でも以下のPhase 1規約を継承します：

### 継承する規約
- **Null安全性**: nullableを極力排除、sealed classで状態表現
- **命名規則**: PascalCase（クラス）、camelCase（メソッド）
- **else禁止**: early returnやwhen式で代替
- **省略禁止**: 変数名・メソッド名を省略しない
- **テストファースト**: 必ずテストから書く

### Phase 2で緩和する規約
- **Immutability**: イベントログなど、Mutableが自然な箇所は柔軟に
- **1行1ドット**: Phase 2では実用性を優先、過度に厳格にしない
- **2インスタンス変数まで**: Phase 2では柔軟に、必要なら3つ以上も許容

---

## リファクタリングのタイミング

### Phase 2実装中のリファクタリング方針

**"3回ルール"を適用**:
1. 1回目: コピー&ペーストで実装
2. 2回目: まだ重複を許容
3. 3回目: 共通化・抽象化する

**例: イベント記録処理の共通化**
```kotlin
// 1回目: movePlayerで直接記録
gameState.events.add(PlayerMoved(...))

// 2回目: buyPropertyでも直接記録（重複発生）
gameState.events.add(PropertyPurchased(...))

// 3回目: 共通化を検討
private fun recordEvent(gameState: GameState, event: GameEvent) {
    gameState.events.add(event)
}

// または EventRecorder クラスに分離
```

---

## テストコードの更新方針

### Phase 1のテストが失敗した場合

**対処法**:
1. **テストを修正する**（推奨）
   - Phase 2の実装に合わせてテストを更新
   - メソッドシグネチャが変わった場合は、呼び出しを修正

2. **実装を見直す**
   - テストが失敗する理由が設計の問題なら、実装を修正

**例: buyPropertyのテスト更新**
```kotlin
// ❌ Phase 1（変更前）
"should purchase property" {
    val player = Player("Alice", strategy)
    val property = Property("Park Place", 5, 350, 35)

    gameService.buyProperty(player, property)

    player.money shouldBe 1150
    property.owner shouldBe player
}

// ✅ Phase 2（変更後）
"should purchase property and record event" {
    val player = Player("Alice", strategy)
    val property = Property("Park Place", 5, 350, 35)
    val gameState = GameState(board, listOf(player))

    gameService.buyProperty(player, property, gameState)  // gameState追加

    player.money shouldBe 1150
    property.owner shouldBe player
    // イベント記録を検証（追加）
    gameState.events.any { it is GameEvent.PropertyPurchased } shouldBe true
}
```

---

## Phase 2特有の注意事項

### JSON Serializationの追加

**kotlinx.serializationを使用**

```kotlin
// build.gradle.kts に追加
plugins {
    kotlin("plugin.serialization") version "1.9.0"
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}
```

**イベントクラスに@Serializableを追加**
```kotlin
import kotlinx.serialization.Serializable

@Serializable
sealed class GameEvent {
    @Serializable
    data class DiceRolled(...) : GameEvent()
    // ...
}
```

---

### ファイル出力のテスト

**一時ディレクトリを使用**

```kotlin
"should save HTML to file" {
    val tempDir = Files.createTempDirectory("test")
    val filePath = tempDir.resolve("report.html").toString()

    htmlGenerator.saveToFile(filePath)

    File(filePath).exists() shouldBe true
    // クリーンアップ
    tempDir.toFile().deleteRecursively()
}
```

---

### 手動テストの実施

**Phase 2では手動テストも重要**

1. **CLIログの視認性**
   - ゲームを実行して、ログが見やすいか確認
   - 色付け、記号、インデントなどを調整

2. **HTMLの表示確認**
   - 生成したHTMLをブラウザで開く
   - レイアウト、テーブル、イベントリストが正しく表示されるか確認

3. **JSONの妥当性**
   - 生成したJSONをエディタで開く
   - フォーマットが正しいか、必要な情報が含まれているか確認

---

## Phase 2完了時のチェックリスト

- [ ] Phase 1のすべてのテストがパス
- [ ] Phase 2の新規テストがすべてパス
- [ ] CLIでゲームログが詳細に表示される
- [ ] HTMLレポートが生成され、ブラウザで表示できる
- [ ] JSONログが保存され、妥当な内容である
- [ ] Phase 1のコードがPhase 2の要件に合わせて適切にリファクタリングされている
- [ ] 技術的負債が残っていない
- [ ] コードが見やすく、保守しやすい状態

---

## まとめ: Phase 2の柔軟性の実践

### Phase 2での最重要原則

1. **Phase 1のコードを柔軟に書き換える**
2. **後方互換性よりも正しい設計を優先**
3. **"動作するコード"よりも"保守しやすいコード"**
4. **技術的負債を残さない**
5. **実装しながら設計を進化させる**

### Phase 2実装中の心構え

- ✅ 躊躇なくPhase 1のコードを変更する
- ✅ メソッドシグネチャを変更する必要があれば変更する
- ✅ テストコードも積極的に更新する
- ✅ リファクタリングを恐れない
- ✅ イベント記録を一貫して行う
- ✅ シンプルさを優先、過度に凝らない

---

**作成日**: 2025-11-15
**対象フェーズ**: Phase 2
**強調事項**: **Phase 1のコードも柔軟に書き換える**
