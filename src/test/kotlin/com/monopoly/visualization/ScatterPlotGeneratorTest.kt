package com.monopoly.visualization

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith

class ScatterPlotGeneratorTest : DescribeSpec({
    describe("ScatterPlotGenerator") {
        val generator = ScatterPlotGenerator()

        describe("generate") {
            context("基本的なSVG生成") {
                it("SVGタグを含む文字列を生成する") {
                    // Arrange
                    val data = ScatterPlotData(
                        title = "Test Scatter",
                        xAxisLabel = "X Axis",
                        yAxisLabel = "Y Axis",
                        points = listOf(
                            ScatterPlotData.Point(label = "A", x = 1.0, y = 2.0, color = "#ff0000")
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
                    val data = ScatterPlotData(
                        title = "Win Rate vs Assets",
                        xAxisLabel = "Win Rate",
                        yAxisLabel = "Assets",
                        points = emptyList()
                    )

                    // Act
                    val svg = generator.generate(data)

                    // Assert
                    svg shouldContain "Win Rate vs Assets"
                }
            }

            context("軸ラベルの描画") {
                it("X軸とY軸のラベルを含む") {
                    // Arrange
                    val data = ScatterPlotData(
                        title = "Test",
                        xAxisLabel = "X Label",
                        yAxisLabel = "Y Label",
                        points = emptyList()
                    )

                    // Act
                    val svg = generator.generate(data)

                    // Assert
                    svg shouldContain "X Label"
                    svg shouldContain "Y Label"
                }
            }

            context("ポイントの描画") {
                it("circleタグを含む") {
                    // Arrange
                    val data = ScatterPlotData(
                        title = "Test",
                        xAxisLabel = "X",
                        yAxisLabel = "Y",
                        points = listOf(
                            ScatterPlotData.Point(label = "A", x = 0.5, y = 0.8, color = "#ff0000"),
                            ScatterPlotData.Point(label = "B", x = 0.7, y = 0.6, color = "#00ff00")
                        )
                    )

                    // Act
                    val svg = generator.generate(data)

                    // Assert
                    svg shouldContain "<circle"
                    svg shouldContain "fill=\"#ff0000\""
                    svg shouldContain "fill=\"#00ff00\""
                }
            }

            context("ポイントのラベル") {
                it("各ポイントのラベルを含む") {
                    // Arrange
                    val data = ScatterPlotData(
                        title = "Test",
                        xAxisLabel = "X",
                        yAxisLabel = "Y",
                        points = listOf(
                            ScatterPlotData.Point(label = "Strategy A", x = 0.5, y = 0.8, color = "#ff0000"),
                            ScatterPlotData.Point(label = "Strategy B", x = 0.7, y = 0.6, color = "#00ff00")
                        )
                    )

                    // Act
                    val svg = generator.generate(data)

                    // Assert
                    svg shouldContain "Strategy A"
                    svg shouldContain "Strategy B"
                }
            }

            context("軸の描画") {
                it("軸のlineタグを含む") {
                    // Arrange
                    val data = ScatterPlotData(
                        title = "Test",
                        xAxisLabel = "X",
                        yAxisLabel = "Y",
                        points = emptyList()
                    )

                    // Act
                    val svg = generator.generate(data)

                    // Assert
                    svg shouldContain "<line"  // 軸はlineタグで描画
                }
            }

            context("空のポイント") {
                it("エラーにならずSVGを生成する") {
                    // Arrange
                    val data = ScatterPlotData(
                        title = "Empty",
                        xAxisLabel = "X",
                        yAxisLabel = "Y",
                        points = emptyList()
                    )

                    // Act & Assert
                    val svg = generator.generate(data)
                    svg shouldContain "<svg"
                }
            }

            context("多数のポイント") {
                it("10個のポイントを描画する") {
                    // Arrange
                    val points = (1..10).map { i ->
                        ScatterPlotData.Point(
                            label = "Point $i",
                            x = i * 0.1,
                            y = i * 0.1,
                            color = "#000000"
                        )
                    }
                    val data = ScatterPlotData(
                        title = "Many Points",
                        xAxisLabel = "X",
                        yAxisLabel = "Y",
                        points = points
                    )

                    // Act
                    val svg = generator.generate(data)

                    // Assert
                    // 10個のcircleが含まれる（おおよそ）
                    val circleCount = svg.split("<circle").size - 1
                    circleCount shouldBe 10
                }
            }
        }
    }
})
