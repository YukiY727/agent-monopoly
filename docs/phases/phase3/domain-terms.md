# Phase 3 ドメイン用語集

このドキュメントはPhase 3で使用する戦略関連の用語を定義します。

---

## 1. 戦略関連用語

### BuyStrategy（購入戦略）

プロパティの購入判断を行うインターフェース。

**定義**:
```kotlin
interface BuyStrategy {
    fun shouldBuy(property: Property, playerMoney: Int): Boolean
}
```

**役割**:
- プロパティと現在の所持金を受け取る
- 購入すべきかどうかを判定する（true/false）

**実装**:
- Phase 1: AlwaysBuyStrategy
- Phase 3: RandomStrategy, ConservativeStrategy
- Phase 10: さらに高度な戦略を追加予定

---

## 2. Phase 3で実装する戦略

### AlwaysBuyStrategy（常時購入戦略）

**概要**: 所持金が足りる限り、常にプロパティを購入する戦略

**判定ロジック**:
```
購入する条件: playerMoney >= property.price
```

**特徴**:
- 最もシンプルな戦略
- 攻撃的な戦略（多くのプロパティを所有）
- 現金が枯渇しやすく、破産リスクが高い
- Phase 1で実装済み

**用途**:
- ベースライン戦略（他の戦略と比較する基準）
- シンプルなゲームシミュレーション

---

### RandomStrategy（ランダム戦略）

**概要**: 所持金が足りる場合、50%の確率でプロパティを購入する戦略

**判定ロジック**:
```
購入する条件:
  1. playerMoney >= property.price
  AND
  2. random.nextBoolean() == true
```

**特徴**:
- 予測不可能な戦略
- 運の要素が強い
- AlwaysBuyStrategyよりは破産リスクが低い（購入頻度が半分）

**パラメータ**:
- `random: Random` - 乱数生成器（テスト時にシード固定可能）

**用途**:
- 運の影響を検証するベースライン
- 他の戦略との比較（ランダムより良いか？）

---

### ConservativeStrategy（保守的戦略）

**概要**: 一定額以上の現金を保持する慎重な戦略

**判定ロジック**:
```
購入する条件:
  (playerMoney - property.price) >= minimumCashReserve
```

**特徴**:
- リスク回避型の戦略
- 破産しにくい（常に一定額の現金を確保）
- レント支払いに余裕を持つ
- プロパティ所有数は少なくなりがち

**パラメータ**:
- `minimumCashReserve: Int` - 保持する最小現金額（デフォルト: $500）

**閾値の意味**:
- $500: バランス型（デフォルト）
- $300: やや攻撃的（より多く購入）
- $1000: 超保守的（ほとんど購入しない）

**用途**:
- リスク管理の重要性を検証
- 破産率の低い戦略の実装
- 閾値をパラメータ化した戦略の例

---

## 3. 戦略選択機能の用語

### Strategy Name（戦略名）

コマンドライン引数で指定する戦略の識別子。

**形式**:
- 小文字のケバブケース（ハイフン区切り）

**有効な戦略名**:
| 戦略名 | クラス名 | 説明 |
|-------|---------|------|
| `always-buy` | AlwaysBuyStrategy | 常に購入 |
| `random` | RandomStrategy | ランダムに購入 |
| `conservative` | ConservativeStrategy | 保守的に購入 |

**使用例**:
```bash
./gradlew run --args="--strategy always-buy"
./gradlew run --args="--strategy random"
./gradlew run --args="--strategy conservative"
```

---

### Strategy Factory（戦略ファクトリー）

戦略名から戦略インスタンスを生成する関数。

**シグネチャ**:
```kotlin
fun createStrategy(strategyName: String): BuyStrategy
```

**役割**:
- 戦略名（文字列）を受け取る
- 対応する戦略クラスのインスタンスを生成して返す
- 不正な戦略名の場合はエラーメッセージを表示してプログラムを終了

**実装場所**:
- Main.kt内

---

## 4. 戦略パラメータ

### Minimum Cash Reserve（最小現金保持額）

ConservativeStrategyで使用するパラメータ。

**定義**:
- プロパティ購入後に保持すべき最小現金額

**単位**:
- ドル（$）

**デフォルト値**:
- $500

**推奨範囲**:
- $300 ~ $1000

**影響**:
- 低い値（$300）: より多くのプロパティを購入、破産リスクやや高
- 高い値（$1000）: プロパティ購入を抑制、破産リスク低

---

## 5. 戦略比較の用語

### Win Rate（勝率）

特定の戦略で複数回ゲームを実行した際の勝利回数の割合。

**計算式**:
```
勝率 = (勝利回数 / 総ゲーム数) × 100%
```

**用途**:
- 戦略間の優劣を比較
- Phase 6で統計収集機能を実装予定

**例**:
```
100ゲーム実行して60回勝利 → 勝率60%
```

---

### Average Final Assets（平均最終資産額）

複数回ゲームを実行した際の最終資産額の平均。

**計算対象**:
- 現金 + 所有プロパティの価値

**用途**:
- 戦略の資産形成能力を評価
- Phase 6で統計収集機能を実装予定

---

### Bankruptcy Rate（破産率）

複数回ゲームを実行した際に破産した回数の割合。

**計算式**:
```
破産率 = (破産回数 / 総ゲーム数) × 100%
```

**用途**:
- 戦略のリスクを評価
- Phase 6で統計収集機能を実装予定

---

## 6. 用語の使い分け

### Strategy vs Policy

このプロジェクトでは「Strategy（戦略）」を使用します。

**理由**:
- デザインパターンの「Strategy Pattern」に準拠
- Kotlinの慣習に従う

**使わない用語**:
- Policy（ポリシー）
- Tactic（戦術）
- Rule（ルール）

---

### Buy Decision vs Purchase Decision

このプロジェクトでは「Buy（購入）」を使用します。

**理由**:
- 簡潔で分かりやすい
- BuyStrategyとの一貫性

**使わない用語**:
- Purchase（購入）
- Acquire（獲得）

---

## 7. Phase 3で導入しない概念

Phase 3では以下の概念は扱いません（今後のフェーズで導入）：

### 将来実装予定の概念

**戦略パラメータの最適化** (Phase 11):
- パラメータグリッドサーチ
- 最適な閾値の発見

**高度な戦略** (Phase 10):
- セット重視戦略（同色セット完成を優先）
- ROI戦略（投資効率を計算）
- 位置戦略（高収益マスを優先）

**複数ゲーム実行** (Phase 5):
- N回のゲームを連続実行
- 戦略間の統計的比較

**戦略の組み合わせ**:
- 複数の戦略を組み合わせたハイブリッド戦略
- 状況に応じて戦略を切り替える適応型戦略

---

## 8. 用語の索引

| 用語 | 英語 | カテゴリ | フェーズ |
|-----|------|---------|---------|
| 購入戦略 | Buy Strategy | 戦略 | Phase 1 |
| 常時購入戦略 | Always Buy Strategy | 戦略 | Phase 1 |
| ランダム戦略 | Random Strategy | 戦略 | Phase 3 |
| 保守的戦略 | Conservative Strategy | 戦略 | Phase 3 |
| 最小現金保持額 | Minimum Cash Reserve | パラメータ | Phase 3 |
| 戦略名 | Strategy Name | CLI | Phase 3 |
| 戦略ファクトリー | Strategy Factory | 実装パターン | Phase 3 |
| 勝率 | Win Rate | 統計 | Phase 6 |
| 平均最終資産額 | Average Final Assets | 統計 | Phase 6 |
| 破産率 | Bankruptcy Rate | 統計 | Phase 6 |

---

**作成日**: 2025-11-16
**最終更新**: 2025-11-16
