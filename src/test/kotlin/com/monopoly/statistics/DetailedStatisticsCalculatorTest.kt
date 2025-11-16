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
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class DetailedStatisticsCalculatorTest : DescribeSpec({
    describe("DetailedStatisticsCalculator") {
        val calculator = DetailedStatisticsCalculator()

        describe("calculate") {
            context("基本的な統計計算") {
                it("DetailedStatisticsを生成する") {
                    // Arrange
                    val board = createSimpleBoard()
                    val alice = "Alice"
                    val bob = "Bob"

                    val property1 = (board.spaces[1] as Space.PropertySpace).property
                    val property2 = (board.spaces[3] as Space.PropertySpace).property

                    val game1 = createGameWithEvents(
                        gameNumber = 1,
                        winner = alice,
                        totalTurns = 100,
                        board = board,
                        players = listOf(
                            createPlayer(alice, money = 2000, properties = setOf(property1)),
                            createPlayer(bob, money = 0, properties = emptySet()),
                        ),
                        events = listOf(
                            GameEvent.PropertyPurchased(
                                turn = 10,
                                player = createPlayer(alice, money = 2000, properties = emptySet()),
                                property = property1,
                            ),
                            GameEvent.RentPaid(
                                turn = 20,
                                player = createPlayer(bob, money = 0, properties = emptySet()),
                                property = property1,
                                amount = 50,
                                owner = createPlayer(alice, money = 2000, properties = emptySet()),
                            ),
                        ),
                    )

                    val result = MultiGameResult(
                        gameResults = listOf(game1),
                        totalGames = 1,
                    )

                    // Act
                    val stats = calculator.calculate(result)

                    // Assert
                    stats shouldNotBe null
                    stats.basicStats.totalGames shouldBe 1
                    stats.propertyStatistics shouldNotBe null
                    stats.assetHistory shouldNotBe null
                    stats.bankruptcyAnalysis shouldNotBe null
                }
            }

            context("プロパティ統計") {
                it("プロパティの購入回数と購入率を正しく計算する") {
                    // Arrange
                    val board = createSimpleBoard()
                    val property = (board.spaces[1] as Space.PropertySpace).property

                    val games = (1..3).map { gameNum ->
                        val isPurchased = gameNum <= 2 // 2回購入
                        val events = if (isPurchased) {
                            listOf(
                                GameEvent.PropertyPurchased(
                                    turn = 10,
                                    player = createPlayer("Alice", money = 1500, properties = emptySet()),
                                    property = property,
                                ),
                            )
                        } else {
                            emptyList()
                        }

                        createGameWithEvents(
                            gameNumber = gameNum,
                            winner = "Alice",
                            totalTurns = 100,
                            board = board,
                            players = listOf(
                                createPlayer("Alice", money = 2000, properties = emptySet()),
                            ),
                            events = events,
                        )
                    }

                    val result = MultiGameResult(
                        gameResults = games,
                        totalGames = 3,
                    )

                    // Act
                    val stats = calculator.calculate(result)

                    // Assert
                    val propStats = stats.propertyStatistics.find { it.propertyName == property.name }
                    propStats shouldNotBe null
                    propStats!!.purchaseCount shouldBe 2
                    propStats.purchaseRate shouldBe (2.0 / 3.0)
                }
            }

            context("資産推移") {
                it("資産スナップショットを記録する") {
                    // Arrange
                    val board = createSimpleBoard()
                    val alice = "Alice"
                    val bob = "Bob"

                    val game = createGameWithEvents(
                        gameNumber = 1,
                        winner = alice,
                        totalTurns = 100,
                        board = board,
                        players = listOf(
                            createPlayer(alice, money = 2000, properties = emptySet()),
                            createPlayer(bob, money = 500, properties = emptySet()),
                        ),
                        events = emptyList(),
                    )

                    val result = MultiGameResult(
                        gameResults = listOf(game),
                        totalGames = 1,
                    )

                    // Act
                    val stats = calculator.calculate(result)

                    // Assert
                    stats.assetHistory.snapshots.size shouldBeGreaterThanOrEqual 2 // 少なくとも2人分
                    val aliceSnapshot = stats.assetHistory.snapshots.find { it.playerName == alice }
                    aliceSnapshot shouldNotBe null
                    aliceSnapshot!!.cash shouldBe 2000
                }
            }

            context("破産分析") {
                it("破産イベントを記録する") {
                    // Arrange
                    val board = createSimpleBoard()
                    val alice = "Alice"
                    val bob = "Bob"

                    val alicePlayer = createPlayer(alice, money = 2000, properties = emptySet())
                    val bobPlayer = createPlayer(bob, money = 0, properties = emptySet())

                    val game = createGameWithEvents(
                        gameNumber = 1,
                        winner = alice,
                        totalTurns = 100,
                        board = board,
                        players = listOf(alicePlayer, bobPlayer),
                        events = listOf(
                            GameEvent.PlayerBankrupt(
                                turn = 50,
                                player = bobPlayer,
                                creditor = alicePlayer,
                            ),
                        ),
                    )

                    val result = MultiGameResult(
                        gameResults = listOf(game),
                        totalGames = 1,
                    )

                    // Act
                    val stats = calculator.calculate(result)

                    // Assert
                    stats.bankruptcyAnalysis.totalBankruptcies shouldBe 1
                    stats.bankruptcyAnalysis.bankruptcyEvents.size shouldBe 1

                    val bankruptcyEvent = stats.bankruptcyAnalysis.bankruptcyEvents.first()
                    bankruptcyEvent.playerName shouldBe bob
                    bankruptcyEvent.causePlayerName shouldBe alice
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
                rent = 2,
            ),
        ),
        Space.ChanceSpace(BoardPosition(2)),
        Space.PropertySpace(
            BoardPosition(3),
            Property(
                name = "Baltic Avenue",
                position = BoardPosition(3),
                price = 60,
                colorGroup = ColorGroup.BROWN,
                rent = 4,
            ),
        ),
    )
    return Board(spaces)
}

/**
 * テスト用のゲーム結果を作成（イベント付き）
 */
private fun createGameWithEvents(
    gameNumber: Int,
    winner: String,
    totalTurns: Int,
    board: Board,
    players: List<Player>,
    events: List<GameEvent>,
): SingleGameResult {
    val gameState = GameState(
        players = players,
        currentPlayerIndex = 0,
        currentTurn = totalTurns,
        board = board,
        events = events,
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
    properties: Set<Property>,
): Player {
    val propertyPositions = properties.map { it.position }.toSet()

    return Player(
        name = name,
        strategy = AlwaysBuyStrategy(),
        state = PlayerState(
            position = BoardPosition(0),
            money = Money(money),
            ownedProperties = propertyPositions,
            isBankrupt = false,
        ),
    )
}
