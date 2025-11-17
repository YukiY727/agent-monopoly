package com.monopoly.statistics

/**
 * 全体の統計データ
 *
 * @property totalGames 総ゲーム数
 * @property playerStats プレイヤー別統計（キー: プレイヤー名）
 * @property turnStats ターン統計
 * @property timestamp タイムスタンプ（ミリ秒）
 */
data class GameStatistics(
    val totalGames: Int,
    val playerStats: Map<String, PlayerStatistics>,
    val turnStats: TurnStatistics,
    val timestamp: Long = System.currentTimeMillis(),
)
