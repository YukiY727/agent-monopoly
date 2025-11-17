package com.monopoly.visualization

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith

class RadarChartGeneratorTest : DescribeSpec({
    describe("RadarChartGenerator") {
        val generator = RadarChartGenerator()

        describe("generate") {
            context("基本的なSVG生成") {
                it("SVGタグを含む文字列を生成する") {
                    // Arrange
                    val data = RadarChartData(
                        title = "Test Radar",
                        axes = listOf(
                            RadarChartData.Axis("Win Rate", 1.0),
                            RadarChartData.Axis("Assets", 1.0),
                            RadarChartData.Axis("Properties", 1.0)
                        ),
                        series = listOf(
                            RadarChartData.Series(
                                label = "Strategy A",
                                values = listOf(0.8, 0.6, 0.7),
                                color = "#ff0000"
                            )
                        )
                    )

                    // Act
                    val svg = generator.generate(data)

                    // Assert
                    svg shouldStartWith "<svg"
                    svg shouldContain "</svg>"
                }
            }

            context("タイトルの描画") {
                it("指定したタイトルを含む") {
                    // Arrange
                    val data = RadarChartData(
                        title = "Strategy Comparison",
                        axes = emptyList(),
                        series = emptyList()
                    )

                    // Act
                    val svg = generator.generate(data)

                    // Assert
                    svg shouldContain "Strategy Comparison"
                }
            }

            context("軸の描画") {
                it("軸ラベルを含む") {
                    // Arrange
                    val data = RadarChartData(
                        title = "Test",
                        axes = listOf(
                            RadarChartData.Axis("Win Rate", 1.0),
                            RadarChartData.Axis("Average Assets", 1.0),
                            RadarChartData.Axis("Properties Owned", 1.0)
                        ),
                        series = emptyList()
                    )

                    // Act
                    val svg = generator.generate(data)

                    // Assert
                    svg shouldContain "Win Rate"
                    svg shouldContain "Average Assets"
                    svg shouldContain "Properties Owned"
                }
            }

            context("系列の描画") {
                it("polygonタグを含む") {
                    // Arrange
                    val data = RadarChartData(
                        title = "Test",
                        axes = listOf(
                            RadarChartData.Axis("A", 1.0),
                            RadarChartData.Axis("B", 1.0),
                            RadarChartData.Axis("C", 1.0)
                        ),
                        series = listOf(
                            RadarChartData.Series(
                                label = "Strategy A",
                                values = listOf(0.8, 0.6, 0.7),
                                color = "#ff0000"
                            )
                        )
                    )

                    // Act
                    val svg = generator.generate(data)

                    // Assert
                    svg shouldContain "<polygon"
                    svg shouldContain "stroke=\"#ff0000\""
                }
            }

            context("複数系列の描画") {
                it("各系列のpolygonタグを含む") {
                    // Arrange
                    val data = RadarChartData(
                        title = "Multi Series",
                        axes = listOf(
                            RadarChartData.Axis("A", 1.0),
                            RadarChartData.Axis("B", 1.0),
                            RadarChartData.Axis("C", 1.0)
                        ),
                        series = listOf(
                            RadarChartData.Series("Strategy A", listOf(0.8, 0.6, 0.7), "#ff0000"),
                            RadarChartData.Series("Strategy B", listOf(0.5, 0.9, 0.6), "#00ff00")
                        )
                    )

                    // Act
                    val svg = generator.generate(data)

                    // Assert
                    svg shouldContain "stroke=\"#ff0000\""
                    svg shouldContain "stroke=\"#00ff00\""
                }
            }

            context("凡例の描画") {
                it("系列のラベルを含む") {
                    // Arrange
                    val data = RadarChartData(
                        title = "Test",
                        axes = listOf(RadarChartData.Axis("A", 1.0)),
                        series = listOf(
                            RadarChartData.Series("Aggressive", listOf(0.8), "#ff0000"),
                            RadarChartData.Series("Conservative", listOf(0.5), "#00ff00")
                        )
                    )

                    // Act
                    val svg = generator.generate(data)

                    // Assert
                    svg shouldContain "Aggressive"
                    svg shouldContain "Conservative"
                }
            }

            context("空の系列") {
                it("エラーにならずSVGを生成する") {
                    // Arrange
                    val data = RadarChartData(
                        title = "Empty",
                        axes = listOf(RadarChartData.Axis("A", 1.0)),
                        series = emptyList()
                    )

                    // Act & Assert
                    val svg = generator.generate(data)
                    svg shouldContain "<svg"
                }
            }
        }
    }
})
