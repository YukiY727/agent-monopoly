package com.monopoly.statistics

import com.monopoly.domain.model.BoardFixtures
import com.monopoly.domain.model.GameEvent
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Player
import com.monopoly.domain.strategy.AlwaysBuyStrategy
import com.monopoly.simulation.MultiGameResult
import com.monopoly.simulation.SingleGameResult
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class BoardStatisticsCalculatorTest : DescribeSpec({
    describe("BoardStatisticsCalculator") {
        val calculator = BoardStatisticsCalculator()

        describe("calculate") {
            context("基本的なイベント処理") {
                it("移動イベントから着地回数を集計する") {
                    // Arrange
                    val board = BoardFixtures.createStandardBoard()
                    val alice = Player("Alice", AlwaysBuyStrategy())

                    val events = mutableListOf<GameEvent>()
                    events.add(
                        GameEvent.PlayerMoved(
                            turnNumber = 1,
                            playerName = "Alice",
                            fromPosition = 0,
                            toPosition = 5,
                            passedGo = false
                        )
                    )
                    events.add(
                        GameEvent.PlayerMoved(
                            turnNumber = 2,
                            playerName = "Alice",
                            fromPosition = 5,
                            toPosition = 10,
                            passedGo = false
                        )
                    )
                    events.add(
                        GameEvent.PlayerMoved(
                            turnNumber = 3,
                            playerName = "Alice",
                            fromPosition = 10,
                            toPosition = 5,  // 同じ位置に再度着地
                            passedGo = false
                        )
                    )

                    val gameState = GameState(listOf(alice), board, events)
                    val game = SingleGameResult(
                        gameNumber = 1,
                        winner = "Alice",
                        totalTurns = 100,
                        finalState = gameState
                    )

                    val result = MultiGameResult(
                        gameResults = listOf(game),
                        totalGames = 1
                    )

                    // Act
                    val stats = calculator.calculate(result, board)

                    // Assert
                    stats.totalGames shouldBe 1
                    stats.positionStats[5]!!.landedCount shouldBe 2  // 位置5に2回着地
                    stats.positionStats[10]!!.landedCount shouldBe 1  // 位置10に1回着地
                }
            }

            context("レント支払いイベント") {
                it("レント収入を集計する") {
                    // Arrange
                    val board = BoardFixtures.createStandardBoard()
                    val property = board.getPropertyAt(1)!!

                    val alice = Player("Alice", AlwaysBuyStrategy())
                    val bob = Player("Bob", AlwaysBuyStrategy())

                    val events = mutableListOf<GameEvent>()
                    events.add(
                        GameEvent.PropertyPurchased(
                            turnNumber = 1,
                            playerName = "Alice",
                            propertyName = property.name,
                            price = property.price
                        )
                    )
                    events.add(
                        GameEvent.RentPaid(
                            turnNumber = 2,
                            payerName = "Bob",
                            receiverName = "Alice",
                            propertyName = property.name,
                            amount = 50
                        )
                    )
                    events.add(
                        GameEvent.RentPaid(
                            turnNumber = 3,
                            payerName = "Bob",
                            receiverName = "Alice",
                            propertyName = property.name,
                            amount = 50
                        )
                    )

                    val gameState = GameState(listOf(alice, bob), board, events)
                    val game = SingleGameResult(
                        gameNumber = 1,
                        winner = "Alice",
                        totalTurns = 100,
                        finalState = gameState
                    )

                    val result = MultiGameResult(
                        gameResults = listOf(game),
                        totalGames = 1
                    )

                    // Act
                    val stats = calculator.calculate(result, board)

                    // Assert
                    stats.positionStats[property.position]!!.totalRentCollected shouldBe 100.0
                }
            }

            context("プロパティ購入イベント") {
                it("所有者分布を記録する") {
                    // Arrange
                    val board = BoardFixtures.createStandardBoard()
                    val property = board.getPropertyAt(1)!!

                    val alice = Player("Alice", AlwaysBuyStrategy())

                    val games = listOf(
                        createGameWithEvents(
                            gameNumber = 1,
                            winner = "Alice",
                            board = board,
                            players = listOf(alice),
                            events = listOf(
                                GameEvent.PropertyPurchased(
                                    turnNumber = 1,
                                    playerName = "Alice",
                                    propertyName = property.name,
                                    price = property.price
                                )
                            )
                        ),
                        createGameWithEvents(
                            gameNumber = 2,
                            winner = "Alice",
                            board = board,
                            players = listOf(alice),
                            events = listOf(
                                GameEvent.PropertyPurchased(
                                    turnNumber = 1,
                                    playerName = "Alice",
                                    propertyName = property.name,
                                    price = property.price
                                )
                            )
                        )
                    )

                    val result = MultiGameResult(
                        gameResults = games,
                        totalGames = 2
                    )

                    // Act
                    val stats = calculator.calculate(result, board)

                    // Assert
                    stats.positionStats[property.position]!!.ownershipDistribution["Alice"] shouldBe 2
                }
            }

            context("複数ゲームの統計") {
                it("ゲーム数を正しく記録する") {
                    // Arrange
                    val board = BoardFixtures.createStandardBoard()
                    val alice = Player("Alice", AlwaysBuyStrategy())

                    val games = (1..3).map { gameNum ->
                        createGameWithEvents(
                            gameNumber = gameNum,
                            winner = "Alice",
                            board = board,
                            players = listOf(alice),
                            events = emptyList()
                        )
                    }

                    val result = MultiGameResult(
                        gameResults = games,
                        totalGames = 3
                    )

                    // Act
                    val stats = calculator.calculate(result, board)

                    // Assert
                    stats.totalGames shouldBe 3
                }
            }

            context("PositionStatisticsの計算メソッド") {
                it("平均着地回数を正しく計算する") {
                    // Arrange
                    val positionStats = BoardStatistics.PositionStatistics(
                        position = 5,
                        landedCount = 10,
                        totalRentCollected = 0.0,
                        ownershipDistribution = emptyMap()
                    )

                    // Act
                    val average = positionStats.averageLandedPerGame(totalGames = 5)

                    // Assert
                    average shouldBe 2.0  // 10 / 5
                }

                it("平均レント収入を正しく計算する") {
                    // Arrange
                    val positionStats = BoardStatistics.PositionStatistics(
                        position = 5,
                        landedCount = 0,
                        totalRentCollected = 150.0,
                        ownershipDistribution = emptyMap()
                    )

                    // Act
                    val average = positionStats.averageRentPerGame(totalGames = 3)

                    // Assert
                    average shouldBe 50.0  // 150 / 3
                }
            }
        }
    }
})

/**
 * テスト用のゲーム結果を作成
 */
private fun createGameWithEvents(
    gameNumber: Int,
    winner: String,
    board: com.monopoly.domain.model.Board,
    players: List<Player>,
    events: List<GameEvent>
): SingleGameResult {
    val gameState = GameState(
        players = players,
        board = board,
        events = events.toMutableList()
    )

    return SingleGameResult(
        gameNumber = gameNumber,
        winner = winner,
        totalTurns = 100,
        finalState = gameState
    )
}
