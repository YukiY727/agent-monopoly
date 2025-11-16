package com.monopoly.visualization

import kotlin.math.max

/**
 * 折れ線グラフを生成するクラス
 */
class LineChartGenerator {
    private val width = 800
    private val height = 400
    private val padding = 60

    /**
     * 折れ線グラフのSVGを生成
     *
     * @param data 折れ線グラフデータ
     * @return SVG文字列
     */
    fun generate(data: LineChartData): String {
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        return buildString {
            appendLine("<svg width=\"$width\" height=\"$height\" xmlns=\"http://www.w3.org/2000/svg\">")
            appendLine("  <!-- Background -->")
            appendLine("  <rect width=\"$width\" height=\"$height\" fill=\"#ffffff\"/>")

            // タイトル
            appendLine(generateTitle(data.title))

            // 軸
            appendLine(generateAxes())

            // ライン
            data.lines.forEach { line ->
                appendLine(generateLine(line, chartWidth, chartHeight))
            }

            // 凡例
            appendLine(generateLegend(data.lines))

            appendLine("</svg>")
        }
    }

    /**
     * タイトルを生成
     */
    private fun generateTitle(title: String): String {
        return "  <text x=\"${width / 2}\" y=\"30\" text-anchor=\"middle\" font-size=\"18\" font-weight=\"bold\">$title</text>"
    }

    /**
     * 軸を生成
     */
    private fun generateAxes(): String {
        return buildString {
            // Y軸
            appendLine("  <line x1=\"$padding\" y1=\"$padding\" x2=\"$padding\" y2=\"${height - padding}\" stroke=\"#333\" stroke-width=\"2\"/>")
            // X軸
            appendLine("  <line x1=\"$padding\" y1=\"${height - padding}\" x2=\"${width - padding}\" y2=\"${height - padding}\" stroke=\"#333\" stroke-width=\"2\"/>")

            // Y軸のラベル
            appendLine("  <text x=\"20\" y=\"${height / 2}\" text-anchor=\"middle\" font-size=\"12\" transform=\"rotate(-90, 20, ${height / 2})\">Assets</text>")
            // X軸のラベル
            appendLine("  <text x=\"${width / 2}\" y=\"${height - 20}\" text-anchor=\"middle\" font-size=\"12\">Turns</text>")
        }
    }

    /**
     * ラインを生成
     */
    private fun generateLine(
        line: LineChartData.Line,
        chartWidth: Int,
        chartHeight: Int,
    ): String {
        if (line.points.isEmpty()) return ""

        val maxX = line.points.maxOfOrNull { it.first } ?: 1
        val maxY = line.points.maxOfOrNull { it.second } ?: 1.0

        val points = line.points.map { (x, y) ->
            val svgX = padding + (x.toDouble() / maxX * chartWidth).toInt()
            val svgY = height - padding - (y / maxY * chartHeight).toInt()
            "$svgX,$svgY"
        }.joinToString(" ")

        return """  <polyline points="$points" stroke="${line.color}" fill="none" stroke-width="2"/>"""
    }

    /**
     * 凡例を生成
     */
    private fun generateLegend(lines: List<LineChartData.Line>): String {
        return buildString {
            lines.forEachIndexed { index, line ->
                val y = 60 + index * 20
                appendLine("  <rect x=\"${width - 150}\" y=\"$y\" width=\"15\" height=\"15\" fill=\"${line.color}\"/>")
                appendLine("  <text x=\"${width - 130}\" y=\"${y + 12}\" font-size=\"12\">${escapeXml(line.label)}</text>")
            }
        }
    }

    /**
     * XML/SVG用にエスケープ
     */
    private fun escapeXml(text: String): String =
        text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
}
