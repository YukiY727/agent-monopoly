package com.monopoly.domain.model

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

/**
 * Utility家賃計算のテスト
 *
 * Phase 15: 公共施設の特殊ルール実装
 */
class UtilityRentTest : DescribeSpec({
    describe("Utility rent calculation") {
        it("should charge dice roll × 4 for 1 utility") {
            val player = Player("Alice", AlwaysBuyStrategy())
            val utility = Utility("Electric Company", 12).withOwner(player)

            utility.calculateRent(listOf(utility), diceRoll = 7) shouldBe 28 // 7 × 4
            utility.calculateRent(listOf(utility), diceRoll = 12) shouldBe 48 // 12 × 4
        }

        it("should charge dice roll × 10 for 2 utilities") {
            val player = Player("Alice", AlwaysBuyStrategy())
            val utility1 = Utility("Electric Company", 12).withOwner(player)
            val utility2 = Utility("Water Works", 28).withOwner(player)

            utility1.calculateRent(listOf(utility1, utility2), diceRoll = 7) shouldBe 70 // 7 × 10
            utility2.calculateRent(listOf(utility1, utility2), diceRoll = 12) shouldBe 120 // 12 × 10
        }

        it("should only count utilities owned by the same player") {
            val player1 = Player("Alice", AlwaysBuyStrategy())
            val player2 = Player("Bob", AlwaysBuyStrategy())

            val utility1 = Utility("Electric Company", 12).withOwner(player1)
            val utility2 = Utility("Water Works", 28).withOwner(player2)

            // player1は1つだけ所有
            utility1.calculateRent(listOf(utility1, utility2), diceRoll = 7) shouldBe 28 // 7 × 4
        }
    }
})
