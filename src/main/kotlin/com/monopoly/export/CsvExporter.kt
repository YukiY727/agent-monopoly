package com.monopoly.export

import com.monopoly.simulation.MultiGameResult
import com.monopoly.simulation.SingleGameResult
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

/**
 * ゲーム結果をCSV形式でエクスポートするクラス
 */
class CsvExporter {
    /**
     * ゲーム結果をCSV形式でエクスポート
     *
     * @param result 複数ゲーム実行結果
     * @param filename ファイル名（省略時は自動生成）
     * @return 保存したファイル
     */
    fun export(result: MultiGameResult, filename: String = generateFilename()): File {
        val csv = toCsv(result)
        val file = File(filename)
        file.writeText(csv)
        return file
    }

    /**
     * MultiGameResultをCSV文字列に変換
     */
    private fun toCsv(result: MultiGameResult): String {
        return buildString {
            // ヘッダー行
            appendLine(generateHeader(result))

            // データ行
            result.gameResults.forEach { gameResult ->
                appendLine(generateDataRow(gameResult))
            }
        }
    }

    /**
     * CSVヘッダーを生成
     */
    private fun generateHeader(result: MultiGameResult): String {
        val playerNames = result.gameResults.first().finalState.players.map { it.name }

        val columns = mutableListOf("GameNumber", "Winner", "TotalTurns")

        playerNames.forEach { playerName ->
            columns.add("${playerName}FinalAssets")
            columns.add("${playerName}FinalCash")
            columns.add("${playerName}Properties")
        }

        return columns.joinToString(",")
    }

    /**
     * CSVデータ行を生成
     */
    private fun generateDataRow(gameResult: SingleGameResult): String {
        val values = mutableListOf<String>()

        values.add(gameResult.gameNumber.toString())
        values.add(gameResult.winner)
        values.add(gameResult.totalTurns.toString())

        gameResult.finalState.players.forEach { player ->
            values.add(player.getTotalAssets().toString())
            values.add(player.money.toString())
            values.add(player.ownedProperties.size.toString())
        }

        return values.joinToString(",")
    }

    /**
     * ファイル名を生成（タイムスタンプ付き）
     */
    private fun generateFilename(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return "simulation-results-$timestamp.csv"
    }
}
