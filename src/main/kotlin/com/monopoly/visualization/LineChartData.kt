package com.monopoly.visualization

/**
 * 折れ線グラフのデータ
 *
 * @property title グラフのタイトル
 * @property lines ラインのリスト
 */
data class LineChartData(
    val title: String,
    val lines: List<Line>,
) : ChartData {
    /**
     * 折れ線グラフの1本のライン
     *
     * @property label ラベル（プレイヤー名など）
     * @property points データポイント（x, y）のリスト
     * @property color 色（HEX形式）
     */
    data class Line(
        val label: String,
        val points: List<Pair<Int, Double>>,
        val color: String,
    )
}
