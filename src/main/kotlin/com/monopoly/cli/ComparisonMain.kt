package com.monopoly.cli

import com.monopoly.domain.experiment.ComparisonMatrix
import com.monopoly.domain.experiment.DominanceAnalysis
import com.monopoly.domain.experiment.StatisticalAnalysis
import com.monopoly.domain.experiment.StrategyComparisonExperiment
import com.monopoly.domain.experiment.StrategyInfo
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

/**
 * 戦略比較実験のエントリーポイント
 *
 * 使用方法:
 *   ./gradlew runComparison
 *   ./gradlew runComparison --args="-g 50"
 *   ./gradlew runComparison --args="-s always,random,conservative"
 *
 * オプション:
 *   -g, --games <N>       各対戦のゲーム数（デフォルト: 100）
 *   -s, --strategies <S>  比較する戦略（カンマ区切り、デフォルト: 全戦略）
 *   -h, --help            ヘルプを表示
 */
fun main(args: Array<String>) {
    val config: ComparisonConfig = parseComparisonArgs(args)

    if (config.showHelp) {
        printComparisonHelp()
        return
    }

    // 実験IDを生成（タイムスタンプベース）
    val formatter = SimpleDateFormat("yyyyMMdd-HHmmss")
    val experimentId: String = "comparison-${formatter.format(Date())}"

    println("=== Strategy Comparison Experiment ===")
    println("Experiment ID: $experimentId")
    println("Games per matchup: ${config.gamesPerMatchup}")
    println("Strategies: ${config.strategyNames.joinToString(", ")}")
    println()

    // 戦略情報を作成
    val strategies: List<StrategyInfo> =
        config.strategyNames.map { name ->
            StrategyInfo(name, StrategyFactory.create(name))
        }

    // 対戦数を計算
    val totalMatchups: Int = strategies.size * (strategies.size - 1) / 2
    val totalGames: Int = totalMatchups * config.gamesPerMatchup
    println("Total matchups: $totalMatchups")
    println("Total games: $totalGames")
    println()

    // 実験を実行
    val experiment =
        StrategyComparisonExperiment(
            gamesPerMatchup = config.gamesPerMatchup,
            progressCallback = { matchup, completed, total ->
                println("[$completed/$total] $matchup completed")
            },
        )

    val startTime: Long = System.currentTimeMillis()
    val result: ComparisonMatrix = experiment.run(strategies)
    val elapsedTime: Long = System.currentTimeMillis() - startTime

    // 結果を表示
    println()
    println("=== Results ===")
    println()
    println(result.toTable())
    println()
    println("Elapsed time: ${elapsedTime / 1000.0} seconds")

    // 結果を保存
    val outputDir = "experiment-results/$experimentId"
    saveResults(result, outputDir, config.gamesPerMatchup)

    println()
    println("Results saved to: $outputDir/")
}

/**
 * 比較実験の設定
 */
data class ComparisonConfig(
    val gamesPerMatchup: Int,
    val strategyNames: List<String>,
    val showHelp: Boolean,
)

/**
 * コマンドライン引数をパース
 */
fun parseComparisonArgs(args: Array<String>): ComparisonConfig {
    var gamesPerMatchup = DEFAULT_GAMES_PER_MATCHUP
    var strategyNames: List<String> = StrategyFactory.availableStrategies
    var showHelp = false

    var i = 0
    while (i < args.size) {
        when (args[i]) {
            "-h", "--help" -> {
                showHelp = true
                i++
            }
            "-g", "--games" -> {
                if (i + 1 < args.size) {
                    gamesPerMatchup = args[i + 1].toIntOrNull() ?: DEFAULT_GAMES_PER_MATCHUP
                    i += 2
                } else {
                    i++
                }
            }
            "-s", "--strategies" -> {
                if (i + 1 < args.size) {
                    strategyNames = args[i + 1].split(",").map { it.trim() }
                    i += 2
                } else {
                    i++
                }
            }
            else -> i++
        }
    }

    return ComparisonConfig(
        gamesPerMatchup = gamesPerMatchup,
        strategyNames = strategyNames,
        showHelp = showHelp,
    )
}

/**
 * ヘルプを表示
 */
fun printComparisonHelp() {
    println(
        """
        Strategy Comparison Experiment

        Usage:
          ./gradlew runComparison [options]

        Options:
          -g, --games <N>       Games per matchup (default: $DEFAULT_GAMES_PER_MATCHUP)
          -s, --strategies <S>  Strategies to compare (comma-separated)
                                Available: ${StrategyFactory.availableStrategies.joinToString(", ")}
          -h, --help            Show this help

        Examples:
          ./gradlew runComparison
          ./gradlew runComparison --args="-g 50"
          ./gradlew runComparison --args="-s always,random,conservative"
        """.trimIndent(),
    )
}

private const val DEFAULT_GAMES_PER_MATCHUP = 100

/**
 * 結果をファイルに保存
 */
fun saveResults(
    matrix: ComparisonMatrix,
    outputDir: String,
    gamesPerMatchup: Int,
) {
    // ディレクトリ作成
    File(outputDir).mkdirs()

    // CSVファイル保存
    val csvPath = "$outputDir/comparison-matrix.csv"
    saveCsv(matrix, csvPath)
    println("  - CSV: $csvPath")

    // HTMLレポート保存
    val htmlPath = "$outputDir/comparison-report.html"
    saveHtml(matrix, htmlPath, gamesPerMatchup)
    println("  - HTML: $htmlPath")
}

/**
 * CSV形式で保存
 */
private fun saveCsv(
    matrix: ComparisonMatrix,
    path: String,
) {
    val sb = StringBuilder()

    // ヘッダー行
    sb.append("Strategy")
    matrix.strategies.forEach { sb.append(",$it") }
    sb.appendLine()

    // データ行
    matrix.strategies.forEach { row ->
        sb.append(row)
        matrix.strategies.forEach { col ->
            val winRate: Double? = matrix.getWinRate(row, col)
            val cell: String =
                when {
                    row == col -> "-"
                    winRate != null -> "%.1f%%".format(winRate * 100)
                    else -> "?"
                }
            sb.append(",$cell")
        }
        sb.appendLine()
    }

    File(path).writeText(sb.toString())
}

/**
 * HTMLレポートを保存
 */
private fun saveHtml(
    matrix: ComparisonMatrix,
    path: String,
    gamesPerMatchup: Int,
) {
    // 統計分析を実行
    val dominance: DominanceAnalysis = StatisticalAnalysis.analyzeDominance(matrix)

    val html =
        """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Strategy Comparison Report</title>
            <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
            <style>
                body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }
                .container { max-width: 1200px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; }
                h1 { color: #333; }
                h2 { color: #555; border-bottom: 2px solid #4CAF50; padding-bottom: 5px; }
                table { border-collapse: collapse; width: 100%; margin: 20px 0; }
                th, td { border: 1px solid #ddd; padding: 12px; text-align: center; }
                th { background-color: #4CAF50; color: white; }
                tr:nth-child(even) { background-color: #f2f2f2; }
                .high-win { background-color: #c8e6c9; }
                .low-win { background-color: #ffcdd2; }
                .significant { font-weight: bold; }
                .chart-container { width: 100%; max-width: 800px; margin: 20px auto; }
                .analysis-box { background: #e8f5e9; padding: 15px; border-radius: 8px; margin: 20px 0; }
                .warning-box { background: #fff3e0; padding: 15px; border-radius: 8px; margin: 20px 0; }
                .info-box { background: #e3f2fd; padding: 15px; border-radius: 8px; margin: 20px 0; }
                ul { margin: 10px 0; padding-left: 20px; }
                .effect-small { color: #888; }
                .effect-medium { color: #f57c00; }
                .effect-large { color: #d32f2f; font-weight: bold; }
            </style>
        </head>
        <body>
            <div class="container">
                <h1>Strategy Comparison Report</h1>
                <p>Games per matchup: $gamesPerMatchup</p>
                <p>Total matchups: ${matrix.matchups.size}</p>
                <p>Total games: ${matrix.matchups.size * gamesPerMatchup}</p>

                <h2>Dominance Analysis</h2>
                <div class="analysis-box">
                    ${generateDominanceHtml(dominance)}
                </div>

                <h2>Win Rate Matrix</h2>
                <table>
                    <tr>
                        <th></th>
                        ${matrix.strategies.joinToString("") { "<th>$it</th>" }}
                    </tr>
                    ${generateMatrixRowsHtml(matrix, gamesPerMatchup)}
                </table>

                <h2>Statistical Significance</h2>
                <div class="info-box">
                    <p><strong>Legend:</strong></p>
                    <ul>
                        <li><span class="significant">Bold</span> = Statistically significant (p &lt; 0.05)</li>
                        <li><span class="effect-small">Gray</span> = Small effect (Cohen's d &lt; 0.5)</li>
                        <li><span class="effect-medium">Orange</span> = Medium effect (0.5 ≤ d &lt; 0.8)</li>
                        <li><span class="effect-large">Red Bold</span> = Large effect (d ≥ 0.8)</li>
                    </ul>
                </div>
                ${generateSignificanceTableHtml(matrix, gamesPerMatchup)}

                <h2>Overall Win Rates</h2>
                <div class="chart-container">
                    <canvas id="winRateChart"></canvas>
                </div>

                <script>
                    const ctx = document.getElementById('winRateChart').getContext('2d');
                    const strategies = [${matrix.strategies.joinToString(", ") { "\"$it\"" }}];
                    const winRates = [${calculateOverallWinRates(matrix)}];

                    new Chart(ctx, {
                        type: 'bar',
                        data: {
                            labels: strategies,
                            datasets: [{
                                label: 'Overall Win Rate (%)',
                                data: winRates,
                                backgroundColor: 'rgba(76, 175, 80, 0.6)',
                                borderColor: 'rgba(76, 175, 80, 1)',
                                borderWidth: 1
                            }]
                        },
                        options: {
                            scales: {
                                y: {
                                    beginAtZero: true,
                                    max: 100
                                }
                            }
                        }
                    });
                </script>
            </div>
        </body>
        </html>
        """.trimIndent()

    File(path).writeText(html)
}

/**
 * 支配関係のHTML生成
 */
private fun generateDominanceHtml(dominance: DominanceAnalysis): String {
    val sb = StringBuilder()

    if (dominance.strictlyDominant != null) {
        sb.append("<p><strong>Strictly Dominant Strategy:</strong> ${dominance.strictlyDominant}</p>")
        sb.append("<p>This strategy beats all other strategies (win rate > 50%)</p>")
    } else if (dominance.weaklyDominant.isNotEmpty()) {
        sb.append("<p><strong>Weakly Dominant Strategies:</strong> ${dominance.weaklyDominant.joinToString(", ")}</p>")
        sb.append("<p>These strategies never lose to any other strategy (win rate ≥ 50%)</p>")
    } else {
        sb.append("<p>No dominant strategy found. This suggests a rock-paper-scissors dynamic.</p>")
    }

    if (dominance.strictlyDominated.isNotEmpty()) {
        sb.append("<p><strong>Strictly Dominated Strategies:</strong> ${dominance.strictlyDominated.joinToString(", ")}</p>")
        sb.append("<p>These strategies lose to all other strategies (win rate < 50%)</p>")
    }

    return sb.toString()
}

/**
 * マトリックス行のHTML生成
 */
private fun generateMatrixRowsHtml(
    matrix: ComparisonMatrix,
    gamesPerMatchup: Int,
): String =
    matrix.strategies.joinToString("") { row ->
        "<tr><th>$row</th>" +
            matrix.strategies.joinToString("") { col ->
                val winRate: Double? = matrix.getWinRate(row, col)
                val matchup = matrix.matchups.find {
                    (it.strategy1 == row && it.strategy2 == col) ||
                        (it.strategy1 == col && it.strategy2 == row)
                }

                val cell: String
                val cssClass: String

                when {
                    row == col -> {
                        cell = "-"
                        cssClass = ""
                    }
                    winRate != null && matchup != null -> {
                        val wins: Int = if (matchup.strategy1 == row) matchup.strategy1Wins else matchup.strategy2Wins
                        val ci = StatisticalAnalysis.confidenceInterval(wins, gamesPerMatchup)
                        cell = "%.1f%%<br><small>[%.0f-%.0f]</small>".format(
                            winRate * 100,
                            ci.lower * 100,
                            ci.upper * 100,
                        )
                        cssClass =
                            when {
                                winRate >= 0.6 -> "high-win"
                                winRate <= 0.4 -> "low-win"
                                else -> ""
                            }
                    }
                    else -> {
                        cell = "?"
                        cssClass = ""
                    }
                }
                "<td class=\"$cssClass\">$cell</td>"
            } +
            "</tr>"
    }

/**
 * 統計的有意性テーブルのHTML生成
 */
private fun generateSignificanceTableHtml(
    matrix: ComparisonMatrix,
    gamesPerMatchup: Int,
): String {
    val sb = StringBuilder()
    sb.append("<table>")
    sb.append("<tr><th>Matchup</th><th>Win Rate</th><th>p-value</th><th>Cohen's d</th><th>Significance</th></tr>")

    matrix.matchups.forEach { matchup ->
        val testResult = StatisticalAnalysis.twoProportionZTest(
            matchup.strategy1Wins,
            gamesPerMatchup,
            matchup.strategy2Wins,
            gamesPerMatchup,
        )
        val effectSize = StatisticalAnalysis.cohensD(
            matchup.strategy1WinRate,
            matchup.strategy2WinRate,
            gamesPerMatchup,
        )

        val effectClass: String =
            when {
                effectSize >= 0.8 -> "effect-large"
                effectSize >= 0.5 -> "effect-medium"
                else -> "effect-small"
            }

        val significanceText: String =
            when {
                testResult.pValue < 0.001 -> "***"
                testResult.pValue < 0.01 -> "**"
                testResult.pValue < 0.05 -> "*"
                else -> "ns"
            }

        val rowClass: String = if (testResult.significant) "significant" else ""

        sb.append(
            """
            <tr class="$rowClass">
                <td>${matchup.strategy1} vs ${matchup.strategy2}</td>
                <td>%.1f%% vs %.1f%%</td>
                <td>%.4f</td>
                <td class="$effectClass">%.2f</td>
                <td>$significanceText</td>
            </tr>
            """.format(
                matchup.strategy1WinRate * 100,
                matchup.strategy2WinRate * 100,
                testResult.pValue,
                effectSize,
            ),
        )
    }

    sb.append("</table>")
    sb.append("<p><small>*** p &lt; 0.001, ** p &lt; 0.01, * p &lt; 0.05, ns = not significant</small></p>")

    return sb.toString()
}

/**
 * 全体勝率を計算
 */
private fun calculateOverallWinRates(matrix: ComparisonMatrix): String {
    val winRates: List<Double> =
        matrix.strategies.map { strategy ->
            val rates: List<Double> =
                matrix.strategies
                    .filter { it != strategy }
                    .mapNotNull { opponent -> matrix.getWinRate(strategy, opponent) }
            if (rates.isNotEmpty()) rates.average() * 100 else 0.0
        }
    return winRates.joinToString(", ") { "%.1f".format(it) }
}
