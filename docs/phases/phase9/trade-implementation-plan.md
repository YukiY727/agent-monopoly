# トレード機能実装計画

## 概要

モノポリー形成を促進するため、プレイヤー間でのプロパティ・現金・刑務所脱出カードのトレード機能を実装する。

## 仕様（docs/specifications/02-game-rules.md より）

### 取引可能なタイミング
- 自分のターン中（ターン終了アクション時）
- 相手プレイヤーの同意が必要

### 取引可能なアイテム
1. **物件** - 建物がある場合は先に売却が必要、抵当物件は取引可能
2. **現金** - 任意の金額
3. **刑務所脱出カード** - 所持している場合

### 取引の制約
- 2者間取引のみ
- 即時決済（将来の支払い約束は無効）
- 銀行は関与しない

---

## 実装設計

### 1. ドメインモデル

#### TradeOffer（トレード提案）
```kotlin
// src/main/kotlin/com/monopoly/domain/model/trade/TradeOffer.kt
data class TradeOffer(
    val proposer: Player,
    val target: Player,
    val offeredProperties: List<Property>,
    val offeredMoney: Int,
    val offeredJailCards: Int,
    val requestedProperties: List<Property>,
    val requestedMoney: Int,
    val requestedJailCards: Int,
)
```

#### Trade（トレード状態 - sealed class）
```kotlin
// src/main/kotlin/com/monopoly/domain/model/trade/Trade.kt
sealed class Trade {
    data class Proposed(val offer: TradeOffer) : Trade()
    data class Accepted(val offer: TradeOffer) : Trade()
    data class Rejected(val offer: TradeOffer) : Trade()
    data class Completed(val offer: TradeOffer) : Trade()
}
```

### 2. サービス層

#### TradeService
```kotlin
// src/main/kotlin/com/monopoly/domain/service/TradeService.kt
class TradeService {
    fun validateOffer(offer: TradeOffer): Boolean
    fun executeTrade(offer: TradeOffer): Boolean
}
```

**バリデーションルール:**
- 提案者が提供物件を所有している
- 提供物件に建物がない
- 提案者が提供金額を持っている
- 提案者が提供カード数を持っている
- ターゲットが要求物件を所有している
- 要求物件に建物がない
- ターゲットが要求金額を持っている
- ターゲットが要求カード数を持っている

### 3. 戦略インターフェース拡張

```kotlin
// PlayerStrategy.kt に追加
interface PlayerStrategy {
    // 既存メソッド...

    /**
     * トレード提案を作成
     * @param gameState 現在のゲーム状態
     * @param otherPlayers 他のプレイヤーリスト
     * @return トレード提案（null = トレードしない）
     */
    fun proposeTradeOffer(
        gameState: GameState,
        otherPlayers: List<Player>,
    ): TradeOffer?

    /**
     * トレード提案を評価
     * @param offer 受け取った提案
     * @return 受け入れる場合true
     */
    fun evaluateTradeOffer(offer: TradeOffer): Boolean
}
```

### 4. 戦略実装方針

#### 基本戦略（シンプルなルールベース）

**トレード提案ロジック:**
1. 自分が2/3所有しているカラーグループを特定
2. 不足物件を持っているプレイヤーを探す
3. 交換対象として自分が1つしか持っていないカラーグループの物件を提示
4. 必要に応じて現金を追加

**トレード評価ロジック:**
1. モノポリー完成に近づくか？
2. 相手にモノポリーを与えないか？
3. 金銭的に損をしないか？

### 5. ゲームフローへの統合

```
// GameService.kt
processTurn() {
    1. サイコロを振る
    2. 移動
    3. マス処理（購入・家賃等）
    4. 建設フェーズ
    5. トレードフェーズ ← 新規追加
    6. ターン終了
}
```

### 6. イベント追加

```kotlin
// GameEvent.kt に追加
data class TradeProposed(
    override val turnNumber: Int,
    override val timestamp: Long,
    val proposerName: String,
    val targetName: String,
    val offeredProperties: List<String>,
    val offeredMoney: Int,
    val requestedProperties: List<String>,
    val requestedMoney: Int,
) : GameEvent()

data class TradeAccepted(
    override val turnNumber: Int,
    override val timestamp: Long,
    val proposerName: String,
    val targetName: String,
) : GameEvent()

data class TradeRejected(
    override val turnNumber: Int,
    override val timestamp: Long,
    val proposerName: String,
    val targetName: String,
) : GameEvent()

data class TradeCompleted(
    override val turnNumber: Int,
    override val timestamp: Long,
    val proposerName: String,
    val targetName: String,
    val propertiesExchanged: Int,
    val moneyExchanged: Int,
) : GameEvent()
```

---

## 実装順序

### Step 1: ドメインモデル
1. `TradeOffer` データクラス
2. `Trade` sealed class
3. テスト作成

### Step 2: TradeService
1. `validateOffer()` 実装
2. `executeTrade()` 実装
3. テスト作成

### Step 3: イベント追加
1. `GameEvent` にトレード関連イベント追加

### Step 4: 戦略インターフェース拡張
1. `PlayerStrategy` に新メソッド追加
2. 既存戦略にデフォルト実装（トレードしない）

### Step 5: 基本トレード戦略実装
1. `TradeAwareStrategy` インターフェースまたはミックスイン
2. 各戦略でトレードロジック実装

### Step 6: GameService統合
1. `tryTradeWithOtherPlayers()` メソッド追加
2. `processTurn()` にトレードフェーズ追加

### Step 7: 統合テスト・実験
1. トレードが発生するシナリオのテスト
2. 比較実験でモノポリー形成率の改善を確認

---

## 期待される効果

1. **モノポリー形成率の向上** - プレイヤーがプロパティを交換してモノポリーを完成
2. **家/ホテル建設の増加** - モノポリー完成により建設が可能に
3. **破産率の向上** - 高家賃による資金流出が発生
4. **戦略の多様性** - トレード戦略の優劣が勝敗に影響

---

## リスクと対策

| リスク | 対策 |
|--------|------|
| 無限ループ（トレード提案の繰り返し） | 1ターン1提案に制限 |
| 不公平なトレード | 戦略でバリデーション |
| 複雑度の増加 | シンプルなルールベースから開始 |
| テスト困難 | 決定論的戦略でテスト |

---

## 見積もり

- ドメインモデル + テスト: 小
- TradeService + テスト: 小
- 戦略拡張 + テスト: 中
- GameService統合 + テスト: 中
- 比較実験: 小

合計: 中規模の実装
