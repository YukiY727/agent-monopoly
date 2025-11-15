# Phase 1: テスト仕様書（簡易版）

**フェーズ目標**: 1ゲームを最後まで実行し、勝者を表示できる

このドキュメントは、TDDでPhase 1を実装するための簡易テストケースリストです。
各テストケースは実装時にGiven-When-Thenコメントとしてテストコードに記載します。

---

## テストケース概要

### データモデル（5カテゴリ、21ケース）
- Player: 7ケース
- Property: 4ケース
- Board: 4ケース
- GameState: 5ケース
- Dice: 1ケース

### ゲームロジック（6カテゴリ、20ケース）
- 移動処理: 3ケース
- プロパティ購入: 3ケース
- レント支払い: 3ケース
- 破産処理: 2ケース
- マス目処理: 3ケース
- ゲーム終了判定: 2ケース
- ターン実行: 2ケース
- ゲーム全体実行: 2ケース

### 戦略（1カテゴリ、2ケース）
- AlwaysBuyStrategy: 2ケース

### 統合テスト（1カテゴリ、3ケース）
- ゲーム全体の実行: 3ケース

**合計**: 46テストケース

---

## 1. データモデルのテスト

### 1.1 Player

#### TC-001: Player初期化
- [x] Given: なし
- [x] When: 新しいPlayerを作成
- [x] Then: 初期所持金が$1500、位置が0、破産フラグがfalse

#### TC-002: 所持金の増加
- [x] Given: 所持金$1500のPlayer
- [x] When: $200を追加
- [x] Then: 所持金が$1700

#### TC-003: 所持金の減少
- [x] Given: 所持金$1500のPlayer
- [x] When: $100を減らす
- [x] Then: 所持金が$1400

#### TC-004: プロパティ追加
- [x] Given: プロパティを持たないPlayer
- [x] When: Propertyを追加
- [x] Then: 所有プロパティリストに含まれる

#### TC-005: 総資産計算（プロパティあり）
- [x] Given: 所持金$1000、価格$200のプロパティ2つ所有
- [x] When: getTotalAssets()
- [x] Then: $1400を返す

#### TC-006: 総資産計算（プロパティなし）
- [x] Given: 所持金$1500、プロパティなし
- [x] When: getTotalAssets()
- [x] Then: $1500を返す

#### TC-007: 破産フラグ
- [x] Given: Player
- [x] When: 破産フラグを設定
- [x] Then: isBankrupt()がtrue

---

### 1.2 Property

#### TC-010: Property初期化
- [x] Given: なし
- [x] When: 新しいPropertyを作成（名前、位置、価格、レント指定）
- [x] Then: フィールドが正しく設定され、ownerがnull

#### TC-011: 所有者設定
- [x] Given: 未所有のProperty
- [x] When: 所有者をPlayerに設定
- [x] Then: getOwner()がそのPlayerを返す

#### TC-012: 所有判定（未所有）
- [x] Given: owner=nullのProperty
- [x] When: isOwned()
- [x] Then: false

#### TC-013: 所有判定（所有済み）
- [x] Given: ownerがPlayerのProperty
- [x] When: isOwned()
- [x] Then: true

---

### 1.3 Board

#### TC-020: Board初期化
- [x] Given: なし
- [x] When: 新しいBoardを作成
- [x] Then: getSpaceCount()が40

#### TC-021: マス取得（有効な位置）
- [x] Given: Board
- [x] When: getSpace(5)
- [x] Then: 位置5のSpaceを返す

#### TC-022: 位置0がGOマス
- [x] Given: Board
- [x] When: getSpace(0)
- [x] Then: SpaceTypeがGO

#### TC-023: 無効な位置でエラー
- [x] Given: Board
- [x] When: getSpace(-1) または getSpace(40)
- [x] Then: IllegalArgumentException

---

### 1.4 GameState

#### TC-030: GameState初期化
- [x] Given: 2人のPlayer
- [x] When: 新しいGameStateを作成
- [x] Then: プレイヤーが登録され、currentPlayerIndexが0、gameOverがfalse

#### TC-031: 現在のプレイヤー取得
- [x] Given: GameStateでcurrentPlayerIndex=1
- [x] When: getCurrentPlayer()
- [x] Then: プレイヤー2を返す

#### TC-032: 次のプレイヤーに交代
- [x] Given: GameStateでcurrentPlayerIndex=0、プレイヤー3人
- [x] When: nextPlayer()
- [x] Then: currentPlayerIndexが1

#### TC-033: 破産プレイヤーをスキップ
- [x] Given: GameStateでプレイヤー2が破産、currentPlayerIndex=0
- [x] When: nextPlayer()
- [x] Then: currentPlayerIndexが2（プレイヤー3）

#### TC-034: アクティブプレイヤー数
- [x] Given: GameStateで3人中1人が破産
- [x] When: getActivePlayerCount()
- [x] Then: 2

---

### 1.5 Dice

#### TC-040: サイコロの値範囲
- [x] Given: Dice
- [x] When: roll()を複数回実行
- [x] Then: すべての結果が2-12の範囲

---

## 2. ゲームロジックのテスト

### 2.1 移動処理（GameService.movePlayer）

#### TC-101: 通常の移動
- [x] Given: 位置0のPlayer、GameState
- [x] When: movePlayer(player, 7, gameState)
- [x] Then: プレイヤーの位置が7

#### TC-102: GO通過（$200獲得）
- [x] Given: 位置38のPlayer、所持金$1500
- [x] When: movePlayer(player, 5, gameState)
- [x] Then: 位置が3、所持金が$1700

#### TC-103: 位置の循環（mod 40）
- [x] Given: 位置39のPlayer
- [x] When: movePlayer(player, 1, gameState)
- [x] Then: 位置が0

---

### 2.2 プロパティ購入（GameService.buyProperty）

#### TC-110: 購入成功
- [x] Given: 所持金$1500のPlayer、価格$200の未所有Property
- [x] When: buyProperty(player, property)
- [x] Then: プレイヤーの所持金が$1300、propertyのownerがplayer、プレイヤーの所有プロパティに追加

#### TC-111: 購入後の所有者設定
- [x] Given: 未所有Property
- [x] When: buyProperty(player, property)
- [x] Then: property.getOwner()がplayer

#### TC-112: 購入後のプロパティリスト
- [x] Given: プロパティ0個のPlayer
- [x] When: buyProperty(player, property)
- [x] Then: player.getOwnedProperties().size()が1

---

### 2.3 レント支払い（GameService.payRent）

#### TC-120: レント支払い（通常）
- [x] Given: 支払者の所持金$1500、受取者の所持金$1000、レント$100
- [x] When: payRent(payer, receiver, 100)
- [x] Then: 支払者の所持金が$1400、受取者の所持金が$1100

#### TC-121: レント支払い後の破産
- [x] Given: 支払者の所持金$50、レント$100
- [x] When: payRent(payer, receiver, 100)
- [x] Then: 支払者の所持金が-$50、破産フラグがtrue

#### TC-122: 受取者の所持金増加
- [x] Given: 受取者の所持金$1000、レント$50
- [x] When: payRent(payer, receiver, 50)
- [x] Then: 受取者の所持金が$1050

---

### 2.4 破産処理（GameService.bankruptPlayer）

#### TC-130: 破産フラグ設定
- [x] Given: Player
- [x] When: bankruptPlayer(player)
- [x] Then: player.isBankrupt()がtrue

#### TC-131: 所有プロパティの解放
- [x] Given: 2つのプロパティを所有するPlayer
- [x] When: bankruptPlayer(player)
- [x] Then: 各プロパティのownerがnull、プレイヤーの所有プロパティリストが空

---

### 2.5 マス目処理（GameService.processSpace）

#### TC-140: 未所有プロパティに止まる（購入する）
- [x] Given: 所持金$1500のPlayer、価格$200の未所有Property、AlwaysBuyStrategy
- [x] When: processSpace(player, gameState)（playerが該当Propertyの位置）
- [x] Then: プロパティが購入される

#### TC-141: 他プレイヤーのプロパティに止まる（レント支払い）
- [x] Given: Player A（所持金$1500）、Player Bが所有するProperty（レント$50）、Player Aがその位置
- [x] When: processSpace(playerA, gameState)
- [x] Then: Player Aの所持金が$1450、Player Bの所持金が増加

#### TC-142: 自分のプロパティに止まる（何も起きない）
- [x] Given: Player A、Player Aが所有するProperty、Player Aがその位置
- [x] When: processSpace(playerA, gameState)
- [x] Then: 何も変化しない

---

### 2.6 ゲーム終了判定（GameService.checkGameEnd）

#### TC-150: ゲーム終了（1人残り）
- [x] Given: GameStateで3人中2人が破産
- [x] When: checkGameEnd(gameState)
- [x] Then: true

#### TC-151: ゲーム継続中（複数人アクティブ）
- [x] Given: GameStateで3人中1人が破産
- [x] When: checkGameEnd(gameState)
- [x] Then: false

---

### 2.7 ターン実行（GameService.executeTurn）

#### TC-160: 1ターンの流れ
- [x] Given: GameState
- [x] When: executeTurn(gameState)
- [x] Then: サイコロが振られ、プレイヤーが移動し、マス目処理が実行され、ターン番号が増加

#### TC-161: ターン後のプレイヤー交代
- [x] Given: GameStateでcurrentPlayerIndex=0、2人プレイヤー
- [x] When: executeTurn(gameState)
- [x] Then: currentPlayerIndexが1

---

### 2.8 ゲーム全体実行（GameService.runGame）

#### TC-170: ゲーム完走
- [x] Given: GameState（2プレイヤー）
- [x] When: runGame(gameState)
- [x] Then: ゲームが終了し、勝者が返される

#### TC-171: 勝者が破産していない
- [x] Given: GameState
- [x] When: runGame(gameState)
- [x] Then: 返された勝者のisBankrupt()がfalse

---

## 3. 戦略のテスト

### 3.1 AlwaysBuyStrategy

#### TC-200: 所持金が足りる場合は購入
- [x] Given: AlwaysBuyStrategy、所持金$1500のPlayer、価格$200のProperty
- [x] When: shouldBuyProperty(player, property, gameState)
- [x] Then: true

#### TC-201: 所持金が足りない場合は購入しない
- [x] Given: AlwaysBuyStrategy、所持金$100のPlayer、価格$200のProperty
- [x] When: shouldBuyProperty(player, property, gameState)
- [x] Then: false

---

## 4. 統合テスト

### 4.1 ゲーム全体の実行

#### TC-300: 2プレイヤーでゲーム完走
- [x] Given: 2人のPlayer、Board、GameState
- [x] When: runGame(gameState)
- [x] Then: ゲームが最後まで実行され、勝者が決定される

#### TC-301: 勝者の妥当性
- [x] Given: GameState
- [x] When: runGame(gameState)
- [x] Then: 勝者は破産しておらず、アクティブプレイヤーの1人

#### TC-302: 複数回実行でランダム性確認
- [x] Given: 同じ初期条件のGameState
- [x] When: runGame()を3回実行（異なるシード）
- [x] Then: 勝者が異なる場合がある（ランダム性が機能）

---

## テスト実装の進め方

### ステップ1: データモデル（TC-001 ~ TC-040）
- Player → Property → Board → GameState → Dice の順

### ステップ2: ゲームロジック（TC-101 ~ TC-171）
- 移動 → 購入 → レント → 破産 → マス目処理 → 終了判定 → ターン → ゲーム全体

### ステップ3: 戦略（TC-200 ~ TC-201）
- AlwaysBuyStrategy

### ステップ4: 統合テスト（TC-300 ~ TC-302）
- ゲーム全体の実行

---

## テストコード内のGiven-When-Then記載例

```kotlin
class PlayerTest : StringSpec({
    // TC-001: Player初期化
    // Given: なし
    // When: 新しいPlayerを作成
    // Then: 初期所持金が$1500、位置が0、破産フラグがfalse
    "player should be initialized with $1500, position 0, and not bankrupt" {
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())

        player.money shouldBe 1500
        player.position shouldBe 0
        player.isBankrupt() shouldBe false
    }
})
```

---

## Phase 1完了チェックリスト

- [x] 全46テストケースがパス
- [x] CLIで2プレイヤーのゲームが最後まで実行される
- [x] 勝者が表示される
- [ ] コードがリファクタリングされている（オブジェクト指向エクササイズ9つのルール適用中）

---

**作成日**: 2025-11-12
**最終更新**: 2025-11-15
