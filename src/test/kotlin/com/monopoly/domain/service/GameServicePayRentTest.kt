package com.monopoly.domain.service

import com.monopoly.domain.model.Player
import com.monopoly.domain.strategy.AlwaysBuyStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class GameServicePayRentTest : StringSpec({
    // TC-120: レント支払い（通常）
    // Given: 支払者の所持金$1500、受取者の所持金$1000、レント$100
    // When: payRent(payer, receiver, 100)
    // Then: 支払者の所持金が$1400、受取者の所持金が$1100
    "should pay rent normally when payer has enough money" {
        // Given
        val payer = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val receiver = Player(name = "Bob", strategy = AlwaysBuyStrategy())
        receiver.subtractMoney(500) // Set receiver's money to $1000
        val gameService = GameService()

        // When
        gameService.payRent(payer, receiver, 100)

        // Then
        payer.money shouldBe 1400
        receiver.money shouldBe 1100
    }

    // TC-121: レント支払い後の破産
    // Given: 支払者の所持金$50、レント$100
    // When: payRent(payer, receiver, 100)
    // Then: 支払者の所持金が-$50、破産フラグがtrue
    "should mark payer as bankrupt when rent exceeds available money" {
        // Given
        val payer = Player(name = "Carol", strategy = AlwaysBuyStrategy())
        payer.subtractMoney(1450) // Set payer's money to $50
        val receiver = Player(name = "Dave", strategy = AlwaysBuyStrategy())
        val gameService = GameService()

        // When
        gameService.payRent(payer, receiver, 100)

        // Then
        payer.money shouldBe -50
        payer.isBankrupt shouldBe true
    }

    // TC-122: 受取者の所持金増加
    // Given: 受取者の所持金$1000、レント$50
    // When: payRent(payer, receiver, 50)
    // Then: 受取者の所持金が$1050
    "should increase receiver's money by rent amount" {
        // Given
        val payer = Player(name = "Eve", strategy = AlwaysBuyStrategy())
        val receiver = Player(name = "Frank", strategy = AlwaysBuyStrategy())
        receiver.subtractMoney(500) // Set receiver's money to $1000
        val gameService = GameService()

        // When
        gameService.payRent(payer, receiver, 50)

        // Then
        receiver.money shouldBe 1050
    }

    // Phase 2: イベント記録のテスト

    // TC-233: payRent実行後、RentPaidイベントが記録される
    // Given: 支払者、受取者、Property、GameState
    // When: payRent(payer, receiver, rent, propertyName, gameState)
    // Then: gameState.eventsにRentPaidイベントが追加されている、amountが正しい
    "should record RentPaid event when paying rent" {
        // Given
        val payer = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val receiver = Player(name = "Bob", strategy = AlwaysBuyStrategy())
        val board = com.monopoly.domain.model.BoardFixtures.createStandardBoard()
        val gameState = com.monopoly.domain.model.GameState(listOf(payer, receiver), board)
        val gameService = GameService()

        // When
        gameService.payRent(payer, receiver, 100, "Park Place", gameState)

        // Then
        payer.money shouldBe 1400
        receiver.money shouldBe 1600
        val rentPaidEvents: List<com.monopoly.domain.event.GameEvent.RentPaid> =
            gameState.events.filterIsInstance<com.monopoly.domain.event.GameEvent.RentPaid>()
        rentPaidEvents.size shouldBe 1
        val rentPaidEvent: com.monopoly.domain.event.GameEvent.RentPaid = rentPaidEvents.first()
        rentPaidEvent.payerName shouldBe "Alice"
        rentPaidEvent.receiverName shouldBe "Bob"
        rentPaidEvent.propertyName shouldBe "Park Place"
        rentPaidEvent.amount shouldBe 100
    }
})
