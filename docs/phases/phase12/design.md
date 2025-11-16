# Phase 12: 高度な可視化 - 詳細設計

## アーキテクチャ概要

### コンポーネント図

```
┌─────────────────────────────────────────────────────────────┐
│                     CLI Layer                                │
│  - Main.kt                                                   │
│  - PdfExporter.kt (optional)                                │
└────────────────┬────────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────────────┐
│              Statistics Layer                                │
│  - BoardStatisticsCalculator                                │
│  - DetailedStatisticsCalculator (Phase 9)                   │
└────────────────┬────────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────────────┐
│              Visualization Layer                             │
│  - HeatmapGenerator                                         │
│  - RadarChartGenerator                                      │
│  - ScatterPlotGenerator                                     │
│  - ResearchReportGenerator                                  │
└─────────────────────────────────────────────────────────────┘
```

## データモデル

### BoardStatistics

```kotlin
data class BoardStatistics(
    val positionStats: Map<Int, PositionStatistics>,
    val totalGames: Int,
) {
    data class PositionStatistics(
        val position: Int,
        val landedCount: Int,          // 停止回数
        val totalRentCollected: Double, // 総レント収入
        val ownershipDistribution: Map<String, Int>, // 所有者分布
    ) {
        val averageLandedPerGame: Double
            get() = landedCount.toDouble() / totalGames

        val averageRentPerGame: Double
            get() = totalRentCollected / totalGames
    }
}
```

### HeatmapData

```kotlin
data class HeatmapData(
    val title: String,
    val cells: List<Cell>,
    val minValue: Double,
    val maxValue: Double,
) {
    data class Cell(
        val label: String,
        val value: Double,
        val position: Int, // ボード上の位置（0-39）
    )
}
```

### RadarChartData

```kotlin
data class RadarChartData(
    val title: String,
    val axes: List<Axis>,
    val series: List<Series>,
) {
    data class Axis(
        val label: String,
        val maxValue: Double,
    )

    data class Series(
        val label: String,
        val values: List<Double>, // 各軸の値（0.0-1.0に正規化）
        val color: String,
    )
}
```

### ScatterPlotData

```kotlin
data class ScatterPlotData(
    val title: String,
    val xAxisLabel: String,
    val yAxisLabel: String,
    val points: List<Point>,
) {
    data class Point(
        val label: String,
        val x: Double,
        val y: Double,
        val color: String,
    )
}
```

## アルゴリズム詳細

### BoardStatisticsCalculator

**入力**: `MultiGameResult`（複数ゲームの結果）

**処理**:
1. 各ゲームのイベントログを走査
2. プレイヤー移動イベントから停止位置を集計
3. レント支払いイベントから収益を集計
4. プロパティ所有者の変化を追跡

**出力**: `BoardStatistics`

**疑似コード**:
```kotlin
fun calculate(multiGameResult: MultiGameResult): BoardStatistics {
    val positionStatsMap = mutableMapOf<Int, MutablePositionStats>()

    multiGameResult.results.forEach { gameResult ->
        gameResult.events.forEach { event ->
            when (event) {
                is PlayerMovedEvent -> {
                    val stats = positionStatsMap.getOrPut(event.toPosition) {
                        MutablePositionStats()
                    }
                    stats.landedCount++
                }
                is RentPaidEvent -> {
                    val position = event.propertyPosition
                    val stats = positionStatsMap.getOrPut(position) {
                        MutablePositionStats()
                    }
                    stats.totalRentCollected += event.amount
                }
                is PropertyPurchasedEvent -> {
                    val position = event.propertyPosition
                    val stats = positionStatsMap.getOrPut(position) {
                        MutablePositionStats()
                    }
                    stats.ownershipDistribution[event.buyer]++
                }
            }
        }
    }

    return BoardStatistics(
        positionStats = positionStatsMap.mapValues { it.value.toImmutable() },
        totalGames = multiGameResult.results.size
    )
}
```

### HeatmapGenerator

**入力**: `HeatmapData`

**出力**: SVG文字列

**視覚化戦略**:
- モノポリーボードを40マスの矩形グリッドとして表示
- 各マスを色の濃さで値を表現（薄い→濃い = 低い→高い）
- カラースケール: 白 → 青 → 濃い青

**SVG生成**:
```kotlin
fun generate(data: HeatmapData): String {
    val cellWidth = 50
    val cellHeight = 50
    val gridWidth = 11  // 横11マス（ボードの形状）
    val gridHeight = 11 // 縦11マス

    // ボード配置（時計回り）
    val positions = calculateBoardPositions(gridWidth, gridHeight)

    return buildString {
        appendLine("<svg width=\"600\" height=\"600\">")

        data.cells.forEach { cell ->
            val (x, y) = positions[cell.position]
            val intensity = (cell.value - data.minValue) / (data.maxValue - data.minValue)
            val color = interpolateColor(intensity)

            appendLine("  <rect x=\"$x\" y=\"$y\" width=\"$cellWidth\" height=\"$cellHeight\" fill=\"$color\" />")
            appendLine("  <text x=\"${x + cellWidth/2}\" y=\"${y + cellHeight/2}\">${cell.label}</text>")
        }

        appendLine("</svg>")
    }
}
```

### RadarChartGenerator

**入力**: `RadarChartData`

**出力**: SVG文字列

**視覚化戦略**:
- N角形のレーダーチャート（Nは軸の数）
- 各軸は中心から外側に向かって0-1の範囲
- 複数の戦略を重ねて表示

**SVG生成**:
```kotlin
fun generate(data: RadarChartData): String {
    val centerX = 300.0
    val centerY = 300.0
    val radius = 200.0
    val numAxes = data.axes.size

    return buildString {
        appendLine("<svg width=\"600\" height=\"600\">")

        // 軸を描画
        data.axes.forEachIndexed { index, axis ->
            val angle = 2 * Math.PI * index / numAxes - Math.PI / 2
            val x = centerX + radius * Math.cos(angle)
            val y = centerY + radius * Math.sin(angle)

            appendLine("  <line x1=\"$centerX\" y1=\"$centerY\" x2=\"$x\" y2=\"$y\" stroke=\"#ccc\" />")
            appendLine("  <text x=\"$x\" y=\"$y\">${axis.label}</text>")
        }

        // 各戦略のポリゴンを描画
        data.series.forEach { series ->
            val points = series.values.mapIndexed { index, value ->
                val angle = 2 * Math.PI * index / numAxes - Math.PI / 2
                val r = radius * value
                val x = centerX + r * Math.cos(angle)
                val y = centerY + r * Math.sin(angle)
                "$x,$y"
            }.joinToString(" ")

            appendLine("  <polygon points=\"$points\" fill=\"${series.color}\" opacity=\"0.3\" stroke=\"${series.color}\" />")
        }

        appendLine("</svg>")
    }
}
```

### ScatterPlotGenerator

**入力**: `ScatterPlotData`

**出力**: SVG文字列

**視覚化戦略**:
- X軸: 勝率（0-100%）
- Y軸: 平均最終資産（$0-$5000）
- 各戦略を円で表示

**SVG生成**:
```kotlin
fun generate(data: ScatterPlotData): String {
    val width = 600.0
    val height = 400.0
    val margin = 50.0

    val xMin = data.points.minOf { it.x }
    val xMax = data.points.maxOf { it.x }
    val yMin = data.points.minOf { it.y }
    val yMax = data.points.maxOf { it.y }

    return buildString {
        appendLine("<svg width=\"${width}\" height=\"${height}\">")

        // 軸を描画
        appendLine("  <line x1=\"$margin\" y1=\"${height - margin}\" x2=\"${width - margin}\" y2=\"${height - margin}\" stroke=\"black\" />")
        appendLine("  <line x1=\"$margin\" y1=\"$margin\" x2=\"$margin\" y2=\"${height - margin}\" stroke=\"black\" />")

        // データポイントを描画
        data.points.forEach { point ->
            val x = margin + (point.x - xMin) / (xMax - xMin) * (width - 2 * margin)
            val y = height - margin - (point.y - yMin) / (yMax - yMin) * (height - 2 * margin)

            appendLine("  <circle cx=\"$x\" cy=\"$y\" r=\"8\" fill=\"${point.color}\" />")
            appendLine("  <text x=\"${x + 12}\" y=\"$y\">${point.label}</text>")
        }

        // 軸ラベルを描画
        appendLine("  <text x=\"${width / 2}\" y=\"${height - 10}\">${data.xAxisLabel}</text>")
        appendLine("  <text x=\"10\" y=\"${height / 2}\" transform=\"rotate(-90 10 ${height / 2})\">${data.yAxisLabel}</text>")

        appendLine("</svg>")
    }
}
```

## ResearchReportGenerator

### レポート構造

```
1. Abstract
   - 研究目的
   - 手法の概要
   - 主要な結果

2. Methodology
   - シミュレーション設定
   - 戦略の説明
   - 実行パラメータ

3. Results
   3.1 Overview
       - 基本統計サマリー
   3.2 Strategy Comparison
       - レーダーチャート
       - 散布図
       - 詳細比較テーブル
   3.3 Property Analysis
       - プロパティROIランキング
       - ヒートマップ（停止回数）
       - ヒートマップ（収益性）
   3.4 Asset Progression
       - 資産推移グラフ
   3.5 Bankruptcy Analysis
       - 破産分析

4. Discussion（プレースホルダー）

5. Conclusion（プレースホルダー）

6. References（プレースホルダー）
```

### HTML生成

```kotlin
class ResearchReportGenerator(
    private val heatmapGenerator: HeatmapGenerator,
    private val radarChartGenerator: RadarChartGenerator,
    private val scatterPlotGenerator: ScatterPlotGenerator,
    // 他のジェネレータ...
) {
    fun generate(
        detailedStats: DetailedStatistics,
        boardStats: BoardStatistics
    ): String {
        return buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html>")
            appendLine("<head>")
            appendLine(generateHead())
            appendLine("</head>")
            appendLine("<body>")

            appendLine(generateAbstract(detailedStats))
            appendLine(generateMethodology(detailedStats))
            appendLine(generateResults(detailedStats, boardStats))
            appendLine(generateDiscussion())
            appendLine(generateConclusion())
            appendLine(generateReferences())

            appendLine("</body>")
            appendLine("</html>")
        }
    }
}
```

## PDF出力設計

### 実装方針

**外部ツール依存**:
- `wkhtmltopdf` を使用してHTML→PDF変換
- インストール確認: `which wkhtmltopdf`
- 変換コマンド: `wkhtmltopdf input.html output.pdf`

**PdfExporter実装**:
```kotlin
class PdfExporter {
    fun export(htmlFile: File, pdfFile: File): Result<File> {
        // wkhtmltopdfの存在確認
        val checkResult = ProcessBuilder("which", "wkhtmltopdf")
            .start()
            .waitFor()

        if (checkResult != 0) {
            return Result.failure(IllegalStateException(
                "wkhtmltopdf not found. Please install it first."
            ))
        }

        // HTML → PDF変換
        val convertResult = ProcessBuilder(
            "wkhtmltopdf",
            htmlFile.absolutePath,
            pdfFile.absolutePath
        ).start().waitFor()

        if (convertResult != 0) {
            return Result.failure(RuntimeException("PDF conversion failed"))
        }

        return Result.success(pdfFile)
    }
}
```

## CLI統合

### 新しいオプション

```bash
# 研究論文用レポート生成
./gradlew run --args="--strategy monopoly --games 1000 --research-report"

# PDF出力も有効化
./gradlew run --args="--strategy monopoly --games 1000 --research-report --pdf"
```

### Main.kt拡張

```kotlin
val researchReportIndex = args.indexOf("--research-report")
val generateResearchReport = researchReportIndex != -1

val pdfIndex = args.indexOf("--pdf")
val generatePdf = pdfIndex != -1

// ... ゲーム実行後 ...

if (generateResearchReport) {
    val boardStats = BoardStatisticsCalculator().calculate(multiGameResult)
    val detailedStats = DetailedStatisticsCalculator().calculate(multiGameResult)

    val reportGenerator = ResearchReportGenerator(...)
    val htmlFile = reportGenerator.saveToFile(detailedStats, boardStats)

    println("Research report saved: ${htmlFile.absolutePath}")

    if (generatePdf) {
        val pdfExporter = PdfExporter()
        val pdfFile = File(htmlFile.absolutePath.replace(".html", ".pdf"))

        pdfExporter.export(htmlFile, pdfFile).onSuccess {
            println("PDF report saved: ${it.absolutePath}")
        }.onFailure {
            println("PDF generation failed: ${it.message}")
        }
    }
}
```

## 作成日

2025-11-16
