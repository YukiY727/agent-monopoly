# Phase 7 コーディングガイド

**Phase 7**: 統計の可視化機能の実装

---

## 1. 基本方針

- 外部ライブラリを使わず、手動でSVG生成
- シンプルなデザインに留める
- Phase 6の統計データを活用

---

## 2. パッケージ構成

```
src/main/kotlin/com/monopoly/
└── visualization/          # 新規
    ├── ChartData.kt
    ├── BarChartData.kt
    ├── HistogramData.kt
    ├── BarChartGenerator.kt
    ├── HistogramGenerator.kt
    └── StatisticsReportGenerator.kt
```

---

## 3. SVG生成パターン

### 3.1 基本構造

```kotlin
fun generateSvg(): String {
    return buildString {
        appendLine("<svg width=\"600\" height=\"400\" xmlns=\"http://www.w3.org/2000/svg\">")
        appendLine("  <rect width=\"600\" height=\"400\" fill=\"#ffffff\"/>")
        // グラフの内容
        appendLine("</svg>")
    }
}
```

### 3.2 座標計算

```kotlin
// チャート領域の計算
val chartWidth = width - padding * 2
val chartHeight = height - padding * 2

// 棒の高さの計算
val barHeight = (value / maxValue * chartHeight).toInt()

// Y座標（上から下へ）
val y = height - padding - barHeight
```

---

## 4. Main.kt の統合

```kotlin
private fun runMultipleGames(config: GameConfig) {
    // ... ゲーム実行 ...

    // 統計エクスポート（Phase 6）
    if (config.numberOfGames > 1) {
        exportStatistics(result, config)

        // 統計レポート生成（Phase 7）
        generateStatisticsReport(result, calculator.calculate(result))
    }
}

private fun generateStatisticsReport(
    result: MultiGameResult,
    statistics: GameStatistics,
) {
    val reportGenerator = StatisticsReportGenerator()
    val reportFile = reportGenerator.saveToFile(statistics, result)
    println("Statistics report generated: ${reportFile.absolutePath}")
}
```

---

**作成日**: 2025-11-16
