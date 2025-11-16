package com.monopoly.statistics

import com.monopoly.domain.model.Board
import com.monopoly.domain.model.BoardPosition
import com.monopoly.domain.model.ColorGroup
import com.monopoly.domain.model.GameEvent
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Money
import com.monopoly.domain.model.Player
import com.monopoly.domain.model.PlayerState
import com.monopoly.domain.model.Property
import com.monopoly.domain.model.Space
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
                    val board = createSimpleBoard()
                    val alice = createPlayer("Alice", 1500)

                    val game = createGameWithEvents(
                        gameNumber = 1,
                        winner = "Alice",
                        board = board,
                        players = listOf(alice),
                        events = listOf(
                            GameEvent.PlayerMoved(
                                turn = 1,
                                player = alice,
                                fromPosition = 0,
                                toPosition = 5,
                                diceRoll1 = 2,
                                diceRoll2 = 3
                            ),
                            GameEvent.PlayerMoved(
                                turn = 2,
                                player = alice,
                                fromPosition = 5,
                                toPosition = 10,
                                diceRoll1 = 3,
                                diceRoll2 = 2
                            ),
                            GameEvent.PlayerMoved(
                                turn = 3,
                                player = alice,
                                fromPosition = 10,
                                toPosition = 5,  // 同じ位置に再度着地
                                diceRoll1 = 1,
                                diceRoll2 = 4
                            )
                        )
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
                    val board = createSimpleBoard()
                    val property = (board.spaces[1] as Space.PropertySpace).property

                    val alice = createPlayer("Alice", 2000)
                    val bob = createPlayer("Bob", 500)

                    val game = createGameWithEvents(
                        gameNumber = 1,
                        winner = "Alice",
                        board = board,
                        players = listOf(alice, bob),
                        events = listOf(
                            GameEvent.PropertyPurchased(
                                turn = 1,
                                player = alice,
                                property = property
                            ),
                            GameEvent.RentPaid(
                                turn = 2,
                                player = bob,
                                property = property,
                                amount = 50,
                                owner = alice
                            ),
                            GameEvent.RentPaid(
                                turn = 3,
                                player = bob,
                                property = property,
                                amount = 50,
                                owner = alice
                            )
                        )
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
                    val board = createSimpleBoard()
                    val property = (board.spaces[1] as Space.PropertySpace).property

                    val alice = createPlayer("Alice", 2000)

                    val games = listOf(
                        createGameWithEvents(
                            gameNumber = 1,
                            winner = "Alice",
                            board = board,
                            players = listOf(alice),
                            events = listOf(
                                GameEvent.PropertyPurchased(
                                    turn = 1,
                                    player = alice,
                                    property = property
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
                                    turn = 1,
                                    player = alice,
                                    property = property
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
                    val board = createSimpleBoard()
                    val alice = createPlayer("Alice", 2000)

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
 * テスト用の簡単なボードを作成
 */
private fun createSimpleBoard(): Board {
    val spaces = listOf(
        Space.StartSpace(BoardPosition(0)),
        Space.PropertySpace(
            BoardPosition(1),
            Property(
                name = "Mediterranean Avenue",
                position = BoardPosition(1),
                price = 60,
                colorGroup = ColorGroup.BROWN,
                rent = 2
            )
        ),
        Space.ChanceSpace(BoardPosition(2)),
        Space.PropertySpace(
            BoardPosition(3),
            Property(
                name = "Baltic Avenue",
                position = BoardPosition(3),
                price = 60,
                colorGroup = ColorGroup.BROWN,
                rent = 4
            )
        )
    )
    return Board(spaces)
}

/**
 * テスト用のゲーム結果を作成
 */
private fun createGameWithEvents(
    gameNumber: Int,
    winner: String,
    board: Board,
    players: List<Player>,
    events: List<GameEvent>
): SingleGameResult {
    val gameState = GameState(
        players = players,
        currentPlayerIndex = 0,
        currentTurn = 100,
        board = board,
        events = events
    )

    return SingleGameResult(
        gameNumber = gameNumber,
        winner = winner,
        totalTurns = 100,
        finalState = gameState
    )
}

/**
 * テスト用のPlayerを生成
 */
private fun createPlayer(name: String, money: Int): Player {
    return Player(
        name = name,
        strategy = AlwaysBuyStrategy(),
        state = PlayerState(
            position = BoardPosition(0),
            money = Money(money),
            ownedProperties = emptySet(),
            isBankrupt = false
        )
    )
}
