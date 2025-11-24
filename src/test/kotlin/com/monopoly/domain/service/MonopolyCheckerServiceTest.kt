package com.monopoly.domain.service

import com.monopoly.domain.model.ColorGroup
import com.monopoly.domain.model.Player
import com.monopoly.domain.model.PropertyTestFixtures
import com.monopoly.domain.strategy.AlwaysBuyStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class MonopolyCheckerServiceTest : StringSpec({
    // TC-301: プレイヤーがモノポリーを持っている（BROWN 2つ全て所有）
    // Given: BROWNグループ2つ全てを所有するPlayer
    // When: hasMonopoly(player, ColorGroup.BROWN)
    // Then: trueを返す
    "should return true when player owns all properties in BROWN group" {
        val player = Player("Alice", AlwaysBuyStrategy())
        val property1 =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Mediterranean Avenue",
                    position = 1,
                    price = 60,
                    baseRent = 2,
                    colorGroup = ColorGroup.BROWN,
                ).withOwner(player)
        val property2 =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Baltic Avenue",
                    position = 3,
                    price = 60,
                    baseRent = 4,
                    colorGroup = ColorGroup.BROWN,
                ).withOwner(player)

        player.acquireProperty(property1)
        player.acquireProperty(property2)

        val checker = MonopolyCheckerService()

        checker.hasMonopoly(player, ColorGroup.BROWN) shouldBe true
    }

    // TC-302: プレイヤーがモノポリーを持っていない（1つだけ所有）
    // Given: BROWNグループのうち1つだけを所有するPlayer
    // When: hasMonopoly(player, ColorGroup.BROWN)
    // Then: falseを返す
    "should return false when player owns only one property in BROWN group" {
        val player = Player("Bob", AlwaysBuyStrategy())
        val property1 =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Mediterranean Avenue",
                    position = 1,
                    price = 60,
                    baseRent = 2,
                    colorGroup = ColorGroup.BROWN,
                ).withOwner(player)

        player.acquireProperty(property1)

        val checker = MonopolyCheckerService()

        checker.hasMonopoly(player, ColorGroup.BROWN) shouldBe false
    }

    // TC-303: プレイヤーがモノポリーを持っていない（プロパティ0個）
    // Given: プロパティを持たないPlayer
    // When: hasMonopoly(player, ColorGroup.BROWN)
    // Then: falseを返す
    "should return false when player owns no properties" {
        val player = Player("Carol", AlwaysBuyStrategy())

        val checker = MonopolyCheckerService()

        checker.hasMonopoly(player, ColorGroup.BROWN) shouldBe false
    }

    // TC-304: LIGHT_BLUEグループのモノポリー（3つ全て所有）
    // Given: LIGHT_BLUEグループ3つ全てを所有するPlayer
    // When: hasMonopoly(player, ColorGroup.LIGHT_BLUE)
    // Then: trueを返す
    "should return true when player owns all three properties in LIGHT_BLUE group" {
        val player = Player("Dave", AlwaysBuyStrategy())
        val property1 =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Oriental Avenue",
                    position = 6,
                    price = 100,
                    baseRent = 6,
                    colorGroup = ColorGroup.LIGHT_BLUE,
                ).withOwner(player)
        val property2 =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Vermont Avenue",
                    position = 8,
                    price = 100,
                    baseRent = 6,
                    colorGroup = ColorGroup.LIGHT_BLUE,
                ).withOwner(player)
        val property3 =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Connecticut Avenue",
                    position = 9,
                    price = 120,
                    baseRent = 8,
                    colorGroup = ColorGroup.LIGHT_BLUE,
                ).withOwner(player)

        player.acquireProperty(property1)
        player.acquireProperty(property2)
        player.acquireProperty(property3)

        val checker = MonopolyCheckerService()

        checker.hasMonopoly(player, ColorGroup.LIGHT_BLUE) shouldBe true
    }

    // TC-305: LIGHT_BLUEグループのモノポリーを持っていない（2つだけ所有）
    // Given: LIGHT_BLUEグループのうち2つだけを所有するPlayer
    // When: hasMonopoly(player, ColorGroup.LIGHT_BLUE)
    // Then: falseを返す
    "should return false when player owns only two of three properties in LIGHT_BLUE group" {
        val player = Player("Eve", AlwaysBuyStrategy())
        val property1 =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Oriental Avenue",
                    position = 6,
                    price = 100,
                    baseRent = 6,
                    colorGroup = ColorGroup.LIGHT_BLUE,
                ).withOwner(player)
        val property2 =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Vermont Avenue",
                    position = 8,
                    price = 100,
                    baseRent = 6,
                    colorGroup = ColorGroup.LIGHT_BLUE,
                ).withOwner(player)

        player.acquireProperty(property1)
        player.acquireProperty(property2)

        val checker = MonopolyCheckerService()

        checker.hasMonopoly(player, ColorGroup.LIGHT_BLUE) shouldBe false
    }

    // TC-306: 異なるグループのプロパティを持つが、指定グループのモノポリーはない
    // Given: BROWNグループ全て + LIGHT_BLUEグループ1つを所有するPlayer
    // When: hasMonopoly(player, ColorGroup.LIGHT_BLUE)
    // Then: falseを返す
    "should return false when checking LIGHT_BLUE monopoly but player only has BROWN monopoly" {
        val player = Player("Frank", AlwaysBuyStrategy())
        val brownProperty1 =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Mediterranean Avenue",
                    position = 1,
                    price = 60,
                    baseRent = 2,
                    colorGroup = ColorGroup.BROWN,
                ).withOwner(player)
        val brownProperty2 =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Baltic Avenue",
                    position = 3,
                    price = 60,
                    baseRent = 4,
                    colorGroup = ColorGroup.BROWN,
                ).withOwner(player)
        val lightBlueProperty =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Oriental Avenue",
                    position = 6,
                    price = 100,
                    baseRent = 6,
                    colorGroup = ColorGroup.LIGHT_BLUE,
                ).withOwner(player)

        player.acquireProperty(brownProperty1)
        player.acquireProperty(brownProperty2)
        player.acquireProperty(lightBlueProperty)

        val checker = MonopolyCheckerService()

        checker.hasMonopoly(player, ColorGroup.LIGHT_BLUE) shouldBe false
        checker.hasMonopoly(player, ColorGroup.BROWN) shouldBe true
    }
})
