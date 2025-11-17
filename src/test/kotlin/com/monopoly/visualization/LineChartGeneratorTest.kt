package com.monopoly.visualization

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith

class LineChartGeneratorTest : DescribeSpec({
    describe("LineChartGenerator") {
        val generator = LineChartGenerator()

        describe("generate") {
            context("基本的なSVG生成") {
                it("SVGタグを含む文字列を生成する") {
                    // Arrange
                    val data = LineChartData(
                        title = "Test Chart",
                        lines = listOf(
                            LineChartData.Line(
                                label = "Line 1",
                                points = listOf(0 to 0.0, 10 to 100.0),
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
                    val data = LineChartData(
                        title = "My Chart Title",
                        lines = emptyList()
                    )

                    // Act
                    val svg = generator.generate(data)

                    // Assert
                    svg shouldContain "My Chart Title"
                }
            }

            context("軸の描画") {
                it("X軸とY軸のlineタグを含む") {
                    // Arrange
                    val data = LineChartData(
                        title = "Test",
                        lines = emptyList()
                    )

                    // Act
                    val svg = generator.generate(data)

                    // Assert
                    svg shouldContain "<line"  // 軸はlineタグで描画される
                    svg shouldContain "Assets"  // Y軸ラベル
                    svg shouldContain "Turns"  // X軸ラベル
                }
            }

            context("ラインの描画") {
                it("polylineタグを含む") {
                    // Arrange
                    val data = LineChartData(
                        title = "Test",
                        lines = listOf(
                            LineChartData.Line(
                                label = "Alice",
                                points = listOf(0 to 0.0, 5 to 50.0, 10 to 100.0),
                                color = "#0000ff"
                            )
                        )
                    )

                    // Act
                    val svg = generator.generate(data)

                    // Assert
                    svg shouldContain "<polyline"
                    svg shouldContain "stroke=\"#0000ff\""  // 指定した色
                }
            }

            context("複数ラインの描画") {
                it("各ラインのpolylineタグを含む") {
                    // Arrange
                    val data = LineChartData(
                        title = "Multi Line",
                        lines = listOf(
                            LineChartData.Line(
                                label = "Alice",
                                points = listOf(0 to 0.0, 10 to 100.0),
                                color = "#ff0000"
                            ),
                            LineChartData.Line(
                                label = "Bob",
                                points = listOf(0 to 0.0, 10 to 80.0),
                                color = "#00ff00"
                            )
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
                it("各ラインのラベルを含む") {
                    // Arrange
                    val data = LineChartData(
                        title = "Test",
                        lines = listOf(
                            LineChartData.Line(
                                label = "Player A",
                                points = listOf(0 to 0.0, 10 to 100.0),
                                color = "#ff0000"
                            ),
                            LineChartData.Line(
                                label = "Player B",
                                points = listOf(0 to 0.0, 10 to 80.0),
                                color = "#00ff00"
                            )
                        )
                    )

                    // Act
                    val svg = generator.generate(data)

                    // Assert
                    svg shouldContain "Player A"
                    svg shouldContain "Player B"
                }
            }

            context("空のポイント") {
                it("空のラインでもエラーにならない") {
                    // Arrange
                    val data = LineChartData(
                        title = "Empty",
                        lines = listOf(
                            LineChartData.Line(
                                label = "Empty Line",
                                points = emptyList(),
                                color = "#000000"
                            )
                        )
                    )

                    // Act & Assert（例外が投げられないことを確認）
                    val svg = generator.generate(data)
                    svg shouldContain "<svg"
                }
            }

            context("XMLエスケープ") {
                it("特殊文字をエスケープする") {
                    // Arrange
                    val data = LineChartData(
                        title = "Test",
                        lines = listOf(
                            LineChartData.Line(
                                label = "A & B < C",  // 特殊文字を含む
                                points = listOf(0 to 0.0),
                                color = "#000000"
                            )
                        )
                    )

                    // Act
                    val svg = generator.generate(data)

                    // Assert
                    svg shouldContain "&amp;"  // & がエスケープされる
                    svg shouldContain "&lt;"   // < がエスケープされる
                }
            }
        }
    }
})
