package com.monopoly.domain.strategy

import com.monopoly.domain.model.property.ColorGroup
import com.monopoly.domain.model.property.Property
import com.monopoly.domain.model.property.PropertyBuildings
import com.monopoly.domain.model.property.PropertyRent
import com.monopoly.domain.model.property.StreetProperty
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.random.Random

class RandomStrategyTest : StringSpec({
    // Helper function to create test property
    fun createTestProperty(houseCount: Int = 0): StreetProperty =
        StreetProperty(
            name = "Mediterranean Avenue",
            position = 1,
            price = 60,
            rent =
                PropertyRent(
                    base = 2,
                    withHouse1 = 10,
                    withHouse2 = 30,
                    withHouse3 = 90,
                    withHouse4 = 160,
                    withHotel = 250,
                ),
            houseCost = 50,
            hotelCost = 50,
            colorGroup = ColorGroup.BROWN,
            buildings = PropertyBuildings(houseCount = houseCount),
        )

    // TC-STR-001: RandomStrategyが購入判断をランダムに行う
    "should randomly decide to buy property" {
        // Given
        val random = Random(42) // 固定シード
        val strategy = RandomStrategy(random)
        val property: Property = createTestProperty()

        // When
        val decision1: Boolean = strategy.shouldBuy(property, 200)
        val decision2: Boolean = strategy.shouldBuy(property, 200)
        val decision3: Boolean = strategy.shouldBuy(property, 200)

        // Then
        // シード42の場合、最初の3回のnextBoolean()の結果を検証
        // （実際の値は実装後に調整）
        val decisions: List<Boolean> = listOf(decision1, decision2, decision3)
        decisions.count { it } shouldNotBe 0 // 少なくとも1回はtrue
        decisions.count { !it } shouldNotBe 0 // 少なくとも1回はfalse
    }

    // TC-STR-002: RandomStrategyが家建設をランダムに判断
    "should randomly decide to build house" {
        // Given
        val random = Random(123)
        val strategy = RandomStrategy(random)
        val property: StreetProperty = createTestProperty()

        // When
        val decision1: Boolean = strategy.shouldBuildHouse(property, 500)
        val decision2: Boolean = strategy.shouldBuildHouse(property, 500)
        val decision3: Boolean = strategy.shouldBuildHouse(property, 500)

        // Then
        val decisions: List<Boolean> = listOf(decision1, decision2, decision3)
        decisions.count { it } shouldNotBe 0
        decisions.count { !it } shouldNotBe 0
    }

    // TC-STR-003: RandomStrategyがホテル建設をランダムに判断
    "should randomly decide to build hotel" {
        // Given
        val random = Random(456)
        val strategy = RandomStrategy(random)
        val property: StreetProperty = createTestProperty(houseCount = 4)

        // When
        val decision1: Boolean = strategy.shouldBuildHotel(property, 500)
        val decision2: Boolean = strategy.shouldBuildHotel(property, 500)
        val decision3: Boolean = strategy.shouldBuildHotel(property, 500)

        // Then
        val decisions: List<Boolean> = listOf(decision1, decision2, decision3)
        decisions.count { it } shouldNotBe 0
        decisions.count { !it } shouldNotBe 0
    }

    // TC-STR-004: RandomStrategyが監獄脱出をランダムに判断
    "should randomly decide to pay jail fine" {
        // Given
        val random = Random(789)
        val strategy = RandomStrategy(random)

        // When
        val decision1: Boolean = strategy.shouldPayToEscapeJail(500)
        val decision2: Boolean = strategy.shouldPayToEscapeJail(500)
        val decision3: Boolean = strategy.shouldPayToEscapeJail(500)

        // Then
        val decisions: List<Boolean> = listOf(decision1, decision2, decision3)
        decisions.count { it } shouldNotBe 0
        decisions.count { !it } shouldNotBe 0
    }

    // TC-STR-005: RandomStrategyがオークション入札をランダムに判断
    "should randomly decide to bid in auction" {
        // Given
        val random = Random(999)
        val strategy = RandomStrategy(random)
        val property: Property = createTestProperty()

        // When - 複数回呼び出す
        val bids: List<Int?> =
            (1..10).map {
                strategy.decideAuctionBid(property, null, 1000)
            }

        // Then
        // nullとnon-nullの両方が含まれること
        bids.count { it == null } shouldNotBe 0 // パスする場合がある
        bids.count { it != null } shouldNotBe 0 // 入札する場合がある
    }

    // TC-STR-006: RandomStrategyの入札額が妥当な範囲内
    "auction bid should be within reasonable range when bidding" {
        // Given
        val random = Random(111)
        val strategy = RandomStrategy(random)
        val property: Property = createTestProperty()
        val currentMoney = 1000

        // When - 入札する場合のみ検証
        val bids: List<Int> =
            (1..100).mapNotNull {
                strategy.decideAuctionBid(property, null, currentMoney)
            }

        // Then
        bids.forEach { bid ->
            bid shouldBeGreaterThan 0
            bid shouldBeLessThanOrEqual currentMoney
        }
    }

    // TC-STR-007: RandomStrategyが既存入札より高い額を入札
    "should bid higher than current bid when bidding" {
        // Given
        val random = Random(222)
        val strategy = RandomStrategy(random)
        val property: Property = createTestProperty()
        val currentBid = 50
        val currentMoney = 1000

        // When
        val bids: List<Int> =
            (1..100).mapNotNull {
                strategy.decideAuctionBid(property, currentBid, currentMoney)
            }

        // Then
        bids.forEach { bid ->
            bid shouldBeGreaterThan currentBid
            bid shouldBeLessThanOrEqual currentMoney
        }
    }

    // TC-STR-008: 資金不足時はオークションでパス
    "should pass auction when insufficient funds" {
        // Given
        val random = Random(333)
        val strategy = RandomStrategy(random)
        val property: Property = createTestProperty()
        val currentBid = 100
        val currentMoney = 50 // 現在の入札より少ない

        // When
        val bid: Int? = strategy.decideAuctionBid(property, currentBid, currentMoney)

        // Then
        bid shouldBe null
    }
})
