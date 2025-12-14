package com.monopoly.domain.experiment

import kotlinx.serialization.Serializable

/**
 * 1ゲームの統計情報
 *
 * @property gameId ゲームID（例: "game_001"）
 * @property timestamp ゲーム開始時刻（Unixタイムスタンプ）
 * @property turnCount ターン数
 * @property winner 勝者の名前
 * @property finalAssets プレイヤー名 -> 最終資産のマップ
 * @property bankruptcyOrder 破産した順番のリスト（先頭が最初に破産したプレイヤー）
 * @property playerStatistics プレイヤーごとの詳細な行動統計
 */
@Serializable
data class GameStatistics(
    val gameId: String,
    val timestamp: Long,
    val turnCount: Int,
    val winner: String,
    val finalAssets: Map<String, Int>,
    val bankruptcyOrder: List<String>,
    val playerStatistics: List<PlayerStatistics> = emptyList(),
)
