package com.monopoly.statistics

import com.monopoly.domain.model.BoardFixtures
import com.monopoly.domain.model.ColorGroup
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Player
import com.monopoly.domain.model.Property
import com.monopoly.domain.strategy.AlwaysBuyStrategy
import com.monopoly.simulation.MultiGameResult
import com.monopoly.simulation.SingleGameResult
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.shouldBe

class StatisticsCalculatorTest : DescribeSpec({
    describe("StatisticsCalculator") {
        val calculator = StatisticsCalculator()

        describe("calculate") {
            context("2人プレイヤーで2ゲームの場合") {
                it("正しく統計を計算する") {
                    // Arrange
                    val board = BoardFixtures.createStandardBoard()

                    // Game 1: Alice wins with 2000, 5 properties
                    val alice1 = Player("Alice", AlwaysBuyStrategy())
                    alice1.addMoney(500) // 1500 + 500 = 2000
                    repeat(5) { alice1.addProperty(createDummyProperty(it)) }

                    val bob1 = Player("Bob", AlwaysBuyStrategy())
                    bob1.subtractMoney(1500) // 1500 - 1500 = 0

                    val game1 = SingleGameResult(
                        gameNumber = 1,
                        winner = "Alice",
                        totalTurns = 100,
                        finalState = GameState(listOf(alice1, bob1), board)
                    )

                    // Game 2: Bob wins with 2500, 6 properties
                    val alice2 = Player("Alice", AlwaysBuyStrategy())
                    alice2.subtractMoney(1500) // 1500 - 1500 = 0

                    val bob2 = Player("Bob", AlwaysBuyStrategy())
                    bob2.addMoney(1000) // 1500 + 1000 = 2500
                    repeat(6) { bob2.addProperty(createDummyProperty(it + 10)) }

                    val game2 = SingleGameResult(
                        gameNumber = 2,
                        winner = "Bob",
                        totalTurns = 150,
                        finalState = GameState(listOf(alice2, bob2), board)
                    )

                    val result = MultiGameResult(
                        gameResults = listOf(game1, game2),
                        totalGames = 2
                    )

                    // Act
                    val stats = calculator.calculate(result)

                    // Assert - 全体統計
                    stats.totalGames shouldBe 2

                    // Assert - Alice の統計
                    val aliceStats = stats.playerStats["Alice"]!!
                    aliceStats.playerName shouldBe "Alice"
                    aliceStats.wins shouldBe 1
                    aliceStats.winRate shouldBe 0.5
                    aliceStats.averageFinalCash shouldBe 1000.0 // (2000 + 0) / 2

                    // Assert - Bob の統計
                    val bobStats = stats.playerStats["Bob"]!!
                    bobStats.playerName shouldBe "Bob"
                    bobStats.wins shouldBe 1
                    bobStats.winRate shouldBe 0.5
                    bobStats.averageFinalCash shouldBe 1250.0 // (0 + 2500) / 2
                    bobStats.averagePropertiesOwned shouldBe 3.0 // (0 + 6) / 2

                    // Assert - ターン統計
                    stats.turnStats.averageTurns shouldBe 125.0 // (100 + 150) / 2
                    stats.turnStats.minTurns shouldBe 100
                    stats.turnStats.maxTurns shouldBe 150
                    stats.turnStats.standardDeviation shouldBeGreaterThan 0.0
                }
            }

            context("全勝したプレイヤーがいる場合") {
                it("勝率が1.0になる") {
                    // Arrange
                    val board = BoardFixtures.createStandardBoard()

                    val games = (1..3).map { gameNum ->
                        val alice = Player("Alice", AlwaysBuyStrategy())
                        alice.addMoney(500 + gameNum * 100)
                        repeat(5) { alice.addProperty(createDummyProperty(it + gameNum * 10)) }

                        val bob = Player("Bob", AlwaysBuyStrategy())
                        bob.subtractMoney(1500)

                        SingleGameResult(
                            gameNumber = gameNum,
                            winner = "Alice",
                            totalTurns = 100 + gameNum * 10,
                            finalState = GameState(listOf(alice, bob), board)
                        )
                    }

                    val result = MultiGameResult(
                        gameResults = games,
                        totalGames = 3
                    )

                    // Act
                    val stats = calculator.calculate(result)

                    // Assert
                    val aliceStats = stats.playerStats["Alice"]!!
                    aliceStats.wins shouldBe 3
                    aliceStats.winRate shouldBe 1.0

                    val bobStats = stats.playerStats["Bob"]!!
                    bobStats.wins shouldBe 0
                    bobStats.winRate shouldBe 0.0
                }
            }

            context("1ゲームのみの場合") {
                it("正しく統計を計算する") {
                    // Arrange
                    val board = BoardFixtures.createStandardBoard()

                    val alice = Player("Alice", AlwaysBuyStrategy())
                    alice.addMoney(500)
                    repeat(5) { alice.addProperty(createDummyProperty(it)) }

                    val bob = Player("Bob", AlwaysBuyStrategy())
                    bob.subtractMoney(1500)

                    val game = SingleGameResult(
                        gameNumber = 1,
                        winner = "Alice",
                        totalTurns = 100,
                        finalState = GameState(listOf(alice, bob), board)
                    )

                    val result = MultiGameResult(
                        gameResults = listOf(game),
                        totalGames = 1
                    )

                    // Act
                    val stats = calculator.calculate(result)

                    // Assert
                    stats.totalGames shouldBe 1

                    val aliceStats = stats.playerStats["Alice"]!!
                    aliceStats.wins shouldBe 1
                    aliceStats.winRate shouldBe 1.0

                    val bobStats = stats.playerStats["Bob"]!!
                    bobStats.wins shouldBe 0
                    bobStats.winRate shouldBe 0.0

                    stats.turnStats.averageTurns shouldBe 100.0
                    stats.turnStats.minTurns shouldBe 100
                    stats.turnStats.maxTurns shouldBe 100
                    stats.turnStats.standardDeviation shouldBe 0.0
                }
            }

            context("3人以上のプレイヤーの場合") {
                it("正しく統計を計算する") {
                    // Arrange
                    val board = BoardFixtures.createStandardBoard()

                    // Game 1: Alice wins
                    val alice1 = Player("Alice", AlwaysBuyStrategy())
                    alice1.addMoney(500)
                    repeat(5) { alice1.addProperty(createDummyProperty(it)) }
                    val bob1 = Player("Bob", AlwaysBuyStrategy())
                    bob1.subtractMoney(1000)
                    repeat(2) { bob1.addProperty(createDummyProperty(it + 10)) }
                    val charlie1 = Player("Charlie", AlwaysBuyStrategy())
                    charlie1.subtractMoney(1500)

                    val game1 = SingleGameResult(
                        gameNumber = 1,
                        winner = "Alice",
                        totalTurns = 100,
                        finalState = GameState(listOf(alice1, bob1, charlie1), board)
                    )

                    // Game 2: Bob wins
                    val alice2 = Player("Alice", AlwaysBuyStrategy())
                    alice2.subtractMoney(1000)
                    repeat(2) { alice2.addProperty(createDummyProperty(it + 20)) }
                    val bob2 = Player("Bob", AlwaysBuyStrategy())
                    bob2.addMoney(700)
                    repeat(6) { bob2.addProperty(createDummyProperty(it + 30)) }
                    val charlie2 = Player("Charlie", AlwaysBuyStrategy())
                    charlie2.subtractMoney(1500)

                    val game2 = SingleGameResult(
                        gameNumber = 2,
                        winner = "Bob",
                        totalTurns = 120,
                        finalState = GameState(listOf(alice2, bob2, charlie2), board)
                    )

                    // Game 3: Charlie wins
                    val alice3 = Player("Alice", AlwaysBuyStrategy())
                    alice3.subtractMoney(1500)
                    val bob3 = Player("Bob", AlwaysBuyStrategy())
                    bob3.subtractMoney(1000)
                    repeat(2) { bob3.addProperty(createDummyProperty(it + 40)) }
                    val charlie3 = Player("Charlie", AlwaysBuyStrategy())
                    charlie3.addMoney(800)
                    repeat(7) { charlie3.addProperty(createDummyProperty(it + 50)) }

                    val game3 = SingleGameResult(
                        gameNumber = 3,
                        winner = "Charlie",
                        totalTurns = 130,
                        finalState = GameState(listOf(alice3, bob3, charlie3), board)
                    )

                    val games = listOf(game1, game2, game3)
                    val result = MultiGameResult(
                        gameResults = games,
                        totalGames = 3
                    )

                    // Act
                    val stats = calculator.calculate(result)

                    // Assert
                    stats.totalGames shouldBe 3
                    stats.playerStats.size shouldBe 3

                    // 各プレイヤーの勝率
                    stats.playerStats["Alice"]!!.winRate shouldBe (1.0 / 3.0)
                    stats.playerStats["Bob"]!!.winRate shouldBe (1.0 / 3.0)
                    stats.playerStats["Charlie"]!!.winRate shouldBe (1.0 / 3.0)
                }
            }

            context("プロパティ所有数が正しく平均される") {
                it("平均プロパティ数を正しく計算する") {
                    // Arrange
                    val board = BoardFixtures.createStandardBoard()

                    // Game 1: Alice 10 properties, Bob 0
                    val alice1 = Player("Alice", AlwaysBuyStrategy())
                    alice1.addMoney(500)
                    repeat(10) { alice1.addProperty(createDummyProperty(it)) }
                    val bob1 = Player("Bob", AlwaysBuyStrategy())
                    bob1.subtractMoney(1500)

                    val game1 = SingleGameResult(
                        gameNumber = 1,
                        winner = "Alice",
                        totalTurns = 100,
                        finalState = GameState(listOf(alice1, bob1), board)
                    )

                    // Game 2: Alice 8 properties, Bob 2
                    val alice2 = Player("Alice", AlwaysBuyStrategy())
                    alice2.addMoney(500)
                    repeat(8) { alice2.addProperty(createDummyProperty(it + 10)) }
                    val bob2 = Player("Bob", AlwaysBuyStrategy())
                    bob2.subtractMoney(1500)
                    repeat(2) { bob2.addProperty(createDummyProperty(it + 20)) }

                    val game2 = SingleGameResult(
                        gameNumber = 2,
                        winner = "Alice",
                        totalTurns = 100,
                        finalState = GameState(listOf(alice2, bob2), board)
                    )

                    val result = MultiGameResult(
                        gameResults = listOf(game1, game2),
                        totalGames = 2
                    )

                    // Act
                    val stats = calculator.calculate(result)

                    // Assert
                    stats.playerStats["Alice"]!!.averagePropertiesOwned shouldBe 9.0 // (10 + 8) / 2
                    stats.playerStats["Bob"]!!.averagePropertiesOwned shouldBe 1.0 // (0 + 2) / 2
                }
            }
        }
    }
})

/**
 * ダミーのPropertyを生成
 */
private fun createDummyProperty(index: Int): Property {
    return Property(
        name = "Property $index",
        position = index % 40,
        price = 100,
        rent = 10,
        colorGroup = ColorGroup.BROWN
    )
}
