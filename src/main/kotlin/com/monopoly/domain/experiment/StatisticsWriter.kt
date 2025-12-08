package com.monopoly.domain.experiment

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

/**
 * 書き込まれたファイルのパス情報
 *
 * @property jsonPath JSONファイルのパス
 * @property csvPath CSVファイルのパス
 */
data class WrittenFiles(
    val jsonPath: String,
    val csvPath: String,
)

/**
 * JSON用の実験結果ラッパー
 *
 * @property games ゲーム統計のリスト
 * @property aggregated 集計統計
 */
@Serializable
private data class ExperimentResult(
    val games: List<GameStatistics>,
    val aggregated: AggregatedStatistics,
)

/**
 * 実験統計をファイルに書き込むクラス
 *
 * @property outputDir 出力ディレクトリ（デフォルト: "experiment-results"）
 */
class StatisticsWriter(
    private val outputDir: String = DEFAULT_OUTPUT_DIR,
) {
    /**
     * 統計をJSONとCSVファイルに書き込む
     *
     * @param statistics ゲーム統計のリスト
     * @param aggregated 集計統計
     * @return 書き込まれたファイルのパス情報
     */
    fun write(
        statistics: List<GameStatistics>,
        aggregated: AggregatedStatistics,
    ): WrittenFiles {
        // ディレクトリを作成
        val dir = File(outputDir)
        dir.mkdirs()

        // タイムスタンプ付きファイル名を生成
        val timestamp: String = generateTimestamp()
        val jsonPath = "$outputDir/experiment-$timestamp.json"
        val csvPath = "$outputDir/experiment-$timestamp.csv"

        // JSONファイルに書き込み
        writeJson(jsonPath, statistics, aggregated)

        // CSVファイルに書き込み
        writeCsv(csvPath, statistics)

        return WrittenFiles(jsonPath, csvPath)
    }

    private fun writeJson(
        path: String,
        statistics: List<GameStatistics>,
        aggregated: AggregatedStatistics,
    ) {
        val result =
            ExperimentResult(
                games = statistics,
                aggregated = aggregated,
            )

        val json: Json =
            Json {
                prettyPrint = true
            }

        val jsonString: String = json.encodeToString(result)
        File(path).writeText(jsonString)
    }

    private fun writeCsv(
        path: String,
        statistics: List<GameStatistics>,
    ) {
        val csv: StringBuilder = StringBuilder()

        // ヘッダー行
        csv.append("gameId,timestamp,turnCount,winner,finalAssets,bankruptcyOrder\n")

        // データ行
        statistics.forEach { stat ->
            csv.append("${stat.gameId},")
            csv.append("${stat.timestamp},")
            csv.append("${stat.turnCount},")
            csv.append("${stat.winner},")
            // finalAssetsをJSON形式で埋め込み
            val assetsJson: String = stat.finalAssets.entries.joinToString(";") { "${it.key}:${it.value}" }
            csv.append("\"$assetsJson\",")
            // bankruptcyOrderを;区切りで埋め込み
            val bankruptcyStr: String = stat.bankruptcyOrder.joinToString(";")
            csv.append("\"$bankruptcyStr\"\n")
        }

        File(path).writeText(csv.toString())
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
