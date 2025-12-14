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
        val property =
            StreetProperty(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                rent = PropertyRent(2, 10, 30, 90, 160, 250),
                houseCost = 50,
                hotelCost = 50,
                colorGroup = ColorGroup.BROWN,
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
        val property =
            StreetProperty(
                name = "Baltic Avenue",
                position = 3,
                price = 60,
                rent = PropertyRent(4, 20, 60, 180, 320, 450),
                houseCost = 50,
                hotelCost = 50,
                colorGroup = ColorGroup.BROWN,
            ).withOwner(player).mortgage()

        property.isMortgaged() shouldBe true
        property.unmortgageValue.amount shouldBe 33 // (60 / 2) * 1.1 = 33

        val unmortgaged = property.unmortgage()

        unmortgaged.isMortgaged() shouldBe false
        unmortgaged.ownership shouldBe PropertyOwnership.OwnedByPlayer(player, isMortgaged = false)
    }

    // TC-MORTGAGE-003: Cannot mortgage unowned property
    "Cannot mortgage unowned property" {
        val property =
            StreetProperty(
                name = "Oriental Avenue",
                position = 6,
                price = 100,
                rent = PropertyRent(6, 30, 90, 270, 400, 550),
                houseCost = 50,
                hotelCost = 50,
                colorGroup = ColorGroup.LIGHT_BLUE,
            )

        property.isOwned() shouldBe false

        shouldThrow<IllegalArgumentException> {
            property.mortgage()
        }
    }

    // TC-MORTGAGE-004: Cannot mortgage already mortgaged property
    "Cannot mortgage already mortgaged property" {
        val player = Player("Charlie", AlwaysPlayerStrategy())
        val property =
            StreetProperty(
                name = "Vermont Avenue",
                position = 8,
                price = 100,
                rent = PropertyRent(6, 30, 90, 270, 400, 550),
                houseCost = 50,
                hotelCost = 50,
                colorGroup = ColorGroup.LIGHT_BLUE,
            ).withOwner(player).mortgage()

        property.isMortgaged() shouldBe true

        shouldThrow<IllegalArgumentException> {
            property.mortgage()
        }
    }

    // TC-MORTGAGE-005: Cannot mortgage property with buildings
    "Cannot mortgage property with buildings" {
        val player = Player("David", AlwaysPlayerStrategy())
        val property =
            StreetProperty(
                name = "Connecticut Avenue",
                position = 9,
                price = 120,
                rent = PropertyRent(8, 40, 100, 300, 450, 600),
                houseCost = 50,
                hotelCost = 50,
                colorGroup = ColorGroup.LIGHT_BLUE,
                ownership = PropertyOwnership.OwnedByPlayer(player),
                buildings = PropertyBuildings(houseCount = 2),
            )

        property.buildings.houseCount shouldBe 2

        shouldThrow<IllegalArgumentException> {
            property.mortgage()
        }
    }

    // TC-MORTGAGE-006: Cannot unmortgage unowned property
    "Cannot unmortgage unowned property" {
        val property =
            StreetProperty(
                name = "St. Charles Place",
                position = 11,
                price = 140,
                rent = PropertyRent(10, 50, 150, 450, 625, 750),
                houseCost = 100,
                hotelCost = 100,
                colorGroup = ColorGroup.PINK,
            )

        shouldThrow<IllegalArgumentException> {
            property.unmortgage()
        }
    }

    // TC-MORTGAGE-007: Cannot unmortgage non-mortgaged property
    "Cannot unmortgage non-mortgaged property" {
        val player = Player("Eve", AlwaysPlayerStrategy())
        val property =
            StreetProperty(
                name = "States Avenue",
                position = 13,
                price = 140,
                rent = PropertyRent(10, 50, 150, 450, 625, 750),
                houseCost = 100,
                hotelCost = 100,
                colorGroup = ColorGroup.PINK,
            ).withOwner(player)

        property.isMortgaged() shouldBe false

        shouldThrow<IllegalArgumentException> {
            property.unmortgage()
        }
    }

    // TC-MORTGAGE-008: Mortgage a railroad property
    "Mortgage a railroad property should work" {
        val player = Player("Frank", AlwaysPlayerStrategy())
        val railroad =
            RailroadProperty(
                name = "Reading Railroad",
                position = 5,
                price = 200,
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
        val utility =
            UtilityProperty(
                name = "Electric Company",
                position = 12,
                price = 150,
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
        val property =
            StreetProperty(
                name = "Boardwalk",
                position = 39,
                price = 400,
                rent = PropertyRent(50, 200, 600, 1400, 1700, 2000),
                houseCost = 200,
                hotelCost = 200,
                colorGroup = ColorGroup.DARK_BLUE,
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

    // TC-MORTGAGE-011: Calculate rent for railroad property
    "Should calculate rent for railroad property based on count" {
        val player = Player("Ian", AlwaysPlayerStrategy())
        val railroad1 =
            RailroadProperty(
                name = "Reading Railroad",
                position = 5,
                price = 200,
            ).withOwner(player)
        val railroad2 =
            RailroadProperty(
                name = "Pennsylvania Railroad",
                position = 15,
                price = 200,
            ).withOwner(player)

        // Add first railroad
        player.acquireProperty(railroad1)

        // Rent with 1 railroad should be $25
        player.calculateRentFor(railroad1) shouldBe 25

        // Add second railroad
        player.acquireProperty(railroad2)

        // Rent with 2 railroads should be $50
        player.calculateRentFor(railroad1) shouldBe 50
        player.calculateRentFor(railroad2) shouldBe 50
    }

    // TC-MORTGAGE-012: Calculate rent for utility property
    "Should calculate rent for utility property based on dice roll" {
        val player = Player("Jane", AlwaysPlayerStrategy())
        val utility1 =
            UtilityProperty(
                name = "Electric Company",
                position = 12,
                price = 150,
            ).withOwner(player)
        val utility2 =
            UtilityProperty(
                name = "Water Works",
                position = 28,
                price = 150,
            ).withOwner(player)

        // Add first utility
        player.acquireProperty(utility1)

        // Rent with 1 utility and dice roll 6 should be 6 × 4 = 24
        player.calculateRentFor(utility1, diceRoll = 6) shouldBe 24

        // Add second utility
        player.acquireProperty(utility2)

        // Rent with 2 utilities and dice roll 6 should be 6 × 10 = 60
        player.calculateRentFor(utility1, diceRoll = 6) shouldBe 60
        player.calculateRentFor(utility2, diceRoll = 6) shouldBe 60
    }

    // TC-MORTGAGE-013: Mortgaged railroad has no rent
    "Mortgaged railroad property should have no rent" {
        val player = Player("Kevin", AlwaysPlayerStrategy())
        val railroad =
            RailroadProperty(
                name = "Reading Railroad",
                position = 5,
                price = 200,
            ).withOwner(player)

        player.acquireProperty(railroad)

        // Non-mortgaged railroad has rent
        player.calculateRentFor(railroad) shouldBe 25

        // Mortgage the railroad
        val mortgaged = railroad.mortgage()
        player.removeProperty(railroad)
        player.acquireProperty(mortgaged)

        // Mortgaged railroad has no rent
        player.calculateRentFor(mortgaged) shouldBe 0
    }

    // TC-MORTGAGE-014: Mortgaged utility has no rent
    "Mortgaged utility property should have no rent" {
        val player = Player("Laura", AlwaysPlayerStrategy())
        val utility =
            UtilityProperty(
                name = "Electric Company",
                position = 12,
                price = 150,
            ).withOwner(player)

        player.acquireProperty(utility)

        // Non-mortgaged utility has rent
        player.calculateRentFor(utility, diceRoll = 6) shouldBe 24 // 6 × 4

        // Mortgage the utility
        val mortgaged = utility.mortgage()
        player.removeProperty(utility)
        player.acquireProperty(mortgaged)

        // Mortgaged utility has no rent
        player.calculateRentFor(mortgaged, diceRoll = 6) shouldBe 0
    }

    // TC-RENT-001: Monopoly bonus doubles base rent
    "Monopoly bonus should double base rent when player owns all properties in color group" {
        val player = Player("MonopolyOwner", AlwaysPlayerStrategy())

        // Brown color group has 2 properties
        val mediterranean =
            StreetProperty(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                rent = PropertyRent(2, 10, 30, 90, 160, 250),
                houseCost = 50,
                hotelCost = 50,
                colorGroup = ColorGroup.BROWN,
            ).withOwner(player)
        val baltic =
            StreetProperty(
                name = "Baltic Avenue",
                position = 3,
                price = 60,
                rent = PropertyRent(4, 20, 60, 180, 320, 450),
                houseCost = 50,
                hotelCost = 50,
                colorGroup = ColorGroup.BROWN,
            ).withOwner(player)

        // First, player owns only one property - no monopoly bonus
        player.acquireProperty(mediterranean)
        player.calculateRentFor(mediterranean) shouldBe 2 // Base rent without monopoly

        // Now player owns both properties - monopoly bonus applies (2x)
        player.acquireProperty(baltic)
        player.calculateRentFor(mediterranean) shouldBe 4 // Base rent × 2 = 2 × 2 = 4
        player.calculateRentFor(baltic) shouldBe 8 // Base rent × 2 = 4 × 2 = 8
    }

    // TC-RENT-002: Monopoly bonus does not apply when buildings exist
    "Monopoly bonus should NOT apply when buildings are built" {
        val player = Player("Builder", AlwaysPlayerStrategy())

        val mediterranean =
            StreetProperty(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                rent = PropertyRent(2, 10, 30, 90, 160, 250),
                houseCost = 50,
                hotelCost = 50,
                colorGroup = ColorGroup.BROWN,
            ).withOwner(player)
        val baltic =
            StreetProperty(
                name = "Baltic Avenue",
                position = 3,
                price = 60,
                rent = PropertyRent(4, 20, 60, 180, 320, 450),
                houseCost = 50,
                hotelCost = 50,
                colorGroup = ColorGroup.BROWN,
                buildings = PropertyBuildings(houseCount = 1), // Has 1 house
            ).withOwner(player)

        player.acquireProperty(mediterranean)
        player.acquireProperty(baltic)

        // Mediterranean has no buildings - monopoly bonus applies
        player.calculateRentFor(mediterranean) shouldBe 4 // Base rent × 2

        // Baltic has 1 house - building rent applies (no monopoly multiplier)
        player.calculateRentFor(baltic) shouldBe 20 // withHouse1 rent
    }

    // TC-RENT-003: Three-property color group monopoly
    "Monopoly bonus should require all properties in color group" {
        val player = Player("PartialOwner", AlwaysPlayerStrategy())

        // Light blue has 3 properties
        val oriental =
            StreetProperty(
                name = "Oriental Avenue",
                position = 6,
                price = 100,
                rent = PropertyRent(6, 30, 90, 270, 400, 550),
                houseCost = 50,
                hotelCost = 50,
                colorGroup = ColorGroup.LIGHT_BLUE,
            ).withOwner(player)
        val vermont =
            StreetProperty(
                name = "Vermont Avenue",
                position = 8,
                price = 100,
                rent = PropertyRent(6, 30, 90, 270, 400, 550),
                houseCost = 50,
                hotelCost = 50,
                colorGroup = ColorGroup.LIGHT_BLUE,
            ).withOwner(player)

        // Player owns 2 of 3 - no monopoly
        player.acquireProperty(oriental)
        player.acquireProperty(vermont)

        // No monopoly bonus (need 3 properties for light blue)
        player.calculateRentFor(oriental) shouldBe 6
        player.calculateRentFor(vermont) shouldBe 6

        // Now add the third property
        val connecticut =
            StreetProperty(
                name = "Connecticut Avenue",
                position = 9,
                price = 120,
                rent = PropertyRent(8, 40, 100, 300, 450, 600),
                houseCost = 50,
                hotelCost = 50,
                colorGroup = ColorGroup.LIGHT_BLUE,
            ).withOwner(player)
        player.acquireProperty(connecticut)

        // Now monopoly bonus applies (2x)
        player.calculateRentFor(oriental) shouldBe 12 // 6 × 2
        player.calculateRentFor(vermont) shouldBe 12 // 6 × 2
        player.calculateRentFor(connecticut) shouldBe 16 // 8 × 2
    }
})
