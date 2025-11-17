package com.monopoly.statistics

import kotlin.math.sqrt

/**
 * ターン数に関する統計データ
 *
 * @property averageTurns 平均ターン数
 * @property minTurns 最短ターン数
 * @property maxTurns 最長ターン数
 * @property standardDeviation 標準偏差
 */
data class TurnStatistics(
    val averageTurns: Double,
    val minTurns: Int,
    val maxTurns: Int,
    val standardDeviation: Double,
) {
    companion object {
        /**
         * ターン数のリストから標準偏差を計算
         *
         * σ = sqrt(Σ(x - μ)² / N)
         *
         * @param values ターン数のリスト
         * @param average 平均ターン数
         * @return 標準偏差
         */
        fun calculateStandardDeviation(values: List<Int>, average: Double): Double {
            if (values.isEmpty()) return 0.0

            val variance = values
                .map { (it - average) * (it - average) }
                .average()

            return sqrt(variance)
        }
    }
}
