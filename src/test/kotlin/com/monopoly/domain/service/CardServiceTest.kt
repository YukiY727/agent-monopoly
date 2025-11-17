package com.monopoly.domain.service

import com.monopoly.domain.model.*
import com.monopoly.domain.strategy.AlwaysBuyStrategy
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class CardServiceTest : DescribeSpec({
    describe("CardService") {
        val board = StandardBoard.create()
        val cardService = CardService()

        describe("drawAndApplyChanceCard") {
            it("should apply CollectMoney card") {
                val player = Player("Test", AlwaysBuyStrategy(), PlayerState(Money(1000), BoardPosition(0)))
                val gameState = GameState(listOf(player), board)
                val deck = CardDeck(listOf(Card.CollectMoney(100)))

                val newDeck = cardService.drawAndApplyCard(player, gameState, deck)

                player.money shouldBe 1100
                newDeck.size shouldBe 1
            }

            it("should apply PayMoney card") {
                val player = Player("Test", AlwaysBuyStrategy(), PlayerState(Money(1000), BoardPosition(0)))
                val gameState = GameState(listOf(player), board)
                val deck = CardDeck(listOf(Card.PayMoney(50)))

                cardService.drawAndApplyCard(player, gameState, deck)

                player.money shouldBe 950
            }

            it("should apply GetOutOfJailFreeCard") {
                val player = Player("Test", AlwaysBuyStrategy(), PlayerState(Money(1000), BoardPosition(0)))
                val gameState = GameState(listOf(player), board)
                val deck = CardDeck(listOf(Card.GetOutOfJailFreeCard, Card.CollectMoney(100)))

                val newDeck = cardService.drawAndApplyCard(player, gameState, deck)

                player.state.getOutOfJailFreeCards shouldBe 1
                newDeck.size shouldBe 1 // GetOutOfJailFreeCard はデッキから削除される
            }

            it("should apply MoveTo card") {
                val player = Player("Test", AlwaysBuyStrategy(), PlayerState(Money(1000), BoardPosition(5)))
                val gameState = GameState(listOf(player), board)
                val deck = CardDeck(listOf(Card.AdvanceToGo))

                cardService.drawAndApplyCard(player, gameState, deck)

                player.position shouldBe 0
                player.money shouldBe 1200 // GO通過ボーナス
            }

            it("should apply GoToJail card") {
                val player = Player("Test", AlwaysBuyStrategy(), PlayerState(Money(1000), BoardPosition(5)))
                val gameState = GameState(listOf(player), board)
                val deck = CardDeck(listOf(Card.GoToJail))

                cardService.drawAndApplyCard(player, gameState, deck)

                player.position shouldBe 10
                player.state.inJail shouldBe true
            }

            it("should apply CollectFromEachPlayer card") {
                val player1 = Player("Player1", AlwaysBuyStrategy(), PlayerState(Money(1000), BoardPosition(0)))
                val player2 = Player("Player2", AlwaysBuyStrategy(), PlayerState(Money(1000), BoardPosition(1)))
                val player3 = Player("Player3", AlwaysBuyStrategy(), PlayerState(Money(1000), BoardPosition(2)))
                val gameState = GameState(listOf(player1, player2, player3), board)
                val deck = CardDeck(listOf(Card.CollectFromEachPlayer(50)))

                cardService.drawAndApplyCard(player1, gameState, deck)

                player1.money shouldBe 1100 // 50 * 2
                player2.money shouldBe 950
                player3.money shouldBe 950
            }

            it("should apply PayEachPlayer card") {
                val player1 = Player("Player1", AlwaysBuyStrategy(), PlayerState(Money(1000), BoardPosition(0)))
                val player2 = Player("Player2", AlwaysBuyStrategy(), PlayerState(Money(1000), BoardPosition(1)))
                val player3 = Player("Player3", AlwaysBuyStrategy(), PlayerState(Money(1000), BoardPosition(2)))
                val gameState = GameState(listOf(player1, player2, player3), board)
                val deck = CardDeck(listOf(Card.PayEachPlayer(50)))

                cardService.drawAndApplyCard(player1, gameState, deck)

                player1.money shouldBe 900 // -50 * 2
                player2.money shouldBe 1050
                player3.money shouldBe 1050
            }

            it("should apply PayRepairs card") {
                val property = Property(
                    name = "Test Property",
                    position = 1,
                    price = 100,
                    baseRent = 10,
                    colorGroup = ColorGroup.BROWN,
                    ownership = PropertyOwnership.Owned("Player1"),
                    houses = 3,
                    hasHotel = false
                )
                val player = Player("Player1", AlwaysBuyStrategy(), PlayerState(
                    Money(1000),
                    BoardPosition(0),
                    ownedProperties = PropertyCollection(listOf(property))
                ))
                val gameState = GameState(listOf(player), board)
                val deck = CardDeck(listOf(Card.PayRepairs(25, 100)))

                cardService.drawAndApplyCard(player, gameState, deck)

                player.money shouldBe 925 // 1000 - (3 * 25)
            }

            it("should apply PayRepairs card with hotel") {
                val property = Property(
                    name = "Test Property",
                    position = 1,
                    price = 100,
                    baseRent = 10,
                    colorGroup = ColorGroup.BROWN,
                    ownership = PropertyOwnership.Owned("Player1"),
                    houses = 0,
                    hasHotel = true
                )
                val player = Player("Player1", AlwaysBuyStrategy(), PlayerState(
                    Money(1000),
                    BoardPosition(0),
                    ownedProperties = PropertyCollection(listOf(property))
                ))
                val gameState = GameState(listOf(player), board)
                val deck = CardDeck(listOf(Card.PayRepairs(25, 100)))

                cardService.drawAndApplyCard(player, gameState, deck)

                player.money shouldBe 900 // 1000 - (1 * 100)
            }
        }
    }
})
