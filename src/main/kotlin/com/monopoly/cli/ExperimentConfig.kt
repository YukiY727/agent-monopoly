package com.monopoly.cli

import com.monopoly.domain.model.player.PlayerStrategy

/**
 * 実験の設定を保持するデータクラス
 *
 * @property gameCount 実行するゲーム数
 * @property strategies 各プレイヤーの戦略リスト（リストの長さ = プレイヤー数）
 */
data class ExperimentConfig(
    val gameCount: Int,
    val strategies: List<PlayerStrategy>,
) {
    init {
        require(gameCount > 0) { "Game count must be positive" }
        require(strategies.size >= 2) { "At least 2 players are required" }
        require(strategies.size <= 8) { "Maximum 8 players are supported" }
    }

    val playerCount: Int
        get() = strategies.size
}
