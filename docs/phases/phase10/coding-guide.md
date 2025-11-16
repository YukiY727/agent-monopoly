# Phase 10: 高度な戦略実装 - コーディングガイド

## 実装順序

1. BuyDecisionContextの作成
2. BuyStrategyインターフェースの拡張
3. 既存戦略の確認（修正不要を確認）
4. 高度な戦略の実装
5. StrategyRegistryの作成
6. GameServiceの修正
7. CLI拡張
8. テスト

## Step 1: BuyDecisionContextの作成

**ファイル**: `src/main/kotlin/com/monopoly/domain/strategy/BuyDecisionContext.kt`

```kotlin
package com.monopoly.domain.strategy

import com.monopoly.domain.model.Board
import com.monopoly.domain.model.Player
import com.monopoly.domain.model.Property

/**
 * 購入判断に必要なコンテキスト情報
 *
 * 高度な戦略が購入判断を行う際に必要な情報を集約したデータクラス。
 * シンプルな戦略はこのコンテキストを使用せず、property と currentMoney のみで判断可能。
 */
data class BuyDecisionContext(
    /** 購入対象のプロパティ */
    val property: Property,

    /** プレイヤーの現在の所持金 */
    val playerMoney: Int,

    /** プレイヤーが所有しているプロパティ一覧 */
    val ownedProperties: List<Property>,

    /** ゲームボード（全プロパティ情報） */
    val board: Board,

    /** 全プレイヤー情報（自分を含む） */
    val allPlayers: List<Player>,

    /** 現在のターン数 */
    val currentTurn: Int,
) {
    /**
     * 他のプレイヤー一覧（自分以外）
     */
    val otherPlayers: List<Player>
        get() = allPlayers.filter { player ->
            // 所有プロパティリストが一致しないプレイヤー = 他人
            player.properties != ownedProperties
        }

    /**
     * 指定したカラーグループで自分が所有しているプロパティ数
     */
    fun countOwnedInColorGroup(colorGroup: String): Int {
        return ownedProperties.count { it.colorGroup == colorGroup }
    }

    /**
     * 指定したカラーグループで他プレイヤーが所有している最大プロパティ数
     */
    fun maxOtherPlayerCountInColorGroup(colorGroup: String): Int {
        return otherPlayers.maxOfOrNull { player ->
            player.properties.count { it.colorGroup == colorGroup }
        } ?: 0
    }

    /**
     * 購入後の所持金
     */
    val moneyAfterPurchase: Int
        get() = playerMoney - property.price
}
```

## Step 2: BuyStrategyインターフェースの拡張

**ファイル**: `src/main/kotlin/com/monopoly/domain/strategy/BuyStrategy.kt`

```kotlin
package com.monopoly.domain.strategy

import com.monopoly.domain.model.Property

interface BuyStrategy {
    /**
     * 購入判断（詳細コンテキスト版）
     *
     * デフォルト実装では既存のshouldBuy(property, currentMoney)に委譲します。
     * 高度な戦略を実装する場合は、このメソッドをオーバーライドしてください。
     *
     * @param context 購入判断に必要な全情報
     * @return 購入する場合true
     */
    fun shouldBuy(context: BuyDecisionContext): Boolean {
        // デフォルト実装: 既存メソッドへ委譲
        return shouldBuy(context.property, context.playerMoney)
    }

    /**
     * 購入判断（シンプル版）
     *
     * 既存の戦略との互換性のために残されています。
     * 新しい高度な戦略を実装する場合は、shouldBuy(context)をオーバーライドし、
     * このメソッドは未実装のままで構いません。
     *
     * @param property 購入対象のプロパティ
     * @param currentMoney プレイヤーの現在の所持金
     * @return 購入する場合true
     */
    fun shouldBuy(
        property: Property,
        currentMoney: Int,
    ): Boolean
}
```

## Step 3: 高度な戦略の実装

### MonopolyFirstStrategy

**ファイル**: `src/main/kotlin/com/monopoly/domain/strategy/MonopolyFirstStrategy.kt`

```kotlin
package com.monopoly.domain.strategy

import com.monopoly.domain.model.Property

/**
 * カラーグループの独占を優先する戦略
 *
 * 既に所有しているカラーグループのプロパティを優先的に購入し、
 * 独占を完成させることを目指します。
 *
 * @property blockOpponentMonopoly 相手の独占を阻止するか
 * @property minCashReserve 最低現金残高
 */
class MonopolyFirstStrategy(
    private val blockOpponentMonopoly: Boolean = true,
    private val minCashReserve: Int = 300,
) : BuyStrategy {
    override fun shouldBuy(context: BuyDecisionContext): Boolean {
        val colorGroup = context.property.colorGroup

        // スコアリング
        var score = 0

        // 自分が同じカラーグループを所有している数
        val ownedCount = context.countOwnedInColorGroup(colorGroup)
        when (ownedCount) {
            1 -> score += 50
            2 -> score += 100  // 独占完成間近
        }

        // 購入で独占が完成する場合
        val totalInGroup = context.board.getPropertiesByColorGroup(colorGroup).size
        if (ownedCount + 1 == totalInGroup) {
            score += 200  // 独占完成は最優先
        }

        // 他プレイヤーの独占を阻止
        if (blockOpponentMonopoly) {
            val maxOtherCount = context.maxOtherPlayerCountInColorGroup(colorGroup)
            if (maxOtherCount >= 2) {
                score += 80  // 相手の独占を阻止
            }
        }

        // 現金が足りない場合はスコアを0に
        if (context.moneyAfterPurchase < minCashReserve) {
            score = 0
        }

        return score >= 50
    }

    override fun shouldBuy(property: Property, currentMoney: Int): Boolean {
        throw NotImplementedError(
            "MonopolyFirstStrategy はコンテキスト版のshouldBuyを使用してください"
        )
    }
}
```

### ROIStrategy

**ファイル**: `src/main/kotlin/com/monopoly/domain/strategy/ROIStrategy.kt`

```kotlin
package com.monopoly.domain.strategy

import com.monopoly.domain.model.Property

/**
 * 投資収益率（ROI）を重視する戦略
 *
 * プロパティの価格に対してレントが高い物件を優先的に購入します。
 *
 * @property minROI 最低ROI閾値（デフォルト: 0.15 = 15%）
 * @property minCashReserve 最低現金残高
 */
class ROIStrategy(
    private val minROI: Double = 0.15,
    private val minCashReserve: Int = 300,
) : BuyStrategy {
    override fun shouldBuy(context: BuyDecisionContext): Boolean {
        val roi = calculateROI(context.property)
        return roi >= minROI && context.moneyAfterPurchase >= minCashReserve
    }

    override fun shouldBuy(property: Property, currentMoney: Int): Boolean {
        throw NotImplementedError(
            "ROIStrategy はコンテキスト版のshouldBuyを使用してください"
        )
    }

    /**
     * ROI（投資収益率）を計算
     *
     * @param property プロパティ
     * @return ROI（基本レント ÷ 価格）
     */
    private fun calculateROI(property: Property): Double {
        return property.baseRent.toDouble() / property.price.toDouble()
    }
}
```

### LowPriceStrategy

**ファイル**: `src/main/kotlin/com/monopoly/domain/strategy/LowPriceStrategy.kt`

```kotlin
package com.monopoly.domain.strategy

import com.monopoly.domain.model.Property

/**
 * 低価格のプロパティを優先する戦略
 *
 * 安いプロパティから購入し、早期に多くのプロパティを確保します。
 *
 * @property maxPrice 購入する最大価格
 * @property minCashReserve 最低現金残高
 */
class LowPriceStrategy(
    private val maxPrice: Int = 200,
    private val minCashReserve: Int = 200,
) : BuyStrategy {
    override fun shouldBuy(context: BuyDecisionContext): Boolean {
        return context.property.price <= maxPrice &&
            context.moneyAfterPurchase >= minCashReserve
    }

    override fun shouldBuy(property: Property, currentMoney: Int): Boolean {
        // シンプルな戦略なので、旧インターフェースでも実装可能
        return property.price <= maxPrice &&
            (currentMoney - property.price) >= minCashReserve
    }
}
```

### HighValueStrategy

**ファイル**: `src/main/kotlin/com/monopoly/domain/strategy/HighValueStrategy.kt`

```kotlin
package com.monopoly.domain.strategy

import com.monopoly.domain.model.Property

/**
 * 高収益のプロパティを優先する戦略
 *
 * レントが高いプロパティを優先的に購入します。
 *
 * @property minRent 購入する最低レント額
 * @property minCashReserve 最低現金残高（低め設定）
 */
class HighValueStrategy(
    private val minRent: Int = 20,
    private val minCashReserve: Int = 100,
) : BuyStrategy {
    override fun shouldBuy(context: BuyDecisionContext): Boolean {
        return context.property.baseRent >= minRent &&
            context.moneyAfterPurchase >= minCashReserve
    }

    override fun shouldBuy(property: Property, currentMoney: Int): Boolean {
        return property.baseRent >= minRent &&
            (currentMoney - property.price) >= minCashReserve
    }
}
```

### BalancedStrategy

**ファイル**: `src/main/kotlin/com/monopoly/domain/strategy/BalancedStrategy.kt`

```kotlin
package com.monopoly.domain.strategy

import com.monopoly.domain.model.Property

/**
 * バランス型の戦略
 *
 * 複数の要因（カラーグループ、ROI、価格、レント）を総合的に判断します。
 *
 * @property threshold 購入スコア閾値
 * @property minCashReserve 最低現金残高
 */
class BalancedStrategy(
    private val threshold: Int = 80,
    private val minCashReserve: Int = 400,
) : BuyStrategy {
    override fun shouldBuy(context: BuyDecisionContext): Boolean {
        if (context.moneyAfterPurchase < minCashReserve) {
            return false
        }

        val score = calculateScore(context)
        return score >= threshold
    }

    override fun shouldBuy(property: Property, currentMoney: Int): Boolean {
        throw NotImplementedError(
            "BalancedStrategy はコンテキスト版のshouldBuyを使用してください"
        )
    }

    private fun calculateScore(context: BuyDecisionContext): Int {
        var score = 0

        // カラーグループスコア
        val ownedCount = context.countOwnedInColorGroup(context.property.colorGroup)
        score += ownedCount * 20

        // ROIスコア
        val roi = context.property.baseRent.toDouble() / context.property.price.toDouble()
        score += (roi * 100).toInt()

        // 価格スコア（安いほど高スコア）
        score += (500 - context.property.price) / 10

        // レントスコア
        score += context.property.baseRent

        return score
    }
}
```

### AggressiveStrategy

**ファイル**: `src/main/kotlin/com/monopoly/domain/strategy/AggressiveStrategy.kt`

```kotlin
package com.monopoly.domain.strategy

import com.monopoly.domain.model.Property

/**
 * 積極的な戦略（相手の独占を阻止）
 *
 * 他のプレイヤーのカラーグループ独占を阻止することを優先します。
 *
 * @property minCashReserve 最低現金残高
 */
class AggressiveStrategy(
    private val minCashReserve: Int = 300,
) : BuyStrategy {
    override fun shouldBuy(context: BuyDecisionContext): Boolean {
        if (context.moneyAfterPurchase < minCashReserve) {
            return false
        }

        var score = 0

        // 他プレイヤーが同じカラーグループを所有している数
        context.otherPlayers.forEach { otherPlayer ->
            val sameColorCount = otherPlayer.properties.count {
                it.colorGroup == context.property.colorGroup
            }

            when (sameColorCount) {
                1 -> score += 40
                2 -> score += 100  // 独占阻止は最優先
            }
        }

        return score >= 40
    }

    override fun shouldBuy(property: Property, currentMoney: Int): Boolean {
        throw NotImplementedError(
            "AggressiveStrategy はコンテキスト版のshouldBuyを使用してください"
        )
    }
}
```

## Step 4: StrategyRegistryの作成

**ファイル**: `src/main/kotlin/com/monopoly/domain/strategy/StrategyRegistry.kt`

```kotlin
package com.monopoly.domain.strategy

/**
 * 戦略のメタデータ
 */
data class StrategyMetadata(
    /** 戦略ID（コマンドライン引数で使用） */
    val id: String,

    /** 戦略の表示名 */
    val displayName: String,

    /** 戦略の説明（1行） */
    val description: String,

    /** 戦略の詳細説明 */
    val details: String,

    /** 戦略インスタンスを生成するファクトリ */
    val factory: () -> BuyStrategy,
)

/**
 * 全戦略の登録と管理
 */
object StrategyRegistry {
    private val strategies = LinkedHashMap<String, StrategyMetadata>()

    init {
        // 既存戦略の登録
        register(
            StrategyMetadata(
                id = "always",
                displayName = "Always Buy",
                description = "常に購入する",
                details = """
                    購入可能なプロパティは必ず購入します。
                    最もシンプルな戦略で、ベースライン比較に使用されます。
                """.trimIndent(),
                factory = { AlwaysBuyStrategy() }
            )
        )

        register(
            StrategyMetadata(
                id = "random",
                displayName = "Random",
                description = "ランダムに購入",
                details = """
                    50%の確率で購入します。
                    確率的な振る舞いの検証に使用されます。
                """.trimIndent(),
                factory = { RandomStrategy() }
            )
        )

        register(
            StrategyMetadata(
                id = "conservative",
                displayName = "Conservative",
                description = "保守的（一定額以上の現金を保持）",
                details = """
                    購入後の所持金が$500以上になる場合のみプロパティを購入します。
                    破産リスクを抑えた安全志向の戦略です。
                """.trimIndent(),
                factory = { ConservativeStrategy() }
            )
        )

        // 新しい高度な戦略の登録
        register(
            StrategyMetadata(
                id = "monopoly",
                displayName = "Monopoly First",
                description = "カラーグループの独占を優先",
                details = """
                    既に所有しているカラーグループのプロパティを優先的に購入し、
                    独占を完成させることを目指します。
                    また、他プレイヤーの独占を阻止することも考慮します。
                """.trimIndent(),
                factory = { MonopolyFirstStrategy() }
            )
        )

        register(
            StrategyMetadata(
                id = "roi",
                displayName = "ROI",
                description = "投資収益率を重視",
                details = """
                    プロパティのROI（基本レント ÷ 価格）を計算し、
                    15%以上のROIがあるプロパティを購入します。
                    長期的な収益性を重視した戦略です。
                """.trimIndent(),
                factory = { ROIStrategy() }
            )
        )

        register(
            StrategyMetadata(
                id = "lowprice",
                displayName = "Low Price",
                description = "安いプロパティを優先",
                details = """
                    $200以下のプロパティを優先的に購入します。
                    早期に多くのプロパティを確保し、レント収入を増やすことを目指します。
                """.trimIndent(),
                factory = { LowPriceStrategy() }
            )
        )

        register(
            StrategyMetadata(
                id = "highvalue",
                displayName = "High Value",
                description = "高収益プロパティを優先",
                details = """
                    レントが$20以上のプロパティを優先的に購入します。
                    高価でも収益性が高ければ購入する、リスクを取った戦略です。
                """.trimIndent(),
                factory = { HighValueStrategy() }
            )
        )

        register(
            StrategyMetadata(
                id = "balanced",
                displayName = "Balanced",
                description = "バランス型",
                details = """
                    カラーグループ、ROI、価格、レントを総合的に判断します。
                    複数の要因をスコアリングし、バランスの取れた購入判断を行います。
                """.trimIndent(),
                factory = { BalancedStrategy() }
            )
        )

        register(
            StrategyMetadata(
                id = "aggressive",
                displayName = "Aggressive",
                description = "積極的（相手の独占を阻止）",
                details = """
                    他のプレイヤーのカラーグループ独占を阻止することを優先します。
                    相手が2つ所有しているカラーグループのプロパティは必ず購入を検討します。
                """.trimIndent(),
                factory = { AggressiveStrategy() }
            )
        )
    }

    fun register(metadata: StrategyMetadata) {
        strategies[metadata.id] = metadata
    }

    fun getStrategy(id: String): BuyStrategy? {
        return strategies[id]?.factory?.invoke()
    }

    fun getMetadata(id: String): StrategyMetadata? {
        return strategies[id]
    }

    fun listAll(): List<StrategyMetadata> {
        return strategies.values.toList()
    }
}
```

## Step 5: Boardの拡張

Boardクラスにカラーグループでプロパティを取得するメソッドを追加:

```kotlin
// Boardクラスに追加
fun getPropertiesByColorGroup(colorGroup: String): List<Property> {
    return properties.filter { it.colorGroup == colorGroup }
}
```

## Step 6: GameServiceの修正

購入判断時にBuyDecisionContextを作成:

```kotlin
// GameServiceクラスのhandleLandOnPropertyメソッドを修正
private fun handleLandOnProperty(
    player: Player,
    property: Property,
    board: Board,
    allPlayers: List<Player>,
    currentTurn: Int,
): GameEvent {
    // ... 既存のロジック ...

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

    // ... 以降の処理 ...
}
```

## Step 7: CLI拡張

Main.ktに以下の機能を追加:

```kotlin
// --list-strategies オプション
if (args.contains("--list-strategies")) {
    println("Available Strategies:")
    StrategyRegistry.listAll().forEachIndexed { index, metadata ->
        println("${index + 1}. ${metadata.id.padEnd(15)} - ${metadata.displayName}: ${metadata.description}")
    }
    return
}

// --strategy-info <id> オプション
val strategyInfoIndex = args.indexOf("--strategy-info")
if (strategyInfoIndex != -1 && strategyInfoIndex + 1 < args.size) {
    val strategyId = args[strategyInfoIndex + 1]
    val metadata = StrategyRegistry.getMetadata(strategyId)
    if (metadata != null) {
        println("Strategy: ${metadata.displayName} (${metadata.id})")
        println("Description: ${metadata.description}")
        println()
        println("Details:")
        println(metadata.details)
    } else {
        println("Strategy '$strategyId' not found.")
        println()
        println("Available strategies:")
        StrategyRegistry.listAll().forEach {
            println("  - ${it.id}")
        }
    }
    return
}
```

## テスト例

```kotlin
class MonopolyFirstStrategyTest {
    @Test
    fun `should prioritize completing monopoly`() {
        val board = Board.createStandardBoard()
        val ownedProperties = listOf(
            board.properties[1],  // 茶色グループ
            board.properties[3]   // 茶色グループ
        )

        val context = BuyDecisionContext(
            property = board.properties[6],  // もう1つの茶色
            playerMoney = 1000,
            ownedProperties = ownedProperties,
            board = board,
            allPlayers = emptyList(),
            currentTurn = 10,
        )

        val strategy = MonopolyFirstStrategy()
        assertTrue(strategy.shouldBuy(context))
    }
}
```

## チェックリスト

- [ ] BuyDecisionContextクラスを作成
- [ ] BuyStrategyインターフェースを拡張
- [ ] MonopolyFirstStrategyを実装
- [ ] ROIStrategyを実装
- [ ] LowPriceStrategyを実装
- [ ] HighValueStrategyを実装
- [ ] BalancedStrategyを実装
- [ ] AggressiveStrategyを実装
- [ ] StrategyRegistryを実装
- [ ] BoardクラスにgetPropertiesByColorGroupを追加
- [ ] GameServiceにBuyDecisionContext作成処理を追加
- [ ] Main.ktに--list-strategiesオプションを追加
- [ ] Main.ktに--strategy-infoオプションを追加
- [ ] 各戦略のユニットテストを作成
