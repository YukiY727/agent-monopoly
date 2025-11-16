# Phase 10: 高度な戦略実装 - 実装計画

## 目標

より洗練された戦略を実装し、戦略の多様性を提供する。

## 現状分析

### 既存の戦略

1. **AlwaysBuyStrategy**: 常に購入する
2. **RandomStrategy**: ランダムに購入を決定
3. **ConservativeStrategy**: 一定額以上の現金を保持

### 課題

現在のBuyStrategyインターフェースは以下の情報しか提供していない:
- プロパティ
- 現在の所持金

高度な戦略を実装するには、以下の情報が必要:
- プレイヤーの所有プロパティ一覧
- ボード全体の構成（カラーグループ情報）
- 他のプレイヤーの所有状況
- ゲームの進行状況（ターン数など）

## 実装する戦略

### 必須戦略（Phase 10で実装）

1. **MonopolyFirstStrategy**（セット重視戦略）
   - カラーグループの独占を優先
   - 既に所有しているカラーの物件を優先的に購入
   - 独占完成が近いグループを重視

2. **ROIStrategy**（投資効率戦略）
   - プロパティのROI（投資収益率）を計算
   - 価格に対してレントが高い物件を優先
   - 長期的な収益性を重視

3. **LowPriceStrategy**（低価格優先戦略）
   - 安いプロパティから優先的に購入
   - 早期に多くのプロパティを確保
   - 現金を温存しやすい

4. **HighValueStrategy**（高収益優先戦略）
   - レントが高いプロパティを優先
   - 高価でも収益性が高ければ購入
   - リスクを取って高いリターンを狙う

5. **BalancedStrategy**（バランス型戦略）
   - 複数の要因を総合的に判断
   - ROI、カラーグループ、価格をスコアリング
   - 中庸的な戦略

6. **AggressiveStrategy**（積極戦略）
   - 他のプレイヤーの独占を阻止
   - 相手が欲しがるプロパティを優先
   - 戦略的妨害を重視

### 追加戦略候補

7. **LocationBasedStrategy**（位置ベース戦略）
   - 統計的に止まりやすい位置のプロパティを優先
   - オレンジ、赤などの中盤エリアを重視

8. **EarlyGameStrategy**（序盤特化戦略）
   - ゲーム序盤は安い物件を多く購入
   - 中盤以降は慎重に

9. **LateGameStrategy**（終盤特化戦略）
   - 序盤は慎重
   - 終盤に攻勢をかける

10. **AdaptiveStrategy**（適応型戦略）
    - 自分の状況に応じて戦略を変更
    - リードしている時は保守的、劣勢の時は積極的

## 設計方針

### インターフェース拡張

#### オプション1: 新しいコンテキストオブジェクトの導入（推奨）

```kotlin
data class BuyDecisionContext(
    val property: Property,
    val playerMoney: Int,
    val ownedProperties: List<Property>,
    val board: Board,
    val allPlayers: List<Player>,
    val currentTurn: Int,
)

interface BuyStrategy {
    // 既存メソッド（後方互換性のため残す）
    fun shouldBuy(property: Property, currentMoney: Int): Boolean

    // 新しいメソッド（デフォルト実装で既存メソッドを呼ぶ）
    fun shouldBuy(context: BuyDecisionContext): Boolean {
        return shouldBuy(context.property, context.playerMoney)
    }
}
```

**利点**:
- 既存の戦略はそのまま動作
- 新しい戦略は詳細な情報を利用可能
- 将来的に情報を追加しやすい

**欠点**:
- 2つのメソッドが存在し、やや複雑

#### オプション2: インターフェースを完全に変更

```kotlin
interface BuyStrategy {
    fun shouldBuy(context: BuyDecisionContext): Boolean
}
```

**利点**:
- シンプルで一貫性がある
- すべての戦略が同じ情報にアクセス

**欠点**:
- 既存の戦略を全て修正する必要がある

### 選択: オプション1を採用

- 保守性を重視（既存コードへの影響を最小化）
- 段階的な移行が可能
- シンプルな戦略は引き続きシンプルに実装できる

## CLI拡張

### 戦略一覧表示機能

```bash
./gradlew run --args="--list-strategies"
```

出力例:
```
Available Strategies:
1. always      - Always Buy: 常に購入する
2. random      - Random: ランダムに購入
3. conservative - Conservative: 一定額以上の現金を保持
4. monopoly    - Monopoly First: カラーグループの独占を優先
5. roi         - ROI: 投資効率を重視
6. lowprice    - Low Price: 安いプロパティを優先
...
```

### 戦略詳細表示機能

```bash
./gradlew run --args="--strategy-info monopoly"
```

出力例:
```
Strategy: Monopoly First (monopoly)
Description: カラーグループの独占を優先する戦略

ロジック:
- 既に所有しているカラーグループのプロパティを最優先
- 独占完成が近いグループを重視
- 他のプレイヤーが独占しそうなグループは阻止を検討

パラメータ:
- blockOpponentMonopoly: 相手の独占を阻止するか（デフォルト: true）
```

## 実装タスク

1. ✅ Phase 10ドキュメント作成
2. BuyDecisionContextクラスの作成
3. BuyStrategyインターフェースの拡張
4. 既存戦略の調整（デフォルトメソッド実装により変更不要）
5. 高度な戦略の実装（6-10種類）
6. StrategyRegistryクラスの作成（戦略の一元管理）
7. CLI拡張（--list-strategies, --strategy-info）
8. 各戦略の動作検証

## 期待される成果

- 6-10種類の高度な戦略が実装される
- 戦略の一覧と詳細を確認できるCLI機能
- より多様なシミュレーション実験が可能になる

## 検証項目

- 各戦略が期待通りの購入判断を行うか
- 既存の戦略が引き続き動作するか
- CLI機能が正しく動作するか
- 戦略間で明確な挙動の違いが見られるか
