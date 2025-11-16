package com.monopoly.domain.service

import com.monopoly.domain.model.*
import com.monopoly.domain.strategy.AlwaysBuyStrategy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class TradingServiceTest : DescribeSpec({
    describe("TradingService") {
        val board = StandardBoard.create()
        val tradingService = TradingService()

        describe("tradeProperties") {
            it("should trade properties between two players") {
                val property1 = Property(
                    name = "Property1",
                    position = 1,
                    price = 100,
                    baseRent = 10,
                    colorGroup = ColorGroup.BROWN,
                    ownership = PropertyOwnership.Owned("Player1")
                )
                val property2 = Property(
                    name = "Property2",
                    position = 3,
                    price = 120,
                    baseRent = 12,
                    colorGroup = ColorGroup.BROWN,
                    ownership = PropertyOwnership.Owned("Player2")
                )

                val player1 = Player("Player1", AlwaysBuyStrategy(), PlayerState(
                    Money(1000),
                    BoardPosition(0),
                    ownedProperties = PropertyCollection(listOf(property1))
                ))
                val player2 = Player("Player2", AlwaysBuyStrategy(), PlayerState(
                    Money(1000),
                    BoardPosition(0),
                    ownedProperties = PropertyCollection(listOf(property2))
                ))

                val gameState = GameState(listOf(player1, player2), board)

                tradingService.tradeProperties(player1, player2, property1, property2, gameState)

                player1.ownedProperties.size shouldBe 1
                player1.ownedProperties.first().name shouldBe "Property2"
                player1.ownedProperties.first().ownership shouldBe PropertyOwnership.Owned("Player1")

                player2.ownedProperties.size shouldBe 1
                player2.ownedProperties.first().name shouldBe "Property1"
                player2.ownedProperties.first().ownership shouldBe PropertyOwnership.Owned("Player2")
            }

            it("should fail if player1 does not own the property") {
                val property1 = Property(
                    name = "Property1",
                    position = 1,
                    price = 100,
                    baseRent = 10,
                    colorGroup = ColorGroup.BROWN,
                    ownership = PropertyOwnership.Owned("OtherPlayer")
                )
                val property2 = Property(
                    name = "Property2",
                    position = 3,
                    price = 120,
                    baseRent = 12,
                    colorGroup = ColorGroup.BROWN,
                    ownership = PropertyOwnership.Owned("Player2")
                )

                val player1 = Player("Player1", AlwaysBuyStrategy())
                val player2 = Player("Player2", AlwaysBuyStrategy(), PlayerState(
                    Money(1000),
                    BoardPosition(0),
                    ownedProperties = PropertyCollection(listOf(property2))
                ))

                val gameState = GameState(listOf(player1, player2), board)

                shouldThrow<IllegalArgumentException> {
                    tradingService.tradeProperties(player1, player2, property1, property2, gameState)
                }
            }
        }

        describe("buyPropertyFromPlayer") {
            it("should allow player to buy property from another player") {
                val property = Property(
                    name = "Property1",
                    position = 1,
                    price = 100,
                    baseRent = 10,
                    colorGroup = ColorGroup.BROWN,
                    ownership = PropertyOwnership.Owned("Player1")
                )

                val player1 = Player("Player1", AlwaysBuyStrategy(), PlayerState(
                    Money(1000),
                    BoardPosition(0),
                    ownedProperties = PropertyCollection(listOf(property))
                ))
                val player2 = Player("Player2", AlwaysBuyStrategy(), PlayerState(
                    Money(1000),
                    BoardPosition(0)
                ))

                val gameState = GameState(listOf(player1, player2), board)
                val agreedPrice = 150

                tradingService.buyPropertyFromPlayer(player2, player1, property, agreedPrice, gameState)

                player1.money shouldBe 1150  // +150
                player1.ownedProperties.size shouldBe 0

                player2.money shouldBe 850  // -150
                player2.ownedProperties.size shouldBe 1
                player2.ownedProperties.first().ownership shouldBe PropertyOwnership.Owned("Player2")
            }

            it("should fail if buyer does not have enough money") {
                val property = Property(
                    name = "Property1",
                    position = 1,
                    price = 100,
                    baseRent = 10,
                    colorGroup = ColorGroup.BROWN,
                    ownership = PropertyOwnership.Owned("Player1")
                )

                val player1 = Player("Player1", AlwaysBuyStrategy(), PlayerState(
                    Money(1000),
                    BoardPosition(0),
                    ownedProperties = PropertyCollection(listOf(property))
                ))
                val player2 = Player("Player2", AlwaysBuyStrategy(), PlayerState(
                    Money(100),
                    BoardPosition(0)
                ))

                val gameState = GameState(listOf(player1, player2), board)
                val agreedPrice = 500

                shouldThrow<IllegalArgumentException> {
                    tradingService.buyPropertyFromPlayer(player2, player1, property, agreedPrice, gameState)
                }
            }
        }
    }
})
