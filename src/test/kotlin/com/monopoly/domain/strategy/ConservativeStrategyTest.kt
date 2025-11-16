package com.monopoly.domain.strategy

import com.monopoly.domain.model.ColorGroup
import com.monopoly.domain.model.Property
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ConservativeStrategyTest : StringSpec({
    // Given: 購入後の所持金が閾値以上
    // When: shouldBuyを呼び出す
    // Then: trueを返す
    "should buy property when enough cash reserve remains" {
        // Given
        val strategy = ConservativeStrategy(minimumCashReserve = 500)
        val property = Property("Test Property", 1, 200, 10, ColorGroup.BROWN)

        // When & Then
        // $1500 - $200 = $1300 (>= $500) なので購入する
        strategy.shouldBuy(property, 1500) shouldBe true
    }

    // Given: 購入後の所持金が閾値未満
    // When: shouldBuyを呼び出す
    // Then: falseを返す
    "should not buy property when cash reserve would be below minimum" {
        // Given
        val strategy = ConservativeStrategy(minimumCashReserve = 500)
        val property = Property("Test Property", 1, 300, 10, ColorGroup.BROWN)

        // When & Then
        // $700 - $300 = $400 (< $500) なので購入しない
        strategy.shouldBuy(property, 700) shouldBe false
    }

    // Given: 購入後の所持金がちょうど閾値
    // When: shouldBuyを呼び出す
    // Then: trueを返す
    "should buy property when cash reserve would be exactly at minimum" {
        // Given
        val strategy = ConservativeStrategy(minimumCashReserve = 500)
        val property = Property("Test Property", 1, 500, 10, ColorGroup.BROWN)

        // When & Then
        // $1000 - $500 = $500 (== $500) なので購入する
        strategy.shouldBuy(property, 1000) shouldBe true
    }

    // Given: 所持金が価格より少ない
    // When: shouldBuyを呼び出す
    // Then: falseを返す（購入後の所持金がマイナスになる）
    "should not buy property when not enough money" {
        // Given
        val strategy = ConservativeStrategy(minimumCashReserve = 500)
        val property = Property("Test Property", 1, 200, 10, ColorGroup.BROWN)

        // When & Then
        // $100 - $200 = -$100 (< $500) なので購入しない
        strategy.shouldBuy(property, 100) shouldBe false
    }

    // Given: デフォルトの閾値（$500）を使用
    // When: shouldBuyを呼び出す
    // Then: デフォルト閾値で判定する
    "should use default minimum cash reserve when not specified" {
        // Given
        val strategy = ConservativeStrategy() // デフォルト: $500
        val property = Property("Test Property", 1, 300, 10, ColorGroup.BROWN)

        // When & Then
        // $700 - $300 = $400 (< $500) なので購入しない
        strategy.shouldBuy(property, 700) shouldBe false

        // $800 - $300 = $500 (== $500) なので購入する
        strategy.shouldBuy(property, 800) shouldBe true
    }

    // Given: カスタムの閾値（$300）を使用
    // When: shouldBuyを呼び出す
    // Then: カスタム閾値で判定する
    "should use custom minimum cash reserve when specified" {
        // Given
        val strategy = ConservativeStrategy(minimumCashReserve = 300)
        val property = Property("Test Property", 1, 200, 10, ColorGroup.BROWN)

        // When & Then
        // $450 - $200 = $250 (< $300) なので購入しない
        strategy.shouldBuy(property, 450) shouldBe false

        // $500 - $200 = $300 (== $300) なので購入する
        strategy.shouldBuy(property, 500) shouldBe true
    }
})
