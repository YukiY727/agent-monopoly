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

class AggressiveStrategyTest : StringSpec({
    // Helper function to create test property
    fun createTestProperty(price: Int = 60, houseCount: Int = 0): StreetProperty =
        StreetProperty(
            name = "Mediterranean Avenue",
            position = 1,
            price = price,
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

    // TC-STR-016: AggressiveStrategyが積極的に購入判断
    "should buy property aggressively" {
        // Given
        val strategy = AggressiveStrategy()
        val property: Property = createTestProperty(price = 60)

        // When & Then
        // 価格が所持金の80%以下なら購入
        strategy.shouldBuy(property, 100) shouldBe true // 60 <= 100 * 0.8 = 80

        // 価格が所持金の80%を超えたら購入しない
        strategy.shouldBuy(property, 70) shouldBe false // 60 > 70 * 0.8 = 56

        // ちょうど境界の場合
        strategy.shouldBuy(property, 75) shouldBe true // 60 == 75 * 0.8 = 60
    }

    // TC-STR-017: AggressiveStrategyが積極的に家建設判断
    "should build house aggressively" {
        // Given
        val strategy = AggressiveStrategy()
        val property: StreetProperty = createTestProperty()
        val houseCost = 50

        // When & Then
        // コストが所持金の70%以下なら建設
        strategy.shouldBuildHouse(property, 100) shouldBe true // 50 <= 100 * 0.7 = 70

        // コストが所持金の70%を超えたら建設しない
        strategy.shouldBuildHouse(property, 60) shouldBe false // 50 > 60 * 0.7 = 42

        // ちょうど境界の場合（72 * 0.7 = 50.4 >= 50）
        strategy.shouldBuildHouse(property, 72) shouldBe true
    }

    // TC-STR-018: AggressiveStrategyが積極的にホテル建設判断
    "should build hotel aggressively" {
        // Given
        val strategy = AggressiveStrategy()
        val property: StreetProperty = createTestProperty(houseCount = 4)
        val hotelCost = 50

        // When & Then
        // コストが所持金の70%以下なら建設
        strategy.shouldBuildHotel(property, 100) shouldBe true // 50 <= 100 * 0.7 = 70

        // コストが所持金の70%を超えたら建設しない
        strategy.shouldBuildHotel(property, 60) shouldBe false // 50 > 60 * 0.7 = 42
    }

    // TC-STR-019: AggressiveStrategyが積極的に監獄脱出
    "should pay jail fine aggressively" {
        // Given
        val strategy = AggressiveStrategy()

        // When & Then
        // 所持金が最低額以上あれば支払う
        strategy.shouldPayToEscapeJail(100) shouldBe true

        // 所持金が最低額未満なら支払わない
        strategy.shouldPayToEscapeJail(99) shouldBe false

        // ちょうど境界の場合
        strategy.shouldPayToEscapeJail(100) shouldBe true
    }

    // TC-STR-020: AggressiveStrategyがオークションで積極的に入札
    "should bid aggressively in auction" {
        // Given
        val strategy = AggressiveStrategy()
        val property: Property = createTestProperty(price = 100)

        // When & Then
        // プロパティ価格の80%まで入札
        val bid1: Int? = strategy.decideAuctionBid(property, null, 500)
        bid1 shouldBe 80 // 価格100の80% = 80

        // 現在の入札額より高く入札
        val bid2: Int? = strategy.decideAuctionBid(property, 50, 500)
        bid2 shouldBe 80 // max(51, 80) = 80

        // 現在の入札額が既に高い場合はパス
        val bid3: Int? = strategy.decideAuctionBid(property, 85, 500)
        bid3 shouldBe null // 85 > 80なのでパス

        // 資金不足の場合はパス
        val bid4: Int? = strategy.decideAuctionBid(property, null, 50)
        bid4 shouldBe null // 80 > 50なので入札できない
    }

    // TC-STR-021: 積極的入札額が所持金を超える場合はパス
    "should pass auction when aggressive bid exceeds current money" {
        // Given
        val strategy = AggressiveStrategy()
        val property: Property = createTestProperty(price = 100)

        // When
        val bid: Int? = strategy.decideAuctionBid(property, null, 70)

        // Then
        // 積極的入札額（80）が所持金（70）を超えるのでパス
        bid shouldBe null
    }
})
