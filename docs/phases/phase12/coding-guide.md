# Phase 12: 高度な可視化 - コーディングガイド

## 実装順序

1. BoardStatistics + BoardStatisticsCalculator
2. HeatmapData + HeatmapGenerator
3. RadarChartData + RadarChartGenerator
4. ScatterPlotData + ScatterPlotGenerator
5. ResearchReportGenerator
6. PdfExporter（オプション）
7. CLI拡張

## 1. BoardStatistics

**ファイル**: `src/main/kotlin/com/monopoly/statistics/BoardStatistics.kt`

```kotlin
package com.monopoly.statistics

/**
 * ボード統計
 *
 * @property positionStats 位置ごとの統計
 * @property totalGames 総ゲーム数
 */
data class BoardStatistics(
    val positionStats: Map<Int, PositionStatistics>,
    val totalGames: Int,
) {
    /**
     * 位置統計
     *
     * @property position ボード上の位置（0-39）
     * @property landedCount 停止回数
     * @property totalRentCollected 総レント収入
     * @property ownershipDistribution 所有者分布（プレイヤー名 → 所有回数）
     */
    data class PositionStatistics(
        val position: Int,
        val landedCount: Int,
        val totalRentCollected: Double,
        val ownershipDistribution: Map<String, Int>,
    ) {
        /**
         * ゲームあたりの平均停止回数
         */
        fun averageLandedPerGame(totalGames: Int): Double =
            landedCount.toDouble() / totalGames

        /**
         * ゲームあたりの平均レント収入
         */
        fun averageRentPerGame(totalGames: Int): Double =
            totalRentCollected / totalGames
    }

    /**
     * 最も停止回数が多い位置
     */
    fun mostLandedPosition(): PositionStatistics? =
        positionStats.values.maxByOrNull { it.landedCount }

    /**
     * 最も収益性が高い位置
     */
    fun mostProfitablePosition(): PositionStatistics? =
        positionStats.values.maxByOrNull { it.totalRentCollected }
}
```

## 2. BoardStatisticsCalculator

**ファイル**: `src/main/kotlin/com/monopoly/statistics/BoardStatisticsCalculator.kt`

```kotlin
package com.monopoly.statistics

import com.monopoly.simulation.MultiGameResult
import com.monopoly.domain.model.GameEvent

/**
 * ボード統計を計算
 */
class BoardStatisticsCalculator {
    /**
     * 複数ゲームからボード統計を計算
     */
    fun calculate(multiGameResult: MultiGameResult): BoardStatistics {
        val positionStatsMap = mutableMapOf<Int, MutablePositionStats>()

        multiGameResult.results.forEach { gameResult ->
            gameResult.events.forEach { event ->
                when (event.type) {
                    "player_moved" -> {
                        val position = event.data["to_position"] as? Int ?: return@forEach
                        val stats = positionStatsMap.getOrPut(position) {
                            MutablePositionStats()
                        }
                        stats.landedCount++
                    }
                    "rent_paid" -> {
                        val position = event.data["property_position"] as? Int ?: return@forEach
                        val amount = (event.data["amount"] as? Number)?.toDouble() ?: 0.0
                        val stats = positionStatsMap.getOrPut(position) {
                            MutablePositionStats()
                        }
                        stats.totalRentCollected += amount
                    }
                    "property_purchased" -> {
                        val position = event.data["property_position"] as? Int ?: return@forEach
                        val buyer = event.data["buyer"] as? String ?: return@forEach
                        val stats = positionStatsMap.getOrPut(position) {
                            MutablePositionStats()
                        }
                        stats.ownershipDistribution[buyer] =
                            stats.ownershipDistribution.getOrDefault(buyer, 0) + 1
                    }
                }
            }
        }

        return BoardStatistics(
            positionStats = positionStatsMap.mapValues { (position, stats) ->
                BoardStatistics.PositionStatistics(
                    position = position,
                    landedCount = stats.landedCount,
                    totalRentCollected = stats.totalRentCollected,
                    ownershipDistribution = stats.ownershipDistribution.toMap()
                )
            },
            totalGames = multiGameResult.results.size
        )
    }

    /**
     * 内部用の可変統計データ
     */
    private data class MutablePositionStats(
        var landedCount: Int = 0,
        var totalRentCollected: Double = 0.0,
        val ownershipDistribution: MutableMap<String, Int> = mutableMapOf()
    )
}
```

## 3. HeatmapData

**ファイル**: `src/main/kotlin/com/monopoly/visualization/HeatmapData.kt`

```kotlin
package com.monopoly.visualization

/**
 * ヒートマップデータ
 *
 * @property title タイトル
 * @property cells セルのリスト
 * @property minValue 最小値
 * @property maxValue 最大値
 */
data class HeatmapData(
    val title: String,
    val cells: List<Cell>,
    val minValue: Double = cells.minOfOrNull { it.value } ?: 0.0,
    val maxValue: Double = cells.maxOfOrNull { it.value } ?: 1.0,
) {
    /**
     * ヒートマップのセル
     *
     * @property label ラベル
     * @property value 値
     * @property position ボード上の位置（0-39）
     */
    data class Cell(
        val label: String,
        val value: Double,
        val position: Int,
    )
}
```

## 4. HeatmapGenerator

**ファイル**: `src/main/kotlin/com/monopoly/visualization/HeatmapGenerator.kt`

```kotlin
package com.monopoly.visualization

/**
 * ヒートマップSVGを生成
 */
class HeatmapGenerator {
    /**
     * ヒートマップSVGを生成
     */
    fun generate(data: HeatmapData): String {
        val cellSize = 50
        val fontSize = 10
        val gridSize = 11 // モノポリーボードは11x11グリッド

        val positions = calculateBoardPositions(gridSize, cellSize)

        return buildString {
            val svgWidth = gridSize * cellSize
            val svgHeight = gridSize * cellSize

            appendLine("<svg width=\"$svgWidth\" height=\"$svgHeight\" xmlns=\"http://www.w3.org/2000/svg\">")

            // 背景
            appendLine("  <rect width=\"$svgWidth\" height=\"$svgHeight\" fill=\"#f0f0f0\" />")

            // セルを描画
            data.cells.forEach { cell ->
                val (x, y) = positions.getOrDefault(cell.position, Pair(0, 0))
                val intensity = if (data.maxValue > data.minValue) {
                    (cell.value - data.minValue) / (data.maxValue - data.minValue)
                } else {
                    0.0
                }
                val color = interpolateColor(intensity)

                // セルの矩形
                appendLine("  <rect x=\"$x\" y=\"$y\" width=\"$cellSize\" height=\"$cellSize\" " +
                    "fill=\"$color\" stroke=\"#333\" stroke-width=\"1\" />")

                // ラベル
                val textX = x + cellSize / 2
                val textY = y + cellSize / 2
                appendLine("  <text x=\"$textX\" y=\"$textY\" font-size=\"$fontSize\" " +
                    "text-anchor=\"middle\" dominant-baseline=\"middle\">${cell.label}</text>")
            }

            appendLine("</svg>")
        }
    }

    /**
     * ボード上の位置からSVG座標を計算
     *
     * モノポリーボードは時計回り:
     * - 0-10: 下辺（右→左）
     * - 11-20: 左辺（下→上）
     * - 21-30: 上辺（左→右）
     * - 31-39: 右辺（上→下）
     */
    private fun calculateBoardPositions(gridSize: Int, cellSize: Int): Map<Int, Pair<Int, Int>> {
        val positions = mutableMapOf<Int, Pair<Int, Int>>()

        // 下辺（0-10）
        for (i in 0..10) {
            val x = (gridSize - 1 - i) * cellSize
            val y = (gridSize - 1) * cellSize
            positions[i] = Pair(x, y)
        }

        // 左辺（11-20）
        for (i in 11..20) {
            val x = 0
            val y = (gridSize - 1 - (i - 10)) * cellSize
            positions[i] = Pair(x, y)
        }

        // 上辺（21-30）
        for (i in 21..30) {
            val x = (i - 20) * cellSize
            val y = 0
            positions[i] = Pair(x, y)
        }

        // 右辺（31-39）
        for (i in 31..39) {
            val x = (gridSize - 1) * cellSize
            val y = (i - 30) * cellSize
            positions[i] = Pair(x, y)
        }

        return positions
    }

    /**
     * 強度（0.0-1.0）から色を補間
     *
     * 白 → 薄い青 → 青 → 濃い青
     */
    private fun interpolateColor(intensity: Double): String {
        val clampedIntensity = intensity.coerceIn(0.0, 1.0)

        val r = (255 * (1 - clampedIntensity)).toInt()
        val g = (255 * (1 - clampedIntensity)).toInt()
        val b = 255

        return String.format("#%02x%02x%02x", r, g, b)
    }
}
```

## 5. RadarChartData

**ファイル**: `src/main/kotlin/com/monopoly/visualization/RadarChartData.kt`

```kotlin
package com.monopoly.visualization

/**
 * レーダーチャートデータ
 *
 * @property title タイトル
 * @property axes 軸のリスト
 * @property series データ系列のリスト
 */
data class RadarChartData(
    val title: String,
    val axes: List<Axis>,
    val series: List<Series>,
) {
    /**
     * 軸
     *
     * @property label ラベル
     * @property maxValue 最大値
     */
    data class Axis(
        val label: String,
        val maxValue: Double = 1.0,
    )

    /**
     * データ系列
     *
     * @property label ラベル（戦略名など）
     * @property values 各軸の値（0.0-1.0に正規化）
     * @property color 色
     */
    data class Series(
        val label: String,
        val values: List<Double>,
        val color: String,
    )
}
```

## 6. RadarChartGenerator

**ファイル**: `src/main/kotlin/com/monopoly/visualization/RadarChartGenerator.kt`

```kotlin
package com.monopoly.visualization

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

/**
 * レーダーチャートSVGを生成
 */
class RadarChartGenerator {
    /**
     * レーダーチャートSVGを生成
     */
    fun generate(data: RadarChartData): String {
        val width = 600
        val height = 600
        val centerX = width / 2.0
        val centerY = height / 2.0
        val radius = 200.0
        val numAxes = data.axes.size

        return buildString {
            appendLine("<svg width=\"$width\" height=\"$height\" xmlns=\"http://www.w3.org/2000/svg\">")

            // 背景
            appendLine("  <rect width=\"$width\" height=\"$height\" fill=\"white\" />")

            // 同心円を描画（ガイドライン）
            for (i in 1..5) {
                val r = radius * i / 5
                appendLine("  <circle cx=\"$centerX\" cy=\"$centerY\" r=\"$r\" " +
                    "fill=\"none\" stroke=\"#ddd\" stroke-width=\"1\" />")
            }

            // 軸を描画
            data.axes.forEachIndexed { index, axis ->
                val angle = 2 * PI * index / numAxes - PI / 2
                val x = centerX + radius * cos(angle)
                val y = centerY + radius * sin(angle)

                appendLine("  <line x1=\"$centerX\" y1=\"$centerY\" x2=\"$x\" y2=\"$y\" " +
                    "stroke=\"#999\" stroke-width=\"1\" />")

                // 軸ラベル
                val labelX = centerX + (radius + 20) * cos(angle)
                val labelY = centerY + (radius + 20) * sin(angle)
                appendLine("  <text x=\"$labelX\" y=\"$labelY\" font-size=\"12\" " +
                    "text-anchor=\"middle\" dominant-baseline=\"middle\">${axis.label}</text>")
            }

            // 各戦略のポリゴンを描画
            data.series.forEach { series ->
                val points = series.values.mapIndexed { index, value ->
                    val angle = 2 * PI * index / numAxes - PI / 2
                    val r = radius * value.coerceIn(0.0, 1.0)
                    val x = centerX + r * cos(angle)
                    val y = centerY + r * sin(angle)
                    "$x,$y"
                }.joinToString(" ")

                appendLine("  <polygon points=\"$points\" fill=\"${series.color}\" " +
                    "opacity=\"0.3\" stroke=\"${series.color}\" stroke-width=\"2\" />")
            }

            // 凡例
            data.series.forEachIndexed { index, series ->
                val legendY = 30 + index * 25
                appendLine("  <rect x=\"20\" y=\"${legendY - 10}\" width=\"15\" height=\"15\" " +
                    "fill=\"${series.color}\" />")
                appendLine("  <text x=\"40\" y=\"$legendY\" font-size=\"14\">${series.label}</text>")
            }

            // タイトル
            appendLine("  <text x=\"$centerX\" y=\"30\" font-size=\"18\" font-weight=\"bold\" " +
                "text-anchor=\"middle\">${data.title}</text>")

            appendLine("</svg>")
        }
    }
}
```

## 7. ScatterPlotData

**ファイル**: `src/main/kotlin/com/monopoly/visualization/ScatterPlotData.kt`

```kotlin
package com.monopoly.visualization

/**
 * 散布図データ
 *
 * @property title タイトル
 * @property xAxisLabel X軸ラベル
 * @property yAxisLabel Y軸ラベル
 * @property points データポイントのリスト
 */
data class ScatterPlotData(
    val title: String,
    val xAxisLabel: String,
    val yAxisLabel: String,
    val points: List<Point>,
) {
    /**
     * データポイント
     *
     * @property label ラベル
     * @property x X座標の値
     * @property y Y座標の値
     * @property color 色
     */
    data class Point(
        val label: String,
        val x: Double,
        val y: Double,
        val color: String,
    )
}
```

## 8. ScatterPlotGenerator

**ファイル**: `src/main/kotlin/com/monopoly/visualization/ScatterPlotGenerator.kt`

```kotlin
package com.monopoly.visualization

/**
 * 散布図SVGを生成
 */
class ScatterPlotGenerator {
    /**
     * 散布図SVGを生成
     */
    fun generate(data: ScatterPlotData): String {
        val width = 700
        val height = 500
        val marginLeft = 80.0
        val marginRight = 50.0
        val marginTop = 50.0
        val marginBottom = 80.0

        val plotWidth = width - marginLeft - marginRight
        val plotHeight = height - marginTop - marginBottom

        val xMin = data.points.minOfOrNull { it.x } ?: 0.0
        val xMax = data.points.maxOfOrNull { it.x } ?: 1.0
        val yMin = data.points.minOfOrNull { it.y } ?: 0.0
        val yMax = data.points.maxOfOrNull { it.y } ?: 1.0

        return buildString {
            appendLine("<svg width=\"$width\" height=\"$height\" xmlns=\"http://www.w3.org/2000/svg\">")

            // 背景
            appendLine("  <rect width=\"$width\" height=\"$height\" fill=\"white\" />")

            // プロット領域の背景
            appendLine("  <rect x=\"$marginLeft\" y=\"$marginTop\" width=\"$plotWidth\" height=\"$plotHeight\" " +
                "fill=\"#f9f9f9\" stroke=\"#333\" stroke-width=\"1\" />")

            // X軸
            appendLine("  <line x1=\"$marginLeft\" y1=\"${marginTop + plotHeight}\" " +
                "x2=\"${marginLeft + plotWidth}\" y2=\"${marginTop + plotHeight}\" " +
                "stroke=\"black\" stroke-width=\"2\" />")

            // Y軸
            appendLine("  <line x1=\"$marginLeft\" y1=\"$marginTop\" " +
                "x2=\"$marginLeft\" y2=\"${marginTop + plotHeight}\" " +
                "stroke=\"black\" stroke-width=\"2\" />")

            // データポイントを描画
            data.points.forEach { point ->
                val x = marginLeft + (point.x - xMin) / (xMax - xMin) * plotWidth
                val y = marginTop + plotHeight - (point.y - yMin) / (yMax - yMin) * plotHeight

                appendLine("  <circle cx=\"$x\" cy=\"$y\" r=\"8\" fill=\"${point.color}\" " +
                    "stroke=\"#333\" stroke-width=\"1\" />")
                appendLine("  <text x=\"${x + 12}\" y=\"${y + 4}\" font-size=\"12\">${point.label}</text>")
            }

            // 軸ラベル
            appendLine("  <text x=\"${marginLeft + plotWidth / 2}\" y=\"${height - 20}\" " +
                "font-size=\"14\" text-anchor=\"middle\">${data.xAxisLabel}</text>")

            appendLine("  <text x=\"20\" y=\"${marginTop + plotHeight / 2}\" font-size=\"14\" " +
                "text-anchor=\"middle\" transform=\"rotate(-90 20 ${marginTop + plotHeight / 2})\">" +
                "${data.yAxisLabel}</text>")

            // タイトル
            appendLine("  <text x=\"${width / 2}\" y=\"30\" font-size=\"18\" font-weight=\"bold\" " +
                "text-anchor=\"middle\">${data.title}</text>")

            // 軸の目盛り
            for (i in 0..5) {
                val xTick = marginLeft + i * plotWidth / 5
                val xValue = xMin + i * (xMax - xMin) / 5
                appendLine("  <text x=\"$xTick\" y=\"${marginTop + plotHeight + 20}\" " +
                    "font-size=\"10\" text-anchor=\"middle\">${String.format("%.2f", xValue)}</text>")

                val yTick = marginTop + plotHeight - i * plotHeight / 5
                val yValue = yMin + i * (yMax - yMin) / 5
                appendLine("  <text x=\"${marginLeft - 10}\" y=\"$yTick\" " +
                    "font-size=\"10\" text-anchor=\"end\">${String.format("%.0f", yValue)}</text>")
            }

            appendLine("</svg>")
        }
    }
}
```

## 9. ResearchReportGenerator

**ファイル**: `src/main/kotlin/com/monopoly/visualization/ResearchReportGenerator.kt`

```kotlin
package com.monopoly.visualization

import com.monopoly.statistics.BoardStatistics
import com.monopoly.statistics.DetailedStatistics
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 研究論文用レポートを生成
 */
class ResearchReportGenerator(
    private val heatmapGenerator: HeatmapGenerator = HeatmapGenerator(),
    private val radarChartGenerator: RadarChartGenerator = RadarChartGenerator(),
    private val scatterPlotGenerator: ScatterPlotGenerator = ScatterPlotGenerator(),
    private val barChartGenerator: BarChartGenerator = BarChartGenerator(),
    private val lineChartGenerator: LineChartGenerator = LineChartGenerator(),
) {
    /**
     * 研究レポートHTMLを生成
     */
    fun generate(
        detailedStats: DetailedStatistics,
        boardStats: BoardStatistics
    ): String {
        return buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html lang=\"en\">")
            appendLine("<head>")
            appendLine(generateHead())
            appendLine("</head>")
            appendLine("<body>")
            appendLine("  <div class=\"container\">")

            appendLine(generateTitle())
            appendLine(generateAbstract(detailedStats))
            appendLine(generateMethodology(detailedStats))
            appendLine(generateResults(detailedStats, boardStats))
            appendLine(generateDiscussion())
            appendLine(generateConclusion())
            appendLine(generateReferences())

            appendLine("  </div>")
            appendLine("</body>")
            appendLine("</html>")
        }
    }

    /**
     * ファイルに保存
     */
    fun saveToFile(
        detailedStats: DetailedStatistics,
        boardStats: BoardStatistics,
        filename: String = generateFilename()
    ): File {
        val file = File(filename)
        file.writeText(generate(detailedStats, boardStats))
        return file
    }

    private fun generateHead(): String {
        return """
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Monopoly Strategy Analysis - Research Report</title>
        <style>
        ${generateStyles()}
        </style>
        """.trimIndent()
    }

    private fun generateStyles(): String {
        return """
        body {
            font-family: 'Times New Roman', serif;
            background: #f5f5f5;
            margin: 0;
            padding: 20px;
            line-height: 1.6;
        }
        .container {
            max-width: 900px;
            margin: 0 auto;
            background: white;
            padding: 60px;
            box-shadow: 0 0 20px rgba(0,0,0,0.1);
        }
        h1 {
            text-align: center;
            font-size: 24pt;
            margin-bottom: 10px;
        }
        .authors {
            text-align: center;
            font-style: italic;
            margin-bottom: 30px;
        }
        .section {
            margin: 40px 0;
        }
        .section h2 {
            font-size: 18pt;
            border-bottom: 2px solid #333;
            padding-bottom: 5px;
            margin-top: 30px;
        }
        .section h3 {
            font-size: 14pt;
            margin-top: 20px;
        }
        .abstract {
            background: #f9f9f9;
            padding: 20px;
            border-left: 4px solid #333;
            margin: 30px 0;
        }
        .chart-container {
            text-align: center;
            margin: 30px 0;
            padding: 20px;
            background: #fafafa;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin: 20px 0;
            font-size: 11pt;
        }
        th, td {
            padding: 8px;
            text-align: left;
            border: 1px solid #ddd;
        }
        th {
            background-color: #f0f0f0;
            font-weight: bold;
        }
        .number {
            text-align: right;
        }
        """.trimIndent()
    }

    private fun generateTitle(): String {
        return """
        <h1>Strategic Decision-Making in Monopoly:<br>A Computational Analysis</h1>
        <div class="authors">
            Generated by Monopoly Simulator v1.0
        </div>
        """.trimIndent()
    }

    private fun generateAbstract(detailedStats: DetailedStatistics): String {
        val totalGames = detailedStats.basicStats.totalGames
        val numStrategies = detailedStats.basicStats.playerStats.size

        return """
        <div class="abstract">
            <h2>Abstract</h2>
            <p>
            This study presents a computational analysis of strategic decision-making in the board game Monopoly.
            We simulated $totalGames games with $numStrategies different strategies to evaluate their effectiveness
            under various conditions. Our results provide insights into optimal property acquisition strategies,
            resource management, and risk assessment in competitive scenarios.
            </p>
        </div>
        """.trimIndent()
    }

    private fun generateMethodology(detailedStats: DetailedStatistics): String {
        val strategies = detailedStats.basicStats.playerStats.keys.joinToString(", ")

        return """
        <div class="section">
            <h2>1. Methodology</h2>
            <h3>1.1 Simulation Setup</h3>
            <p>
            We conducted ${detailedStats.basicStats.totalGames} Monte Carlo simulations of Monopoly games
            using a custom game engine. Each simulation followed standard Monopoly rules with deterministic
            property prices and rent values.
            </p>
            <h3>1.2 Strategies Evaluated</h3>
            <p>
            The following strategies were tested: $strategies.
            Each strategy represents a different approach to property acquisition and resource management.
            </p>
            <h3>1.3 Metrics</h3>
            <p>
            We measured win rate, average final assets, property ownership patterns, and bankruptcy rates
            for each strategy across all simulations.
            </p>
        </div>
        """.trimIndent()
    }

    private fun generateResults(
        detailedStats: DetailedStatistics,
        boardStats: BoardStatistics
    ): String {
        return buildString {
            appendLine("<div class=\"section\">")
            appendLine("  <h2>2. Results</h2>")

            // 2.1 Overview
            appendLine(generateResultsOverview(detailedStats))

            // 2.2 Strategy Comparison
            appendLine(generateStrategyComparison(detailedStats))

            // 2.3 Property Analysis
            appendLine(generatePropertyAnalysis(detailedStats, boardStats))

            // 2.4 Asset Progression
            appendLine(generateAssetProgression(detailedStats))

            appendLine("</div>")
        }
    }

    private fun generateResultsOverview(detailedStats: DetailedStatistics): String {
        return """
        <h3>2.1 Overview</h3>
        <p>
        Average game length: ${String.format("%.1f", detailedStats.basicStats.turnStats.averageTurns)} turns.
        Range: ${detailedStats.basicStats.turnStats.minTurns}-${detailedStats.basicStats.turnStats.maxTurns} turns.
        </p>
        """.trimIndent()
    }

    private fun generateStrategyComparison(detailedStats: DetailedStatistics): String {
        // レーダーチャート
        val colors = listOf("#3498db", "#e74c3c", "#2ecc71", "#f39c12")
        val radarData = RadarChartData(
            title = "Strategy Comparison",
            axes = listOf(
                RadarChartData.Axis("Win Rate"),
                RadarChartData.Axis("Avg Assets"),
                RadarChartData.Axis("Properties"),
                RadarChartData.Axis("Survival"),
                RadarChartData.Axis("Efficiency")
            ),
            series = detailedStats.basicStats.playerStats.values.mapIndexed { index, playerStats ->
                RadarChartData.Series(
                    label = playerStats.playerName,
                    values = listOf(
                        playerStats.winRate,
                        playerStats.averageFinalAssets / 5000.0, // 正規化
                        playerStats.averagePropertiesOwned / 28.0, // 最大28プロパティ
                        1.0 - (detailedStats.bankruptcyAnalysis.getBankruptcyCountByPlayer()[playerStats.playerName] ?: 0).toDouble() / detailedStats.basicStats.totalGames,
                        1.0 / (detailedStats.basicStats.turnStats.averageTurns / 100.0) // 効率性
                    ),
                    color = colors[index % colors.size]
                )
            }
        )

        // 散布図
        val scatterData = ScatterPlotData(
            title = "Win Rate vs Average Final Assets",
            xAxisLabel = "Win Rate",
            yAxisLabel = "Average Final Assets ($)",
            points = detailedStats.basicStats.playerStats.values.mapIndexed { index, playerStats ->
                ScatterPlotData.Point(
                    label = playerStats.playerName,
                    x = playerStats.winRate,
                    y = playerStats.averageFinalAssets,
                    color = colors[index % colors.size]
                )
            }
        )

        return buildString {
            appendLine("<h3>2.2 Strategy Comparison</h3>")
            appendLine("<div class=\"chart-container\">")
            appendLine(radarChartGenerator.generate(radarData))
            appendLine("</div>")
            appendLine("<div class=\"chart-container\">")
            appendLine(scatterPlotGenerator.generate(scatterData))
            appendLine("</div>")
        }
    }

    private fun generatePropertyAnalysis(
        detailedStats: DetailedStatistics,
        boardStats: BoardStatistics
    ): String {
        // 停止回数ヒートマップ
        val landedHeatmap = HeatmapData(
            title = "Landing Frequency Heatmap",
            cells = boardStats.positionStats.values.map { stats ->
                HeatmapData.Cell(
                    label = stats.position.toString(),
                    value = stats.landedCount.toDouble(),
                    position = stats.position
                )
            }
        )

        // 収益性ヒートマップ
        val profitabilityHeatmap = HeatmapData(
            title = "Profitability Heatmap",
            cells = boardStats.positionStats.values.map { stats ->
                HeatmapData.Cell(
                    label = stats.position.toString(),
                    value = stats.totalRentCollected,
                    position = stats.position
                )
            }
        )

        return buildString {
            appendLine("<h3>2.3 Property Analysis</h3>")
            appendLine("<p>Board position heatmaps showing landing frequency and profitability.</p>")
            appendLine("<div class=\"chart-container\">")
            appendLine(heatmapGenerator.generate(landedHeatmap))
            appendLine("</div>")
            appendLine("<div class=\"chart-container\">")
            appendLine(heatmapGenerator.generate(profitabilityHeatmap))
            appendLine("</div>")
        }
    }

    private fun generateAssetProgression(detailedStats: DetailedStatistics): String {
        val colors = listOf("#3498db", "#e74c3c", "#2ecc71", "#f39c12")
        val playerNames = detailedStats.basicStats.playerStats.keys.toList()

        val lineChartData = LineChartData(
            title = "Average Asset Progression",
            lines = playerNames.mapIndexed { index, playerName ->
                LineChartData.Line(
                    label = playerName,
                    points = detailedStats.assetHistory.getAverageAssetsByPlayer(playerName),
                    color = colors[index % colors.size]
                )
            }
        )

        return buildString {
            appendLine("<h3>2.4 Asset Progression</h3>")
            appendLine("<div class=\"chart-container\">")
            appendLine(lineChartGenerator.generate(lineChartData))
            appendLine("</div>")
        }
    }

    private fun generateDiscussion(): String {
        return """
        <div class="section">
            <h2>3. Discussion</h2>
            <p>
            [Discussion section to be filled with analysis of the results.]
            </p>
        </div>
        """.trimIndent()
    }

    private fun generateConclusion(): String {
        return """
        <div class="section">
            <h2>4. Conclusion</h2>
            <p>
            [Conclusion section summarizing key findings.]
            </p>
        </div>
        """.trimIndent()
    }

    private fun generateReferences(): String {
        return """
        <div class="section">
            <h2>References</h2>
            <p>
            [1] Reference 1<br>
            [2] Reference 2
            </p>
        </div>
        """.trimIndent()
    }

    private fun generateFilename(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
            .withZone(ZoneId.systemDefault())
        val timestamp = formatter.format(Instant.now())
        return "monopoly-research-report-$timestamp.html"
    }
}
```

## 10. PdfExporter（オプション）

**ファイル**: `src/main/kotlin/com/monopoly/cli/PdfExporter.kt`

```kotlin
package com.monopoly.cli

import java.io.File

/**
 * HTMLをPDFに変換
 *
 * 外部ツール（wkhtmltopdf）に依存
 */
class PdfExporter {
    /**
     * HTMLファイルをPDFに変換
     *
     * @param htmlFile 入力HTMLファイル
     * @param pdfFile 出力PDFファイル
     * @return 成功時はPDFファイル、失敗時はエラー
     */
    fun export(htmlFile: File, pdfFile: File): Result<File> {
        // wkhtmltopdfの存在確認
        val checkProcess = ProcessBuilder("which", "wkhtmltopdf")
            .redirectErrorStream(true)
            .start()

        val checkResult = checkProcess.waitFor()
        if (checkResult != 0) {
            return Result.failure(
                IllegalStateException(
                    "wkhtmltopdf not found. Please install it:\n" +
                    "  Ubuntu/Debian: sudo apt-get install wkhtmltopdf\n" +
                    "  macOS: brew install wkhtmltopdf\n" +
                    "  Windows: Download from https://wkhtmltopdf.org/"
                )
            )
        }

        // HTML → PDF変換
        val convertProcess = ProcessBuilder(
            "wkhtmltopdf",
            "--enable-local-file-access",
            htmlFile.absolutePath,
            pdfFile.absolutePath
        ).redirectErrorStream(true).start()

        val output = convertProcess.inputStream.bufferedReader().readText()
        val exitCode = convertProcess.waitFor()

        if (exitCode != 0) {
            return Result.failure(
                RuntimeException("PDF conversion failed:\n$output")
            )
        }

        return Result.success(pdfFile)
    }
}
```

## 11. Main.kt拡張

**変更箇所**: `src/main/kotlin/com/monopoly/cli/Main.kt`

```kotlin
// 既存のインポートに追加
import com.monopoly.statistics.BoardStatisticsCalculator
import com.monopoly.visualization.ResearchReportGenerator
import com.monopoly.cli.PdfExporter

// parseArgs関数の前に追加
val researchReportIndex = args.indexOf("--research-report")
val generateResearchReport = researchReportIndex != -1

val pdfIndex = args.indexOf("--pdf")
val generatePdf = pdfIndex != -1

// ゲーム実行後、既存のレポート生成の後に追加
if (generateResearchReport) {
    println("\nGenerating research report...")

    val boardStats = BoardStatisticsCalculator().calculate(multiGameResult)
    val detailedStats = detailedStatisticsCalculator.calculate(multiGameResult)

    val researchReportGenerator = ResearchReportGenerator()
    val htmlFile = researchReportGenerator.saveToFile(detailedStats, boardStats)

    println("Research report saved: ${htmlFile.absolutePath}")

    if (generatePdf) {
        println("Generating PDF...")
        val pdfExporter = PdfExporter()
        val pdfFile = File(htmlFile.absolutePath.replace(".html", ".pdf"))

        pdfExporter.export(htmlFile, pdfFile)
            .onSuccess {
                println("PDF report saved: ${it.absolutePath}")
            }
            .onFailure {
                println("PDF generation failed: ${it.message}")
            }
    }
}
```

## 使用例

```bash
# 研究レポート生成
./gradlew run --args="--strategy monopoly --games 1000 --research-report"

# PDF出力も有効化
./gradlew run --args="--strategy monopoly --games 1000 --research-report --pdf"

# グリッドサーチと組み合わせて研究レポート生成
./gradlew run --args="--grid-search config/grid-search-example.json --research-report"
```

## 作成日

2025-11-16
