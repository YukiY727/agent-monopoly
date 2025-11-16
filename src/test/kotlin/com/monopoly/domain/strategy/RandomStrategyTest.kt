package com.monopoly.domain.strategy

import com.monopoly.domain.model.ColorGroup
import com.monopoly.domain.model.Property
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import kotlin.random.Random

class RandomStrategyTest : StringSpec({
    // Given: シード固定のRandom、購入可能な所持金
    // When: shouldBuyを複数回呼び出す
    // Then: 約50%の確率でtrueを返す
    "should buy property approximately 50% of the time when random and has enough money" {
        // Given
        val strategy = RandomStrategy(Random(seed = 1))
        val property = Property("Test Property", 1, 200, 10, ColorGroup.BROWN)

        // When
        val results: List<Boolean> = (1..100).map { strategy.shouldBuy(property, 1500) }

        // Then
        val trueCount: Int = results.count { it }
        trueCount shouldBeInRange 40..60 // 約50%
    }

    // Given: 所持金が足りない
    // When: shouldBuyを呼び出す
    // Then: falseを返す（ランダムに関係なく）
    "should not buy property when not enough money" {
        // Given
        val strategy = RandomStrategy(Random(seed = 1))
        val property = Property("Test Property", 1, 200, 10, ColorGroup.BROWN)

        // When & Then
        strategy.shouldBuy(property, 100) shouldBe false
    }

    // Given: 所持金がちょうど価格と同じ
    // When: shouldBuyを呼び出す
    // Then: ランダムに購入判断を行う
    "should make random decision when money equals property price" {
        // Given
        val strategy = RandomStrategy(Random(seed = 1))
        val property = Property("Test Property", 1, 200, 10, ColorGroup.BROWN)

        // When
        val results: List<Boolean> = (1..100).map { strategy.shouldBuy(property, 200) }

        // Then
        val trueCount: Int = results.count { it }
        trueCount shouldBeInRange 40..60 // 約50%
    }
})
