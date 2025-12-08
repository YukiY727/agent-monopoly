package com.monopoly.domain.strategy

import com.monopoly.domain.model.property.ColorGroup
import com.monopoly.domain.model.property.Property
import com.monopoly.domain.model.property.PropertyBuildings
import com.monopoly.domain.model.property.PropertyRent
import com.monopoly.domain.model.property.StreetProperty
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ConservativeStrategyTest : StringSpec({
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

    // TC-STR-009: ConservativeStrategyが最低保持現金を確保して購入判断
    "should buy property only if minimum cash reserve is maintained" {
        // Given
        val minCashReserve = 400
        val strategy = ConservativeStrategy(minCashReserve = minCashReserve)
        val property: Property = createTestProperty(price = 60)

        // When & Then
        // 購入後も最低現金を確保できる場合は購入
        strategy.shouldBuy(property, 500) shouldBe true // 500 - 60 = 440 >= 400

        // 購入すると最低現金を下回る場合は購入しない
        strategy.shouldBuy(property, 450) shouldBe false // 450 - 60 = 390 < 400

        // ちょうど最低現金になる場合は購入
        strategy.shouldBuy(property, 460) shouldBe true // 460 - 60 = 400 >= 400
    }

    // TC-STR-010: ConservativeStrategyが最低保持現金を確保して家建設判断
    "should build house only if minimum cash reserve is maintained" {
        // Given
        val minCashReserve = 400
        val strategy = ConservativeStrategy(minCashReserve = minCashReserve)
        val property: StreetProperty = createTestProperty()
        val houseCost = 50

        // When & Then
        // 建設後も最低現金を確保できる場合は建設
        strategy.shouldBuildHouse(property, 500) shouldBe true // 500 - 50 = 450 >= 400

        // 建設すると最低現金を下回る場合は建設しない
        strategy.shouldBuildHouse(property, 440) shouldBe false // 440 - 50 = 390 < 400

        // ちょうど最低現金になる場合は建設
        strategy.shouldBuildHouse(property, 450) shouldBe true // 450 - 50 = 400 >= 400
    }

    // TC-STR-011: ConservativeStrategyが最低保持現金を確保してホテル建設判断
    "should build hotel only if minimum cash reserve is maintained" {
        // Given
        val minCashReserve = 400
        val strategy = ConservativeStrategy(minCashReserve = minCashReserve)
        val property: StreetProperty = createTestProperty(houseCount = 4)
        val hotelCost = 50

        // When & Then
        // 建設後も最低現金を確保できる場合は建設
        strategy.shouldBuildHotel(property, 500) shouldBe true // 500 - 50 = 450 >= 400

        // 建設すると最低現金を下回る場合は建設しない
        strategy.shouldBuildHotel(property, 440) shouldBe false // 440 - 50 = 390 < 400
    }

    // TC-STR-012: ConservativeStrategyが監獄脱出を慎重に判断
    "should pay jail fine only if sufficient cash remains" {
        // Given
        val minCashReserve = 400
        val strategy = ConservativeStrategy(minCashReserve = minCashReserve)
        val jailFine = 50

        // When & Then
        // 支払後も最低現金を確保できる場合は支払う
        strategy.shouldPayToEscapeJail(500) shouldBe true // 500 - 50 = 450 >= 400

        // 支払うと最低現金を下回る場合は支払わない
        strategy.shouldPayToEscapeJail(440) shouldBe false // 440 - 50 = 390 < 400

        // ちょうど最低現金になる場合は支払う
        strategy.shouldPayToEscapeJail(450) shouldBe true // 450 - 50 = 400 >= 400
    }

    // TC-STR-013: ConservativeStrategyがオークションで控えめに入札
    "should bid conservatively in auction" {
        // Given
        val minCashReserve = 400
        val strategy = ConservativeStrategy(minCashReserve = minCashReserve)
        val property: Property = createTestProperty(price = 60)

        // When & Then
        // 最低現金を確保できない場合はパス
        val bid1: Int? = strategy.decideAuctionBid(property, null, 420)
        bid1 shouldBe null // 420 - 30 = 390 < 400なので入札しない

        // 十分な資金がある場合のみ入札（プロパティ価格の50%程度）
        val bid2: Int? = strategy.decideAuctionBid(property, null, 500)
        bid2 shouldBe 30 // 価格60の50% = 30

        // 現在の入札額より高く、かつ最低現金を確保
        val bid3: Int? = strategy.decideAuctionBid(property, 20, 500)
        bid3 shouldBe 30 // max(21, 30) = 30

        // 現在の入札額が既に高い場合はパス
        val bid4: Int? = strategy.decideAuctionBid(property, 80, 500)
        bid4 shouldBe null // 80 > 60 * 0.5なのでパス
    }

    // TC-STR-014: 最低現金確保のため資金不足時はオークションパス
    "should pass auction when would violate minimum cash reserve" {
        // Given
        val minCashReserve = 400
        val strategy = ConservativeStrategy(minCashReserve = minCashReserve)
        val property: Property = createTestProperty(price = 60)
        val currentMoney = 420

        // When
        val bid: Int? = strategy.decideAuctionBid(property, null, currentMoney)

        // Then
        // 入札すると最低現金を下回るのでパス
        bid shouldBe null
    }

    // TC-STR-015: デフォルト最低現金は400
    "should use default minimum cash reserve of 400" {
        // Given
        val strategy = ConservativeStrategy() // デフォルト
        val property: Property = createTestProperty(price = 60)

        // When & Then
        strategy.shouldBuy(property, 460) shouldBe true // 460 - 60 = 400
        strategy.shouldBuy(property, 450) shouldBe false // 450 - 60 = 390 < 400
    }
})
