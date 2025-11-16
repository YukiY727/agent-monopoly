# Phase 9: 詳細統計の追加 - 設計詳細

---

## アーキテクチャ概要

```
┌─────────────────────────────────────────────┐
│         MultiGameResult                      │
│  (Phase 5で実装済み)                          │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│    DetailedStatisticsCalculator              │
│  - プロパティ統計の集計                        │
│  - 資産推移の記録                              │
│  - 破産分析の実施                              │
└──────────────────┬──────────────────────────┘
                   │
         ┌─────────┴─────────┐
         ▼                   ▼
┌──────────────────┐  ┌──────────────────┐
│ DetailedStats    │  │ AssetHistory     │
│ - PropertyStats  │  │ - Snapshots      │
│ - Bankruptcy     │  │                  │
└─────────┬────────┘  └────────┬─────────┘
          │                    │
          └──────────┬─────────┘
                     ▼
          ┌──────────────────────┐
          │ StatisticsReportGen  │
          │ (拡張)                │
          │ - 折れ線グラフ         │
          │ - ランキング表         │
          └──────────────────────┘
```

---

## データモデル設計

### 1. PropertyStatistics

```kotlin
/**
 * プロパティ別の統計情報
 */
data class PropertyStatistics(
    val propertyName: String,
    val position: Int,
    val colorGroup: String,
    val price: Int,

    // 購入統計
    val purchaseCount: Int,
    val purchaseRate: Double,  // 購入された割合

    // 収益統計
    val totalRentCollected: Int,
    val averageRentPerGame: Double,
    val maxRentInSingleGame: Int,

    // 勝利統計
    val winRateWhenOwned: Double,  // このプロパティを所有した時の勝率
    val avgTurnsHeldByWinner: Double,

    // ROI
    val roi: Double,  // (総レント収入 / 購入価格) / ゲーム数
)
```

**計算ロジック**:
```kotlin
fun calculatePropertyStatistics(
    multiGameResult: MultiGameResult,
    propertyName: String
): PropertyStatistics {
    val relevantGames = multiGameResult.gameResults.filter { game ->
        game.finalState.events.any { event ->
            event is PropertyPurchasedEvent &&
            event.property.name == propertyName
        }
    }

    val purchaseCount = relevantGames.size
    val purchaseRate = purchaseCount.toDouble() / multiGameResult.totalGames

    // レント収入の集計
    val totalRent = relevantGames.sumOf { game ->
        game.finalState.events
            .filterIsInstance<RentPaidEvent>()
            .filter { it.property.name == propertyName }
            .sumOf { it.amount }
    }

    // ROI計算
    val roi = if (purchaseCount > 0) {
        (totalRent.toDouble() / property.price) / multiGameResult.totalGames
    } else {
        0.0
    }

    // ...
}
```

---

### 2. AssetHistory

```kotlin
/**
 * 資産推移のスナップショット
 */
data class AssetSnapshot(
    val turnNumber: Int,
    val playerName: String,
    val cash: Int,
    val totalAssets: Int,
    val propertiesCount: Int,
)

/**
 * 全ゲームの資産推移
 */
data class AssetHistory(
    val snapshots: List<AssetSnapshot>,
) {
    /**
     * プレイヤー別の平均資産推移を取得
     */
    fun getAverageAssetsByPlayer(playerName: String): List<Pair<Int, Double>> {
        return snapshots
            .filter { it.playerName == playerName }
            .groupBy { it.turnNumber }
            .mapValues { (_, snaps) ->
                snaps.map { it.totalAssets }.average()
            }
            .toList()
            .sortedBy { it.first }
    }
}
```

**データ収集**:
- 各ターン終了時にスナップショットを記録
- 全プレイヤーの状態を保存
- メモリ効率のため、N ターンごとにサンプリング可能

---

### 3. BankruptcyAnalysis

```kotlin
/**
 * 破産イベント
 */
data class BankruptcyEvent(
    val gameNumber: Int,
    val turnNumber: Int,
    val playerName: String,
    val causePlayerName: String?,  // 破産させたプレイヤー
    val lastCash: Int,
    val propertiesOwned: Int,
)

/**
 * 破産分析
 */
data class BankruptcyAnalysis(
    val bankruptcyEvents: List<BankruptcyEvent>,
    val totalBankruptcies: Int,
    val averageBankruptcyTurn: Double,
    val bankruptcyDistribution: Map<IntRange, Int>,  // ターン範囲ごとの破産数
) {
    /**
     * プレイヤー別の破産回数
     */
    fun getBankruptcyCountByPlayer(): Map<String, Int> {
        return bankruptcyEvents
            .groupingBy { it.playerName }
            .eachCount()
    }

    /**
     * 破産させたプレイヤーランキング
     */
    fun getTopBankruptcyCausers(): List<Pair<String, Int>> {
        return bankruptcyEvents
            .mapNotNull { it.causePlayerName }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
    }
}
```

---

### 4. DetailedStatistics

```kotlin
/**
 * 詳細統計（Phase 6の基本統計を拡張）
 */
data class DetailedStatistics(
    // Phase 6の基本統計
    val basicStats: GameStatistics,

    // Phase 9の詳細統計
    val propertyStatistics: List<PropertyStatistics>,
    val assetHistory: AssetHistory,
    val bankruptcyAnalysis: BankruptcyAnalysis,

    val timestamp: Long = System.currentTimeMillis(),
)
```

---

## DetailedStatisticsCalculator 設計

```kotlin
/**
 * 詳細統計を計算するクラス
 */
class DetailedStatisticsCalculator(
    private val basicCalculator: StatisticsCalculator,
) {
    /**
     * 詳細統計を計算
     */
    fun calculate(multiGameResult: MultiGameResult): DetailedStatistics {
        // 基本統計
        val basicStats = basicCalculator.calculate(multiGameResult)

        // プロパティ統計
        val propertyStats = calculatePropertyStatistics(multiGameResult)

        // 資産推移
        val assetHistory = calculateAssetHistory(multiGameResult)

        // 破産分析
        val bankruptcyAnalysis = calculateBankruptcyAnalysis(multiGameResult)

        return DetailedStatistics(
            basicStats = basicStats,
            propertyStatistics = propertyStats,
            assetHistory = assetHistory,
            bankruptcyAnalysis = bankruptcyAnalysis,
        )
    }

    /**
     * プロパティ統計を計算
     */
    private fun calculatePropertyStatistics(
        result: MultiGameResult
    ): List<PropertyStatistics> {
        // 全プロパティのリストを取得
        val allProperties = result.gameResults
            .firstOrNull()
            ?.finalState
            ?.board
            ?.spaces
            ?.filterIsInstance<Space.PropertySpace>()
            ?.map { it.property }
            ?: emptyList()

        return allProperties.map { property ->
            calculateSinglePropertyStatistics(result, property)
        }.sortedByDescending { it.roi }
    }

    /**
     * 単一プロパティの統計を計算
     */
    private fun calculateSinglePropertyStatistics(
        result: MultiGameResult,
        property: Property
    ): PropertyStatistics {
        var purchaseCount = 0
        var totalRentCollected = 0
        var winCountWhenOwned = 0

        result.gameResults.forEach { game ->
            val events = game.finalState.events

            // 購入チェック
            val wasPurchased = events.any { event ->
                event is GameEvent.PropertyPurchased &&
                event.property.name == property.name
            }

            if (wasPurchased) {
                purchaseCount++

                // レント収入の集計
                val rentFromThisProperty = events
                    .filterIsInstance<GameEvent.RentPaid>()
                    .filter { it.property.name == property.name }
                    .sumOf { it.amount }

                totalRentCollected += rentFromThisProperty

                // 勝利チェック
                val owner = game.finalState.players.find { player ->
                    player.ownedProperties.any { it.name == property.name }
                }
                if (owner?.name == game.winner) {
                    winCountWhenOwned++
                }
            }
        }

        val purchaseRate = purchaseCount.toDouble() / result.totalGames
        val averageRent = if (purchaseCount > 0) {
            totalRentCollected.toDouble() / purchaseCount
        } else {
            0.0
        }
        val winRate = if (purchaseCount > 0) {
            winCountWhenOwned.toDouble() / purchaseCount
        } else {
            0.0
        }
        val roi = if (purchaseCount > 0) {
            (totalRentCollected.toDouble() / property.price) / result.totalGames
        } else {
            0.0
        }

        return PropertyStatistics(
            propertyName = property.name,
            position = property.position,
            colorGroup = property.colorGroup.name,
            price = property.price,
            purchaseCount = purchaseCount,
            purchaseRate = purchaseRate,
            totalRentCollected = totalRentCollected,
            averageRentPerGame = averageRent,
            winRateWhenOwned = winRate,
            roi = roi,
        )
    }

    /**
     * 資産推移を計算
     */
    private fun calculateAssetHistory(
        result: MultiGameResult
    ): AssetHistory {
        val snapshots = mutableListOf<AssetSnapshot>()

        result.gameResults.forEach { game ->
            // ターンごとのスナップショットイベントを収集
            game.finalState.events
                .filterIsInstance<GameEvent.TurnCompleted>()
                .forEach { turnEvent ->
                    game.finalState.players.forEach { player ->
                        snapshots.add(
                            AssetSnapshot(
                                turnNumber = turnEvent.turnNumber,
                                playerName = player.name,
                                cash = player.money,
                                totalAssets = player.getTotalAssets(),
                                propertiesCount = player.ownedProperties.size,
                            )
                        )
                    }
                }
        }

        return AssetHistory(snapshots)
    }

    /**
     * 破産分析を計算
     */
    private fun calculateBankruptcyAnalysis(
        result: MultiGameResult
    ): BankruptcyAnalysis {
        val bankruptcyEvents = result.gameResults.flatMap { game ->
            game.finalState.events
                .filterIsInstance<GameEvent.PlayerBankrupt>()
                .map { event ->
                    BankruptcyEvent(
                        gameNumber = game.gameNumber,
                        turnNumber = game.finalState.turnNumber,
                        playerName = event.player.name,
                        causePlayerName = event.creditor?.name,
                        lastCash = event.player.money,
                        propertiesOwned = event.player.ownedProperties.size,
                    )
                }
        }

        val averageTurn = if (bankruptcyEvents.isNotEmpty()) {
            bankruptcyEvents.map { it.turnNumber }.average()
        } else {
            0.0
        }

        // ターン範囲ごとの分布
        val distribution = bankruptcyEvents
            .groupBy { it.turnNumber / 10 * 10 }  // 10ターンごと
            .mapKeys { (key, _) -> key..(key + 9) }
            .mapValues { it.value.size }

        return BankruptcyAnalysis(
            bankruptcyEvents = bankruptcyEvents,
            totalBankruptcies = bankruptcyEvents.size,
            averageBankruptcyTurn = averageTurn,
            bankruptcyDistribution = distribution,
        )
    }
}
```

---

## 可視化の拡張

### 1. LineChartGenerator（折れ線グラフ）

```kotlin
class LineChartGenerator {
    private val width = 800
    private val height = 400
    private val padding = 60

    fun generate(data: LineChartData): String {
        return buildString {
            appendLine("<svg width=\"$width\" height=\"$height\" xmlns=\"http://www.w3.org/2000/svg\">")
            // 背景
            appendLine(generateBackground())
            // タイトル
            appendLine(generateTitle(data.title))
            // 軸
            appendLine(generateAxes(data))
            // ライン（各プレイヤー）
            data.lines.forEach { line ->
                appendLine(generateLine(line))
            }
            // 凡例
            appendLine(generateLegend(data.lines))
            appendLine("</svg>")
        }
    }

    private fun generateLine(line: LineChartData.Line): String {
        val points = line.points
            .map { (x, y) ->
                val svgX = xToSvg(x)
                val svgY = yToSvg(y)
                "$svgX,$svgY"
            }
            .joinToString(" ")

        return """<polyline points="$points"
                           stroke="${line.color}"
                           fill="none"
                           stroke-width="2"/>"""
    }
}

data class LineChartData(
    val title: String,
    val lines: List<Line>,
) : ChartData {
    data class Line(
        val label: String,
        val points: List<Pair<Int, Double>>,  // (x, y)
        val color: String,
    )
}
```

---

## イベント拡張

Phase 9で必要な新しいイベント（既存のGameEventに追加）:

```kotlin
sealed class GameEvent {
    // 既存のイベント...

    /**
     * ターン完了イベント（Phase 9で追加）
     */
    data class TurnCompleted(
        val turnNumber: Int,
        val activePlayerName: String,
    ) : GameEvent()
}
```

---

## パフォーマンス最適化

### メモリ管理

**問題**: 資産推移の記録でメモリ使用量が増加

**対策**:
1. サンプリング: N ターンごとに記録
2. 集約: 平均値のみ保持
3. オプション: 詳細記録の有効/無効を選択可能

```kotlin
data class DetailedStatsConfig(
    val enableAssetHistory: Boolean = true,
    val assetSnapshotInterval: Int = 5,  // 5ターンごと
)
```

---

## テスト戦略

### 1. 統計計算のテスト

```kotlin
@Test
fun `プロパティ統計が正確に計算される`() {
    val result = createTestMultiGameResult()
    val calculator = DetailedStatisticsCalculator()

    val stats = calculator.calculate(result)

    stats.propertyStatistics.find { it.propertyName == "Mediterranean Avenue" }?.let {
        it.purchaseCount shouldBe 50
        it.purchaseRate shouldBeGreaterThan 0.4
        it.roi shouldBeGreaterThan 0.0
    }
}
```

---

**作成日**: 2025-11-16
