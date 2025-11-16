package com.monopoly.domain.model

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

/**
 * Railroad家賃計算のテスト
 *
 * Phase 15: 鉄道の特殊ルール実装
 */
class RailroadRentTest : DescribeSpec({
    describe("Railroad rent calculation") {
        it("should charge $25 for 1 railroad") {
            val player = Player("Alice", AlwaysBuyStrategy())
            val railroad = Railroad("Reading Railroad", 5).withOwner(player)

            railroad.calculateRent(listOf(railroad)) shouldBe 25
        }

        it("should charge $50 for 2 railroads") {
            val player = Player("Alice", AlwaysBuyStrategy())
            val railroad1 = Railroad("Reading Railroad", 5).withOwner(player)
            val railroad2 = Railroad("Pennsylvania Railroad", 15).withOwner(player)

            railroad1.calculateRent(listOf(railroad1, railroad2)) shouldBe 50
            railroad2.calculateRent(listOf(railroad1, railroad2)) shouldBe 50
        }

        it("should charge $100 for 3 railroads") {
            val player = Player("Alice", AlwaysBuyStrategy())
            val railroad1 = Railroad("Reading Railroad", 5).withOwner(player)
            val railroad2 = Railroad("Pennsylvania Railroad", 15).withOwner(player)
            val railroad3 = Railroad("B&O Railroad", 25).withOwner(player)

            railroad1.calculateRent(listOf(railroad1, railroad2, railroad3)) shouldBe 100
        }

        it("should charge $200 for 4 railroads") {
            val player = Player("Alice", AlwaysBuyStrategy())
            val railroad1 = Railroad("Reading Railroad", 5).withOwner(player)
            val railroad2 = Railroad("Pennsylvania Railroad", 15).withOwner(player)
            val railroad3 = Railroad("B&O Railroad", 25).withOwner(player)
            val railroad4 = Railroad("Short Line Railroad", 35).withOwner(player)

            railroad1.calculateRent(listOf(railroad1, railroad2, railroad3, railroad4)) shouldBe 200
        }

        it("should only count railroads owned by the same player") {
            val player1 = Player("Alice", AlwaysBuyStrategy())
            val player2 = Player("Bob", AlwaysBuyStrategy())

            val railroad1 = Railroad("Reading Railroad", 5).withOwner(player1)
            val railroad2 = Railroad("Pennsylvania Railroad", 15).withOwner(player2)

            // player1は1つだけ所有
            railroad1.calculateRent(listOf(railroad1, railroad2)) shouldBe 25
        }
    }
})
