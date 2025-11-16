package com.monopoly.visualization

/**
 * レーダーチャートデータ
 *
 * @property title タイトル
 * @property axes 軸のリスト
 * @property series データ系列のリスト
 */
data class RadarChartData(
    val title: String,
    val axes: List<Axis>,
    val series: List<Series>,
) {
    /**
     * 軸
     *
     * @property label ラベル
     * @property maxValue 最大値
     */
    data class Axis(
        val label: String,
        val maxValue: Double = 1.0,
    )

    /**
     * データ系列
     *
     * @property label ラベル（戦略名など）
     * @property values 各軸の値（0.0-1.0に正規化）
     * @property color 色
     */
    data class Series(
        val label: String,
        val values: List<Double>,
        val color: String,
    )
}
