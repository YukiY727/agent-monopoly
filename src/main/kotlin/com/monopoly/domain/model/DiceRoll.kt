package com.monopoly.domain.model

/**
 * サイコロを振った結果を表すデータクラス
 *
 * @property die1 サイコロ1の目（1-6）
 * @property die2 サイコロ2の目（1-6）
 */
data class DiceRoll(
    val die1: Int,
    val die2: Int,
) {
    /** サイコロの合計値 */
    val total: Int = die1 + die2

    /** ゾロ目かどうか */
    val isDoubles: Boolean = die1 == die2
}
