package com.monopoly.domain.strategy

import com.monopoly.domain.model.property.ColorGroup
import com.monopoly.domain.model.property.Property
import com.monopoly.domain.model.PropertyTestFixtures
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class AlwaysPlayerStrategyTest : StringSpec({
    "should return true when player has enough money" {
        val strategy = AlwaysPlayerStrategy()
        val property: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                baseRent = 2,
                colorGroup = ColorGroup.BROWN,
            )
        val currentMoney = 100

        val result: Boolean = strategy.shouldBuy(property, currentMoney)

        result shouldBe true
    }

    "should return false when player does not have enough money" {
        val strategy = AlwaysPlayerStrategy()
        val property: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                baseRent = 2,
                colorGroup = ColorGroup.BROWN,
            )
        val currentMoney = 50

        val result: Boolean = strategy.shouldBuy(property, currentMoney)

        result shouldBe false
    }

    "should return true when player has exactly enough money" {
        val strategy = AlwaysPlayerStrategy()
        val property: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                baseRent = 2,
                colorGroup = ColorGroup.BROWN,
            )
        val currentMoney = 60

        val result: Boolean = strategy.shouldBuy(property, currentMoney)

        result shouldBe true
    }

    // Auction bid tests with default parameters
    "should bid 30% of property price when no current bid (default)" {
        val strategy = AlwaysPlayerStrategy()
        val property: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Mediterranean Avenue",
                position = 1,
                price = 100,
                baseRent = 2,
                colorGroup = ColorGroup.BROWN,
            )
        val currentMoney = 1500

        val bid: Int? = strategy.decideAuctionBid(property, null, currentMoney)

        bid shouldBe 30 // 100 * 0.3
    }

    "should bid current bid + 10 when there is a current bid (default)" {
        val strategy = AlwaysPlayerStrategy()
        val property: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Mediterranean Avenue",
                position = 1,
                price = 100,
                baseRent = 2,
                colorGroup = ColorGroup.BROWN,
            )
        val currentMoney = 1500

        val bid: Int? = strategy.decideAuctionBid(property, 50, currentMoney)

        bid shouldBe 60 // 50 + 10
    }

    "should pass when initial bid would exceed 80% of money (default)" {
        val strategy = AlwaysPlayerStrategy()
        val property: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Mediterranean Avenue",
                position = 1,
                price = 500,  // High price property
                baseRent = 2,
                colorGroup = ColorGroup.BROWN,
            )
        val currentMoney = 100

        // maxBid = 100 * 0.8 = 80
        // initialBid = 500 * 0.3 = 150 > 80, so should pass
        val bid: Int? = strategy.decideAuctionBid(property, null, currentMoney)

        bid shouldBe null
    }

    // Auction bid tests with custom aggressive parameters
    "should bid 50% of property price with aggressive strategy" {
        val aggressiveStrategy = AlwaysPlayerStrategy(
            maxBidRatio = 0.9,
            initialBidRatio = 0.5,
            bidIncrement = 20
        )
        val property: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Mediterranean Avenue",
                position = 1,
                price = 100,
                baseRent = 2,
                colorGroup = ColorGroup.BROWN,
            )
        val currentMoney = 1500

        val bid: Int? = aggressiveStrategy.decideAuctionBid(property, null, currentMoney)

        bid shouldBe 50 // 100 * 0.5
    }

    "should bid current bid + 20 with aggressive strategy" {
        val aggressiveStrategy = AlwaysPlayerStrategy(
            maxBidRatio = 0.9,
            initialBidRatio = 0.5,
            bidIncrement = 20
        )
        val property: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Mediterranean Avenue",
                position = 1,
                price = 100,
                baseRent = 2,
                colorGroup = ColorGroup.BROWN,
            )
        val currentMoney = 1500

        val bid: Int? = aggressiveStrategy.decideAuctionBid(property, 50, currentMoney)

        bid shouldBe 70 // 50 + 20
    }

    // Auction bid tests with custom cautious parameters
    "should bid 20% of property price with cautious strategy" {
        val cautiousStrategy = AlwaysPlayerStrategy(
            maxBidRatio = 0.5,
            initialBidRatio = 0.2,
            bidIncrement = 5
        )
        val property: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Mediterranean Avenue",
                position = 1,
                price = 100,
                baseRent = 2,
                colorGroup = ColorGroup.BROWN,
            )
        val currentMoney = 1500

        val bid: Int? = cautiousStrategy.decideAuctionBid(property, null, currentMoney)

        bid shouldBe 20 // 100 * 0.2
    }

    "should bid current bid + 5 with cautious strategy" {
        val cautiousStrategy = AlwaysPlayerStrategy(
            maxBidRatio = 0.5,
            initialBidRatio = 0.2,
            bidIncrement = 5
        )
        val property: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Mediterranean Avenue",
                position = 1,
                price = 100,
                baseRent = 2,
                colorGroup = ColorGroup.BROWN,
            )
        val currentMoney = 1500

        val bid: Int? = cautiousStrategy.decideAuctionBid(property, 50, currentMoney)

        bid shouldBe 55 // 50 + 5
    }

    "should pass when bid would exceed max with cautious strategy" {
        val cautiousStrategy = AlwaysPlayerStrategy(
            maxBidRatio = 0.5,
            initialBidRatio = 0.2,
            bidIncrement = 5
        )
        val property: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Mediterranean Avenue",
                position = 1,
                price = 100,
                baseRent = 2,
                colorGroup = ColorGroup.BROWN,
            )
        val currentMoney = 100

        // maxBid = 100 * 0.5 = 50
        // next bid = 48 + 5 = 53 > 50, so should pass
        val bid: Int? = cautiousStrategy.decideAuctionBid(property, 48, currentMoney)

        bid shouldBe null
    }
})
