package com.monopoly.statistics

import com.monopoly.domain.model.BoardFixtures
import com.monopoly.domain.model.GameEvent
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Player
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
                    val board = BoardFixtures.createStandardBoard()
                    val property1 = board.getPropertyAt(1)!!
                    val property2 = board.getPropertyAt(3)!!

                    val alice = Player("Alice", AlwaysBuyStrategy())
                    val bob = Player("Bob", AlwaysBuyStrategy())

                    val events = mutableListOf<GameEvent>()
                    events.add(
                        GameEvent.PropertyPurchased(
                            turnNumber = 10,
                            playerName = "Alice",
                            propertyName = property1.name,
                            price = property1.price
                        )
                    )
                    events.add(
                        GameEvent.RentPaid(
                            turnNumber = 20,
                            payerName = "Bob",
                            receiverName = "Alice",
                            propertyName = property1.name,
                            amount = 50
                        )
                    )

                    val gameState = GameState(listOf(alice, bob), board, events)
                    val game1 = SingleGameResult(
                        gameNumber = 1,
                        winner = "Alice",
                        totalTurns = 100,
                        finalState = gameState
                    )

                    val result = MultiGameResult(
                        gameResults = listOf(game1),
                        totalGames = 1
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
                    val board = BoardFixtures.createStandardBoard()
                    val property = board.getPropertyAt(1)!!

                    val games = (1..3).map { gameNum ->
                        val isPurchased = gameNum <= 2 // 2回購入
                        val events = if (isPurchased) {
                            mutableListOf<GameEvent>(
                                GameEvent.PropertyPurchased(
                                    turnNumber = 10,
                                    playerName = "Alice",
                                    propertyName = property.name,
                                    price = property.price
                                )
                            )
                        } else {
                            mutableListOf()
                        }

                        val alice = Player("Alice", AlwaysBuyStrategy())
                        val gameState = GameState(listOf(alice), board, events)

                        SingleGameResult(
                            gameNumber = gameNum,
                            winner = "Alice",
                            totalTurns = 100,
                            finalState = gameState
                        )
                    }

                    val result = MultiGameResult(
                        gameResults = games,
                        totalGames = 3
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
                    val board = BoardFixtures.createStandardBoard()

                    val alice = Player("Alice", AlwaysBuyStrategy())
                    val bob = Player("Bob", AlwaysBuyStrategy())

                    // Aliceに+500、Bobから-1000を引く
                    alice.receiveMoney(com.monopoly.domain.model.Money(500))
                    bob.subtractMoney(1000)

                    val gameState = GameState(listOf(alice, bob), board, mutableListOf())
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
                    val stats = calculator.calculate(result)

                    // Assert
                    stats.assetHistory.snapshots.size shouldBeGreaterThanOrEqual 2 // 少なくとも2人分
                    val aliceSnapshot = stats.assetHistory.snapshots.find { it.playerName == "Alice" }
                    aliceSnapshot shouldNotBe null
                    aliceSnapshot!!.cash shouldBe 2000 // 初期値1500 + 500
                }
            }

            context("破産分析") {
                it("破産イベントを記録する") {
                    // Arrange
                    val board = BoardFixtures.createStandardBoard()
                    val alice = Player("Alice", AlwaysBuyStrategy())
                    val bob = Player("Bob", AlwaysBuyStrategy())

                    val events = mutableListOf<GameEvent>(
                        GameEvent.PlayerBankrupted(
                            turnNumber = 50,
                            playerName = "Bob",
                            finalMoney = 0
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
                    val stats = calculator.calculate(result)

                    // Assert
                    stats.bankruptcyAnalysis.totalBankruptcies shouldBe 1
                    stats.bankruptcyAnalysis.bankruptcyEvents.size shouldBe 1

                    val bankruptcyEvent = stats.bankruptcyAnalysis.bankruptcyEvents.first()
                    bankruptcyEvent.playerName shouldBe "Bob"
                }
            }
        }
    }
})
