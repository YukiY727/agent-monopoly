package com.monopoly.cli

import com.monopoly.domain.model.ColorGroup
import com.monopoly.domain.model.GameEvent
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Player
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

/**
 * ゲーム結果をサマリー形式でHTML出力するレポートジェネレーター
 *
 * Phase 2の詳細レポートとは別に、結果に特化したコンパクトなレポートを生成します。
 */
class SummaryReportGenerator {
    /**
     * ゲーム状態からサマリーレポートを生成
     */
    fun generate(gameState: GameState): String {
        val html = buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html lang=\"ja\">")
            appendLine("<head>")
            appendLine("    <meta charset=\"UTF-8\">")
            appendLine("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
            appendLine("    <title>Monopoly Game Summary</title>")
            appendLine("    <style>")
            appendLine(getStyleSheet())
            appendLine("    </style>")
            appendLine("</head>")
            appendLine("<body>")
            appendLine("    <div class=\"container\">")
            appendLine("        <h1>🏆 Game Summary</h1>")
            appendLine(generateWinnerSection(gameState))
            appendLine(generateStatisticsSection(gameState))
            appendLine(generatePlayerRankingSection(gameState))
            appendLine(generatePropertyDetailsSection(gameState))
            appendLine("    </div>")
            appendLine("</body>")
            appendLine("</html>")
        }
        return html
    }

    /**
     * サマリーレポートをファイルに保存
     */
    fun saveToFile(gameState: GameState, filename: String = generateFilename()): File {
        val html = generate(gameState)
        val file = File(filename)
        file.writeText(html)
        return file
    }

    /**
     * 勝者情報セクションを生成
     */
    private fun generateWinnerSection(gameState: GameState): String {
        val winner = gameState.players.maxByOrNull { it.getTotalAssets() }
            ?: return "<p>No winner found</p>"

        return buildString {
            appendLine("        <div class=\"winner-section\">")
            appendLine("            <div class=\"winner-badge\">🏆 Winner: ${escapeHtml(winner.name)}</div>")
            appendLine("            <div class=\"winner-stats\">")
            appendLine("                <div class=\"stat\">")
            appendLine("                    <span class=\"stat-label\">Final Assets</span>")
            appendLine("                    <span class=\"stat-value\">\$${winner.getTotalAssets()}</span>")
            appendLine("                </div>")
            appendLine("                <div class=\"stat\">")
            appendLine("                    <span class=\"stat-label\">Cash</span>")
            appendLine("                    <span class=\"stat-value\">\$${winner.money}</span>")
            appendLine("                </div>")
            appendLine("                <div class=\"stat\">")
            appendLine("                    <span class=\"stat-label\">Properties</span>")
            appendLine("                    <span class=\"stat-value\">${winner.ownedProperties.size}</span>")
            appendLine("                </div>")
            appendLine("            </div>")
            appendLine("        </div>")
        }
    }

    /**
     * ゲーム統計セクションを生成
     */
    private fun generateStatisticsSection(gameState: GameState): String {
        val statistics = calculateStatistics(gameState)

        return buildString {
            appendLine("        <div class=\"section\">")
            appendLine("            <h2>📊 Game Statistics</h2>")
            appendLine("            <table class=\"stats-table\">")
            appendLine("                <tr>")
            appendLine("                    <th>Total Turns</th>")
            appendLine("                    <td>${statistics.totalTurns}</td>")
            appendLine("                </tr>")
            appendLine("                <tr>")
            appendLine("                    <th>Players</th>")
            appendLine("                    <td>${statistics.totalPlayers}</td>")
            appendLine("                </tr>")
            appendLine("                <tr>")
            appendLine("                    <th>Bankruptcies</th>")
            appendLine("                    <td>${statistics.bankruptcies}</td>")
            appendLine("                </tr>")
            appendLine("                <tr>")
            appendLine("                    <th>Properties Purchased</th>")
            appendLine("                    <td>${statistics.propertiesPurchased}</td>")
            appendLine("                </tr>")
            appendLine("                <tr>")
            appendLine("                    <th>Total Rent Paid</th>")
            appendLine("                    <td>\$${statistics.totalRentPaid}</td>")
            appendLine("                </tr>")
            appendLine("            </table>")
            appendLine("        </div>")
        }
    }

    /**
     * プレイヤーランキングセクションを生成
     */
    private fun generatePlayerRankingSection(gameState: GameState): String {
        val ranking = getPlayerRanking(gameState)

        return buildString {
            appendLine("        <div class=\"section\">")
            appendLine("            <h2>👥 Player Ranking</h2>")
            appendLine("            <table class=\"ranking-table\">")
            appendLine("                <thead>")
            appendLine("                    <tr>")
            appendLine("                        <th>Rank</th>")
            appendLine("                        <th>Player</th>")
            appendLine("                        <th>Status</th>")
            appendLine("                        <th>Final Assets</th>")
            appendLine("                        <th>Cash</th>")
            appendLine("                        <th>Properties</th>")
            appendLine("                    </tr>")
            appendLine("                </thead>")
            appendLine("                <tbody>")

            ranking.forEachIndexed { index, entry ->
                appendLine(generatePlayerRankingRow(entry, index))
            }

            appendLine("                </tbody>")
            appendLine("            </table>")
            appendLine("        </div>")
        }
    }

    /**
     * プレイヤーランキング行を生成
     */
    private fun generatePlayerRankingRow(entry: PlayerRankingEntry, index: Int): String {
        val rankEmoji = when (index) {
            0 -> " 🥇"
            1 -> " 🥈"
            2 -> " 🥉"
            else -> ""
        }

        val statusClass = if (entry.player.isBankrupt) "status-bankrupt" else "status-active"
        val statusText = if (entry.player.isBankrupt) "BANKRUPT" else "ACTIVE"
        val bankruptClass = if (entry.player.isBankrupt) " bankrupted" else ""

        return buildString {
            appendLine("                    <tr class=\"rank-${index + 1}$bankruptClass\">")
            appendLine("                        <td>${entry.rank}${if (index < 3) rankEmoji else ""}</td>")
            appendLine("                        <td>${escapeHtml(entry.player.name)}</td>")
            appendLine("                        <td class=\"$statusClass\">$statusText</td>")
            appendLine("                        <td>\$${entry.totalAssets}</td>")
            appendLine("                        <td>\$${entry.player.money}</td>")
            appendLine("                        <td>${entry.player.ownedProperties.size}</td>")
            appendLine("                    </tr>")
        }.toString()
    }

    /**
     * 所有プロパティ詳細セクションを生成
     */
    private fun generatePropertyDetailsSection(gameState: GameState): String {
        return buildString {
            appendLine("        <div class=\"section\">")
            appendLine("            <h2>🏠 Property Details</h2>")

            gameState.players.forEach { player ->
                appendLine("            <div class=\"player-properties\">")
                appendLine("                <h3>${escapeHtml(player.name)}'s Properties</h3>")

                if (player.ownedProperties.isEmpty()) {
                    appendLine("                <p class=\"no-properties\">No properties owned</p>")
                } else {
                    appendLine("                <table class=\"properties-table\">")
                    appendLine("                    <thead>")
                    appendLine("                        <tr>")
                    appendLine("                            <th>Property</th>")
                    appendLine("                            <th>Price</th>")
                    appendLine("                            <th>Rent</th>")
                    appendLine("                            <th>Color Group</th>")
                    appendLine("                        </tr>")
                    appendLine("                    </thead>")
                    appendLine("                    <tbody>")

                    player.ownedProperties.forEach { property ->
                        val colorClass = "color-${property.colorGroup.name.lowercase().replace("_", "-")}"
                        appendLine("                        <tr>")
                        appendLine("                            <td>${escapeHtml(property.name)}</td>")
                        appendLine("                            <td>\$${property.price}</td>")
                        appendLine("                            <td>\$${property.rent}</td>")
                        appendLine("                            <td class=\"$colorClass\">${property.colorGroup.name.replace("_", " ")}</td>")
                        appendLine("                        </tr>")
                    }

                    appendLine("                    </tbody>")
                    appendLine("                </table>")
                }

                appendLine("            </div>")
            }

            appendLine("        </div>")
        }
    }

    /**
     * ゲーム統計を計算
     */
    private fun calculateStatistics(gameState: GameState): GameStatistics {
        val bankruptcies = gameState.players.count { it.isBankrupt }

        val purchaseEvents = gameState.events.filterIsInstance<GameEvent.PropertyPurchased>()
        val propertiesPurchased = purchaseEvents.size

        val rentEvents = gameState.events.filterIsInstance<GameEvent.RentPaid>()
        val totalRentPaid = rentEvents.sumOf { it.amount }

        return GameStatistics(
            totalTurns = gameState.turnNumber,
            totalPlayers = gameState.players.size,
            bankruptcies = bankruptcies,
            propertiesPurchased = propertiesPurchased,
            totalRentPaid = totalRentPaid,
        )
    }

    /**
     * プレイヤーランキングを取得
     */
    private fun getPlayerRanking(gameState: GameState): List<PlayerRankingEntry> {
        return gameState.players
            .map { player ->
                PlayerRankingEntry(
                    player = player,
                    rank = 0,
                    totalAssets = player.getTotalAssets(),
                )
            }
            .sortedByDescending { it.totalAssets }
            .mapIndexed { index, entry ->
                entry.copy(rank = index + 1)
            }
    }

    /**
     * HTMLエスケープ処理
     */
    private fun escapeHtml(text: String): String =
        text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")

    /**
     * CSSスタイルシートを取得
     */
    private fun getStyleSheet(): String =
        """
        :root {
            --color-primary: #2c3e50;
            --color-winner: #27ae60;
            --color-bankrupt: #e74c3c;
            --color-active: #3498db;
            --color-bg: #f5f5f5;
            --color-card: #ffffff;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: var(--color-bg);
            margin: 0;
            padding: 20px;
            color: #333;
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
            background-color: var(--color-card);
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }

        h1 {
            color: var(--color-primary);
            border-bottom: 3px solid var(--color-active);
            padding-bottom: 10px;
            margin-bottom: 30px;
        }

        h2 {
            color: #34495e;
            margin-top: 30px;
        }

        h3 {
            color: #555;
            margin-top: 20px;
            margin-bottom: 10px;
        }

        .section {
            margin-bottom: 40px;
        }

        /* 勝者セクション */
        .winner-section {
            background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
            padding: 30px;
            border-radius: 15px;
            text-align: center;
            margin-bottom: 30px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
        }

        .winner-badge {
            font-size: 2em;
            font-weight: bold;
            color: var(--color-winner);
            margin-bottom: 20px;
            text-shadow: 2px 2px 4px rgba(0,0,0,0.1);
        }

        .winner-stats {
            display: flex;
            justify-content: center;
            gap: 40px;
            flex-wrap: wrap;
        }

        .stat {
            display: flex;
            flex-direction: column;
            align-items: center;
        }

        .stat-label {
            font-size: 0.9em;
            color: #555;
            margin-bottom: 5px;
        }

        .stat-value {
            font-size: 1.5em;
            font-weight: bold;
            color: var(--color-primary);
        }

        /* テーブル */
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 15px;
        }

        th, td {
            padding: 12px;
            text-align: left;
            border-bottom: 1px solid #ddd;
        }

        th {
            background-color: var(--color-active);
            color: white;
            font-weight: bold;
        }

        tr:hover {
            background-color: #f5f5f5;
        }

        /* ランキングテーブル */
        .ranking-table .rank-1 {
            background-color: #ffd700;
        }

        .ranking-table .rank-2 {
            background-color: #c0c0c0;
        }

        .ranking-table .rank-3 {
            background-color: #cd7f32;
        }

        .ranking-table .bankrupted {
            opacity: 0.6;
        }

        .ranking-table .bankrupted td {
            background-color: #fadbd8 !important;
        }

        /* ステータスバッジ */
        .status-active {
            color: var(--color-active);
            font-weight: bold;
        }

        .status-bankrupt {
            color: var(--color-bankrupt);
            font-weight: bold;
        }

        /* 統計テーブル */
        .stats-table {
            max-width: 500px;
        }

        .stats-table th {
            width: 60%;
        }

        .stats-table td {
            width: 40%;
            font-weight: bold;
            text-align: right;
        }

        /* プロパティテーブル */
        .player-properties {
            margin-bottom: 30px;
        }

        .properties-table {
            margin-top: 10px;
        }

        .no-properties {
            color: #999;
            font-style: italic;
            margin: 10px 0;
        }

        /* カラーグループ */
        .color-brown {
            color: #8b4513;
            font-weight: bold;
        }

        .color-light-blue {
            color: #87ceeb;
            font-weight: bold;
        }

        .color-pink {
            color: #ff69b4;
            font-weight: bold;
        }

        .color-orange {
            color: #ff8c00;
            font-weight: bold;
        }

        .color-red {
            color: #dc143c;
            font-weight: bold;
        }

        .color-yellow {
            color: #ffd700;
            font-weight: bold;
        }

        .color-green {
            color: #228b22;
            font-weight: bold;
        }

        .color-dark-blue {
            color: #00008b;
            font-weight: bold;
        }
        """.trimIndent()

    /**
     * ファイル名を生成（タイムスタンプ付き）
     */
    private fun generateFilename(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return "game-summary-$timestamp.html"
    }

    /**
     * ゲーム統計データクラス
     */
    private data class GameStatistics(
        val totalTurns: Int,
        val totalPlayers: Int,
        val bankruptcies: Int,
        val propertiesPurchased: Int,
        val totalRentPaid: Int,
    )

    /**
     * プレイヤーランキングエントリ
     */
    private data class PlayerRankingEntry(
        val player: Player,
        val rank: Int,
        val totalAssets: Int,
    )
}
