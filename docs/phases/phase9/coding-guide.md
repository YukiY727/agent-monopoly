# Phase 9: 詳細統計の追加 - コーディングガイド

---

## ディレクトリ構造

```
src/main/kotlin/com/monopoly/
├── statistics/
│   ├── GameStatistics.kt           (既存)
│   ├── StatisticsCalculator.kt     (既存)
│   ├── PropertyStatistics.kt       (新規)
│   ├── AssetHistory.kt             (新規)
│   ├── BankruptcyAnalysis.kt       (新規)
│   ├── DetailedStatistics.kt       (新規)
│   └── DetailedStatisticsCalculator.kt (新規)
├── visualization/
│   ├── LineChartGenerator.kt       (新規)
│   ├── LineChartData.kt            (新規)
│   └── StatisticsReportGenerator.kt (更新)
└── ...
```

---

## 実装順序

### Step 1: データモデルの実装

#### 1.1 PropertyStatistics.kt

```kotlin
package com.monopoly.statistics

/**
 * プロパティ別の統計情報
 *
 * @property propertyName プロパティ名
 * @property position ボード上の位置
 * @property colorGroup 色グループ
 * @property price 購入価格
 * @property purchaseCount 購入された回数
 * @property purchaseRate 購入された割合 (0.0-1.0)
 * @property totalRentCollected 総レント収入
 * @property averageRentPerGame ゲームあたりの平均レント収入
 * @property maxRentInSingleGame 単一ゲームでの最大レント収入
 * @property winRateWhenOwned このプロパティを所有した時の勝率
 * @property avgTurnsHeldByWinner 勝者が保持していた平均ターン数
 * @property roi 投資利益率 (Return on Investment)
 */
data class PropertyStatistics(
    val propertyName: String,
    val position: Int,
    val colorGroup: String,
    val price: Int,
    val purchaseCount: Int,
    val purchaseRate: Double,
    val totalRentCollected: Int,
    val averageRentPerGame: Double,
    val maxRentInSingleGame: Int,
    val winRateWhenOwned: Double,
    val avgTurnsHeldByWinner: Double,
    val roi: Double,
)
```

#### 1.2 AssetHistory.kt

```kotlin
package com.monopoly.statistics

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
     *
     * @param playerName プレイヤー名
     * @return ターン数と平均資産額のペアのリスト
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

    /**
     * 全プレイヤーの名前を取得
     */
    fun getPlayerNames(): Set<String> {
        return snapshots.map { it.playerName }.toSet()
    }
}
```

#### 1.3 BankruptcyAnalysis.kt

```kotlin
package com.monopoly.statistics

/**
 * 破産イベント
 */
data class BankruptcyEvent(
    val gameNumber: Int,
    val turnNumber: Int,
    val playerName: String,
    val causePlayerName: String?,
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
    val bankruptcyDistribution: Map<IntRange, Int>,
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

#### 1.4 DetailedStatistics.kt

```kotlin
package com.monopoly.statistics

/**
 * 詳細統計（Phase 6の基本統計を拡張）
 */
data class DetailedStatistics(
    val basicStats: GameStatistics,
    val propertyStatistics: List<PropertyStatistics>,
    val assetHistory: AssetHistory,
    val bankruptcyAnalysis: BankruptcyAnalysis,
    val timestamp: Long = System.currentTimeMillis(),
)
```

---

### Step 2: DetailedStatisticsCalculator の実装

```kotlin
package com.monopoly.statistics

import com.monopoly.domain.model.GameEvent
import com.monopoly.domain.model.Property
import com.monopoly.domain.model.Space
import com.monopoly.simulation.MultiGameResult

/**
 * 詳細統計を計算するクラス
 */
class DetailedStatisticsCalculator(
    private val basicCalculator: StatisticsCalculator = StatisticsCalculator(),
) {
    /**
     * 詳細統計を計算
     */
    fun calculate(multiGameResult: MultiGameResult): DetailedStatistics {
        val basicStats = basicCalculator.calculate(multiGameResult)
        val propertyStats = calculatePropertyStatistics(multiGameResult)
        val assetHistory = calculateAssetHistory(multiGameResult)
        val bankruptcyAnalysis = calculateBankruptcyAnalysis(multiGameResult)

        return DetailedStatistics(
            basicStats = basicStats,
            propertyStatistics = propertyStats,
            assetHistory = assetHistory,
            bankruptcyAnalysis = bankruptcyAnalysis,
        )
    }

    private fun calculatePropertyStatistics(
        result: MultiGameResult,
    ): List<PropertyStatistics> {
        val allProperties = extractAllProperties(result)
        return allProperties.map { property ->
            calculateSinglePropertyStatistics(result, property)
        }.sortedByDescending { it.roi }
    }

    private fun extractAllProperties(result: MultiGameResult): List<Property> {
        return result.gameResults
            .firstOrNull()
            ?.finalState
            ?.board
            ?.spaces
            ?.filterIsInstance<Space.PropertySpace>()
            ?.map { it.property }
            ?: emptyList()
    }

    private fun calculateSinglePropertyStatistics(
        result: MultiGameResult,
        property: Property,
    ): PropertyStatistics {
        var purchaseCount = 0
        var totalRentCollected = 0
        var maxRentInGame = 0
        var winCountWhenOwned = 0
        var totalTurnsHeldByWinner = 0

        result.gameResults.forEach { game ->
            val events = game.finalState.events

            val wasPurchased = events.any { event ->
                event is GameEvent.PropertyPurchased &&
                    event.property.name == property.name
            }

            if (wasPurchased) {
                purchaseCount++

                val rentInThisGame = events
                    .filterIsInstance<GameEvent.RentPaid>()
                    .filter { it.property.name == property.name }
                    .sumOf { it.amount }

                totalRentCollected += rentInThisGame
                maxRentInGame = maxOf(maxRentInGame, rentInThisGame)

                val winner = game.finalState.players.find { it.name == game.winner }
                if (winner?.ownedProperties?.any { it.name == property.name } == true) {
                    winCountWhenOwned++
                    totalTurnsHeldByWinner += game.totalTurns
                }
            }
        }

        return PropertyStatistics(
            propertyName = property.name,
            position = property.position,
            colorGroup = property.colorGroup.name,
            price = property.price,
            purchaseCount = purchaseCount,
            purchaseRate = purchaseCount.toDouble() / result.totalGames,
            totalRentCollected = totalRentCollected,
            averageRentPerGame = if (purchaseCount > 0) {
                totalRentCollected.toDouble() / purchaseCount
            } else {
                0.0
            },
            maxRentInSingleGame = maxRentInGame,
            winRateWhenOwned = if (purchaseCount > 0) {
                winCountWhenOwned.toDouble() / purchaseCount
            } else {
                0.0
            },
            avgTurnsHeldByWinner = if (winCountWhenOwned > 0) {
                totalTurnsHeldByWinner.toDouble() / winCountWhenOwned
            } else {
                0.0
            },
            roi = if (purchaseCount > 0) {
                (totalRentCollected.toDouble() / property.price) / result.totalGames
            } else {
                0.0
            },
        )
    }

    private fun calculateAssetHistory(
        result: MultiGameResult,
    ): AssetHistory {
        val snapshots = result.gameResults.flatMap { game ->
            // 各ターンのスナップショットを作成
            // 簡略化: 最終状態のみを記録（将来的にターンごとに拡張可能）
            game.finalState.players.map { player ->
                AssetSnapshot(
                    turnNumber = game.totalTurns,
                    playerName = player.name,
                    cash = player.money,
                    totalAssets = player.getTotalAssets(),
                    propertiesCount = player.ownedProperties.size,
                )
            }
        }

        return AssetHistory(snapshots)
    }

    private fun calculateBankruptcyAnalysis(
        result: MultiGameResult,
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

        val distribution = bankruptcyEvents
            .groupBy { it.turnNumber / 10 * 10 }
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

### Step 3: LineChartGenerator の実装

```kotlin
package com.monopoly.visualization

/**
 * 折れ線グラフを生成するクラス
 */
class LineChartGenerator {
    private val width = 800
    private val height = 400
    private val padding = 60

    fun generate(data: LineChartData): String {
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        return buildString {
            appendLine("<svg width=\"$width\" height=\"$height\" xmlns=\"http://www.w3.org/2000/svg\">")
            appendLine("  <rect width=\"$width\" height=\"$height\" fill=\"#ffffff\"/>")

            appendLine(generateTitle(data.title))
            appendLine(generateAxes())

            data.lines.forEach { line ->
                appendLine(generateLine(line, chartWidth, chartHeight))
            }

            appendLine(generateLegend(data.lines))
            appendLine("</svg>")
        }
    }

    private fun generateTitle(title: String): String {
        return "  <text x=\"${width / 2}\" y=\"30\" text-anchor=\"middle\" font-size=\"18\" font-weight=\"bold\">$title</text>"
    }

    private fun generateAxes(): String {
        return buildString {
            appendLine("  <line x1=\"$padding\" y1=\"$padding\" x2=\"$padding\" y2=\"${height - padding}\" stroke=\"#333\" stroke-width=\"2\"/>")
            appendLine("  <line x1=\"$padding\" y1=\"${height - padding}\" x2=\"${width - padding}\" y2=\"${height - padding}\" stroke=\"#333\" stroke-width=\"2\"/>")
            appendLine("  <text x=\"20\" y=\"${height / 2}\" text-anchor=\"middle\" font-size=\"12\" transform=\"rotate(-90, 20, ${height / 2})\">Assets</text>")
            appendLine("  <text x=\"${width / 2}\" y=\"${height - 20}\" text-anchor=\"middle\" font-size=\"12\">Turns</text>")
        }
    }

    private fun generateLine(
        line: LineChartData.Line,
        chartWidth: Int,
        chartHeight: Int,
    ): String {
        if (line.points.isEmpty()) return ""

        val maxX = line.points.maxOfOrNull { it.first } ?: 1
        val maxY = line.points.maxOfOrNull { it.second } ?: 1.0

        val points = line.points.map { (x, y) ->
            val svgX = padding + (x.toDouble() / maxX * chartWidth).toInt()
            val svgY = height - padding - (y / maxY * chartHeight).toInt()
            "$svgX,$svgY"
        }.joinToString(" ")

        return """  <polyline points="$points" stroke="${line.color}" fill="none" stroke-width="2"/>"""
    }

    private fun generateLegend(lines: List<LineChartData.Line>): String {
        return buildString {
            lines.forEachIndexed { index, line ->
                val y = 60 + index * 20
                appendLine("  <rect x=\"${width - 150}\" y=\"$y\" width=\"15\" height=\"15\" fill=\"${line.color}\"/>")
                appendLine("  <text x=\"${width - 130}\" y=\"${y + 12}\" font-size=\"12\">${line.label}</text>")
            }
        }
    }
}
```

---

### Step 4: LineChartData の実装

```kotlin
package com.monopoly.visualization

data class LineChartData(
    val title: String,
    val lines: List<Line>,
) : ChartData {
    data class Line(
        val label: String,
        val points: List<Pair<Int, Double>>,
        val color: String,
    )
}
```

---

### Step 5: StatisticsReportGenerator の更新

既存の `StatisticsReportGenerator.kt` に詳細統計セクションを追加：

```kotlin
// DetailedStatistics を受け取るように拡張
fun generateDetailed(statistics: DetailedStatistics): String {
    return buildString {
        // 既存のHTML生成...

        // プロパティランキングセクション
        appendLine(generatePropertyRankingSection(statistics))

        // 資産推移セクション
        appendLine(generateAssetHistorySection(statistics))

        // 破産分析セクション
        appendLine(generateBankruptcySection(statistics))
    }
}

private fun generatePropertyRankingSection(statistics: DetailedStatistics): String {
    // プロパティ別ROIランキング表を生成
}

private fun generateAssetHistorySection(statistics: DetailedStatistics): String {
    // 折れ線グラフを生成
}
```

---

## コーディング規約

### 1. Null安全性

```kotlin
// Good
val averageRent = if (purchaseCount > 0) {
    totalRent.toDouble() / purchaseCount
} else {
    0.0
}

// Bad: ゼロ除算のリスク
val averageRent = totalRent.toDouble() / purchaseCount
```

### 2. イミュータブルなデータ

```kotlin
// Good: data class は不変
data class PropertyStatistics(val name: String, val roi: Double)

// Bad: var は避ける
data class PropertyStatistics(var name: String, var roi: Double)
```

---

## デバッグのヒント

### 統計が0になる場合

```kotlin
// イベントログを確認
println("Total events: ${game.finalState.events.size}")
println("PropertyPurchased events: ${game.finalState.events.filterIsInstance<GameEvent.PropertyPurchased>().size}")
```

---

**作成日**: 2025-11-16
