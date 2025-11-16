package com.monopoly.visualization

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

/**
 * レーダーチャートSVGを生成
 */
class RadarChartGenerator {
    /**
     * レーダーチャートSVGを生成
     */
    fun generate(data: RadarChartData): String {
        val width = 600
        val height = 600
        val centerX = width / 2.0
        val centerY = height / 2.0
        val radius = 200.0
        val numAxes = data.axes.size

        return buildString {
            appendLine("<svg width=\"$width\" height=\"$height\" xmlns=\"http://www.w3.org/2000/svg\">")

            // 背景
            appendLine("  <rect width=\"$width\" height=\"$height\" fill=\"white\" />")

            // 同心円を描画（ガイドライン）
            for (i in 1..5) {
                val r = radius * i / 5
                appendLine("  <circle cx=\"$centerX\" cy=\"$centerY\" r=\"$r\" " +
                    "fill=\"none\" stroke=\"#ddd\" stroke-width=\"1\" />")
            }

            // 軸を描画
            data.axes.forEachIndexed { index, axis ->
                val angle = 2 * PI * index / numAxes - PI / 2
                val x = centerX + radius * cos(angle)
                val y = centerY + radius * sin(angle)

                appendLine("  <line x1=\"$centerX\" y1=\"$centerY\" x2=\"$x\" y2=\"$y\" " +
                    "stroke=\"#999\" stroke-width=\"1\" />")

                // 軸ラベル
                val labelX = centerX + (radius + 20) * cos(angle)
                val labelY = centerY + (radius + 20) * sin(angle)
                appendLine("  <text x=\"$labelX\" y=\"$labelY\" font-size=\"12\" " +
                    "text-anchor=\"middle\" dominant-baseline=\"middle\">${axis.label}</text>")
            }

            // 各戦略のポリゴンを描画
            data.series.forEach { series ->
                val points = series.values.mapIndexed { index, value ->
                    val angle = 2 * PI * index / numAxes - PI / 2
                    val r = radius * value.coerceIn(0.0, 1.0)
                    val x = centerX + r * cos(angle)
                    val y = centerY + r * sin(angle)
                    "$x,$y"
                }.joinToString(" ")

                appendLine("  <polygon points=\"$points\" fill=\"${series.color}\" " +
                    "opacity=\"0.3\" stroke=\"${series.color}\" stroke-width=\"2\" />")
            }

            // 凡例
            data.series.forEachIndexed { index, series ->
                val legendY = 30 + index * 25
                appendLine("  <rect x=\"20\" y=\"${legendY - 10}\" width=\"15\" height=\"15\" " +
                    "fill=\"${series.color}\" />")
                appendLine("  <text x=\"40\" y=\"$legendY\" font-size=\"14\">${series.label}</text>")
            }

            // タイトル
            appendLine("  <text x=\"$centerX\" y=\"30\" font-size=\"18\" font-weight=\"bold\" " +
                "text-anchor=\"middle\">${data.title}</text>")

            appendLine("</svg>")
        }
    }
}
