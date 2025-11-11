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
- [ ] Given: なし
- [ ] When: 新しいPlayerを作成
- [ ] Then: 初期所持金が$1500、位置が0、破産フラグがfalse

#### TC-002: 所持金の増加
- [ ] Given: 所持金$1500のPlayer
- [ ] When: $200を追加
- [ ] Then: 所持金が$1700

#### TC-003: 所持金の減少
- [ ] Given: 所持金$1500のPlayer
- [ ] When: $100を減らす
- [ ] Then: 所持金が$1400

#### TC-004: プロパティ追加
- [ ] Given: プロパティを持たないPlayer
- [ ] When: Propertyを追加
- [ ] Then: 所有プロパティリストに含まれる

#### TC-005: 総資産計算（プロパティあり）
- [ ] Given: 所持金$1000、価格$200のプロパティ2つ所有
- [ ] When: getTotalAssets()
- [ ] Then: $1400を返す

#### TC-006: 総資産計算（プロパティなし）
- [ ] Given: 所持金$1500、プロパティなし
- [ ] When: getTotalAssets()
- [ ] Then: $1500を返す

#### TC-007: 破産フラグ
- [ ] Given: Player
- [ ] When: 破産フラグを設定
- [ ] Then: isBankrupt()がtrue

---

### 1.2 Property

#### TC-010: Property初期化
- [ ] Given: なし
- [ ] When: 新しいPropertyを作成（名前、位置、価格、レント指定）
- [ ] Then: フィールドが正しく設定され、ownerがnull

#### TC-011: 所有者設定
- [ ] Given: 未所有のProperty
- [ ] When: 所有者をPlayerに設定
- [ ] Then: getOwner()がそのPlayerを返す

#### TC-012: 所有判定（未所有）
- [ ] Given: owner=nullのProperty
- [ ] When: isOwned()
- [ ] Then: false

#### TC-013: 所有判定（所有済み）
- [ ] Given: ownerがPlayerのProperty
- [ ] When: isOwned()
- [ ] Then: true

---

### 1.3 Board

#### TC-020: Board初期化
- [ ] Given: なし
- [ ] When: 新しいBoardを作成
- [ ] Then: getSpaceCount()が40

#### TC-021: マス取得（有効な位置）
- [ ] Given: Board
- [ ] When: getSpace(5)
- [ ] Then: 位置5のSpaceを返す

#### TC-022: 位置0がGOマス
- [ ] Given: Board
- [ ] When: getSpace(0)
- [ ] Then: SpaceTypeがGO

#### TC-023: 無効な位置でエラー
- [ ] Given: Board
- [ ] When: getSpace(-1) または getSpace(40)
- [ ] Then: IllegalArgumentException

---

### 1.4 GameState

#### TC-030: GameState初期化
- [ ] Given: 2人のPlayer
- [ ] When: 新しいGameStateを作成
- [ ] Then: プレイヤーが登録され、currentPlayerIndexが0、gameOverがfalse

#### TC-031: 現在のプレイヤー取得
- [ ] Given: GameStateでcurrentPlayerIndex=1
- [ ] When: getCurrentPlayer()
- [ ] Then: プレイヤー2を返す

#### TC-032: 次のプレイヤーに交代
- [ ] Given: GameStateでcurrentPlayerIndex=0、プレイヤー3人
- [ ] When: nextPlayer()
- [ ] Then: currentPlayerIndexが1

#### TC-033: 破産プレイヤーをスキップ
- [ ] Given: GameStateでプレイヤー2が破産、currentPlayerIndex=0
- [ ] When: nextPlayer()
- [ ] Then: currentPlayerIndexが2（プレイヤー3）

#### TC-034: アクティブプレイヤー数
- [ ] Given: GameStateで3人中1人が破産
- [ ] When: getActivePlayerCount()
- [ ] Then: 2

---

### 1.5 Dice

#### TC-040: サイコロの値範囲
- [ ] Given: Dice
- [ ] When: roll()を複数回実行
- [ ] Then: すべての結果が2-12の範囲

---

## 2. ゲームロジックのテスト

### 2.1 移動処理（GameService.movePlayer）

#### TC-101: 通常の移動
- [ ] Given: 位置0のPlayer、GameState
- [ ] When: movePlayer(player, 7, gameState)
- [ ] Then: プレイヤーの位置が7

#### TC-102: GO通過（$200獲得）
- [ ] Given: 位置38のPlayer、所持金$1500
- [ ] When: movePlayer(player, 5, gameState)
- [ ] Then: 位置が3、所持金が$1700

#### TC-103: 位置の循環（mod 40）
- [ ] Given: 位置39のPlayer
- [ ] When: movePlayer(player, 1, gameState)
- [ ] Then: 位置が0

---

### 2.2 プロパティ購入（GameService.buyProperty）

#### TC-110: 購入成功
- [ ] Given: 所持金$1500のPlayer、価格$200の未所有Property
- [ ] When: buyProperty(player, property)
- [ ] Then: プレイヤーの所持金が$1300、propertyのownerがplayer、プレイヤーの所有プロパティに追加

#### TC-111: 購入後の所有者設定
- [ ] Given: 未所有Property
- [ ] When: buyProperty(player, property)
- [ ] Then: property.getOwner()がplayer

#### TC-112: 購入後のプロパティリスト
- [ ] Given: プロパティ0個のPlayer
- [ ] When: buyProperty(player, property)
- [ ] Then: player.getOwnedProperties().size()が1

---

### 2.3 レント支払い（GameService.payRent）

#### TC-120: レント支払い（通常）
- [ ] Given: 支払者の所持金$1500、受取者の所持金$1000、レント$100
- [ ] When: payRent(payer, receiver, 100)
- [ ] Then: 支払者の所持金が$1400、受取者の所持金が$1100

#### TC-121: レント支払い後の破産
- [ ] Given: 支払者の所持金$50、レント$100
- [ ] When: payRent(payer, receiver, 100)
- [ ] Then: 支払者の所持金が-$50、破産フラグがtrue

#### TC-122: 受取者の所持金増加
- [ ] Given: 受取者の所持金$1000、レント$50
- [ ] When: payRent(payer, receiver, 50)
- [ ] Then: 受取者の所持金が$1050

---

### 2.4 破産処理（GameService.bankruptPlayer）

#### TC-130: 破産フラグ設定
- [ ] Given: Player
- [ ] When: bankruptPlayer(player)
- [ ] Then: player.isBankrupt()がtrue

#### TC-131: 所有プロパティの解放
- [ ] Given: 2つのプロパティを所有するPlayer
- [ ] When: bankruptPlayer(player)
- [ ] Then: 各プロパティのownerがnull、プレイヤーの所有プロパティリストが空

---

### 2.5 マス目処理（GameService.processSpace）

#### TC-140: 未所有プロパティに止まる（購入する）
- [ ] Given: 所持金$1500のPlayer、価格$200の未所有Property、AlwaysBuyStrategy
- [ ] When: processSpace(player, gameState)（playerが該当Propertyの位置）
- [ ] Then: プロパティが購入される

#### TC-141: 他プレイヤーのプロパティに止まる（レント支払い）
- [ ] Given: Player A（所持金$1500）、Player Bが所有するProperty（レント$50）、Player Aがその位置
- [ ] When: processSpace(playerA, gameState)
- [ ] Then: Player Aの所持金が$1450、Player Bの所持金が増加

#### TC-142: 自分のプロパティに止まる（何も起きない）
- [ ] Given: Player A、Player Aが所有するProperty、Player Aがその位置
- [ ] When: processSpace(playerA, gameState)
- [ ] Then: 何も変化しない

---

### 2.6 ゲーム終了判定（GameService.checkGameEnd）

#### TC-150: ゲーム終了（1人残り）
- [ ] Given: GameStateで3人中2人が破産
- [ ] When: checkGameEnd(gameState)
- [ ] Then: true

#### TC-151: ゲーム継続中（複数人アクティブ）
- [ ] Given: GameStateで3人中1人が破産
- [ ] When: checkGameEnd(gameState)
- [ ] Then: false

---

### 2.7 ターン実行（GameService.executeTurn）

#### TC-160: 1ターンの流れ
- [ ] Given: GameState
- [ ] When: executeTurn(gameState)
- [ ] Then: サイコロが振られ、プレイヤーが移動し、マス目処理が実行され、ターン番号が増加

#### TC-161: ターン後のプレイヤー交代
- [ ] Given: GameStateでcurrentPlayerIndex=0、2人プレイヤー
- [ ] When: executeTurn(gameState)
- [ ] Then: currentPlayerIndexが1

---

### 2.8 ゲーム全体実行（GameService.runGame）

#### TC-170: ゲーム完走
- [ ] Given: GameState（2プレイヤー）
- [ ] When: runGame(gameState)
- [ ] Then: ゲームが終了し、勝者が返される

#### TC-171: 勝者が破産していない
- [ ] Given: GameState
- [ ] When: runGame(gameState)
- [ ] Then: 返された勝者のisBankrupt()がfalse

---

## 3. 戦略のテスト

### 3.1 AlwaysBuyStrategy

#### TC-200: 所持金が足りる場合は購入
- [ ] Given: AlwaysBuyStrategy、所持金$1500のPlayer、価格$200のProperty
- [ ] When: shouldBuyProperty(player, property, gameState)
- [ ] Then: true

#### TC-201: 所持金が足りない場合は購入しない
- [ ] Given: AlwaysBuyStrategy、所持金$100のPlayer、価格$200のProperty
- [ ] When: shouldBuyProperty(player, property, gameState)
- [ ] Then: false

---

## 4. 統合テスト

### 4.1 ゲーム全体の実行

#### TC-300: 2プレイヤーでゲーム完走
- [ ] Given: 2人のPlayer、Board、GameState
- [ ] When: runGame(gameState)
- [ ] Then: ゲームが最後まで実行され、勝者が決定される

#### TC-301: 勝者の妥当性
- [ ] Given: GameState
- [ ] When: runGame(gameState)
- [ ] Then: 勝者は破産しておらず、アクティブプレイヤーの1人

#### TC-302: 複数回実行でランダム性確認
- [ ] Given: 同じ初期条件のGameState
- [ ] When: runGame()を3回実行（異なるシード）
- [ ] Then: 勝者が異なる場合がある（ランダム性が機能）

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

- [ ] 全46テストケースがパス
- [ ] CLIで2プレイヤーのゲームが最後まで実行される
- [ ] 勝者が表示される
- [ ] コードがリファクタリングされている

---

**作成日**: 2025-11-12
**最終更新**: 2025-11-12
