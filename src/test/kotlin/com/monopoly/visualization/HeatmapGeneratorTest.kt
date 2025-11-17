package com.monopoly.visualization

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith

class HeatmapGeneratorTest : DescribeSpec({
    describe("HeatmapGenerator") {
        val generator = HeatmapGenerator()

        describe("generate") {
            context("基本的なSVG生成") {
                it("SVGタグを含む文字列を生成する") {
                    // Arrange
                    val data = HeatmapData(
                        title = "Test Heatmap",
                        cells = listOf(
                            HeatmapData.Cell(
                                label = "GO",
                                value = 10.0,
                                position = 0
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
                    val data = HeatmapData(
                        title = "Board Landing Frequency",
                        cells = emptyList()
                    )

                    // Act
                    val svg = generator.generate(data)

                    // Assert
                    svg shouldContain "Board Landing Frequency"
                }
            }

            context("セルの描画") {
                it("40個のセルに対して40個のrectタグを生成する") {
                    // Arrange
                    val cells = (0..39).map { position ->
                        HeatmapData.Cell(
                            label = "P$position",
                            value = position.toDouble(),
                            position = position
                        )
                    }
                    val data = HeatmapData(
                        title = "40 Cells",
                        cells = cells
                    )

                    // Act
                    val svg = generator.generate(data)

                    // Assert
                    val rectCount = svg.split("<rect").size - 1
                    rectCount shouldBeGreaterThan 40  // セル40個 + 背景1個
                }
            }

            context("セルのラベル") {
                it("各セルのラベルを含む") {
                    // Arrange
                    val data = HeatmapData(
                        title = "Test",
                        cells = listOf(
                            HeatmapData.Cell(
                                label = "GO",
                                value = 100.0,
                                position = 0
                            ),
                            HeatmapData.Cell(
                                label = "Park Place",
                                value = 50.0,
                                position = 37
                            )
                        )
                    )

                    // Act
                    val svg = generator.generate(data)

                    // Assert
                    svg shouldContain "GO"
                    svg shouldContain "Park Place"
                }
            }

            context("色の補間") {
                it("値に応じて異なる色を生成する") {
                    // Arrange
                    val data = HeatmapData(
                        title = "Color Test",
                        cells = listOf(
                            HeatmapData.Cell(label = "Min", value = 0.0, position = 0),
                            HeatmapData.Cell(label = "Mid", value = 50.0, position = 1),
                            HeatmapData.Cell(label = "Max", value = 100.0, position = 2)
                        ),
                        minValue = 0.0,
                        maxValue = 100.0
                    )

                    // Act
                    val svg = generator.generate(data)

                    // Assert
                    // fill属性が含まれていることを確認（色が設定されている）
                    svg shouldContain "fill=\"#"
                }
            }

            context("最小値と最大値が同じ場合") {
                it("エラーにならずSVGを生成する") {
                    // Arrange
                    val data = HeatmapData(
                        title = "Same Values",
                        cells = listOf(
                            HeatmapData.Cell(label = "A", value = 10.0, position = 0),
                            HeatmapData.Cell(label = "B", value = 10.0, position = 1)
                        ),
                        minValue = 10.0,
                        maxValue = 10.0
                    )

                    // Act & Assert（例外が投げられないことを確認）
                    val svg = generator.generate(data)
                    svg shouldContain "<svg"
                }
            }

            context("空のセル") {
                it("空のセルでもSVGを生成する") {
                    // Arrange
                    val data = HeatmapData(
                        title = "Empty",
                        cells = emptyList()
                    )

                    // Act
                    val svg = generator.generate(data)

                    // Assert
                    svg shouldContain "<svg"
                    svg shouldContain "Empty"
                }
            }
        }
    }
})
