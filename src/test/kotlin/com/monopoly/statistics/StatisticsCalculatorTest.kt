package com.monopoly.statistics

import com.monopoly.domain.model.BoardPosition
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Money
import com.monopoly.domain.model.Player
import com.monopoly.domain.model.PlayerState
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
                    val alice = "Alice"
                    val bob = "Bob"

                    val game1 = createSingleGameResult(
                        gameNumber = 1,
                        winner = alice,
                        totalTurns = 100,
                        players = listOf(
                            createPlayer(alice, money = 2000, properties = 5),
                            createPlayer(bob, money = 0, properties = 0),
                        ),
                    )

                    val game2 = createSingleGameResult(
                        gameNumber = 2,
                        winner = bob,
                        totalTurns = 150,
                        players = listOf(
                            createPlayer(alice, money = 0, properties = 0),
                            createPlayer(bob, money = 2500, properties = 6),
                        ),
                    )

                    val result = MultiGameResult(
                        gameResults = listOf(game1, game2),
                        totalGames = 2,
                    )

                    // Act
                    val stats = calculator.calculate(result)

                    // Assert - 全体統計
                    stats.totalGames shouldBe 2

                    // Assert - Alice の統計
                    val aliceStats = stats.playerStats[alice]!!
                    aliceStats.playerName shouldBe alice
                    aliceStats.wins shouldBe 1
                    aliceStats.winRate shouldBe 0.5
                    aliceStats.averageFinalAssets shouldBe 1000.0 // (2000 + 0) / 2
                    aliceStats.averageFinalCash shouldBe 1000.0 // (2000 + 0) / 2
                    aliceStats.averagePropertiesOwned shouldBe 2.5 // (5 + 0) / 2

                    // Assert - Bob の統計
                    val bobStats = stats.playerStats[bob]!!
                    bobStats.playerName shouldBe bob
                    bobStats.wins shouldBe 1
                    bobStats.winRate shouldBe 0.5
                    bobStats.averageFinalAssets shouldBe 1250.0 // (0 + 2500) / 2
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
                    val alice = "Alice"
                    val bob = "Bob"

                    val games = listOf(
                        createSingleGameResult(
                            gameNumber = 1,
                            winner = alice,
                            totalTurns = 100,
                            players = listOf(
                                createPlayer(alice, money = 2000, properties = 5),
                                createPlayer(bob, money = 0, properties = 0),
                            ),
                        ),
                        createSingleGameResult(
                            gameNumber = 2,
                            winner = alice,
                            totalTurns = 120,
                            players = listOf(
                                createPlayer(alice, money = 2200, properties = 6),
                                createPlayer(bob, money = 0, properties = 0),
                            ),
                        ),
                        createSingleGameResult(
                            gameNumber = 3,
                            winner = alice,
                            totalTurns = 110,
                            players = listOf(
                                createPlayer(alice, money = 2100, properties = 5),
                                createPlayer(bob, money = 0, properties = 0),
                            ),
                        ),
                    )

                    val result = MultiGameResult(
                        gameResults = games,
                        totalGames = 3,
                    )

                    // Act
                    val stats = calculator.calculate(result)

                    // Assert
                    val aliceStats = stats.playerStats[alice]!!
                    aliceStats.wins shouldBe 3
                    aliceStats.winRate shouldBe 1.0

                    val bobStats = stats.playerStats[bob]!!
                    bobStats.wins shouldBe 0
                    bobStats.winRate shouldBe 0.0
                }
            }

            context("1ゲームのみの場合") {
                it("正しく統計を計算する") {
                    // Arrange
                    val alice = "Alice"
                    val bob = "Bob"

                    val game = createSingleGameResult(
                        gameNumber = 1,
                        winner = alice,
                        totalTurns = 100,
                        players = listOf(
                            createPlayer(alice, money = 2000, properties = 5),
                            createPlayer(bob, money = 0, properties = 0),
                        ),
                    )

                    val result = MultiGameResult(
                        gameResults = listOf(game),
                        totalGames = 1,
                    )

                    // Act
                    val stats = calculator.calculate(result)

                    // Assert
                    stats.totalGames shouldBe 1

                    val aliceStats = stats.playerStats[alice]!!
                    aliceStats.wins shouldBe 1
                    aliceStats.winRate shouldBe 1.0

                    val bobStats = stats.playerStats[bob]!!
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
                    val alice = "Alice"
                    val bob = "Bob"
                    val charlie = "Charlie"

                    val games = listOf(
                        createSingleGameResult(
                            gameNumber = 1,
                            winner = alice,
                            totalTurns = 100,
                            players = listOf(
                                createPlayer(alice, money = 2000, properties = 5),
                                createPlayer(bob, money = 500, properties = 2),
                                createPlayer(charlie, money = 0, properties = 0),
                            ),
                        ),
                        createSingleGameResult(
                            gameNumber = 2,
                            winner = bob,
                            totalTurns = 120,
                            players = listOf(
                                createPlayer(alice, money = 500, properties = 2),
                                createPlayer(bob, money = 2200, properties = 6),
                                createPlayer(charlie, money = 0, properties = 0),
                            ),
                        ),
                        createSingleGameResult(
                            gameNumber = 3,
                            winner = charlie,
                            totalTurns = 130,
                            players = listOf(
                                createPlayer(alice, money = 0, properties = 0),
                                createPlayer(bob, money = 500, properties = 2),
                                createPlayer(charlie, money = 2300, properties = 7),
                            ),
                        ),
                    )

                    val result = MultiGameResult(
                        gameResults = games,
                        totalGames = 3,
                    )

                    // Act
                    val stats = calculator.calculate(result)

                    // Assert
                    stats.totalGames shouldBe 3
                    stats.playerStats.size shouldBe 3

                    // 各プレイヤーの勝率
                    stats.playerStats[alice]!!.winRate shouldBe (1.0 / 3.0)
                    stats.playerStats[bob]!!.winRate shouldBe (1.0 / 3.0)
                    stats.playerStats[charlie]!!.winRate shouldBe (1.0 / 3.0)
                }
            }

            context("プロパティ所有数が正しく平均される") {
                it("平均プロパティ数を正しく計算する") {
                    // Arrange
                    val alice = "Alice"
                    val bob = "Bob"

                    val games = listOf(
                        createSingleGameResult(
                            gameNumber = 1,
                            winner = alice,
                            totalTurns = 100,
                            players = listOf(
                                createPlayer(alice, money = 2000, properties = 10),
                                createPlayer(bob, money = 0, properties = 0),
                            ),
                        ),
                        createSingleGameResult(
                            gameNumber = 2,
                            winner = alice,
                            totalTurns = 100,
                            players = listOf(
                                createPlayer(alice, money = 2000, properties = 8),
                                createPlayer(bob, money = 0, properties = 2),
                            ),
                        ),
                    )

                    val result = MultiGameResult(
                        gameResults = games,
                        totalGames = 2,
                    )

                    // Act
                    val stats = calculator.calculate(result)

                    // Assert
                    stats.playerStats[alice]!!.averagePropertiesOwned shouldBe 9.0 // (10 + 8) / 2
                    stats.playerStats[bob]!!.averagePropertiesOwned shouldBe 1.0 // (0 + 2) / 2
                }
            }
        }
    }
})

/**
 * テスト用のSingleGameResultを生成
 */
private fun createSingleGameResult(
    gameNumber: Int,
    winner: String,
    totalTurns: Int,
    players: List<Player>,
): SingleGameResult {
    val gameState = GameState(
        players = players,
        currentPlayerIndex = 0,
        currentTurn = totalTurns,
    )

    return SingleGameResult(
        gameNumber = gameNumber,
        winner = winner,
        totalTurns = totalTurns,
        finalState = gameState,
    )
}

/**
 * テスト用のPlayerを生成
 */
private fun createPlayer(
    name: String,
    money: Int,
    properties: Int,
): Player {
    // プロパティリストを作成（ダミー）
    val propertyList = (1..properties).map { BoardPosition(it) }.toSet()

    return Player(
        name = name,
        strategy = AlwaysBuyStrategy(),
        state = PlayerState(
            position = BoardPosition(0),
            money = Money(money),
            ownedProperties = propertyList,
            isBankrupt = false,
        ),
    )
}
