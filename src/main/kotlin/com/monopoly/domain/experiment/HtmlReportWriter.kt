package com.monopoly.domain.experiment

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

/**
 * 実験統計をHTMLレポートとして書き込むクラス
 *
 * @property outputDir 出力ディレクトリ（デフォルト: "experiment-results"）
 */
class HtmlReportWriter(
    private val outputDir: String = DEFAULT_OUTPUT_DIR,
) {
    /**
     * 統計をHTMLレポートに書き込む
     *
     * @param statistics ゲーム統計のリスト
     * @param aggregated 集計統計
     * @return 書き込まれたHTMLファイルのパス
     */
    fun write(
        statistics: List<GameStatistics>,
        aggregated: AggregatedStatistics,
    ): String {
        // ディレクトリを作成
        val dir = File(outputDir)
        dir.mkdirs()

        // タイムスタンプ付きファイル名を生成
        val timestamp: String = generateTimestamp()
        val htmlPath = "$outputDir/experiment-$timestamp.html"

        // HTMLを生成
        val html: String = generateHtml(statistics, aggregated)

        // ファイルに書き込み
        File(htmlPath).writeText(html)

        return htmlPath
    }

    private fun generateHtml(
        statistics: List<GameStatistics>,
        aggregated: AggregatedStatistics,
    ): String =
        """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Monopoly Experiment Report</title>
            <style>
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
                    max-width: 1200px;
                    margin: 0 auto;
                    padding: 20px;
                    background-color: #f5f5f5;
                    color: #333;
                }
                h1 {
                    color: #2c3e50;
                    border-bottom: 3px solid #3498db;
                    padding-bottom: 10px;
                }
                h2 {
                    color: #34495e;
                    margin-top: 30px;
                }
                .summary {
                    background-color: white;
                    padding: 20px;
                    border-radius: 8px;
                    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    margin-bottom: 20px;
                }
                .summary-grid {
                    display: grid;
                    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
                    gap: 15px;
                    margin-top: 15px;
                }
                .summary-item {
                    background-color: #ecf0f1;
                    padding: 15px;
                    border-radius: 5px;
                }
                .summary-item .label {
                    font-size: 12px;
                    color: #7f8c8d;
                    text-transform: uppercase;
                    font-weight: 600;
                }
                .summary-item .value {
                    font-size: 24px;
                    color: #2c3e50;
                    font-weight: bold;
                    margin-top: 5px;
                }
                .win-rates {
                    background-color: white;
                    padding: 20px;
                    border-radius: 8px;
                    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    margin-bottom: 20px;
                }
                .bar-chart {
                    margin-top: 15px;
                }
                .bar-item {
                    margin-bottom: 15px;
                }
                .bar-label {
                    font-size: 14px;
                    color: #34495e;
                    margin-bottom: 5px;
                    font-weight: 500;
                }
                .bar-container {
                    background-color: #ecf0f1;
                    height: 30px;
                    border-radius: 15px;
                    overflow: hidden;
                    position: relative;
                }
                .bar-fill {
                    background: linear-gradient(90deg, #3498db, #2980b9);
                    height: 100%;
                    transition: width 0.3s ease;
                    display: flex;
                    align-items: center;
                    justify-content: flex-end;
                    padding-right: 10px;
                }
                .bar-percentage {
                    color: white;
                    font-size: 12px;
                    font-weight: bold;
                }
                .bar-fill-green {
                    background: linear-gradient(90deg, #27ae60, #229954);
                }
                .bar-value {
                    color: white;
                    font-size: 11px;
                    font-weight: bold;
                }
                .histogram {
                    display: flex;
                    align-items: flex-end;
                    gap: 5px;
                    margin-top: 15px;
                    height: 150px;
                }
                .histogram-bar {
                    flex: 1;
                    background: linear-gradient(180deg, #3498db, #2980b9);
                    border-radius: 3px 3px 0 0;
                    position: relative;
                    min-height: 10px;
                }
                .histogram-label {
                    position: absolute;
                    bottom: -25px;
                    left: 50%;
                    transform: translateX(-50%);
                    font-size: 11px;
                    color: #7f8c8d;
                    white-space: nowrap;
                }
                .histogram-value {
                    position: absolute;
                    top: -20px;
                    left: 50%;
                    transform: translateX(-50%);
                    font-size: 11px;
                    color: #34495e;
                    font-weight: bold;
                }
                table {
                    width: 100%;
                    border-collapse: collapse;
                    background-color: white;
                    border-radius: 8px;
                    overflow: hidden;
                    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                }
                th {
                    background-color: #34495e;
                    color: white;
                    padding: 12px;
                    text-align: left;
                    font-weight: 600;
                }
                td {
                    padding: 10px 12px;
                    border-bottom: 1px solid #ecf0f1;
                }
                tr:last-child td {
                    border-bottom: none;
                }
                tr:hover {
                    background-color: #f8f9fa;
                }
                .winner {
                    color: #27ae60;
                    font-weight: bold;
                }
                .timestamp {
                    color: #95a5a6;
                    font-size: 14px;
                    margin-top: 10px;
                }
            </style>
        </head>
        <body>
            <h1>Monopoly Experiment Report</h1>
            <p class="timestamp">Generated: ${Date()}</p>

            ${generateSummarySection(aggregated)}
            ${generateWinRatesSection(aggregated)}
            ${generateAverageFinalAssetsSection(aggregated)}
            ${generateTurnCountDistributionSection(statistics)}
            ${generatePlayerStatisticsSection(statistics)}
            ${generateGameDetailsSection(statistics)}
        </body>
        </html>
        """.trimIndent()

    private fun generateSummarySection(aggregated: AggregatedStatistics): String =
        """
        <div class="summary">
            <h2>Summary Statistics</h2>
            <div class="summary-grid">
                <div class="summary-item">
                    <div class="label">Total Games</div>
                    <div class="value">${aggregated.totalGames}</div>
                </div>
                <div class="summary-item">
                    <div class="label">Average Turn Count</div>
                    <div class="value">${"%.0f".format(aggregated.averageTurnCount)}</div>
                </div>
                <div class="summary-item">
                    <div class="label">Total Duration</div>
                    <div class="value">${aggregated.totalDuration / 1000.0}s</div>
                </div>
            </div>
        </div>
        """.trimIndent()

    private fun generateWinRatesSection(aggregated: AggregatedStatistics): String {
        val winRatesHtml: String =
            aggregated.winRates
                .toList()
                .sortedByDescending { it.second }
                .joinToString("\n") { (player, rate) ->
                    val percentage: Int = (rate * 100).toInt()
                    """
                    <div class="bar-item">
                        <div class="bar-label">$player</div>
                        <div class="bar-container">
                            <div class="bar-fill" style="width: $percentage%">
                                <span class="bar-percentage">$percentage%</span>
                            </div>
                        </div>
                    </div>
                    """.trimIndent()
                }

        return """
            <div class="win-rates">
                <h2>Win Rates</h2>
                <div class="bar-chart">
                    $winRatesHtml
                </div>
            </div>
        """.trimIndent()
    }

    private fun generateAverageFinalAssetsSection(aggregated: AggregatedStatistics): String {
        // 最大値を取得してスケーリング
        val maxAssets: Double = aggregated.averageFinalAssets.values.maxOrNull() ?: 1.0

        val assetsHtml: String =
            aggregated.averageFinalAssets
                .toList()
                .sortedByDescending { it.second }
                .joinToString("\n") { (player, assets) ->
                    val percentage: Int = ((assets / maxAssets) * 100).toInt()
                    val formattedAssets: String = "%.0f".format(assets)
                    """
                    <div class="bar-item">
                        <div class="bar-label">$player</div>
                        <div class="bar-container">
                            <div class="bar-fill bar-fill-green" style="width: $percentage%">
                                <span class="bar-value">$$formattedAssets</span>
                            </div>
                        </div>
                    </div>
                    """.trimIndent()
                }

        return """
            <div class="win-rates">
                <h2>Average Final Assets</h2>
                <div class="bar-chart">
                    $assetsHtml
                </div>
            </div>
        """.trimIndent()
    }

    private fun generateTurnCountDistributionSection(statistics: List<GameStatistics>): String {
        if (statistics.isEmpty()) {
            return ""
        }

        // ターン数を10の倍数でグループ化
        val minTurn: Int = statistics.minOf { it.turnCount }
        val maxTurn: Int = statistics.maxOf { it.turnCount }

        // ビンの範囲を計算（10ターン刻み）
        val binSize = 10
        val bins: MutableMap<Int, Int> = mutableMapOf()

        statistics.forEach { stat ->
            val bin: Int = (stat.turnCount / binSize) * binSize
            bins[bin] = bins.getOrDefault(bin, 0) + 1
        }

        // 最大カウントを取得（高さのスケーリング用）
        val maxCount: Int = bins.values.maxOrNull() ?: 1

        // ヒストグラムのHTMLを生成
        val histogramHtml: String =
            bins
                .toList()
                .sortedBy { it.first }
                .joinToString("\n") { (bin, count) ->
                    val height: Double = (count.toDouble() / maxCount) * 100
                    val label: String = "$bin-${bin + binSize - 1}"
                    """
                    <div class="histogram-bar" style="height: $height%">
                        <div class="histogram-value">$count</div>
                        <div class="histogram-label">$label</div>
                    </div>
                    """.trimIndent()
                }

        return """
            <div class="win-rates">
                <h2>Turn Count Distribution</h2>
                <div class="histogram">
                    $histogramHtml
                </div>
            </div>
        """.trimIndent()
    }

    private fun generatePlayerStatisticsSection(statistics: List<GameStatistics>): String {
        // TODO: プレイヤー統計の詳細表示は次のコミットで実装
        return ""
    }

    private fun generateGameDetailsSection(statistics: List<GameStatistics>): String {
        val rows: String =
            statistics.joinToString("\n") { stat ->
                """
                <tr>
                    <td>${stat.gameId}</td>
                    <td>${stat.turnCount}</td>
                    <td class="winner">${stat.winner}</td>
                    <td>${stat.finalAssets.entries.joinToString(", ") { "${it.key}: ${it.value}" }}</td>
                </tr>
                """.trimIndent()
            }

        return """
            <div>
                <h2>Game Details</h2>
                <table>
                    <thead>
                        <tr>
                            <th>Game ID</th>
                            <th>Turn Count</th>
                            <th>Winner</th>
                            <th>Final Assets</th>
                        </tr>
                    </thead>
                    <tbody>
                        $rows
                    </tbody>
                </table>
            </div>
        """.trimIndent()
    }

    private fun generateTimestamp(): String {
        val formatter = SimpleDateFormat("yyyyMMdd-HHmmss")
        return formatter.format(Date())
    }

    companion object {
        /** デフォルトの出力ディレクトリ */
        const val DEFAULT_OUTPUT_DIR = "experiment-results"
    }
}
