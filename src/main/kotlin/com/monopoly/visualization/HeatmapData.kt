package com.monopoly.visualization

/**
 * ヒートマップデータ
 *
 * @property title タイトル
 * @property cells セルのリスト
 * @property minValue 最小値
 * @property maxValue 最大値
 */
data class HeatmapData(
    val title: String,
    val cells: List<Cell>,
    val minValue: Double = cells.minOfOrNull { it.value } ?: 0.0,
    val maxValue: Double = cells.maxOfOrNull { it.value } ?: 1.0,
) {
    /**
     * ヒートマップのセル
     *
     * @property label ラベル
     * @property value 値
     * @property position ボード上の位置（0-39）
     */
    data class Cell(
        val label: String,
        val value: Double,
        val position: Int,
    )
}
