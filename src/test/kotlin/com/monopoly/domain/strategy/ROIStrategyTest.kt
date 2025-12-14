package com.monopoly.domain.strategy

import com.monopoly.domain.model.property.ColorGroup
import com.monopoly.domain.model.property.Property
import com.monopoly.domain.model.property.PropertyBuildings
import com.monopoly.domain.model.property.PropertyRent
import com.monopoly.domain.model.property.RailroadProperty
import com.monopoly.domain.model.property.StreetProperty
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ROIStrategyTest : StringSpec({
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

    // TC-STR-027: ROIStrategyが投資効率の良いプロパティを購入
    "should buy properties with good ROI" {
        // Given
        val strategy = ROIStrategy()

        // When & Then
        // 高ROI（baseRent / price >= 0.03）なら購入
        // 60 / 2000 = 0.03 → ちょうど閾値
        strategy.shouldBuy(createTestProperty(price = 2000, baseRent = 60), 3000) shouldBe true

        // 低ROI（baseRent / price < 0.03）なら購入しない
        // 59 / 2000 = 0.0295 < 0.03
        strategy.shouldBuy(createTestProperty(price = 2000, baseRent = 59), 3000) shouldBe false

        // ROIが良くても所持金が不足していたら購入しない
        strategy.shouldBuy(createTestProperty(price = 100, baseRent = 5), 50) shouldBe false
    }

    // TC-STR-027b: ROIStrategyがStreetProperty以外を購入しない
    "should not buy non-street properties" {
        // Given
        val strategy = ROIStrategy()
        val railroadProperty: Property = createRailroadProperty(price = 200)

        // When & Then
        // StreetProperty以外は購入しない
        strategy.shouldBuy(railroadProperty, 500) shouldBe false
    }

    // TC-STR-028: ROIStrategyが投資効率の良い家建設を判断
    "should build house with good ROI" {
        // Given
        val strategy = ROIStrategy()

        // When & Then
        // 家を建設するとレントが上がる（0 → 1家で baseRent * 5）
        // レント増加 = 10 - 2 = 8
        // ROI = 8 / 50 = 0.16 < 0.2なので建設しない
        val property1: StreetProperty = createTestProperty(price = 60, baseRent = 2, houseCount = 0)
        strategy.shouldBuildHouse(property1, 100) shouldBe false

        // レントが高いプロパティでは建設する
        // レント増加 = 50 - 10 = 40
        // ROI = 40 / 50 = 0.8 >= 0.2なので建設
        val property2: StreetProperty = createTestProperty(price = 200, baseRent = 10, houseCount = 0)
        strategy.shouldBuildHouse(property2, 100) shouldBe true

        // 所持金が不足していたら建設しない
        strategy.shouldBuildHouse(property2, 30) shouldBe false
    }

    // TC-STR-028b: ROIStrategyが異なる家数での建設を判断
    "should build house with different house counts" {
        // Given
        val strategy = ROIStrategy()

        // When & Then
        // 1家 → 2家: レント増加 = 150 - 50 = 100, ROI = 100 / 50 = 2.0 >= 0.2
        val property1House: StreetProperty = createTestProperty(price = 200, baseRent = 10, houseCount = 1)
        strategy.shouldBuildHouse(property1House, 100) shouldBe true

        // 2家 → 3家: レント増加 = 450 - 150 = 300, ROI = 300 / 50 = 6.0 >= 0.2
        val property2Houses: StreetProperty = createTestProperty(price = 200, baseRent = 10, houseCount = 2)
        strategy.shouldBuildHouse(property2Houses, 100) shouldBe true

        // 3家 → 4家: レント増加 = 800 - 450 = 350, ROI = 350 / 50 = 7.0 >= 0.2
        val property3Houses: StreetProperty = createTestProperty(price = 200, baseRent = 10, houseCount = 3)
        strategy.shouldBuildHouse(property3Houses, 100) shouldBe true
    }

    // TC-STR-029: ROIStrategyが投資効率の良いホテル建設を判断
    "should build hotel with good ROI" {
        // Given
        val strategy = ROIStrategy()

        // When & Then
        // ホテル建設でレント大幅アップ（4家 → ホテルで baseRent * 125 - 80 = 45 * baseRent）
        // レント増加 = 1250 - 800 = 450
        // ROI = 450 / 50 = 9.0 >= 0.2なので建設
        val property: StreetProperty = createTestProperty(price = 200, baseRent = 10, houseCount = 4)
        strategy.shouldBuildHotel(property, 100) shouldBe true

        // 所持金が不足していたら建設しない
        strategy.shouldBuildHotel(property, 30) shouldBe false
    }

    // TC-STR-030: ROIStrategyが監獄脱出を合理的に判断
    "should pay jail fine when cost-effective" {
        // Given
        val strategy = ROIStrategy()

        // When & Then
        // 所持金が一定額以上あれば支払う
        strategy.shouldPayToEscapeJail(150) shouldBe true

        // 所持金が不足していれば支払わない
        strategy.shouldPayToEscapeJail(149) shouldBe false
    }

    // TC-STR-031: ROIStrategyがオークションでROIに基づいて入札
    "should bid based on ROI in auction" {
        // Given
        val strategy = ROIStrategy()

        // When & Then
        // 高ROI（0.05 = 5 / 100）なら積極的に入札（価格の70%）
        val highROIProperty: Property = createTestProperty(price = 100, baseRent = 5)
        val bid1: Int? = strategy.decideAuctionBid(highROIProperty, null, 500)
        bid1 shouldBe 70 // 100 * 0.7 = 70

        // 低ROI（0.02 = 2 / 100）なら消極的（価格の40%）
        val lowROIProperty: Property = createTestProperty(price = 100, baseRent = 2)
        val bid2: Int? = strategy.decideAuctionBid(lowROIProperty, null, 500)
        bid2 shouldBe 40 // 100 * 0.4 = 40

        // 現在の入札額より高く入札
        val bid3: Int? = strategy.decideAuctionBid(highROIProperty, 60, 500)
        bid3 shouldBe 70 // max(61, 70) = 70

        // 現在の入札額が既に高い場合はパス
        val bid4: Int? = strategy.decideAuctionBid(highROIProperty, 75, 500)
        bid4 shouldBe null

        // 資金不足の場合はパス
        val bid5: Int? = strategy.decideAuctionBid(highROIProperty, null, 60)
        bid5 shouldBe null
    }

    // TC-STR-031b: ROIStrategyがStreetProperty以外でオークション入札
    "should bid conservatively for non-street properties in auction" {
        // Given
        val strategy = ROIStrategy()
        val railroadProperty: Property = createRailroadProperty(price = 200)

        // When & Then
        // StreetProperty以外は低ROI扱い（価格の40%）
        val bid: Int? = strategy.decideAuctionBid(railroadProperty, null, 500)
        bid shouldBe 80 // 200 * 0.4 = 80
    }
})
