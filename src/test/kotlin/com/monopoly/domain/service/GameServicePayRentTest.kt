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

    // TC-123: レント支払い（0レント）
    // Given: 支払者と受取者
    // When: payRent(payer, receiver, 0)
    // Then: 両者の所持金が変わらず、破産しない
    "should handle zero rent payment correctly" {
        // Given
        val payer = Player(name = "George", strategy = AlwaysBuyStrategy())
        val receiver = Player(name = "Helen", strategy = AlwaysBuyStrategy())
        val gameService = GameService()

        // When
        gameService.payRent(payer, receiver, 0)

        // Then
        payer.money shouldBe 1500
        receiver.money shouldBe 1500
        payer.isBankrupt shouldBe false
    }

    // TC-124: レント支払い（高額レント）
    // Given: 支払者の所持金$1500、レント$5000
    // When: payRent(payer, receiver, 5000)
    // Then: 支払者が破産し、所持金が-$3500
    "should handle very high rent causing bankruptcy" {
        // Given
        val payer = Player(name = "Ian", strategy = AlwaysBuyStrategy())
        val receiver = Player(name = "Jane", strategy = AlwaysBuyStrategy())
        val gameService = GameService()

        // When
        gameService.payRent(payer, receiver, 5000)

        // Then
        payer.money shouldBe -3500
        payer.isBankrupt shouldBe true
        receiver.money shouldBe 6500
    }

    // TC-125: レント支払い（ちょうど所持金と同額）
    // Given: 支払者の所持金$1500、レント$1500
    // When: payRent(payer, receiver, 1500)
    // Then: 支払者の所持金が$0、破産しない
    "should not go bankrupt when rent equals available money" {
        // Given
        val payer = Player(name = "Kevin", strategy = AlwaysBuyStrategy())
        val receiver = Player(name = "Laura", strategy = AlwaysBuyStrategy())
        val gameService = GameService()

        // When
        gameService.payRent(payer, receiver, 1500)

        // Then
        payer.money shouldBe 0
        payer.isBankrupt shouldBe false
        receiver.money shouldBe 3000
    }

    // TC-126: レント支払い（所持金より1多い）
    // Given: 支払者の所持金$1500、レント$1501
    // When: payRent(payer, receiver, 1501)
    // Then: 支払者が破産し、所持金が-$1
    "should go bankrupt when rent is one more than available money" {
        // Given
        val payer = Player(name = "Mike", strategy = AlwaysBuyStrategy())
        val receiver = Player(name = "Nancy", strategy = AlwaysBuyStrategy())
        val gameService = GameService()

        // When
        gameService.payRent(payer, receiver, 1501)

        // Then
        payer.money shouldBe -1
        payer.isBankrupt shouldBe true
        receiver.money shouldBe 3001
    }

    // TC-127: 複数回のレント支払いで破産
    // Given: 支払者の所持金$1500
    // When: 複数回レントを支払い、最終的に破産
    // Then: 最後の支払いで破産フラグがtrue
    "should go bankrupt after multiple rent payments" {
        // Given
        val payer = Player(name = "Oscar", strategy = AlwaysBuyStrategy())
        val receiver1 = Player(name = "Patricia", strategy = AlwaysBuyStrategy())
        val receiver2 = Player(name = "Quinn", strategy = AlwaysBuyStrategy())
        val receiver3 = Player(name = "Rachel", strategy = AlwaysBuyStrategy())
        val gameService = GameService()

        // When & Then
        gameService.payRent(payer, receiver1, 600)
        payer.money shouldBe 900
        payer.isBankrupt shouldBe false

        gameService.payRent(payer, receiver2, 500)
        payer.money shouldBe 400
        payer.isBankrupt shouldBe false

        gameService.payRent(payer, receiver3, 600)
        payer.money shouldBe -200
        payer.isBankrupt shouldBe true
    }
})
