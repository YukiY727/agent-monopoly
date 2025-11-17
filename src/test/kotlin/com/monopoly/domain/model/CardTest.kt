package com.monopoly.domain.model

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class CardTest : DescribeSpec({
    describe("Card") {
        it("GetOutOfJailFreeCard should have correct description") {
            val card = Card.GetOutOfJailFreeCard
            card.description shouldBe "Get Out of Jail Free"
        }

        it("AdvanceToGo should move player to position 0") {
            val card = Card.AdvanceToGo
            card.shouldBeInstanceOf<Card.MoveTo>()
            card.position shouldBe 0
        }

        it("CollectMoney should have positive amount") {
            val card = Card.CollectMoney(50)
            card.amount shouldBe 50
        }

        it("PayMoney should have positive amount") {
            val card = Card.PayMoney(50)
            card.amount shouldBe 50
        }

        it("GoToJail should move to jail position") {
            val card = Card.GoToJail
            card.position shouldBe 10
        }
    }

    describe("CardDeck") {
        it("should create deck with cards") {
            val cards = listOf(
                Card.GetOutOfJailFreeCard,
                Card.AdvanceToGo,
                Card.CollectMoney(100)
            )
            val deck = CardDeck(cards)
            deck.size shouldBe 3
        }

        it("should draw card from deck") {
            val cards = listOf(
                Card.GetOutOfJailFreeCard,
                Card.AdvanceToGo
            )
            val deck = CardDeck(cards)
            val (card, newDeck) = deck.draw()
            card.shouldBeInstanceOf<Card>()
            newDeck.size shouldBe 1  // GetOutOfJailFreeCard は削除されるので 1
        }

        it("should shuffle drawn card back to bottom") {
            val cards = listOf(
                Card.CollectMoney(100),
                Card.CollectMoney(200),
                Card.CollectMoney(300)
            )
            val deck = CardDeck(cards)
            val (firstCard, deck2) = deck.draw()
            firstCard shouldBe Card.CollectMoney(100)
            deck2.size shouldBe 3
        }

        it("should handle GetOutOfJailFreeCard specially by removing from deck") {
            val cards = listOf(
                Card.GetOutOfJailFreeCard,
                Card.CollectMoney(100)
            )
            val deck = CardDeck(cards)
            val (card, newDeck) = deck.draw()
            card shouldBe Card.GetOutOfJailFreeCard
            newDeck.size shouldBe 1 // カードはデッキに戻らない
        }
    }
})
