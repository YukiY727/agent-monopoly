package com.monopoly.domain.model.property

import com.monopoly.domain.model.player.Player
import com.monopoly.domain.strategy.AlwaysPlayerStrategy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class MortgageTest : StringSpec({

    // TC-MORTGAGE-001: Mortgage a street property
    "Mortgage a street property should set mortgage flag and return mortgage value" {
        val player = Player("Alice", AlwaysPlayerStrategy())
        val property = StreetProperty(
            name = "Mediterranean Avenue",
            position = 1,
            price = 60,
            rent = PropertyRent(2, 10, 30, 90, 160, 250),
            houseCost = 50,
            hotelCost = 50,
            colorGroup = ColorGroup.BROWN
        ).withOwner(player)

        property.isMortgaged() shouldBe false
        property.mortgageValue.amount shouldBe 30 // 60 / 2

        val mortgaged = property.mortgage()

        mortgaged.isMortgaged() shouldBe true
        mortgaged.ownership shouldBe PropertyOwnership.OwnedByPlayer(player, isMortgaged = true)
    }

    // TC-MORTGAGE-002: Unmortgage a property
    "Unmortgage a property should clear mortgage flag and require payment" {
        val player = Player("Bob", AlwaysPlayerStrategy())
        val property = StreetProperty(
            name = "Baltic Avenue",
            position = 3,
            price = 60,
            rent = PropertyRent(4, 20, 60, 180, 320, 450),
            houseCost = 50,
            hotelCost = 50,
            colorGroup = ColorGroup.BROWN
        ).withOwner(player).mortgage()

        property.isMortgaged() shouldBe true
        property.unmortgageValue.amount shouldBe 33 // (60 / 2) * 1.1 = 33

        val unmortgaged = property.unmortgage()

        unmortgaged.isMortgaged() shouldBe false
        unmortgaged.ownership shouldBe PropertyOwnership.OwnedByPlayer(player, isMortgaged = false)
    }

    // TC-MORTGAGE-003: Cannot mortgage unowned property
    "Cannot mortgage unowned property" {
        val property = StreetProperty(
            name = "Oriental Avenue",
            position = 6,
            price = 100,
            rent = PropertyRent(6, 30, 90, 270, 400, 550),
            houseCost = 50,
            hotelCost = 50,
            colorGroup = ColorGroup.LIGHT_BLUE
        )

        property.isOwned() shouldBe false

        shouldThrow<IllegalArgumentException> {
            property.mortgage()
        }
    }

    // TC-MORTGAGE-004: Cannot mortgage already mortgaged property
    "Cannot mortgage already mortgaged property" {
        val player = Player("Charlie", AlwaysPlayerStrategy())
        val property = StreetProperty(
            name = "Vermont Avenue",
            position = 8,
            price = 100,
            rent = PropertyRent(6, 30, 90, 270, 400, 550),
            houseCost = 50,
            hotelCost = 50,
            colorGroup = ColorGroup.LIGHT_BLUE
        ).withOwner(player).mortgage()

        property.isMortgaged() shouldBe true

        shouldThrow<IllegalArgumentException> {
            property.mortgage()
        }
    }

    // TC-MORTGAGE-005: Cannot mortgage property with buildings
    "Cannot mortgage property with buildings" {
        val player = Player("David", AlwaysPlayerStrategy())
        val property = StreetProperty(
            name = "Connecticut Avenue",
            position = 9,
            price = 120,
            rent = PropertyRent(8, 40, 100, 300, 450, 600),
            houseCost = 50,
            hotelCost = 50,
            colorGroup = ColorGroup.LIGHT_BLUE,
            ownership = PropertyOwnership.OwnedByPlayer(player),
            buildings = PropertyBuildings(houseCount = 2)
        )

        property.buildings.houseCount shouldBe 2

        shouldThrow<IllegalArgumentException> {
            property.mortgage()
        }
    }

    // TC-MORTGAGE-006: Cannot unmortgage unowned property
    "Cannot unmortgage unowned property" {
        val property = StreetProperty(
            name = "St. Charles Place",
            position = 11,
            price = 140,
            rent = PropertyRent(10, 50, 150, 450, 625, 750),
            houseCost = 100,
            hotelCost = 100,
            colorGroup = ColorGroup.PINK
        )

        shouldThrow<IllegalArgumentException> {
            property.unmortgage()
        }
    }

    // TC-MORTGAGE-007: Cannot unmortgage non-mortgaged property
    "Cannot unmortgage non-mortgaged property" {
        val player = Player("Eve", AlwaysPlayerStrategy())
        val property = StreetProperty(
            name = "States Avenue",
            position = 13,
            price = 140,
            rent = PropertyRent(10, 50, 150, 450, 625, 750),
            houseCost = 100,
            hotelCost = 100,
            colorGroup = ColorGroup.PINK
        ).withOwner(player)

        property.isMortgaged() shouldBe false

        shouldThrow<IllegalArgumentException> {
            property.unmortgage()
        }
    }

    // TC-MORTGAGE-008: Mortgage a railroad property
    "Mortgage a railroad property should work" {
        val player = Player("Frank", AlwaysPlayerStrategy())
        val railroad = RailroadProperty(
            name = "Reading Railroad",
            position = 5,
            price = 200
        ).withOwner(player)

        railroad.mortgageValue.amount shouldBe 100 // 200 / 2
        railroad.unmortgageValue.amount shouldBe 110 // (200 / 2) * 1.1

        val mortgaged = railroad.mortgage()
        mortgaged.isMortgaged() shouldBe true

        val unmortgaged = mortgaged.unmortgage()
        unmortgaged.isMortgaged() shouldBe false
    }

    // TC-MORTGAGE-009: Mortgage a utility property
    "Mortgage a utility property should work" {
        val player = Player("Grace", AlwaysPlayerStrategy())
        val utility = UtilityProperty(
            name = "Electric Company",
            position = 12,
            price = 150
        ).withOwner(player)

        utility.mortgageValue.amount shouldBe 75 // 150 / 2
        utility.unmortgageValue.amount shouldBe 82 // (150 / 2) * 1.1 = 82.5 -> 82

        val mortgaged = utility.mortgage()
        mortgaged.isMortgaged() shouldBe true

        val unmortgaged = mortgaged.unmortgage()
        unmortgaged.isMortgaged() shouldBe false
    }

    // TC-MORTGAGE-010: Mortgaged property has no rent
    "Mortgaged property should have no rent income" {
        val player = Player("Henry", AlwaysPlayerStrategy())
        val property = StreetProperty(
            name = "Boardwalk",
            position = 39,
            price = 400,
            rent = PropertyRent(50, 200, 600, 1400, 1700, 2000),
            houseCost = 200,
            hotelCost = 200,
            colorGroup = ColorGroup.DARK_BLUE
        ).withOwner(player)

        // Add property to player's owned properties
        player.acquireProperty(property)

        // Non-mortgaged property has rent
        player.calculateRentFor(property) shouldBe 50

        // Mortgage the property
        val mortgaged = property.mortgage()
        player.removeProperty(property)
        player.acquireProperty(mortgaged)

        // Mortgaged property has no rent
        player.calculateRentFor(mortgaged) shouldBe 0

        // Unmortgage the property
        val unmortgaged = mortgaged.unmortgage()
        player.removeProperty(mortgaged)
        player.acquireProperty(unmortgaged)

        // Rent is restored after unmortgage
        player.calculateRentFor(unmortgaged) shouldBe 50
    }
})
