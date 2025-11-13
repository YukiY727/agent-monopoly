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
})
