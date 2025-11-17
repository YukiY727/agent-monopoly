package com.monopoly.visualization

/**
 * 散布図データ
 *
 * @property title タイトル
 * @property xAxisLabel X軸ラベル
 * @property yAxisLabel Y軸ラベル
 * @property points データポイントのリスト
 */
data class ScatterPlotData(
    val title: String,
    val xAxisLabel: String,
    val yAxisLabel: String,
    val points: List<Point>,
) {
    /**
     * データポイント
     *
     * @property label ラベル
     * @property x X座標の値
     * @property y Y座標の値
     * @property color 色
     */
    data class Point(
        val label: String,
        val x: Double,
        val y: Double,
        val color: String,
    )
}
