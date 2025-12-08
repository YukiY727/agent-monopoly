package com.monopoly.domain.experiment

import kotlinx.serialization.Serializable

/**
 * 複数ゲームの統計を集計した情報
 *
 * @property totalGames 総ゲーム数
 * @property winRates プレイヤーごとの勝率（プレイヤー名 -> 勝率）
 * @property averageTurnCount 平均ターン数
 * @property averageFinalAssets プレイヤーごとの平均最終資産（プレイヤー名 -> 平均資産）
 * @property totalDuration 実験全体の実行時間（ミリ秒）
 */
@Serializable
data class AggregatedStatistics(
    val totalGames: Int,
    val winRates: Map<String, Double>,
    val averageTurnCount: Double,
    val averageFinalAssets: Map<String, Double>,
    val totalDuration: Long,
) {
    companion object {
        /**
         * ゲーム統計のリストから集計統計を作成する
         *
         * @param statistics ゲーム統計のリスト
         * @return 集計統計
         */
        fun from(statistics: List<GameStatistics>): AggregatedStatistics {
            if (statistics.isEmpty()) {
                return AggregatedStatistics(
                    totalGames = 0,
                    winRates = emptyMap(),
                    averageTurnCount = 0.0,
                    averageFinalAssets = emptyMap(),
                    totalDuration = 0L,
                )
            }

            val totalGames: Int = statistics.size

            // 勝率の計算
            val winCounts: MutableMap<String, Int> = mutableMapOf()
            statistics.forEach { stat ->
                winCounts[stat.winner] = winCounts.getOrDefault(stat.winner, 0) + 1
            }

            // 全プレイヤーの名前を取得
            val allPlayers: Set<String> = statistics.flatMap { it.finalAssets.keys }.toSet()

            val winRates: Map<String, Double> =
                allPlayers.associateWith { playerName ->
                    val wins: Int = winCounts.getOrDefault(playerName, 0)
                    wins.toDouble() / totalGames.toDouble()
                }

            // 平均ターン数の計算
            val averageTurnCount: Double =
                statistics
                    .map { it.turnCount.toDouble() }
                    .average()

            // プレイヤーごとの平均最終資産の計算
            val averageFinalAssets: Map<String, Double> =
                allPlayers.associateWith { playerName ->
                    val assets: List<Int> = statistics.mapNotNull { it.finalAssets[playerName] }
                    if (assets.isEmpty()) 0.0 else assets.average()
                }

            // 実行時間の計算（最初のゲームから最後のゲームまで）
            val firstTimestamp: Long = statistics.minOf { it.timestamp }
            val lastTimestamp: Long = statistics.maxOf { it.timestamp }
            val totalDuration: Long = lastTimestamp - firstTimestamp

            return AggregatedStatistics(
                totalGames = totalGames,
                winRates = winRates,
                averageTurnCount = averageTurnCount,
                averageFinalAssets = averageFinalAssets,
                totalDuration = totalDuration,
            )
        }
    }
}
