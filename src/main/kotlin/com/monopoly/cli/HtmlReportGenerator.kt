package com.monopoly.cli

import com.monopoly.domain.model.GameEvent
import com.monopoly.domain.model.GameState
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

/**
 * ゲーム進行をHTML形式で可視化するレポートジェネレーター
 */
class HtmlReportGenerator {
    /**
     * ゲーム状態からHTMLレポートを生成
     */
    fun generate(gameState: GameState): String {
        val html = buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html lang=\"ja\">")
            appendLine("<head>")
            appendLine("    <meta charset=\"UTF-8\">")
            appendLine("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
            appendLine("    <title>Monopoly Game Report</title>")
            appendLine("    <style>")
            appendLine(getStyleSheet())
            appendLine("    </style>")
            appendLine("</head>")
            appendLine("<body>")
            appendLine("    <div class=\"container\">")
            appendLine("        <h1>🎮 Monopoly Game Report</h1>")
            appendLine(generateGameSummary(gameState))
            appendLine(generatePlayerStatus(gameState))
            appendLine(generateEventTimeline(gameState.events))
            appendLine("    </div>")
            appendLine("</body>")
            appendLine("</html>")
        }
        return html
    }

    /**
     * HTMLレポートをファイルに保存
     */
    fun saveToFile(gameState: GameState, filename: String = generateFilename()): File {
        val html = generate(gameState)
        val file = File(filename)
        file.writeText(html)
        return file
    }

    /**
     * ゲームサマリーセクションを生成
     */
    private fun generateGameSummary(gameState: GameState): String {
        val gameStartedEvent = gameState.events.filterIsInstance<GameEvent.GameStarted>().firstOrNull()
        val gameEndedEvent = gameState.events.filterIsInstance<GameEvent.GameEnded>().firstOrNull()

        return buildString {
            appendLine("        <div class=\"section\">")
            appendLine("            <h2>📊 Game Summary</h2>")
            appendLine("            <table>")
            appendLine("                <tr>")
            appendLine("                    <th>Players</th>")
            appendLine("                    <td>${gameStartedEvent?.playerNames?.joinToString(", ") ?: "N/A"}</td>")
            appendLine("                </tr>")
            appendLine("                <tr>")
            appendLine("                    <th>Winner</th>")
            appendLine("                    <td class=\"winner\">${gameEndedEvent?.winner ?: "N/A"}</td>")
            appendLine("                </tr>")
            appendLine("                <tr>")
            appendLine("                    <th>Total Turns</th>")
            appendLine("                    <td>${gameEndedEvent?.totalTurns ?: "N/A"}</td>")
            appendLine("                </tr>")
            appendLine("                <tr>")
            appendLine("                    <th>Active Players</th>")
            appendLine("                    <td>${gameState.getActivePlayerCount()}</td>")
            appendLine("                </tr>")
            appendLine("            </table>")
            appendLine("        </div>")
        }
    }

    /**
     * プレイヤー状態セクションを生成
     */
    private fun generatePlayerStatus(gameState: GameState): String {
        return buildString {
            appendLine("        <div class=\"section\">")
            appendLine("            <h2>👥 Final Player Status</h2>")
            appendLine("            <table>")
            appendLine("                <tr>")
            appendLine("                    <th>Player</th>")
            appendLine("                    <th>Status</th>")
            appendLine("                    <th>Money</th>")
            appendLine("                    <th>Properties</th>")
            appendLine("                    <th>Total Assets</th>")
            appendLine("                </tr>")

            gameState.players.forEach { player ->
                val status = if (player.isBankrupt) "BANKRUPT" else "ACTIVE"
                val statusClass = if (player.isBankrupt) "bankrupt" else "active"

                appendLine("                <tr class=\"$statusClass\">")
                appendLine("                    <td>${player.name}</td>")
                appendLine("                    <td>$status</td>")
                appendLine("                    <td>\$${player.money}</td>")
                appendLine("                    <td>${player.ownedProperties.size}</td>")
                appendLine("                    <td>\$${player.getTotalAssets()}</td>")
                appendLine("                </tr>")
            }

            appendLine("            </table>")
            appendLine("        </div>")
        }
    }

    /**
     * イベントタイムラインセクションを生成
     */
    private fun generateEventTimeline(events: List<GameEvent>): String {
        return buildString {
            appendLine("        <div class=\"section\">")
            appendLine("            <h2>📜 Event Timeline</h2>")
            appendLine("            <div class=\"timeline\">")

            events.forEach { event ->
                val eventHtml = formatEventAsHtml(event)
                if (eventHtml.isNotEmpty()) {
                    appendLine("                <div class=\"event ${getEventClass(event)}\">")
                    appendLine("                    $eventHtml")
                    appendLine("                </div>")
                }
            }

            appendLine("            </div>")
            appendLine("        </div>")
        }
    }

    /**
     * イベントをHTML形式でフォーマット
     */
    private fun formatEventAsHtml(event: GameEvent): String =
        when (event) {
            is GameEvent.GameStarted ->
                "🎮 <strong>Game Started!</strong> Players: ${event.playerNames.joinToString(", ")}"

            is GameEvent.DiceRolled ->
                "🎲 <strong>${event.playerName}</strong> rolled ${event.die1} + ${event.die2} = ${event.total}"

            is GameEvent.PlayerMoved -> {
                val goBonus = if (event.passedGo) " <span class=\"go-bonus\">(Passed GO! +\$200)</span>" else ""
                "🚶 <strong>${event.playerName}</strong> moved from position ${event.fromPosition} to ${event.toPosition}$goBonus"
            }

            is GameEvent.PropertyPurchased ->
                "💰 <strong>${event.playerName}</strong> purchased <em>${event.propertyName}</em> for \$${event.price}"

            is GameEvent.RentPaid ->
                "💸 <strong>${event.payerName}</strong> paid \$${event.amount} rent to <strong>${event.receiverName}</strong> for <em>${event.propertyName}</em>"

            is GameEvent.PlayerBankrupted ->
                "💀 <strong>${event.playerName}</strong> went <span class=\"bankrupt\">BANKRUPT!</span> (Final money: \$${event.finalMoney})"

            is GameEvent.TurnStarted ->
                "<div class=\"turn-header\">--- Turn ${event.turnNumber + 1}: ${event.playerName}'s turn ---</div>"

            is GameEvent.TurnEnded ->
                ""

            is GameEvent.GameEnded ->
                "🏆 <strong>Game Over!</strong> Winner: <span class=\"winner\">${event.winner}</span> (Total turns: ${event.totalTurns})"

            is GameEvent.PlayerSentToJail ->
                "🚔 <strong>${event.playerName}</strong> was sent to <span class=\"jail\">JAIL!</span>"

            is GameEvent.PlayerExitedJail ->
                "🔓 <strong>${event.playerName}</strong> exited jail (${event.method})"
        }

    /**
     * イベントに対応するCSSクラスを取得
     */
    private fun getEventClass(event: GameEvent): String =
        when (event) {
            is GameEvent.GameStarted -> "game-start"
            is GameEvent.GameEnded -> "game-end"
            is GameEvent.TurnStarted -> "turn-start"
            is GameEvent.PropertyPurchased -> "property-purchase"
            is GameEvent.RentPaid -> "rent-paid"
            is GameEvent.PlayerBankrupted -> "bankrupt"
            else -> ""
        }

    /**
     * CSSスタイルシートを取得
     */
    private fun getStyleSheet(): String =
        """
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: #f5f5f5;
            margin: 0;
            padding: 20px;
            color: #333;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background-color: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
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
        .section {
            margin-bottom: 40px;
        }
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
            background-color: #3498db;
            color: white;
            font-weight: bold;
        }
        tr:hover {
            background-color: #f5f5f5;
        }
        .active {
            background-color: #e8f8f5;
        }
        .bankrupt {
            background-color: #fadbd8;
            color: #a93226;
        }
        .winner {
            color: #27ae60;
            font-weight: bold;
        }
        .timeline {
            margin-top: 15px;
        }
        .event {
            padding: 10px 15px;
            margin: 5px 0;
            border-left: 4px solid #bdc3c7;
            background-color: #f9f9f9;
        }
        .event.game-start {
            border-left-color: #3498db;
            background-color: #ebf5fb;
        }
        .event.game-end {
            border-left-color: #27ae60;
            background-color: #eafaf1;
        }
        .event.turn-start {
            border-left-color: #f39c12;
            background-color: #fef5e7;
            font-weight: bold;
        }
        .event.property-purchase {
            border-left-color: #2ecc71;
            background-color: #eafaf1;
        }
        .event.rent-paid {
            border-left-color: #e74c3c;
            background-color: #fadbd8;
        }
        .event.bankrupt {
            border-left-color: #c0392b;
            background-color: #f5b7b1;
        }
        .turn-header {
            font-size: 1.1em;
            font-weight: bold;
            color: #f39c12;
        }
        .go-bonus {
            color: #27ae60;
            font-weight: bold;
        }
        """.trimIndent()

    /**
     * ファイル名を生成（タイムスタンプ付き）
     */
    private fun generateFilename(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return "game-report-$timestamp.html"
    }
}
