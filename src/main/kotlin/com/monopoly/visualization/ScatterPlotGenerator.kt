package com.monopoly.visualization

/**
 * 散布図SVGを生成
 */
class ScatterPlotGenerator {
    /**
     * 散布図SVGを生成
     */
    fun generate(data: ScatterPlotData): String {
        val width = 700
        val height = 500
        val marginLeft = 80.0
        val marginRight = 50.0
        val marginTop = 50.0
        val marginBottom = 80.0

        val plotWidth = width - marginLeft - marginRight
        val plotHeight = height - marginTop - marginBottom

        val xMin = data.points.minOfOrNull { it.x } ?: 0.0
        val xMax = data.points.maxOfOrNull { it.x } ?: 1.0
        val yMin = data.points.minOfOrNull { it.y } ?: 0.0
        val yMax = data.points.maxOfOrNull { it.y } ?: 1.0

        return buildString {
            appendLine("<svg width=\"$width\" height=\"$height\" xmlns=\"http://www.w3.org/2000/svg\">")

            // 背景
            appendLine("  <rect width=\"$width\" height=\"$height\" fill=\"white\" />")

            // プロット領域の背景
            appendLine("  <rect x=\"$marginLeft\" y=\"$marginTop\" width=\"$plotWidth\" height=\"$plotHeight\" " +
                "fill=\"#f9f9f9\" stroke=\"#333\" stroke-width=\"1\" />")

            // X軸
            appendLine("  <line x1=\"$marginLeft\" y1=\"${marginTop + plotHeight}\" " +
                "x2=\"${marginLeft + plotWidth}\" y2=\"${marginTop + plotHeight}\" " +
                "stroke=\"black\" stroke-width=\"2\" />")

            // Y軸
            appendLine("  <line x1=\"$marginLeft\" y1=\"$marginTop\" " +
                "x2=\"$marginLeft\" y2=\"${marginTop + plotHeight}\" " +
                "stroke=\"black\" stroke-width=\"2\" />")

            // データポイントを描画
            data.points.forEach { point ->
                val x = marginLeft + (point.x - xMin) / (xMax - xMin) * plotWidth
                val y = marginTop + plotHeight - (point.y - yMin) / (yMax - yMin) * plotHeight

                appendLine("  <circle cx=\"$x\" cy=\"$y\" r=\"8\" fill=\"${point.color}\" " +
                    "stroke=\"#333\" stroke-width=\"1\" />")
                appendLine("  <text x=\"${x + 12}\" y=\"${y + 4}\" font-size=\"12\">${point.label}</text>")
            }

            // 軸ラベル
            appendLine("  <text x=\"${marginLeft + plotWidth / 2}\" y=\"${height - 20}\" " +
                "font-size=\"14\" text-anchor=\"middle\">${data.xAxisLabel}</text>")

            appendLine("  <text x=\"20\" y=\"${marginTop + plotHeight / 2}\" font-size=\"14\" " +
                "text-anchor=\"middle\" transform=\"rotate(-90 20 ${marginTop + plotHeight / 2})\">" +
                "${data.yAxisLabel}</text>")

            // タイトル
            appendLine("  <text x=\"${width / 2}\" y=\"30\" font-size=\"18\" font-weight=\"bold\" " +
                "text-anchor=\"middle\">${data.title}</text>")

            // 軸の目盛り
            for (i in 0..5) {
                val xTick = marginLeft + i * plotWidth / 5
                val xValue = xMin + i * (xMax - xMin) / 5
                appendLine("  <text x=\"$xTick\" y=\"${marginTop + plotHeight + 20}\" " +
                    "font-size=\"10\" text-anchor=\"middle\">${String.format("%.2f", xValue)}</text>")

                val yTick = marginTop + plotHeight - i * plotHeight / 5
                val yValue = yMin + i * (yMax - yMin) / 5
                appendLine("  <text x=\"${marginLeft - 10}\" y=\"$yTick\" " +
                    "font-size=\"10\" text-anchor=\"end\">${String.format("%.0f", yValue)}</text>")
            }

            appendLine("</svg>")
        }
    }
}
