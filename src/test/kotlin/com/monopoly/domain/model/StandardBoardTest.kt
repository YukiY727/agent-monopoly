package com.monopoly.domain.model

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * 標準的なMonopolyボード（40マス）のテスト
 *
 * Phase 13: 実際のMonopolyボードの全40マスを正確に実装
 */
class StandardBoardTest : DescribeSpec({
    describe("StandardBoard") {
        it("should have exactly 40 spaces") {
            val board = StandardBoard.create()
            board.getSpaceCount() shouldBe 40
        }

        it("should have GO at position 0") {
            val board = StandardBoard.create()
            val space = board.getSpace(0)
            space.shouldBeInstanceOf<Space.Go>()
            space.position shouldBe 0
        }

        it("should have Jail at position 10") {
            val board = StandardBoard.create()
            val space = board.getSpace(10)
            space.shouldBeInstanceOf<Space.Jail>()
            space.position shouldBe 10
        }

        it("should have Free Parking at position 20") {
            val board = StandardBoard.create()
            val space = board.getSpace(20)
            space.shouldBeInstanceOf<Space.FreeParking>()
            space.position shouldBe 20
        }

        it("should have Go To Jail at position 30") {
            val board = StandardBoard.create()
            val space = board.getSpace(30)
            space.shouldBeInstanceOf<Space.GoToJail>()
            space.position shouldBe 30
        }

        it("should have Mediterranean Avenue (BROWN) at position 1") {
            val board = StandardBoard.create()
            val space = board.getSpace(1)
            space.shouldBeInstanceOf<Space.PropertySpace>()
            val property = (space as Space.PropertySpace).property
            property.name shouldBe "Mediterranean Avenue"
            property.colorGroup shouldBe ColorGroup.BROWN
            property.price shouldBe 60
            property.baseRent shouldBe 2
        }

        it("should have Boardwalk (DARK_BLUE) at position 39") {
            val board = StandardBoard.create()
            val space = board.getSpace(39)
            space.shouldBeInstanceOf<Space.PropertySpace>()
            val property = (space as Space.PropertySpace).property
            property.name shouldBe "Boardwalk"
            property.colorGroup shouldBe ColorGroup.DARK_BLUE
            property.price shouldBe 400
            property.baseRent shouldBe 50
        }

        it("should have 28 properties total") {
            val board = StandardBoard.create()
            board.getAllProperties().size shouldBe 28
        }

        it("should have 22 regular properties") {
            val board = StandardBoard.create()
            val regularProperties = board.getAllProperties().filterIsInstance<Property>()
            regularProperties.size shouldBe 22
        }

        it("should have 4 railroads") {
            val board = StandardBoard.create()
            val railroads = board.getAllProperties().filterIsInstance<Railroad>()
            railroads.size shouldBe 4
            railroads.map { it.position } shouldBe listOf(5, 15, 25, 35)
        }

        it("should have 2 utilities") {
            val board = StandardBoard.create()
            val utilities = board.getAllProperties().filterIsInstance<Utility>()
            utilities.size shouldBe 2
            utilities.map { it.position } shouldBe listOf(12, 28)
        }

        it("should have Income Tax at position 4") {
            val board = StandardBoard.create()
            val space = board.getSpace(4)
            space.shouldBeInstanceOf<Space.Tax>()
            val tax = space as Space.Tax
            tax.amount shouldBe 200
        }

        it("should have Luxury Tax at position 38") {
            val board = StandardBoard.create()
            val space = board.getSpace(38)
            space.shouldBeInstanceOf<Space.Tax>()
            val tax = space as Space.Tax
            tax.amount shouldBe 100
        }

        it("should have 3 Chance spaces") {
            val board = StandardBoard.create()
            val chances = (0..39).map { board.getSpace(it) }
                .filterIsInstance<Space.ChanceSpace>()
            chances.size shouldBe 3
            chances.map { it.position } shouldBe listOf(7, 22, 36)
        }

        it("should have 3 Community Chest spaces") {
            val board = StandardBoard.create()
            val communityChests = (0..39).map { board.getSpace(it) }
                .filterIsInstance<Space.CommunityChestSpace>()
            communityChests.size shouldBe 3
            communityChests.map { it.position } shouldBe listOf(2, 17, 33)
        }
    }
})
