package com.monopoly.domain.service

import com.monopoly.domain.model.ColorGroup
import com.monopoly.domain.model.Player
import com.monopoly.domain.model.Property
import com.monopoly.domain.model.StreetProperty
import com.monopoly.domain.model.PropertyBuildings
import com.monopoly.domain.model.PropertyTestFixtures
import com.monopoly.domain.strategy.AlwaysBuyStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class BuildingServiceTest : StringSpec({
    // TC-401: モノポリーなしでは建設できない
    // Given: BROWNグループのうち1つだけを所有するPlayer
    // When: buildHouse()
    // Then: falseを返し、建物は建たない
    "should not allow building house without monopoly" {
        val player = Player("Alice", AlwaysBuyStrategy())
        val property: StreetProperty =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Mediterranean Avenue",
                    position = 1,
                    price = 60,
                    baseRent = 2,
                    colorGroup = ColorGroup.BROWN,
                ).withOwner(player)

        player.acquireProperty(property)

        val buildingService = BuildingService(MonopolyCheckerService())
        val result: Boolean = buildingService.buildHouse(player, property)

        result shouldBe false
        property.buildings.houseCount shouldBe 0
    }

    // TC-402: モノポリーあり、所持金十分で建設成功
    // Given: BROWNグループ全て所有、houseCost=$50、所持金=$1500
    // When: buildHouse()
    // Then: trueを返し、家が1つ建ち、所持金が$1450
    "should allow building house with monopoly and enough money" {
        val player = Player("Bob", AlwaysBuyStrategy())

        val property1: StreetProperty =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Mediterranean Avenue",
                    position = 1,
                    price = 60,
                    baseRent = 2,
                    houseCost = 50,
                    colorGroup = ColorGroup.BROWN,
                ).withOwner(player)

        val property2: StreetProperty =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Baltic Avenue",
                    position = 3,
                    price = 60,
                    baseRent = 4,
                    houseCost = 50,
                    colorGroup = ColorGroup.BROWN,
                ).withOwner(player)

        player.acquireProperty(property1)
        player.acquireProperty(property2)

        val buildingService = BuildingService(MonopolyCheckerService())
        val result: Boolean = buildingService.buildHouse(player, property1)

        result shouldBe true
        // 更新後のプロパティを取得してチェック
        val updatedProperty: StreetProperty = player.ownedProperties.first { it.name == "Mediterranean Avenue" } as StreetProperty
        updatedProperty.buildings.houseCount shouldBe 1
        player.money shouldBe 1450 // 1500 - 50
    }

    // TC-403: 所持金不足で建設失敗
    // Given: BROWNグループ全て所有、houseCost=$50、所持金=$30
    // When: buildHouse()
    // Then: falseを返し、家は建たない、所持金は変わらない
    "should not allow building house without enough money" {
        val player = Player("Carol", AlwaysBuyStrategy())
        player.subtractMoney(1470) // 1500 - 1470 = 30

        val property1: StreetProperty =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Mediterranean Avenue",
                    position = 1,
                    price = 60,
                    baseRent = 2,
                    houseCost = 50,
                    colorGroup = ColorGroup.BROWN,
                ).withOwner(player)

        val property2: StreetProperty =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Baltic Avenue",
                    position = 3,
                    price = 60,
                    baseRent = 4,
                    houseCost = 50,
                    colorGroup = ColorGroup.BROWN,
                ).withOwner(player)

        player.acquireProperty(property1)
        player.acquireProperty(property2)

        val buildingService = BuildingService(MonopolyCheckerService())
        val result: Boolean = buildingService.buildHouse(player, property1)

        result shouldBe false
        property1.buildings.houseCount shouldBe 0
        player.money shouldBe 30
    }

    // TC-404: 均等建設ルール - 他の物件が家0の時は2つ目の家を建てられない
    // Given: BROWNグループ全て所有、property1に家1、property2に家0
    // When: property1にbuildHouse()
    // Then: falseを返し、property1の家は1のまま
    "should enforce even building rule - cannot build second house when other property has zero" {
        val player = Player("Dave", AlwaysBuyStrategy())

        val property1: StreetProperty =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Mediterranean Avenue",
                    position = 1,
                    price = 60,
                    baseRent = 2,
                    houseCost = 50,
                    colorGroup = ColorGroup.BROWN,
                    buildings = PropertyBuildings(houseCount = 1),
                ).withOwner(player)

        val property2: StreetProperty =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Baltic Avenue",
                    position = 3,
                    price = 60,
                    baseRent = 4,
                    houseCost = 50,
                    colorGroup = ColorGroup.BROWN,
                ).withOwner(player)

        player.acquireProperty(property1)
        player.acquireProperty(property2)

        val buildingService = BuildingService(MonopolyCheckerService())
        val result: Boolean = buildingService.buildHouse(player, property1)

        result shouldBe false
        property1.buildings.houseCount shouldBe 1
        property2.buildings.houseCount shouldBe 0
    }

    // TC-405: 均等建設ルール - 全ての物件が家1の時は2つ目の家を建てられる
    // Given: BROWNグループ全て所有、両方に家1
    // When: property1にbuildHouse()
    // Then: trueを返し、property1の家が2
    "should allow building when all properties have equal houses" {
        val player = Player("Eve", AlwaysBuyStrategy())

        val property1: StreetProperty =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Mediterranean Avenue",
                    position = 1,
                    price = 60,
                    baseRent = 2,
                    houseCost = 50,
                    colorGroup = ColorGroup.BROWN,
                    buildings = PropertyBuildings(houseCount = 1),
                ).withOwner(player)

        val property2: StreetProperty =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Baltic Avenue",
                    position = 3,
                    price = 60,
                    baseRent = 4,
                    houseCost = 50,
                    colorGroup = ColorGroup.BROWN,
                    buildings = PropertyBuildings(houseCount = 1),
                ).withOwner(player)

        player.acquireProperty(property1)
        player.acquireProperty(property2)

        val buildingService = BuildingService(MonopolyCheckerService())
        val result: Boolean = buildingService.buildHouse(player, property1)

        result shouldBe true
        // 更新後のプロパティを取得してチェック
        val updatedProperty1: StreetProperty = player.ownedProperties.first { it.name == "Mediterranean Avenue" } as StreetProperty
        updatedProperty1.buildings.houseCount shouldBe 2
        // property2は変更されていないので直接チェック可能
        val updatedProperty2: StreetProperty = player.ownedProperties.first { it.name == "Baltic Avenue" } as StreetProperty
        updatedProperty2.buildings.houseCount shouldBe 1
        player.money shouldBe 1450
    }

    // TC-406: 家4つの時はホテル建設可能
    // Given: 家4つ、hotelCost=$50、所持金=$1500
    // When: buildHotel()
    // Then: trueを返し、ホテルが建ち、家が0、所持金が$1450
    "should allow building hotel when property has 4 houses" {
        val player = Player("Frank", AlwaysBuyStrategy())

        val property1: StreetProperty =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Mediterranean Avenue",
                    position = 1,
                    price = 60,
                    baseRent = 2,
                    houseCost = 50,
                    hotelCost = 50,
                    colorGroup = ColorGroup.BROWN,
                    buildings = PropertyBuildings(houseCount = 4),
                ).withOwner(player)

        val property2: StreetProperty =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Baltic Avenue",
                    position = 3,
                    price = 60,
                    baseRent = 4,
                    houseCost = 50,
                    hotelCost = 50,
                    colorGroup = ColorGroup.BROWN,
                    buildings = PropertyBuildings(houseCount = 4),
                ).withOwner(player)

        player.acquireProperty(property1)
        player.acquireProperty(property2)

        val buildingService = BuildingService(MonopolyCheckerService())
        val result: Boolean = buildingService.buildHotel(player, property1)

        result shouldBe true
        // 更新後のプロパティを取得してチェック
        val updatedProperty: StreetProperty = player.ownedProperties.first { it.name == "Mediterranean Avenue" } as StreetProperty
        updatedProperty.buildings.hasHotel shouldBe true
        updatedProperty.buildings.houseCount shouldBe 0
        player.money shouldBe 1450
    }

    // TC-407: 家3つ以下の時はホテル建設不可
    // Given: 家3つ、hotelCost=$50、所持金=$1500
    // When: buildHotel()
    // Then: falseを返し、ホテルは建たず、所持金も変わらない
    "should not allow building hotel when property has less than 4 houses" {
        val player = Player("Grace", AlwaysBuyStrategy())

        val property: StreetProperty =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Mediterranean Avenue",
                    position = 1,
                    price = 60,
                    baseRent = 2,
                    houseCost = 50,
                    hotelCost = 50,
                    colorGroup = ColorGroup.BROWN,
                    buildings = PropertyBuildings(houseCount = 3),
                ).withOwner(player)

        val property2: StreetProperty =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Baltic Avenue",
                    position = 3,
                    price = 60,
                    baseRent = 4,
                    houseCost = 50,
                    hotelCost = 50,
                    colorGroup = ColorGroup.BROWN,
                    buildings = PropertyBuildings(houseCount = 4),
                ).withOwner(player)

        player.acquireProperty(property)
        player.acquireProperty(property2)

        val buildingService = BuildingService(MonopolyCheckerService())
        val result: Boolean = buildingService.buildHotel(player, property)

        result shouldBe false
        property.buildings.hasHotel shouldBe false
        property.buildings.houseCount shouldBe 3
        player.money shouldBe 1500
    }

    // TC-408: ホテル建設時の所持金不足
    // Given: 家4つ、hotelCost=$50、所持金=$30
    // When: buildHotel()
    // Then: falseを返し、ホテルは建たない
    "should not allow building hotel without enough money" {
        val player = Player("Henry", AlwaysBuyStrategy())
        player.subtractMoney(1470) // 1500 - 1470 = 30

        val property: StreetProperty =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Mediterranean Avenue",
                    position = 1,
                    price = 60,
                    baseRent = 2,
                    houseCost = 50,
                    hotelCost = 50,
                    colorGroup = ColorGroup.BROWN,
                    buildings = PropertyBuildings(houseCount = 4),
                ).withOwner(player)

        val property2: StreetProperty =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Baltic Avenue",
                    position = 3,
                    price = 60,
                    baseRent = 4,
                    houseCost = 50,
                    hotelCost = 50,
                    colorGroup = ColorGroup.BROWN,
                    buildings = PropertyBuildings(houseCount = 4),
                ).withOwner(player)

        player.acquireProperty(property)
        player.acquireProperty(property2)

        val buildingService = BuildingService(MonopolyCheckerService())
        val result: Boolean = buildingService.buildHotel(player, property)

        result shouldBe false
        property.buildings.hasHotel shouldBe false
        player.money shouldBe 30
    }

    // TC-409: 家が既に4つある時は追加の家を建てられない
    // Given: 家が既に4つあるプロパティ、モノポリーあり
    // When: buildHouse()
    // Then: falseを返し、家は増えない
    "should not allow building 5th house when property already has 4 houses" {
        val player = Player("Ivy", AlwaysBuyStrategy())

        val property1: StreetProperty =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Mediterranean Avenue",
                    position = 1,
                    price = 60,
                    baseRent = 2,
                    houseCost = 50,
                    colorGroup = ColorGroup.BROWN,
                    buildings = PropertyBuildings(houseCount = 4),
                ).withOwner(player)

        val property2: StreetProperty =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Baltic Avenue",
                    position = 3,
                    price = 60,
                    baseRent = 4,
                    houseCost = 50,
                    colorGroup = ColorGroup.BROWN,
                    buildings = PropertyBuildings(houseCount = 4),
                ).withOwner(player)

        player.acquireProperty(property1)
        player.acquireProperty(property2)

        val buildingService = BuildingService(MonopolyCheckerService())
        val result: Boolean = buildingService.buildHouse(player, property1)

        result shouldBe false
        property1.buildings.houseCount shouldBe 4
    }

    // TC-410: ホテルが既にある時は追加のホテルを建てられない
    // Given: ホテルが既にあるプロパティ、モノポリーあり
    // When: buildHotel()
    // Then: falseを返し、状態は変わらない
    "should not allow building second hotel when property already has hotel" {
        val player = Player("Jack", AlwaysBuyStrategy())

        val property1: StreetProperty =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Mediterranean Avenue",
                    position = 1,
                    price = 60,
                    baseRent = 2,
                    houseCost = 50,
                    hotelCost = 50,
                    colorGroup = ColorGroup.BROWN,
                    buildings = PropertyBuildings(houseCount = 0, hasHotel = true),
                ).withOwner(player)

        val property2: StreetProperty =
            PropertyTestFixtures
                .createTestProperty(
                    name = "Baltic Avenue",
                    position = 3,
                    price = 60,
                    baseRent = 4,
                    houseCost = 50,
                    hotelCost = 50,
                    colorGroup = ColorGroup.BROWN,
                    buildings = PropertyBuildings(houseCount = 4),
                ).withOwner(player)

        player.acquireProperty(property1)
        player.acquireProperty(property2)

        val buildingService = BuildingService(MonopolyCheckerService())
        val result: Boolean = buildingService.buildHotel(player, property1)

        result shouldBe false
        property1.buildings.hasHotel shouldBe true
        property1.buildings.houseCount shouldBe 0
    }
})
