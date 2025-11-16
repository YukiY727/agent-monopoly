package com.monopoly.simulation

import com.monopoly.domain.model.GameState

/**
 * 1ゲームの実行結果
 *
 * @property gameNumber ゲーム番号（1から開始）
 * @property winner 勝者名
 * @property totalTurns 総ターン数
 * @property finalState 最終ゲーム状態
 */
data class SingleGameResult(
    val gameNumber: Int,
    val winner: String,
    val totalTurns: Int,
    val finalState: GameState,
)
