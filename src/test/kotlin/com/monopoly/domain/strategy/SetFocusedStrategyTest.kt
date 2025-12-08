package com.monopoly.domain.strategy

import com.monopoly.domain.model.property.ColorGroup
import com.monopoly.domain.model.property.Property
import com.monopoly.domain.model.property.PropertyBuildings
import com.monopoly.domain.model.property.PropertyRent
import com.monopoly.domain.model.property.StreetProperty
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SetFocusedStrategyTest : StringSpec({
    // Helper function to create test property
    fun createTestProperty(
        price: Int = 60,
        houseCount: Int = 0,
    ): StreetProperty =
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

    // TC-STR-022: SetFocusedStrategyが安いプロパティを優先的に購入
    "should buy cheap properties to complete sets" {
        // Given
        val strategy = SetFocusedStrategy()

        // When & Then
        // 安いプロパティ（<= 200）は積極的に購入
        strategy.shouldBuy(createTestProperty(price = 100), 200) shouldBe true
        strategy.shouldBuy(createTestProperty(price = 200), 300) shouldBe true

        // 高いプロパティでも所持金の60%以下なら購入
        strategy.shouldBuy(createTestProperty(price = 300), 600) shouldBe true // 300 <= 600 * 0.6 = 360

        // 高いプロパティで所持金の60%を超えたら購入しない
        strategy.shouldBuy(createTestProperty(price = 350), 500) shouldBe false // 350 > 500 * 0.6 = 300
    }

    // TC-STR-023: SetFocusedStrategyが建設を優先
    "should build to maximize set value" {
        // Given
        val strategy = SetFocusedStrategy()
        val property: StreetProperty = createTestProperty()
        val houseCost = 50

        // When & Then
        // コストが所持金の60%以下なら建設
        strategy.shouldBuildHouse(property, 100) shouldBe true // 50 <= 100 * 0.6 = 60

        // コストが所持金の60%を超えたら建設しない
        strategy.shouldBuildHouse(property, 80) shouldBe false // 50 > 80 * 0.6 = 48
    }

    // TC-STR-024: SetFocusedStrategyがホテル建設を優先
    "should build hotel to maximize set value" {
        // Given
        val strategy = SetFocusedStrategy()
        val property: StreetProperty = createTestProperty(houseCount = 4)
        val hotelCost = 50

        // When & Then
        // コストが所持金の60%以下なら建設
        strategy.shouldBuildHotel(property, 100) shouldBe true // 50 <= 100 * 0.6 = 60

        // コストが所持金の60%を超えたら建設しない
        strategy.shouldBuildHotel(property, 80) shouldBe false // 50 > 80 * 0.6 = 48
    }

    // TC-STR-025: SetFocusedStrategyが監獄脱出を判断
    "should pay jail fine when sufficient funds" {
        // Given
        val strategy = SetFocusedStrategy()

        // When & Then
        // 所持金が一定額以上あれば支払う
        strategy.shouldPayToEscapeJail(200) shouldBe true

        // 所持金が一定額未満なら支払わない
        strategy.shouldPayToEscapeJail(199) shouldBe false
    }

    // TC-STR-026: SetFocusedStrategyがオークションで入札
    "should bid moderately in auction to acquire set properties" {
        // Given
        val strategy = SetFocusedStrategy()
        val cheapProperty: Property = createTestProperty(price = 100)
        val expensiveProperty: Property = createTestProperty(price = 400)

        // When & Then
        // 安いプロパティには積極的に入札（価格の60%）
        val bid1: Int? = strategy.decideAuctionBid(cheapProperty, null, 500)
        bid1 shouldBe 60 // 100 * 0.6 = 60

        // 高いプロパティでも入札（価格の60%）
        val bid2: Int? = strategy.decideAuctionBid(expensiveProperty, null, 500)
        bid2 shouldBe 240 // 400 * 0.6 = 240

        // 現在の入札額より高く入札
        val bid3: Int? = strategy.decideAuctionBid(cheapProperty, 50, 500)
        bid3 shouldBe 60 // max(51, 60) = 60

        // 現在の入札額が既に高い場合はパス
        val bid4: Int? = strategy.decideAuctionBid(cheapProperty, 70, 500)
        bid4 shouldBe null // 70 > 60なのでパス

        // 資金不足の場合はパス
        val bid5: Int? = strategy.decideAuctionBid(expensiveProperty, null, 200)
        bid5 shouldBe null // 240 > 200なので入札できない
    }
})
