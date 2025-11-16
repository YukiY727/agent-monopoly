package com.monopoly.visualization

/**
 * 棒グラフのデータ
 *
 * @property title グラフのタイトル
 * @property bars 棒のリスト
 */
data class BarChartData(
    val title: String,
    val bars: List<Bar>,
) : ChartData {
    /**
     * 棒グラフの1本の棒
     *
     * @property label ラベル（プレイヤー名など）
     * @property value 値（0.0〜1.0の範囲を想定）
     * @property color 色（HEX形式）
     */
    data class Bar(
        val label: String,
        val value: Double,
        val color: String = "#3498db",
    )
}
