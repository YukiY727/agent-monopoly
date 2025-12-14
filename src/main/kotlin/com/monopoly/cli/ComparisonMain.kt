package com.monopoly.cli

import com.monopoly.domain.experiment.ComparisonMatrix
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
                table { border-collapse: collapse; width: 100%; margin: 20px 0; }
                th, td { border: 1px solid #ddd; padding: 12px; text-align: center; }
                th { background-color: #4CAF50; color: white; }
                tr:nth-child(even) { background-color: #f2f2f2; }
                .high-win { background-color: #c8e6c9; }
                .low-win { background-color: #ffcdd2; }
                .chart-container { width: 100%; max-width: 800px; margin: 20px auto; }
            </style>
        </head>
        <body>
            <div class="container">
                <h1>Strategy Comparison Report</h1>
                <p>Games per matchup: $gamesPerMatchup</p>
                <p>Total matchups: ${matrix.matchups.size}</p>

                <h2>Win Rate Matrix</h2>
                <table>
                    <tr>
                        <th></th>
                        ${matrix.strategies.joinToString("") { "<th>$it</th>" }}
                    </tr>
                    ${
            matrix.strategies.joinToString("") { row ->
                "<tr><th>$row</th>" +
                    matrix.strategies.joinToString("") { col ->
                        val winRate: Double? = matrix.getWinRate(row, col)
                        val cell: String =
                            when {
                                row == col -> "-"
                                winRate != null -> "%.1f%%".format(winRate * 100)
                                else -> "?"
                            }
                        val cssClass: String =
                            when {
                                winRate == null -> ""
                                winRate >= 0.6 -> "high-win"
                                winRate <= 0.4 -> "low-win"
                                else -> ""
                            }
                        "<td class=\"$cssClass\">$cell</td>"
                    } +
                    "</tr>"
            }
        }
                </table>

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
