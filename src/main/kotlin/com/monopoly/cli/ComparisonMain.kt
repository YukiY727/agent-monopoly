package com.monopoly.cli

import com.monopoly.domain.experiment.ComparisonMatrix
import com.monopoly.domain.experiment.DominanceAnalysis
import com.monopoly.domain.experiment.ExperimentRunner
import com.monopoly.domain.experiment.GameStatistics
import com.monopoly.domain.experiment.StatisticalAnalysis
import com.monopoly.domain.experiment.StrategyComparisonExperiment
import com.monopoly.domain.experiment.StrategyInfo
import com.monopoly.domain.model.player.PlayerStrategy
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
    println("Games: ${config.gamesPerMatchup}")
    println("Players per game: ${config.playerCount}")
    println("Strategies: ${config.strategyNames.joinToString(", ")}")
    println()

    // 戦略情報を作成
    val strategies: List<StrategyInfo> =
        config.strategyNames.map { name ->
            StrategyInfo(name, StrategyFactory.create(name))
        }

    if (config.playerCount > 2) {
        // マルチプレイヤーモード
        runMultiPlayerExperiment(strategies, config, experimentId)
    } else {
        // 2人対戦モード（従来の比較実験）
        runPairwiseExperiment(strategies, config, experimentId)
    }
}

/**
 * 2人対戦モード（従来の比較実験）
 */
private fun runPairwiseExperiment(
    strategies: List<StrategyInfo>,
    config: ComparisonConfig,
    experimentId: String,
) {
    val totalMatchups: Int = strategies.size * (strategies.size - 1) / 2
    val totalGames: Int = totalMatchups * config.gamesPerMatchup
    println("Total matchups: $totalMatchups")
    println("Total games: $totalGames")
    println()

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

    println()
    println("=== Results ===")
    println()
    println(result.toTable())
    println()
    println("Elapsed time: ${elapsedTime / 1000.0} seconds")

    val outputDir = "experiment-results/$experimentId"
    saveResults(result, outputDir, config.gamesPerMatchup)

    println()
    println("Results saved to: $outputDir/")
}

/**
 * マルチプレイヤーモード（全戦略が同時に競争）
 */
private fun runMultiPlayerExperiment(
    strategies: List<StrategyInfo>,
    config: ComparisonConfig,
    experimentId: String,
) {
    println("Mode: Multi-player (${config.playerCount} players per game)")
    println("Total games: ${config.gamesPerMatchup}")
    println()

    val experimentRunner = ExperimentRunner()
    val winCounts: MutableMap<String, Int> = mutableMapOf()
    val bankruptcyCounts: MutableMap<String, Int> = mutableMapOf()
    var totalBankruptcyGames = 0
    var totalTurns = 0

    // 戦略を指定人数分繰り返して選択
    val playerStrategies: List<PlayerStrategy> =
        (0 until config.playerCount).map { index ->
            strategies[index % strategies.size].strategy
        }
    val playerNames: List<String> =
        (0 until config.playerCount).map { index ->
            strategies[index % strategies.size].name
        }

    println("Players: ${playerNames.joinToString(", ")}")
    println()

    val startTime: Long = System.currentTimeMillis()

    repeat(config.gamesPerMatchup) { gameIndex ->
        val gameId: String = "multiplayer_game_%03d".format(gameIndex + 1)
        val result: GameStatistics =
            experimentRunner.runSingleGame(
                gameId = gameId,
                strategies = playerStrategies,
            )

        // 勝者を特定（Player1, Player2... -> 戦略名にマッピング）
        val winnerIndex: Int = result.winner.removePrefix("Player").toIntOrNull()?.minus(1) ?: 0
        val winnerStrategy: String = playerNames[winnerIndex]
        winCounts[winnerStrategy] = winCounts.getOrDefault(winnerStrategy, 0) + 1

        // 破産者をカウント
        if (result.bankruptcyOrder.isNotEmpty()) {
            totalBankruptcyGames++
            result.bankruptcyOrder.forEach { bankruptPlayer ->
                val bankruptIndex: Int = bankruptPlayer.removePrefix("Player").toIntOrNull()?.minus(1) ?: 0
                val bankruptStrategy: String = playerNames[bankruptIndex]
                bankruptcyCounts[bankruptStrategy] = bankruptcyCounts.getOrDefault(bankruptStrategy, 0) + 1
            }
        }

        totalTurns += result.turnCount

        if ((gameIndex + 1) % 10 == 0 || gameIndex + 1 == config.gamesPerMatchup) {
            println("Progress: ${gameIndex + 1} / ${config.gamesPerMatchup} games completed")
        }
    }

    val elapsedTime: Long = System.currentTimeMillis() - startTime
    val avgTurns: Double = totalTurns.toDouble() / config.gamesPerMatchup
    val bankruptcyRate: Double = totalBankruptcyGames.toDouble() / config.gamesPerMatchup * 100

    println()
    println("=== Results ===")
    println()
    println("Bankruptcy Rate: %.1f%% (%d/%d games)".format(bankruptcyRate, totalBankruptcyGames, config.gamesPerMatchup))
    println("Average Turns: %.1f".format(avgTurns))
    println()
    println("Win Counts:")
    strategies.forEach { strategy ->
        val wins: Int = winCounts.getOrDefault(strategy.name, 0)
        val winRate: Double = wins.toDouble() / config.gamesPerMatchup * 100
        println("  ${strategy.name}: $wins wins (%.1f%%)".format(winRate))
    }
    println()
    println("Bankruptcy Counts:")
    strategies.forEach { strategy ->
        val bankruptcies: Int = bankruptcyCounts.getOrDefault(strategy.name, 0)
        println("  ${strategy.name}: $bankruptcies bankruptcies")
    }
    println()
    println("Elapsed time: ${elapsedTime / 1000.0} seconds")

    // 簡易レポート保存
    val outputDir = "experiment-results/$experimentId"
    File(outputDir).mkdirs()
    val reportPath = "$outputDir/multiplayer-report.txt"
    val report = buildString {
        appendLine("=== Multi-Player Experiment Report ===")
        appendLine("Players per game: ${config.playerCount}")
        appendLine("Total games: ${config.gamesPerMatchup}")
        appendLine("Player strategies: ${playerNames.joinToString(", ")}")
        appendLine()
        appendLine("Bankruptcy Rate: %.1f%% (%d/%d games)".format(bankruptcyRate, totalBankruptcyGames, config.gamesPerMatchup))
        appendLine("Average Turns: %.1f".format(avgTurns))
        appendLine()
        appendLine("Win Counts:")
        strategies.forEach { strategy ->
            val wins: Int = winCounts.getOrDefault(strategy.name, 0)
            val winRate: Double = wins.toDouble() / config.gamesPerMatchup * 100
            appendLine("  ${strategy.name}: $wins wins (%.1f%%)".format(winRate))
        }
        appendLine()
        appendLine("Bankruptcy Counts:")
        strategies.forEach { strategy ->
            val bankruptcies: Int = bankruptcyCounts.getOrDefault(strategy.name, 0)
            appendLine("  ${strategy.name}: $bankruptcies bankruptcies")
        }
    }
    File(reportPath).writeText(report)

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
    val playerCount: Int = 2,
)

/**
 * コマンドライン引数をパース
 */
fun parseComparisonArgs(args: Array<String>): ComparisonConfig {
    var gamesPerMatchup = DEFAULT_GAMES_PER_MATCHUP
    var strategyNames: List<String> = StrategyFactory.availableStrategies
    var showHelp = false
    var playerCount = 2

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
            "-p", "--players" -> {
                if (i + 1 < args.size) {
                    playerCount = args[i + 1].toIntOrNull()?.coerceIn(2, 6) ?: 2
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
        playerCount = playerCount,
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

                <h2>Game Termination Statistics</h2>
                <div class="info-box">
                    ${generateTerminationStatsHtml(matrix)}
                </div>

                <h2>Building Statistics</h2>
                <div class="info-box">
                    ${generateBuildingStatsHtml(matrix)}
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
 * 建築統計のHTML生成
 */
private fun generateBuildingStatsHtml(matrix: ComparisonMatrix): String {
    val sb = StringBuilder()

    sb.append("<p>Average values per game for each matchup:</p>")
    sb.append("<table>")
    sb.append("<tr>")
    sb.append("<th>Matchup</th>")
    sb.append("<th>Strategy</th>")
    sb.append("<th>Properties</th>")
    sb.append("<th>Houses</th>")
    sb.append("<th>Hotels</th>")
    sb.append("<th>Rent Paid</th>")
    sb.append("<th>Rent Received</th>")
    sb.append("</tr>")

    matrix.matchups.forEach { matchup ->
        // Strategy 1
        sb.append(
            """
            <tr>
                <td rowspan="2">${matchup.strategy1} vs ${matchup.strategy2}</td>
                <td><strong>${matchup.strategy1}</strong></td>
                <td>%.1f</td>
                <td>%.1f</td>
                <td>%.1f</td>
                <td>$%.0f</td>
                <td>$%.0f</td>
            </tr>
            """.format(
                matchup.strategy1BuildingStats.avgProperties,
                matchup.strategy1BuildingStats.avgHouses,
                matchup.strategy1BuildingStats.avgHotels,
                matchup.strategy1BuildingStats.avgRentPaid,
                matchup.strategy1BuildingStats.avgRentReceived,
            ),
        )
        // Strategy 2
        sb.append(
            """
            <tr>
                <td><strong>${matchup.strategy2}</strong></td>
                <td>%.1f</td>
                <td>%.1f</td>
                <td>%.1f</td>
                <td>$%.0f</td>
                <td>$%.0f</td>
            </tr>
            """.format(
                matchup.strategy2BuildingStats.avgProperties,
                matchup.strategy2BuildingStats.avgHouses,
                matchup.strategy2BuildingStats.avgHotels,
                matchup.strategy2BuildingStats.avgRentPaid,
                matchup.strategy2BuildingStats.avgRentReceived,
            ),
        )
    }

    sb.append("</table>")

    // 全体の平均を計算
    val allStats = matrix.matchups.flatMap { listOf(it.strategy1BuildingStats, it.strategy2BuildingStats) }
    val avgHouses: Double = allStats.map { it.avgHouses }.average()
    val avgHotels: Double = allStats.map { it.avgHotels }.average()

    if (avgHouses < 1.0 && avgHotels < 0.1) {
        sb.append("<p style=\"color: #d32f2f;\">⚠️ Very few buildings constructed! ")
        sb.append("This likely explains the 0% bankruptcy rate.</p>")
    }

    return sb.toString()
}

/**
 * ゲーム終了統計のHTML生成
 */
private fun generateTerminationStatsHtml(matrix: ComparisonMatrix): String {
    val totalGames: Int = matrix.matchups.sumOf { it.gamesPlayed }
    val totalBankruptcyGames: Int = matrix.matchups.sumOf { it.bankruptcyEndedGames }
    val overallBankruptcyRate: Double =
        if (totalGames > 0) totalBankruptcyGames.toDouble() / totalGames * 100 else 0.0
    val averageTurns: Double =
        if (matrix.matchups.isNotEmpty()) matrix.matchups.map { it.averageTurns }.average() else 0.0

    val sb = StringBuilder()
    sb.append("<p><strong>Overall Bankruptcy Rate:</strong> %.1f%% (%d/%d games)</p>".format(
        overallBankruptcyRate,
        totalBankruptcyGames,
        totalGames,
    ))
    sb.append("<p><strong>Average Turns per Game:</strong> %.1f</p>".format(averageTurns))

    if (overallBankruptcyRate < 50.0) {
        sb.append("<p style=\"color: #f57c00;\">⚠️ Low bankruptcy rate suggests many games hit the 1000-turn limit.</p>")
    }

    sb.append("<h4>Per Matchup Details:</h4>")
    sb.append("<table>")
    sb.append("<tr><th>Matchup</th><th>Bankruptcy Rate</th><th>Avg Turns</th></tr>")

    matrix.matchups.forEach { matchup ->
        sb.append(
            """
            <tr>
                <td>${matchup.strategy1} vs ${matchup.strategy2}</td>
                <td>%.1f%% (%d/%d)</td>
                <td>%.1f</td>
            </tr>
            """.format(
                matchup.bankruptcyRate * 100,
                matchup.bankruptcyEndedGames,
                matchup.gamesPlayed,
                matchup.averageTurns,
            ),
        )
    }

    sb.append("</table>")
    return sb.toString()
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
