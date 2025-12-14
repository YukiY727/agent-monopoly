package com.monopoly.domain.strategy

import com.monopoly.domain.model.property.ColorGroup
import com.monopoly.domain.model.property.Property
import com.monopoly.domain.model.property.PropertyBuildings
import com.monopoly.domain.model.property.PropertyRent
import com.monopoly.domain.model.property.RailroadProperty
import com.monopoly.domain.model.property.StreetProperty
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class BalancedStrategyTest : StringSpec({
    // Helper function to create test property
    fun createTestProperty(
        price: Int = 60,
        baseRent: Int = 2,
        houseCount: Int = 0,
    ): StreetProperty =
        StreetProperty(
            name = "Mediterranean Avenue",
            position = 1,
            price = price,
            rent =
                PropertyRent(
                    base = baseRent,
                    withHouse1 = baseRent * 5,
                    withHouse2 = baseRent * 15,
                    withHouse3 = baseRent * 45,
                    withHouse4 = baseRent * 80,
                    withHotel = baseRent * 125,
                ),
            houseCost = 50,
            hotelCost = 50,
            colorGroup = ColorGroup.BROWN,
            buildings = PropertyBuildings(houseCount = houseCount),
        )

    // Helper function to create railroad property
    fun createRailroadProperty(price: Int = 200): RailroadProperty =
        RailroadProperty(
            name = "Reading Railroad",
            position = 5,
            price = price,
        )

    // TC-STR-032: BalancedStrategyがバランスよくプロパティを購入
    "should buy properties with balanced approach" {
        // Given
        val strategy = BalancedStrategy()

        // When & Then
        // ROIが良く、価格が所持金の50%以下なら購入
        // ROI = 4 / 100 = 0.04 > 0.02 ✓
        // Price = 100 <= 300 * 0.5 = 150 ✓
        strategy.shouldBuy(createTestProperty(price = 100, baseRent = 4), 300) shouldBe true

        // ROIが悪い場合は購入しない
        // ROI = 1 / 100 = 0.01 < 0.02
        strategy.shouldBuy(createTestProperty(price = 100, baseRent = 1), 300) shouldBe false

        // ROIは良いが価格が所持金の50%を超える場合は購入しない
        // ROI = 4 / 100 = 0.04 > 0.02 ✓
        // Price = 100 > 150 * 0.5 = 75
        strategy.shouldBuy(createTestProperty(price = 100, baseRent = 4), 150) shouldBe false

        // 所持金が不足していたら購入しない
        strategy.shouldBuy(createTestProperty(price = 100, baseRent = 4), 50) shouldBe false
    }

    // TC-STR-032b: BalancedStrategyがStreetProperty以外を購入しない
    "should not buy non-street properties" {
        // Given
        val strategy = BalancedStrategy()
        val railroadProperty: Property = createRailroadProperty(price = 200)

        // When & Then
        // StreetProperty以外は購入しない
        strategy.shouldBuy(railroadProperty, 500) shouldBe false
    }

    // TC-STR-033: BalancedStrategyがバランスよく家を建設
    "should build house with balanced approach" {
        // Given
        val strategy = BalancedStrategy()

        // When & Then
        // レント増加ROIが良く、コストが所持金の50%以下なら建設
        // Rent increase = 50 - 10 = 40
        // ROI = 40 / 50 = 0.8 > 0.15 ✓
        // Cost = 50 <= 200 * 0.5 = 100 ✓
        val property1: StreetProperty = createTestProperty(price = 200, baseRent = 10, houseCount = 0)
        strategy.shouldBuildHouse(property1, 200) shouldBe true

        // ROIが悪い場合は建設しない
        // Rent increase = 10 - 2 = 8
        // ROI = 8 / 50 = 0.16 > 0.15 ✓ (ギリギリOK)
        val property2: StreetProperty = createTestProperty(price = 60, baseRent = 2, houseCount = 0)
        strategy.shouldBuildHouse(property2, 200) shouldBe true

        // さらにROIが悪い場合
        // Rent increase = 7 - 1.4 = 5.6 (実際には整数なので 7 - 1 = 6)
        // ROI = 6 / 50 = 0.12 < 0.15
        val property3: StreetProperty = createTestProperty(price = 60, baseRent = 1, houseCount = 0)
        strategy.shouldBuildHouse(property3, 200) shouldBe false

        // コストが所持金の50%を超える場合は建設しない
        strategy.shouldBuildHouse(property1, 90) shouldBe false

        // 所持金が不足していたら建設しない
        strategy.shouldBuildHouse(property1, 40) shouldBe false
    }

    // TC-STR-033b: BalancedStrategyが異なる家数での建設を判断
    "should build house with different house counts" {
        // Given
        val strategy = BalancedStrategy()

        // When & Then
        // 1家 → 2家: レント増加 = 150 - 50 = 100, ROI = 100 / 50 = 2.0 >= 0.15
        // Cost = 50 <= 200 * 0.5 = 100 ✓
        val property1House: StreetProperty = createTestProperty(price = 200, baseRent = 10, houseCount = 1)
        strategy.shouldBuildHouse(property1House, 200) shouldBe true

        // 2家 → 3家: レント増加 = 450 - 150 = 300, ROI = 300 / 50 = 6.0 >= 0.15
        val property2Houses: StreetProperty = createTestProperty(price = 200, baseRent = 10, houseCount = 2)
        strategy.shouldBuildHouse(property2Houses, 200) shouldBe true

        // 3家 → 4家: レント増加 = 800 - 450 = 350, ROI = 350 / 50 = 7.0 >= 0.15
        val property3Houses: StreetProperty = createTestProperty(price = 200, baseRent = 10, houseCount = 3)
        strategy.shouldBuildHouse(property3Houses, 200) shouldBe true
    }

    // TC-STR-034: BalancedStrategyがバランスよくホテルを建設
    "should build hotel with balanced approach" {
        // Given
        val strategy = BalancedStrategy()

        // When & Then
        // ホテル建設でレント大幅アップ
        // Rent increase = 1250 - 800 = 450
        // ROI = 450 / 50 = 9.0 > 0.15 ✓
        // Cost = 50 <= 200 * 0.5 = 100 ✓
        val property: StreetProperty = createTestProperty(price = 200, baseRent = 10, houseCount = 4)
        strategy.shouldBuildHotel(property, 200) shouldBe true

        // コストが所持金の50%を超える場合は建設しない
        strategy.shouldBuildHotel(property, 90) shouldBe false

        // 所持金が不足していたら建設しない
        strategy.shouldBuildHotel(property, 40) shouldBe false
    }

    // TC-STR-035: BalancedStrategyが監獄脱出をバランスよく判断
    "should pay jail fine when money is sufficient" {
        // Given
        val strategy = BalancedStrategy()

        // When & Then
        // 所持金が一定額以上あれば支払う
        strategy.shouldPayToEscapeJail(300) shouldBe true

        // 所持金が不足していれば支払わない
        strategy.shouldPayToEscapeJail(299) shouldBe false
    }

    // TC-STR-036: BalancedStrategyがオークションでバランスよく入札
    "should bid balanced in auction" {
        // Given
        val strategy = BalancedStrategy()

        // When & Then
        // バランスの取れた入札（価格の50%）
        val property: Property = createTestProperty(price = 100, baseRent = 4)
        val bid1: Int? = strategy.decideAuctionBid(property, null, 500)
        bid1 shouldBe 50 // 100 * 0.5 = 50

        // 現在の入札額より高く入札
        val bid2: Int? = strategy.decideAuctionBid(property, 40, 500)
        bid2 shouldBe 50 // max(41, 50) = 50

        // 現在の入札額が既に高い場合はパス
        val bid3: Int? = strategy.decideAuctionBid(property, 55, 500)
        bid3 shouldBe null

        // 資金不足の場合はパス
        val bid4: Int? = strategy.decideAuctionBid(property, null, 40)
        bid4 shouldBe null
    }
})
