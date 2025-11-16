package com.monopoly.statistics

/**
 * プレイヤー別の統計データ
 *
 * @property playerName プレイヤー名
 * @property wins 勝利数
 * @property winRate 勝率（0.0〜1.0）
 * @property averageFinalAssets 平均最終資産
 * @property averageFinalCash 平均最終現金
 * @property averagePropertiesOwned 平均所有プロパティ数
 */
data class PlayerStatistics(
    val playerName: String,
    val wins: Int,
    val winRate: Double,
    val averageFinalAssets: Double,
    val averageFinalCash: Double,
    val averagePropertiesOwned: Double,
)
