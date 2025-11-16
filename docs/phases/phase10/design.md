# Phase 10: 高度な戦略実装 - 設計書

## アーキテクチャ設計

### コンポーネント構成

```
┌─────────────────────────────────────┐
│        StrategyRegistry             │
│  (戦略の登録・検索・メタデータ管理)    │
└─────────────────────────────────────┘
              │
              │ uses
              ▼
┌─────────────────────────────────────┐
│       BuyStrategy (interface)       │
│  - shouldBuy(context)               │
│  - shouldBuy(property, money) ※互換 │
└─────────────────────────────────────┘
              △
              │ implements
    ┌─────────┼─────────┬──────────┬────────────┐
    │         │         │          │            │
┌─────┐  ┌──────┐  ┌───────┐  ┌───────┐  ┌──────────┐
│Always│  │Random│  │Conserv│  │Monopoly│  │ROI       │
│Buy   │  │      │  │ative  │  │First   │  │Strategy  │
└─────┘  └──────┘  └───────┘  └───────┘  └──────────┘
                                  ...他の戦略
```

### BuyDecisionContext

戦略の意思決定に必要な情報を集約したデータクラス。

```kotlin
/**
 * 購入判断に必要なコンテキスト情報
 */
data class BuyDecisionContext(
    /** 購入対象のプロパティ */
    val property: Property,

    /** プレイヤーの現在の所持金 */
    val playerMoney: Int,

    /** プレイヤーが所有しているプロパティ一覧 */
    val ownedProperties: List<Property>,

    /** ゲームボード */
    val board: Board,

    /** 全プレイヤー情報（自分を含む） */
    val allPlayers: List<Player>,

    /** 現在のターン数 */
    val currentTurn: Int,
) {
    /**
     * プレイヤーの名前（コンテキスト所有者）
     */
    val playerName: String
        get() = allPlayers.first { it.properties.containsAll(ownedProperties) }.name

    /**
     * 他のプレイヤー一覧
     */
    val otherPlayers: List<Player>
        get() = allPlayers.filter { !it.properties.containsAll(ownedProperties) }
}
```

### BuyStrategy拡張インターフェース

```kotlin
interface BuyStrategy {
    /**
     * 購入判断（詳細コンテキスト版）
     *
     * @param context 購入判断に必要な全情報
     * @return 購入する場合true
     */
    fun shouldBuy(context: BuyDecisionContext): Boolean {
        // デフォルト実装: 既存メソッドへ委譲
        return shouldBuy(context.property, context.playerMoney)
    }

    /**
     * 購入判断（シンプル版、後方互換性のため残す）
     *
     * @param property 購入対象のプロパティ
     * @param currentMoney プレイヤーの現在の所持金
     * @return 購入する場合true
     */
    fun shouldBuy(property: Property, currentMoney: Int): Boolean {
        throw NotImplementedError(
            "この戦略はコンテキスト版のshouldBuy(context)を実装してください"
        )
    }
}
```

### StrategyRegistry

戦略の一元管理とメタデータの提供。

```kotlin
/**
 * 戦略のメタデータ
 */
data class StrategyMetadata(
    /** 戦略ID（コマンドライン引数などで使用） */
    val id: String,

    /** 戦略の表示名 */
    val displayName: String,

    /** 戦略の説明 */
    val description: String,

    /** 戦略のロジック詳細 */
    val details: String,

    /** 戦略ファクトリ */
    val factory: () -> BuyStrategy,
)

/**
 * 全戦略の登録と管理
 */
object StrategyRegistry {
    private val strategies = mutableMapOf<String, StrategyMetadata>()

    init {
        // 既存戦略の登録
        register(...)

        // 新しい戦略の登録
        register(...)
    }

    fun register(metadata: StrategyMetadata) { ... }

    fun getStrategy(id: String): BuyStrategy? { ... }

    fun getMetadata(id: String): StrategyMetadata? { ... }

    fun listAll(): List<StrategyMetadata> { ... }
}
```

## 各戦略の詳細設計

### 1. MonopolyFirstStrategy（セット重視戦略）

**目的**: カラーグループの独占を最優先

**ロジック**:

1. プロパティのカラーグループを確認
2. 既に同じカラーグループを所有している → 高優先度
3. 購入すると独占が完成する → 最優先
4. 他プレイヤーが独占しそう → 阻止を検討（パラメータ依存）
5. それ以外 → 基本判定（現金残高）

**スコアリング**:
```
score = 0
if (同じカラーグループを1つ所有) score += 50
if (同じカラーグループを2つ所有) score += 100
if (購入で独占完成) score += 200
if (他プレイヤーが独占しそう && 阻止モード) score += 80
if (購入後の残高 < 閾値) score = 0

購入判定: score > 50
```

**パラメータ**:
- `blockOpponentMonopoly: Boolean = true`: 相手の独占を阻止するか

### 2. ROIStrategy（投資効率戦略）

**目的**: 投資収益率が高いプロパティを優先

**ロジック**:

1. プロパティの基本レント ÷ 価格 = ROI
2. ROIが閾値以上なら購入
3. 現金残高も考慮

**ROI計算**:
```kotlin
fun calculateROI(property: Property): Double {
    return property.baseRent.toDouble() / property.price.toDouble()
}
```

**購入判定**:
```
roi = calculateROI(property)
購入判定: roi >= minROI && (playerMoney - property.price) >= minCashReserve
```

**パラメータ**:
- `minROI: Double = 0.15`: 最低ROI閾値
- `minCashReserve: Int = 300`: 最低現金残高

### 3. LowPriceStrategy（低価格優先戦略）

**目的**: 安いプロパティから購入し、早期に多くのプロパティを確保

**ロジック**:

1. プロパティ価格が閾値以下なら購入
2. 購入後の現金残高を確認

**購入判定**:
```
購入判定: property.price <= maxPrice &&
         (playerMoney - property.price) >= minCashReserve
```

**パラメータ**:
- `maxPrice: Int = 200`: 購入する最大価格
- `minCashReserve: Int = 200`: 最低現金残高

### 4. HighValueStrategy（高収益優先戦略）

**目的**: レントが高いプロパティを優先（高リスク・高リターン）

**ロジック**:

1. プロパティの基本レントが閾値以上なら購入
2. 高価でも収益性が高ければ購入

**購入判定**:
```
購入判定: property.baseRent >= minRent &&
         (playerMoney - property.price) >= minCashReserve
```

**パラメータ**:
- `minRent: Int = 20`: 最低レント額
- `minCashReserve: Int = 100`: 最低現金残高（低め）

### 5. BalancedStrategy（バランス型戦略）

**目的**: 複数の要因を総合的に判断

**ロジック**:

複数の要因をスコアリング:

```
colorGroupScore = 同じカラーグループの所有数 * 20
roiScore = ROI * 100
priceScore = (500 - property.price) / 10  // 安いほど高スコア
rentScore = property.baseRent

totalScore = colorGroupScore + roiScore + priceScore + rentScore
購入判定: totalScore >= threshold &&
         (playerMoney - property.price) >= minCashReserve
```

**パラメータ**:
- `threshold: Int = 80`: 購入スコア閾値
- `minCashReserve: Int = 400`: 最低現金残高

### 6. AggressiveStrategy（積極戦略）

**目的**: 他のプレイヤーの独占を阻止

**ロジック**:

1. 他プレイヤーが同じカラーグループを所有している → 高優先度
2. 相手が独占間近 → 最優先で阻止
3. それ以外 → 通常判定

**スコアリング**:
```
score = 0
for each otherPlayer:
    sameColorCount = otherPlayer.properties.count { it.colorGroup == property.colorGroup }
    if (sameColorCount == 1) score += 40
    if (sameColorCount == 2) score += 100  // 独占阻止

購入判定: score >= 40 && (playerMoney - property.price) >= minCashReserve
```

**パラメータ**:
- `minCashReserve: Int = 300`: 最低現金残高

### 7-10. 追加戦略（オプション）

**LocationBasedStrategy**: 統計的に止まりやすい位置を優先
- オレンジ（positions 16, 18, 19）: スコア+30
- 赤（positions 21, 23, 24）: スコア+25
- その他: 標準スコア

**EarlyGameStrategy**: ゲーム序盤は積極的、後半は保守的
- Turn < 20: 積極購入（低い閾値）
- Turn >= 20: 保守的（高い閾値）

**AdaptiveStrategy**: 自分の順位に応じて戦略変更
- 1位: 保守的（現金を保持）
- 2-3位: バランス型
- 4位: 積極的（リスクを取る）

**CashFlowStrategy**: キャッシュフロー重視
- 購入より現金確保を優先
- 常に高い現金残高を維持

## データフロー

```
GameService
    │
    ├─ Player情報（所持金、所有プロパティ）
    ├─ Board情報
    ├─ Property情報
    │
    ▼
BuyDecisionContext作成
    │
    ▼
BuyStrategy.shouldBuy(context)
    │
    ├─ 各戦略のロジック実行
    ├─ スコアリング
    ├─ 閾値判定
    │
    ▼
購入可否をGameServiceに返却
```

## GameServiceの変更

BuyDecisionContextを作成し、戦略に渡す:

```kotlin
class GameService {
    fun handleLandOnProperty(
        player: Player,
        property: Property,
        board: Board,
        allPlayers: List<Player>,
        currentTurn: Int,
    ): GameEvent {
        // ...既存のロジック...

        // コンテキスト作成
        val context = BuyDecisionContext(
            property = property,
            playerMoney = player.money,
            ownedProperties = player.properties,
            board = board,
            allPlayers = allPlayers,
            currentTurn = currentTurn,
        )

        // 戦略による購入判断
        val shouldBuy = player.strategy.shouldBuy(context)

        // ...以降の処理...
    }
}
```

## パフォーマンス考慮事項

- BuyDecisionContextの作成は購入判断のたびに発生
- allPlayersやboardのコピーは行わず、参照を渡す（不変性を前提）
- 複雑なスコアリング計算は最小限に

## エラーハンドリング

- 存在しない戦略IDが指定された場合: エラーメッセージ + 利用可能な戦略一覧を表示
- 旧インターフェース（shouldBuy(property, money)）のみ実装した新戦略: NotImplementedErrorで明確に指示

## テスト戦略

1. **ユニットテスト**: 各戦略の購入判断ロジック
2. **統合テスト**: GameServiceとの連携
3. **シナリオテスト**: 実際のゲームシミュレーションで挙動を確認
4. **比較テスト**: 戦略間で明確な勝率差が出るか
