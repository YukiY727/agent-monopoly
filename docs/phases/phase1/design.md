# Phase 1 詳細設計（TDD版）

**フェーズ目標**: 1ゲームを最後まで実行し、勝者を表示できる

このドキュメントは、TDD（Test-Driven Development）でPhase 1を実装するための設計指針です。

---

## TDD開発の流れ

各機能について以下のサイクルを回します：

1. **Red**: テストを書く（失敗する）
2. **Green**: テストが通る最小限の実装
3. **Refactor**: コードをきれいにする

---

## パッケージ構造（高レベル設計）

```
com.monopoly
├── domain           # ドメイン層（ゲームロジック）
│   ├── model       # データモデル
│   ├── service     # ゲームロジック
│   └── strategy    # 戦略パターン
└── cli             # CLIエントリーポイント
```

---

## クラス設計と責務

### ドメインモデル層（com.monopoly.domain.model）

#### 1. Player
**責務**: プレイヤーの状態を保持

**主要なフィールド**:
- `String name`: プレイヤー名
- `int position`: 現在位置（0-39）
- `int money`: 所持金
- `List<Property> ownedProperties`: 所有プロパティ
- `boolean bankrupt`: 破産フラグ
- `Strategy strategy`: 戦略

**主要なメソッド**:
- `getMoney()`: 所持金を取得
- `addMoney(int amount)`: 所持金を増やす
- `subtractMoney(int amount)`: 所持金を減らす
- `isBankrupt()`: 破産しているか
- `getTotalAssets()`: 総資産を計算

---

#### 2. Property
**責務**: プロパティ（購入可能な土地）の情報を保持

**主要なフィールド**:
- `String name`: プロパティ名
- `int position`: 位置
- `int price`: 購入価格
- `int rent`: レント（家なし）
- `ColorGroup colorGroup`: 色グループ
- `Player owner`: 所有者（null = 未所有）

**主要なメソッド**:
- `getPrice()`: 購入価格を取得
- `getRent()`: レントを取得
- `isOwned()`: 所有されているか
- `setOwner(Player owner)`: 所有者を設定

---

#### 3. Board
**責務**: ボード（40マス）を管理

**主要なフィールド**:
- `List<Space> spaces`: マス目のリスト（40マス）

**主要なメソッド**:
- `getSpace(int position)`: 指定位置のマスを取得
- `getSpaceCount()`: マス数を取得

---

#### 4. Space インターフェース
**責務**: マス目の共通インターフェース

**主要なメソッド**:
- `String getName()`: マス名を取得
- `int getPosition()`: 位置を取得
- `SpaceType getType()`: マスの種類を取得

**具象クラス**:
- `Go`: GOマス
- `Property`: プロパティマス（Spaceを実装）
- その他（Phase 1では最小限）

---

#### 5. GameState
**責務**: ゲームの状態を管理

**主要なフィールド**:
- `Board board`: ボード
- `List<Player> players`: プレイヤーリスト
- `int currentPlayerIndex`: 現在のプレイヤーインデックス
- `int turnNumber`: ターン番号
- `boolean gameOver`: ゲーム終了フラグ

**主要なメソッド**:
- `getCurrentPlayer()`: 現在のプレイヤーを取得
- `nextPlayer()`: 次のプレイヤーに交代
- `isGameOver()`: ゲーム終了か
- `getActivePlayerCount()`: 破産していないプレイヤー数

---

### 戦略層（com.monopoly.domain.strategy）

#### 6. Strategy インターフェース
**責務**: プレイヤーの戦略を抽象化

**主要なメソッド**:
- `boolean shouldBuyProperty(Player player, Property property, GameState gameState)`: プロパティを購入すべきか
- `String getName()`: 戦略名を取得

**具象クラス**:
- `AlwaysBuyStrategy`: 常に購入する戦略（所持金が足りる場合）

---

### サービス層（com.monopoly.domain.service）

#### 7. Dice
**責務**: サイコロを振る

**主要なメソッド**:
- `int roll()`: 2つのサイコロを振り、合計値を返す（2-12）

**テスタビリティ**:
- シード指定可能なコンストラクタ `Dice(long seed)` を提供

---

#### 8. GameService
**責務**: ゲームロジックを提供

**主要なメソッド**:
- `void movePlayer(Player player, int diceValue, GameState gameState)`: プレイヤーを移動
- `void processSpace(Player player, GameState gameState)`: マス目処理
- `void buyProperty(Player player, Property property)`: プロパティ購入
- `void payRent(Player payer, Player receiver, int rent)`: レント支払い
- `void bankruptPlayer(Player player)`: 破産処理
- `boolean checkGameEnd(GameState gameState)`: ゲーム終了判定
- `void executeTurn(GameState gameState)`: 1ターン実行
- `Player runGame(GameState gameState)`: ゲーム全体を実行

---

### CLI層（com.monopoly.cli）

#### 9. MonopolyGame
**責務**: CLIエントリーポイント

**主要なメソッド**:
- `public static void main(String[] args)`: メイン関数
- `private static void displayResult(Player winner, GameState gameState)`: 結果表示

---

## テストケースリスト

### データモデルのテスト

#### Playerのテスト
- [ ] プレイヤーが初期化され、初期所持金が1500であること
- [ ] プレイヤーが初期位置0（GO）にいること
- [ ] プレイヤーが破産していないこと
- [ ] 所持金を増やせること
- [ ] 所持金を減らせること
- [ ] プロパティを追加できること
- [ ] 総資産が正しく計算されること（所持金 + プロパティ価値）

#### Propertyのテスト
- [ ] プロパティが初期化され、価格が正しく設定されていること
- [ ] プロパティが最初は未所有であること
- [ ] 所有者を設定できること
- [ ] 所有されているか判定できること

#### Boardのテスト
- [ ] ボードが40マス持つこと
- [ ] 指定位置のマスを取得できること
- [ ] 位置0がGOマスであること
- [ ] 無効な位置でエラーが発生すること

#### GameStateのテスト
- [ ] ゲーム状態が初期化され、プレイヤーが登録されること
- [ ] 現在のプレイヤーを取得できること
- [ ] 次のプレイヤーに交代できること
- [ ] 破産したプレイヤーをスキップすること
- [ ] 破産していないプレイヤー数を取得できること
- [ ] プレイヤー数が2-4人でないとエラーになること

---

### ゲームロジックのテスト

#### Diceのテスト
- [ ] サイコロの値が2-12の範囲であること
- [ ] シード固定で同じ結果が得られること

#### GameServiceのテスト

**移動処理**:
- [ ] プレイヤーが正しく移動すること
- [ ] GO通過で200もらえること（位置が一周した場合）
- [ ] 位置が0-39の範囲に収まること（mod 40）

**プロパティ購入**:
- [ ] プロパティを購入すると所持金が減ること
- [ ] 所有者が設定されること
- [ ] プレイヤーの所有プロパティに追加されること

**レント支払い**:
- [ ] レント支払い後、支払者の所持金が減ること
- [ ] レント受取者の所持金が増えること
- [ ] 所持金が負になったら破産処理が呼ばれること

**破産処理**:
- [ ] 破産フラグが立つこと
- [ ] 所有プロパティがすべて解放されること（owner = null）

**マス目処理**:
- [ ] 未所有プロパティに止まったら購入判定が行われること
- [ ] 他プレイヤーのプロパティに止まったらレント支払いが行われること
- [ ] 自分のプロパティに止まっても何も起きないこと

**ゲーム終了判定**:
- [ ] 破産していないプレイヤーが1人以下でゲーム終了すること
- [ ] ターン数が1000を超えたらゲーム終了すること（無限ループ防止）

**ターン実行**:
- [ ] 1ターンでサイコロ→移動→マス目処理→終了判定が行われること
- [ ] ターン後、次のプレイヤーに交代すること

**ゲーム全体実行**:
- [ ] ゲームが最後まで実行され、勝者が返されること
- [ ] 勝者は破産していないこと
- [ ] 無限ループにならないこと

---

### 戦略のテスト

#### AlwaysBuyStrategyのテスト
- [ ] 所持金が足りる場合、購入を選択すること
- [ ] 所持金が足りない場合、購入しないこと

---

### 統合テスト

#### ゲーム全体の実行テスト
- [ ] 2プレイヤーでゲームが最後まで実行されること
- [ ] 勝者が決定されること
- [ ] 複数回実行して結果が変わること（ランダム性の確認）

---

## TDD実装順序

### ステップ1: データモデル
1. **Player**
   - テスト: 初期化、所持金操作、破産フラグ
   - 実装: フィールドとゲッター/セッター

2. **Property**
   - テスト: 初期化、所有者設定
   - 実装: フィールドとゲッター/セッター

3. **Space インターフェース、Go、ColorGroup enum**
   - テスト: インターフェース実装の確認
   - 実装: インターフェースと基本実装

4. **Board**
   - テスト: 40マス、位置指定でマス取得
   - 実装: マスの初期化（Phase 1では簡略化）

5. **GameState**
   - テスト: 初期化、プレイヤー管理、ターン管理
   - 実装: フィールドとゲッター/セッター

---

### ステップ2: 戦略システム
6. **Strategy インターフェース**
   - テスト: インターフェース定義の確認
   - 実装: インターフェース

7. **AlwaysBuyStrategy**
   - テスト: 購入判定ロジック
   - 実装: shouldBuyPropertyの実装

---

### ステップ3: ゲームロジック（シンプルな順）
8. **Dice**
   - テスト: サイコロの値範囲、シード固定
   - 実装: roll()メソッド

9. **GameService - 移動処理**
   - テスト: 移動、GO通過
   - 実装: movePlayer()

10. **GameService - プロパティ購入**
    - テスト: 購入処理
    - 実装: buyProperty()

11. **GameService - レント支払い**
    - テスト: レント処理、破産トリガー
    - 実装: payRent()

12. **GameService - 破産処理**
    - テスト: 破産フラグ、プロパティ解放
    - 実装: bankruptPlayer()

13. **GameService - マス目処理**
    - テスト: プロパティマスでの購入/レント
    - 実装: processSpace()

14. **GameService - ゲーム終了判定**
    - テスト: 1人残り、最大ターン
    - 実装: checkGameEnd()

15. **GameService - ターン実行**
    - テスト: 1ターンの流れ
    - 実装: executeTurn()

16. **GameService - ゲーム全体実行**
    - テスト: ゲーム完走、勝者決定
    - 実装: runGame()

---

### ステップ4: CLI
17. **MonopolyGame**
    - テスト: 手動テスト（実行して勝者表示）
    - 実装: main()とdisplayResult()

---

### ステップ5: 統合テスト
18. **ゲーム全体の統合テスト**
    - テスト: 複数回実行、結果の妥当性確認
    - 実装: 統合テストコード

---

## 実装ガイドライン

### Javaの慣習
- パッケージ命名: 小文字のみ（`com.monopoly.domain.model`）
- クラス命名: PascalCase（`GameService`）
- メソッド命名: camelCase（`shouldBuyProperty`）
- 定数命名: UPPER_SNAKE_CASE（`MAX_PLAYERS`）

### 防御的プログラミング
- コレクションを返す場合は防御的コピー（`new ArrayList<>(originalList)`）
- 不変性を重視（できるだけ `final` を使用）

### テスタビリティ
- 依存をコンストラクタで注入（DI）
- `Dice` はシード指定可能にする（テストで決定的な結果を得るため）
- Mockitoでモック化可能な設計

### Phase 1の簡略化
- **ボード構成**: 全40マスを実装せず、一部のプロパティのみ実装してもOK
- **マス目の種類**: Phase 1ではプロパティとGOのみ実装、税金やチャンスは無視
- **ゾロ目**: Phase 1では無視（サイコロの合計値のみ使用）

---

## 次のアクション

1. **環境セットアップ**
   - Java 21+ インストール確認
   - Gradle プロジェクト初期化（`gradle init`）
   - JUnit 5, AssertJ, Mockito 依存関係追加

2. **TDD開発開始**
   - ステップ1から順に実装
   - 各ステップで Red → Green → Refactor サイクル
   - テストがすべてパスしたら次のステップへ

3. **動作確認**
   - 全テストがパスすることを確認
   - CLIでゲームを実行し、勝者が表示されることを確認

---

## Phase 1完了の定義

以下がすべて満たされたらPhase 1完了：

- ✅ すべてのユニットテストがパス
- ✅ 統合テストがパス
- ✅ CLIで2プレイヤーのゲームが最後まで実行される
- ✅ 勝者が表示される
- ✅ コードがリファクタリングされ、きれいな状態

---

**作成日**: 2025-11-11
**最終更新**: 2025-11-11
