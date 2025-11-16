# Phase 7 詳細設計

**Phase 7**: 統計の可視化機能の設計

---

## 1. アーキテクチャ概要

```
┌─────────────────────────────────────────────────┐
│                   Main.kt                       │
│  - 複数ゲーム実行後にグラフ生成                    │
└────────────┬────────────────────────────────────┘
             │
             ├→ GameRunner → MultiGameResult
             │
             ├→ StatisticsCalculator → GameStatistics
             │
             └→ Visualization
                 ├→ BarChartGenerator → SVG
                 ├→ HistogramGenerator → SVG
                 └→ StatisticsReportGenerator → HTML
```

---

## 2. データモデル設計

### 2.1 ChartData（基底インターフェース）

```kotlin
package com.monopoly.visualization

interface ChartData
```

### 2.2 BarChartData

```kotlin
package com.monopoly.visualization

data class BarChartData(
    val title: String,
    val bars: List<Bar>,
) : ChartData {
    data class Bar(
        val label: String,
        val value: Double,
        val color: String = "#3498db",
    )
}
```

### 2.3 HistogramData

```kotlin
package com.monopoly.visualization

data class HistogramData(
    val title: String,
    val bins: List<Bin>,
) : ChartData {
    data class Bin(
        val rangeStart: Int,
        val rangeEnd: Int,
        val count: Int,
    )
}
```

---

## 3. グラフジェネレーター設計

### 3.1 BarChartGenerator

```kotlin
package com.monopoly.visualization

class BarChartGenerator {
    private val width = 600
    private val height = 400
    private val padding = 60

    fun generate(data: BarChartData): String {
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        return buildString {
            appendLine("<svg width=\"$width\" height=\"$height\" xmlns=\"http://www.w3.org/2000/svg\">")
            appendLine("  <!-- Background -->")
            appendLine("  <rect width=\"$width\" height=\"$height\" fill=\"#ffffff\"/>")

            // タイトル
            appendLine(generateTitle(data.title))

            // 軸
            appendLine(generateAxes())

            // 棒グラフ
            appendLine(generateBars(data.bars, chartWidth, chartHeight))

            // ラベル
            appendLine(generateLabels(data.bars))

            appendLine("</svg>")
        }
    }

    private fun generateTitle(title: String): String {
        return "  <text x=\"${width / 2}\" y=\"30\" text-anchor=\"middle\" font-size=\"18\" font-weight=\"bold\">$title</text>"
    }

    private fun generateAxes(): String {
        return buildString {
            // Y軸
            appendLine("  <line x1=\"$padding\" y1=\"$padding\" x2=\"$padding\" y2=\"${height - padding}\" stroke=\"#333\" stroke-width=\"2\"/>")
            // X軸
            appendLine("  <line x1=\"$padding\" y1=\"${height - padding}\" x2=\"${width - padding}\" y2=\"${height - padding}\" stroke=\"#333\" stroke-width=\"2\"/>")
        }
    }

    private fun generateBars(bars: List<BarChartData.Bar>, chartWidth: Int, chartHeight: Int): String {
        val maxValue = bars.maxOfOrNull { it.value } ?: 1.0
        val barWidth = chartWidth / bars.size * 0.8
        val barSpacing = chartWidth / bars.size

        return buildString {
            bars.forEachIndexed { index, bar ->
                val barHeight = (bar.value / maxValue * chartHeight).toInt()
                val x = padding + index * barSpacing + barSpacing * 0.1
                val y = height - padding - barHeight

                appendLine("  <rect x=\"$x\" y=\"$y\" width=\"$barWidth\" height=\"$barHeight\" fill=\"${bar.color}\"/>")

                // 値のラベル
                appendLine("  <text x=\"${x + barWidth / 2}\" y=\"${y - 5}\" text-anchor=\"middle\" font-size=\"12\">${String.format("%.1f", bar.value * 100)}%</text>")
            }
        }
    }

    private fun generateLabels(bars: List<BarChartData.Bar>): String {
        val chartWidth = width - padding * 2
        val barSpacing = chartWidth / bars.size

        return buildString {
            bars.forEachIndexed { index, bar ->
                val x = padding + index * barSpacing + barSpacing / 2
                val y = height - padding + 20

                appendLine("  <text x=\"$x\" y=\"$y\" text-anchor=\"middle\" font-size=\"12\">${bar.label}</text>")
            }
        }
    }
}
```

### 3.2 HistogramGenerator

```kotlin
package com.monopoly.visualization

class HistogramGenerator {
    private val width = 600
    private val height = 400
    private val padding = 60

    fun generate(data: HistogramData): String {
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        return buildString {
            appendLine("<svg width=\"$width\" height=\"$height\" xmlns=\"http://www.w3.org/2000/svg\">")
            appendLine("  <rect width=\"$width\" height=\"$height\" fill=\"#ffffff\"/>")

            appendLine(generateTitle(data.title))
            appendLine(generateAxes())
            appendLine(generateBins(data.bins, chartWidth, chartHeight))
            appendLine(generateAxisLabels(data.bins))

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

            // Y軸ラベル
            appendLine("  <text x=\"20\" y=\"${height / 2}\" text-anchor=\"middle\" font-size=\"12\" transform=\"rotate(-90, 20, ${height / 2})\">Frequency</text>")
            // X軸ラベル
            appendLine("  <text x=\"${width / 2}\" y=\"${height - 20}\" text-anchor=\"middle\" font-size=\"12\">Turns</text>")
        }
    }

    private fun generateBins(bins: List<HistogramData.Bin>, chartWidth: Int, chartHeight: Int): String {
        val maxCount = bins.maxOfOrNull { it.count } ?: 1
        val binWidth = chartWidth / bins.size * 0.9
        val binSpacing = chartWidth / bins.size

        return buildString {
            bins.forEachIndexed { index, bin ->
                val binHeight = (bin.count.toDouble() / maxCount * chartHeight).toInt()
                val x = padding + index * binSpacing
                val y = height - padding - binHeight

                appendLine("  <rect x=\"$x\" y=\"$y\" width=\"$binWidth\" height=\"$binHeight\" fill=\"#2ecc71\" stroke=\"#27ae60\" stroke-width=\"1\"/>")

                // カウントのラベル
                if (bin.count > 0) {
                    appendLine("  <text x=\"${x + binWidth / 2}\" y=\"${y - 5}\" text-anchor=\"middle\" font-size=\"10\">${bin.count}</text>")
                }
            }
        }
    }

    private fun generateAxisLabels(bins: List<HistogramData.Bin>): String {
        val chartWidth = width - padding * 2
        val binSpacing = chartWidth / bins.size

        return buildString {
            bins.forEachIndexed { index, bin ->
                val x = padding + index * binSpacing + binSpacing / 2
                val y = height - padding + 20

                val label = if (index == bins.size - 1) "${bin.rangeStart}+" else "${bin.rangeStart}-${bin.rangeEnd}"
                appendLine("  <text x=\"$x\" y=\"$y\" text-anchor=\"middle\" font-size=\"10\">$label</text>")
            }
        }
    }
}
```

---

## 4. StatisticsReportGenerator設計

```kotlin
package com.monopoly.visualization

import com.monopoly.simulation.MultiGameResult
import com.monopoly.statistics.GameStatistics
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class StatisticsReportGenerator {
    fun generate(statistics: GameStatistics, result: MultiGameResult): String {
        return buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html lang=\"ja\">")
            appendLine("<head>")
            appendLine("    <meta charset=\"UTF-8\">")
            appendLine("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
            appendLine("    <title>Statistics Report</title>")
            appendLine("    <style>")
            appendLine(getStyleSheet())
            appendLine("    </style>")
            appendLine("</head>")
            appendLine("<body>")
            appendLine("    <div class=\"container\">")
            appendLine("        <h1>📊 Statistics Report</h1>")
            appendLine(generateSummarySection(statistics))
            appendLine(generateWinRateChart(statistics))
            appendLine(generatePlayerStatsTable(statistics))
            appendLine(generateTurnDistributionChart(result))
            appendLine(generateTurnStatsTable(statistics))
            appendLine("    </div>")
            appendLine("</body>")
            appendLine("</html>")
        }
    }

    fun saveToFile(statistics: GameStatistics, result: MultiGameResult, filename: String = generateFilename()): File {
        val html = generate(statistics, result)
        val file = File(filename)
        file.writeText(html)
        return file
    }

    private fun generateSummarySection(statistics: GameStatistics): String {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(statistics.timestamp))

        return """
        <div class="section">
            <h2>Game Summary</h2>
            <p>Total Games: ${statistics.totalGames}</p>
            <p>Date: $date</p>
        </div>
        """.trimIndent()
    }

    private fun generateWinRateChart(statistics: GameStatistics): String {
        val bars = statistics.playerStats.map { (name, stats) ->
            BarChartData.Bar(name, stats.winRate, getPlayerColor(name))
        }

        val chartData = BarChartData("Player Win Rates", bars)
        val barChartGenerator = BarChartGenerator()
        val svg = barChartGenerator.generate(chartData)

        return """
        <div class="section">
            <h2>Player Win Rates</h2>
            <div class="chart-container">
                $svg
            </div>
        </div>
        """.trimIndent()
    }

    private fun generateTurnDistributionChart(result: MultiGameResult): String {
        val bins = createTurnBins(result)
        val histogramData = HistogramData("Turn Distribution", bins)
        val histogramGenerator = HistogramGenerator()
        val svg = histogramGenerator.generate(histogramData)

        return """
        <div class="section">
            <h2>Turn Distribution</h2>
            <div class="chart-container">
                $svg
            </div>
        </div>
        """.trimIndent()
    }

    private fun createTurnBins(result: MultiGameResult): List<HistogramData.Bin> {
        val turns = result.gameResults.map { it.totalTurns }
        val minTurn = turns.minOrNull() ?: 0
        val maxTurn = turns.maxOrNull() ?: 100

        val binSize = 10
        val binCount = ((maxTurn - minTurn) / binSize) + 2

        return (0 until binCount).map { i ->
            val start = minTurn + i * binSize
            val end = start + binSize
            val count = turns.count { it >= start && it < end }

            HistogramData.Bin(start, end, count)
        }
    }

    private fun getPlayerColor(playerName: String): String {
        return when (playerName) {
            "Alice" -> "#3498db"
            "Bob" -> "#e74c3c"
            else -> "#95a5a6"
        }
    }

    private fun getStyleSheet(): String = """
        /* CSS省略 - Phase 2/4と同様のスタイル */
    """.trimIndent()

    private fun generateFilename(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return "statistics-report-$timestamp.html"
    }
}
```

---

**作成日**: 2025-11-16
