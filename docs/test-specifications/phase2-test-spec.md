# Phase 2: テスト仕様書（簡易版）

**フェーズ目標**: ゲーム進行を最初から最後まで追跡できるようにする

このドキュメントは、TDDでPhase 2を実装するための簡易テストケースリストです。
各テストケースは実装時にGiven-When-Thenコメントとしてテストコードに記載します。

**重要**: Phase 2では**Phase 1のコードも柔軟に書き換えます**。Phase 1のテストも必要に応じて更新します。

---

## ⚠️ このテスト仕様書の位置づけ

**このドキュメントに書かれているテストケースは「参考例」であり、「この通りにテストを書かなければならない」わけではありません。**

### TDDでの柔軟なテスト設計

このドキュメントには約35のテストケースが記載されていますが：

- ✅ **テスト実装の出発点**として活用する
- ✅ **実装中に不要と判断したテストはスキップする**
- ✅ **新たに必要なテストが見つかれば追加する**
- ✅ **テストケースの内容（Given-When-Then）も柔軟に変更する**
- ❌ **この通りにテストを書かなければならない**わけではない

### テスト変更の例

```
例1: TC-201「GameStarted初期化」
→ 実装中に「turnNumberフィールドは不要」と判断したらテストを変更

例2: TC-250「ConsoleLoggerのフォーマット」
→ 実装中に「記号ではなく色で表現する方が良い」と判断したらテストを変更

例3: 新規テストの追加
→ 実装中に「イベントの順序が重要」と気づいたら、順序検証のテストを追加
```

**TDDでテストを書きながら、最適なテストケースを見つける。**

---

## テストケース概要

### Phase 2 新規テスト（5カテゴリ、約30ケース）
- GameEvent: 9ケース
- GameState（拡張）: 3ケース
- GameService（拡張）: 9ケース
- Dice（拡張）: 2ケース
- ConsoleLogger: 2ケース
- EventLogger: 3ケース
- HtmlReportGenerator: 4ケース
- 統合テスト: 3ケース

### Phase 1 既存テスト（改修）
- GameServiceTest: メソッドシグネチャ変更に伴う更新
- その他: 必要に応じて更新

**Phase 2新規テスト合計**: 約35テストケース
**Phase 1既存テスト**: 全46テストケースがパスすること

---

## 1. GameEventのテスト

### 1.1 GameEvent.GameStarted

#### TC-201: GameStarted初期化
- [ ] Given: プレイヤー名リスト["Alice", "Bob"]
- [ ] When: GameStartedイベントを作成
- [ ] Then: playerNamesが正しく設定されている、turnNumberが0

---

### 1.2 GameEvent.DiceRolled

#### TC-202: DiceRolled初期化
- [ ] Given: ターン番号1、プレイヤー名"Alice"、サイコロ3と4
- [ ] When: DiceRolledイベントを作成
- [ ] Then: die1が3、die2が4、totalが7、playerNameが"Alice"

---

### 1.3 GameEvent.PlayerMoved

#### TC-203: PlayerMoved初期化
- [ ] Given: ターン番号1、プレイヤー名"Alice"、位置0→7、GO通過なし
- [ ] When: PlayerMovedイベントを作成
- [ ] Then: fromPositionが0、toPositionが7、passedGoがfalse

#### TC-204: PlayerMoved（GO通過あり）
- [ ] Given: ターン番号1、プレイヤー名"Alice"、位置38→3、GO通過あり
- [ ] When: PlayerMovedイベントを作成
- [ ] Then: fromPositionが38、toPositionが3、passedGoがtrue

---

### 1.4 GameEvent.PropertyPurchased

#### TC-205: PropertyPurchased初期化
- [ ] Given: ターン番号2、プレイヤー名"Bob"、プロパティ"Park Place"、価格350
- [ ] When: PropertyPurchasedイベントを作成
- [ ] Then: playerNameが"Bob"、propertyNameが"Park Place"、priceが350

---

### 1.5 GameEvent.RentPaid

#### TC-206: RentPaid初期化
- [ ] Given: ターン番号3、支払者"Alice"、受取者"Bob"、プロパティ"Boardwalk"、レント50
- [ ] When: RentPaidイベントを作成
- [ ] Then: payerNameが"Alice"、receiverNameが"Bob"、amountが50

---

### 1.6 GameEvent.PlayerBankrupted

#### TC-207: PlayerBankrupted初期化
- [ ] Given: ターン番号10、プレイヤー名"Alice"、最終所持金-50
- [ ] When: PlayerBankruptedイベントを作成
- [ ] Then: playerNameが"Alice"、finalMoneyが-50

---

### 1.7 GameEvent.TurnStarted

#### TC-208: TurnStarted初期化
- [ ] Given: ターン番号5、プレイヤー名"Bob"
- [ ] When: TurnStartedイベントを作成
- [ ] Then: turnNumberが5、playerNameが"Bob"

---

### 1.8 GameEvent.TurnEnded

#### TC-209: TurnEnded初期化
- [ ] Given: ターン番号5、プレイヤー名"Bob"
- [ ] When: TurnEndedイベントを作成
- [ ] Then: turnNumberが5、playerNameが"Bob"

---

### 1.9 GameEvent.GameEnded

#### TC-210: GameEnded初期化
- [ ] Given: ターン番号50、勝者"Alice"、総ターン数50
- [ ] When: GameEndedイベントを作成
- [ ] Then: winnerが"Alice"、totalTurnsが50

---

## 2. GameState（拡張）のテスト

### 2.1 イベントログの初期化

#### TC-220: eventsフィールドが初期化される
- [ ] Given: なし
- [ ] When: GameStateを作成（デフォルト引数）
- [ ] Then: events.size()が0、eventsがMutableList

---

### 2.2 イベントの追加

#### TC-221: イベントを追加できる
- [ ] Given: GameState
- [ ] When: events.add(GameStarted(...))
- [ ] Then: events.size()が1、eventsにGameStartedが含まれる

---

### 2.3 イベントの順序

#### TC-222: イベントが追加順に記録される
- [ ] Given: GameState
- [ ] When: events.add(GameStarted(...))、events.add(TurnStarted(...))、events.add(DiceRolled(...))
- [ ] Then: events[0]がGameStarted、events[1]がTurnStarted、events[2]がDiceRolled

---

## 3. GameService（拡張）のテスト

**重要**: Phase 1のGameServiceTestに以下のテストを追加します。既存テストも更新が必要です。

### 3.1 movePlayerのイベント記録

#### TC-230: movePlayer実行後、PlayerMovedイベントが記録される
- [ ] Given: 位置0のPlayer、GameState
- [ ] When: movePlayer(player, 7, gameState)
- [ ] Then: gameState.eventsにPlayerMovedイベントが追加されている、fromPositionが0、toPositionが7

#### TC-231: movePlayerでGO通過時、passedGoがtrue
- [ ] Given: 位置38のPlayer、GameState
- [ ] When: movePlayer(player, 5, gameState)
- [ ] Then: PlayerMovedイベントのpassedGoがtrue

---

### 3.2 buyPropertyのイベント記録

#### TC-232: buyProperty実行後、PropertyPurchasedイベントが記録される
- [ ] Given: 所持金$1500のPlayer、価格$200のProperty、GameState
- [ ] When: buyProperty(player, property, gameState)
- [ ] Then: gameState.eventsにPropertyPurchasedイベントが追加されている、priceが200

**Phase 1のテスト更新**:
- [ ] TC-110（Phase 1）: buyProperty呼び出しに`gameState`引数を追加

---

### 3.3 payRentのイベント記録

#### TC-233: payRent実行後、RentPaidイベントが記録される
- [ ] Given: 支払者、受取者、Property、GameState
- [ ] When: payRent(payer, receiver, rent, gameState)
- [ ] Then: gameState.eventsにRentPaidイベントが追加されている、amountが正しい

**Phase 1のテスト更新**:
- [ ] TC-120, TC-121, TC-122（Phase 1）: payRent呼び出しに`gameState`引数を追加

---

### 3.4 bankruptPlayerのイベント記録

#### TC-234: bankruptPlayer実行後、PlayerBankruptedイベントが記録される
- [ ] Given: Player、GameState
- [ ] When: bankruptPlayer(player, gameState)
- [ ] Then: gameState.eventsにPlayerBankruptedイベントが追加されている、playerNameが正しい

**Phase 1のテスト更新**:
- [ ] TC-130, TC-131（Phase 1）: bankruptPlayer呼び出しに`gameState`引数を追加

---

### 3.5 executeTurnのイベント記録

#### TC-235: executeTurn実行後、TurnStarted, DiceRolled, TurnEndedイベントが記録される
- [ ] Given: GameState
- [ ] When: executeTurn(gameState)
- [ ] Then: gameState.eventsにTurnStarted, DiceRolled, TurnEndedイベントが順に追加されている

#### TC-236: DiceRolledイベントに正しいサイコロの目が記録される
- [ ] Given: シード固定のDice、GameState
- [ ] When: executeTurn(gameState)
- [ ] Then: DiceRolledイベントのdie1, die2, totalが正しい値

---

### 3.6 runGameのイベント記録

#### TC-237: runGame実行後、GameStarted, GameEndedイベントが記録される
- [ ] Given: GameState
- [ ] When: runGame(gameState)
- [ ] Then: gameState.eventsの最初がGameStarted、最後がGameEnded

#### TC-238: GameEndedイベントに正しい勝者と総ターン数が記録される
- [ ] Given: GameState
- [ ] When: runGame(gameState)
- [ ] Then: GameEndedイベントのwinnerが破産していないプレイヤー、totalTurnsが正しい

---

## 4. ConsoleLoggerのテスト

### 4.1 イベントのフォーマット

#### TC-250: DiceRolledイベントが正しくフォーマットされる
- [ ] Given: ConsoleLogger、DiceRolledイベント
- [ ] When: logEvent(event)
- [ ] Then: 標準出力に「🎲 Alice rolled 3 + 4 = 7」のような形式で表示される（手動テストまたは標準出力キャプチャ）

#### TC-251: PropertyPurchasedイベントが正しくフォーマットされる
- [ ] Given: ConsoleLogger、PropertyPurchasedイベント
- [ ] When: logEvent(event)
- [ ] Then: 標準出力に「💰 Bob purchased Park Place for $350」のような形式で表示される

**手動テスト**: 実際にゲームを実行して、ログが見やすいか確認

---

## 6. EventLoggerのテスト

### 6.1 JSON保存

#### TC-260: イベントリストをJSON形式で保存できる
- [ ] Given: EventLogger、イベントリスト
- [ ] When: saveToJson(events, "test.json")
- [ ] Then: ファイルが作成され、JSON形式で保存されている

---

### 6.2 JSON読み込み

#### TC-261: JSONファイルからイベントリストを読み込める
- [ ] Given: EventLogger、保存済みのJSONファイル
- [ ] When: loadFromJson("test.json")
- [ ] Then: イベントリストが復元される、元のイベントと同じ内容

---

### 6.3 JSONフォーマット

#### TC-262: JSONフォーマットが正しい
- [ ] Given: EventLogger、GameStartedイベントを含むリスト
- [ ] When: saveToJson(events, "test.json")
- [ ] Then: JSONファイルを開いて確認、必要なフィールド（turnNumber, timestamp, playerNamesなど）が含まれている

---

## 7. HtmlReportGeneratorのテスト

### 7.1 HTML生成

#### TC-270: HTMLが生成される
- [ ] Given: HtmlReportGenerator、GameStateWithEvents
- [ ] When: generate()
- [ ] Then: HTML文字列が返される、<!DOCTYPE html>で始まる

---

### 7.2 HTML要素の確認

#### TC-271: HTMLに必要な要素が含まれる
- [ ] Given: HtmlReportGenerator、GameStateWithEvents
- [ ] When: generate()
- [ ] Then: HTML文字列に<h1>、<table>、イベントリストが含まれている

---

### 7.3 ファイル保存

#### TC-272: HTMLをファイルに保存できる
- [ ] Given: HtmlReportGenerator
- [ ] When: saveToFile("test.html")
- [ ] Then: ファイルが作成されている、ファイルを開いてHTML内容が正しいことを確認

---

### 7.4 手動テスト

#### TC-273: ブラウザでHTMLを表示して確認
- [ ] Given: 生成されたHTMLファイル
- [ ] When: ブラウザで開く
- [ ] Then: ゲームサマリー、プレイヤー状態テーブル、イベントタイムラインが正しく表示される

---

## 8. 統合テスト（Phase 2）

### 8.1 ゲーム全体の実行とイベント記録

#### TC-300: ゲームを実行し、イベントログが記録される
- [ ] Given: GameState（2プレイヤー）
- [ ] When: runGame(gameState)
- [ ] Then: gameState.eventsにGameStarted, TurnStarted, DiceRolled, ..., GameEndedが記録されている

---

### 8.2 ファイル出力

#### TC-301: JSON/HTMLファイルが出力される
- [ ] Given: GameState（ゲーム実行済み）
- [ ] When: EventLogger.saveToJson(), HtmlReportGenerator.saveToFile()
- [ ] Then: game-log-{timestamp}.json、game-report-{timestamp}.htmlが作成されている

---

### 8.3 手動テスト: HTMLの確認

#### TC-302: HTMLをブラウザで開き、ゲームの流れを確認
- [ ] Given: 生成されたHTMLファイル
- [ ] When: ブラウザで開く
- [ ] Then: ゲームの流れ（ターンごとの状態、イベント履歴）が確認できる、表示が見やすい

---

## Phase 1 既存テストの更新

### 更新が必要なテストケース

以下のPhase 1テストケースは、メソッドシグネチャ変更に伴い更新が必要です：

#### GameServiceTest
- [ ] TC-110: buyProperty → `buyProperty(player, property, gameState)`に変更
- [ ] TC-111: buyProperty → 同上
- [ ] TC-112: buyProperty → 同上
- [ ] TC-120: payRent → `payRent(payer, receiver, rent, gameState)`に変更
- [ ] TC-121: payRent → 同上
- [ ] TC-122: payRent → 同上
- [ ] TC-130: bankruptPlayer → `bankruptPlayer(player, gameState)`に変更
- [ ] TC-131: bankruptPlayer → 同上

#### その他のテスト
- [ ] processSpace、executeTurn、runGameを呼び出しているテストは、必要に応じてGameStateにeventsフィールドがあることを前提に更新

---

## Phase 1テストの継続性

Phase 2実装後も、**Phase 1のすべてのテスト（全46テストケース）がパスすること**を確認します。

- [ ] Phase 1のすべてのテストがパス（改修後）
- [ ] Phase 2の新規テストがすべてパス

---

## テスト実装の進め方

### ステップ1: イベント型のテスト（TC-201 ~ TC-210）
- GameEventの各型が正しく初期化されること

### ステップ2: GameStateのテスト（TC-220 ~ TC-222）
- eventsフィールドが正しく機能すること

### ステップ3: GameServiceのテスト（TC-230 ~ TC-238）
- イベント記録が各メソッドで正しく行われること
- Phase 1のテストを更新

### ステップ4: ConsoleLoggerのテスト（TC-250 ~ TC-251）
- イベントが正しくフォーマットされること

### ステップ5: EventLoggerのテスト（TC-260 ~ TC-262）
- JSON保存/読み込みが正しく機能すること

### ステップ6: HtmlReportGeneratorのテスト（TC-270 ~ TC-273）
- HTML生成が正しく機能すること
- 手動テストでブラウザ表示を確認

### ステップ7: 統合テスト（TC-300 ~ TC-302）
- ゲーム全体が正しく動作し、ファイル出力されること
- 手動テストでHTML表示を確認

---

## テストコード内のGiven-When-Then記載例

```kotlin
class GameEventTest : StringSpec({
    // TC-201: GameStarted初期化
    // Given: プレイヤー名リスト["Alice", "Bob"]
    // When: GameStartedイベントを作成
    // Then: playerNamesが正しく設定されている、turnNumberが0
    "GameStarted should be initialized with player names" {
        val event = GameEvent.GameStarted(
            turnNumber = 0,
            timestamp = System.currentTimeMillis(),
            playerNames = listOf("Alice", "Bob")
        )

        event.playerNames shouldBe listOf("Alice", "Bob")
        event.turnNumber shouldBe 0
    }
})
```

```kotlin
class GameServiceTest : StringSpec({
    // TC-230: movePlayer実行後、PlayerMovedイベントが記録される
    // Given: 位置0のPlayer、GameState
    // When: movePlayer(player, 7, gameState)
    // Then: gameState.eventsにPlayerMovedイベントが追加されている
    "movePlayer should record PlayerMoved event" {
        val player = Player("Alice", AlwaysBuyStrategy())
        val board = Board()
        val gameState = GameState(board, listOf(player))
        val gameService = GameService()

        gameService.movePlayer(player, 7, gameState)

        val movedEvent = gameState.events.filterIsInstance<GameEvent.PlayerMoved>().firstOrNull()
        movedEvent shouldNotBe null
        movedEvent?.fromPosition shouldBe 0
        movedEvent?.toPosition shouldBe 7
    }
})
```

---

## Phase 2完了チェックリスト

- [ ] Phase 1のすべてのテスト（全46テストケース）がパス
- [ ] Phase 2の新規テスト（約35テストケース）がすべてパス
- [ ] CLIでゲームログが詳細に表示される
- [ ] HTMLレポートが生成され、ブラウザで表示できる
- [ ] JSONログが保存される
- [ ] 手動テスト: HTMLをブラウザで開き、ゲームの流れを確認できる
- [ ] コードがリファクタリングされている
- [ ] Phase 1のコードが適切に改修されている

---

**作成日**: 2025-11-15
**最終更新**: 2025-11-15
