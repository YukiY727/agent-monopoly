package com.monopoly.export

import com.monopoly.statistics.GameStatistics
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

/**
 * 統計データをJSON形式でエクスポートするクラス
 */
class JsonExporter {
    /**
     * 統計データをJSON形式でエクスポート
     *
     * @param statistics 統計データ
     * @param filename ファイル名（省略時は自動生成）
     * @return 保存したファイル
     */
    fun export(statistics: GameStatistics, filename: String = generateFilename()): File {
        val json = toJson(statistics)
        val file = File(filename)
        file.writeText(json)
        return file
    }

    /**
     * 統計データをJSON文字列に変換
     */
    private fun toJson(statistics: GameStatistics): String {
        return buildString {
            appendLine("{")
            appendLine("  \"totalGames\": ${statistics.totalGames},")
            appendLine("  \"timestamp\": ${statistics.timestamp},")
            appendLine("  \"playerStats\": {")

            val playerEntries = statistics.playerStats.entries.toList()
            playerEntries.forEachIndexed { index, (playerName, stats) ->
                appendLine("    \"$playerName\": {")
                appendLine("      \"playerName\": \"$playerName\",")
                appendLine("      \"wins\": ${stats.wins},")
                appendLine("      \"winRate\": ${stats.winRate},")
                appendLine("      \"averageFinalAssets\": ${stats.averageFinalAssets},")
                appendLine("      \"averageFinalCash\": ${stats.averageFinalCash},")
                appendLine("      \"averagePropertiesOwned\": ${stats.averagePropertiesOwned}")

                // 最後の要素以外はカンマを付ける
                val comma = if (index < playerEntries.size - 1) "," else ""
                appendLine("    }$comma")
            }

            appendLine("  },")
            appendLine("  \"turnStats\": {")
            appendLine("    \"averageTurns\": ${statistics.turnStats.averageTurns},")
            appendLine("    \"minTurns\": ${statistics.turnStats.minTurns},")
            appendLine("    \"maxTurns\": ${statistics.turnStats.maxTurns},")
            appendLine("    \"standardDeviation\": ${statistics.turnStats.standardDeviation}")
            appendLine("  }")
            append("}")
        }
    }

    /**
     * ファイル名を生成（タイムスタンプ付き）
     */
    private fun generateFilename(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return "simulation-stats-$timestamp.json"
    }
}
