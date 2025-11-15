# Phase 2: ドメイン用語メモ

**目的**: Phase 2実装前に追加される用語を整理（15分で完了）

このメモは詳細設計ではありません。実装しながら進化させます。

---

## Phase 2で追加される概念

### イベント駆動アーキテクチャ
ゲーム内のすべてのアクション（サイコロ、移動、購入など）を**イベント**として記録し、後から可視化・分析できるようにする設計

---

## 新規追加エンティティ・値オブジェクト

### GameEvent (sealed class)
ゲーム内で発生するすべてのイベントを型安全に表現

**主要なイベント型**:
- `GameStarted`: ゲーム開始
- `GameEnded`: ゲーム終了
- `TurnStarted`: ターン開始
- `TurnEnded`: ターン終了
- `DiceRolled`: サイコロを振った
- `PlayerMoved`: プレイヤー移動（GO通過含む）
- `PropertyPurchased`: プロパティ購入
- `RentPaid`: レント支払い
- `PlayerBankrupted`: 破産

**共通フィールド**:
- `turnNumber: Int` - どのターンで発生したか
- `timestamp: Long` - いつ発生したか（System.currentTimeMillis()）

---

## 新規サービス

### EventRecorder（オプション、必要に応じて導入）
イベントをGameStateに記録する責務を持つ

- `record(gameState, event)`: イベントを記録
- `getEvents(gameState)`: イベント一覧を取得

**設計判断**:
- 初期段階では不要かもしれない（GameServiceに直接組み込む）
- イベント記録処理が複雑になったら導入

---

## 新規インフラストラクチャ層

Phase 2では新たに`infrastructure`パッケージを導入します。

### ConsoleLogger
ゲームの進行をCLIに表示

- `logEvent(event)`: イベントを読みやすくフォーマットして表示
- ANSI カラーコードや記号を使用して見やすく

### EventLogger
イベントログをJSON形式で保存・読み込み

- `saveToJson(events, filePath)`: イベントをJSON形式で保存
- `loadFromJson(filePath)`: JSONからイベントを読み込み

### HtmlReportGenerator
イベントログからHTML形式のレポートを生成

- `generate()`: HTMLを生成
- `saveToFile(filePath)`: ファイルに保存
- `appendGameSummary()`: ゲームサマリーを追加
- `appendEventTimeline()`: イベントタイムラインを追加

---

## Phase 1エンティティの拡張

### GameState（拡張）
イベントログを保持するフィールドを追加

**新規フィールド**:
- `val events: MutableList<GameEvent> = mutableListOf()`

**変更理由**:
- ゲーム進行のすべてのイベントを記録するため
- 後からゲームの流れを再生・分析できるようにするため

---

### GameService（拡張）
各メソッドにイベント記録処理を追加

**変更されるメソッド**:
- `movePlayer`: `PlayerMoved`イベントを記録
- `buyProperty`: `PropertyPurchased`イベントを記録（シグネチャ変更: `gameState`引数追加）
- `payRent`: `RentPaid`イベントを記録（シグネチャ変更）
- `bankruptPlayer`: `PlayerBankrupted`イベントを記録（シグネチャ変更）
- `executeTurn`: `TurnStarted`, `DiceRolled`, `TurnEnded`イベントを記録
- `runGame`: `GameStarted`, `GameEnded`イベントを記録

**重要**: Phase 1のメソッドシグネチャを柔軟に変更する

---

### Dice（拡張）
最後のサイコロ結果を記録

**新規フィールド**:
- `private var lastDie1: Int`
- `private var lastDie2: Int`

**新規メソッド**:
- `getLastRoll(): Pair<Int, Int>` - 最後に振ったサイコロの目を取得

**変更理由**:
- `DiceRolled`イベントに個別のサイコロの目（die1, die2）を含めるため

---

## パッケージ構成（Phase 2拡張版）

```
com.monopoly
├── domain
│   ├── model
│   │   ├── Player           (Phase 1)
│   │   ├── Property         (Phase 1)
│   │   ├── Board            (Phase 1)
│   │   ├── GameState        (Phase 1 → 拡張)
│   │   └── GameEvent        (Phase 2 新規)
│   ├── service
│   │   ├── GameService      (Phase 1 → 拡張)
│   │   ├── Dice             (Phase 1 → 拡張)
│   │   └── EventRecorder    (Phase 2 新規、オプション)
│   └── strategy
│       ├── Strategy         (Phase 1)
│       └── AlwaysBuyStrategy (Phase 1)
├── infrastructure           (Phase 2 新規パッケージ)
│   ├── logging
│   │   ├── ConsoleLogger    (Phase 2 新規)
│   │   └── EventLogger      (Phase 2 新規)
│   └── reporting
│       └── HtmlReportGenerator (Phase 2 新規)
└── cli
    └── MonopolyGame         (Phase 1 → 拡張)
```

---

## 用語の定義

### イベント (Event)
ゲーム内で発生した「起こったこと」を表すデータ。過去形で命名（例: `DiceRolled`, `PlayerMoved`）

### イベントログ (Event Log)
ゲーム開始から終了までのすべてのイベントを時系列で記録したリスト

### タイムライン (Timeline)
イベントを時間順に並べた表示。HTMLレポートで使用

### ターン番号 (Turn Number)
ゲーム開始時を0として、各ターンに割り当てられる連番。すべてのイベントに含まれる

### タイムスタンプ (Timestamp)
イベントが発生した時刻をミリ秒で記録（`System.currentTimeMillis()`）

---

## Phase 2での設計進化のポイント

### イベント記録の一貫性
すべてのゲームアクションで必ずイベントを記録する。忘れるとログに穴が空く。

### 柔軟なリファクタリング
Phase 1のコードを柔軟に書き換える。後方互換性よりも正しい設計を優先。

### シンプルさの維持
Phase 2では凝ったデザインやリッチなUIは不要。シンプルで見やすいログ表示とHTMLを目指す。

---

## 実装方針

1. **イベント型定義から開始**: まず`GameEvent` sealed classを定義
2. **GameStateを拡張**: `events`フィールドを追加
3. **GameServiceを改修**: 各メソッドにイベント記録を組み込む
4. **Phase 1のテストを更新**: シグネチャ変更に伴いテストを修正
5. **新規機能を追加**: ConsoleLogger, EventLogger, HtmlReportGenerator
6. **統合テスト**: 全体が動作することを確認

---

## 次のアクション

**今すぐTDD開始**:
1. TC-201: GameEvent.GameStartedのテストを書く
2. 最小実装
3. TC-202へ進む

詳細設計はテストを書きながら決める。Phase 1のコードも柔軟に書き換える。

---

**作成日**: 2025-11-15
