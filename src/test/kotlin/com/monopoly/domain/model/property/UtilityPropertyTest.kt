package com.monopoly.domain.model.property

import com.monopoly.domain.model.core.Money
import com.monopoly.domain.model.player.Player
import com.monopoly.domain.strategy.AlwaysPlayerStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class UtilityPropertyTest : StringSpec({

    // TC-UTILITY-001: Calculate rent with 1 utility and dice roll 6
    "Rent with 1 utility and dice roll 6 should be $24 (6 × 4)" {
        val utility =
            UtilityProperty(
                name = "Electric Company",
                position = 12,
                price = 150,
            )

        val rent: Int = utility.calculateRentWithDice(utilityCount = 1, diceRoll = 6)

        rent shouldBe 24 // 6 × 4
    }

    // TC-UTILITY-002: Calculate rent with 2 utilities and dice roll 6
    "Rent with 2 utilities and dice roll 6 should be $60 (6 × 10)" {
        val utility =
            UtilityProperty(
                name = "Water Works",
                position = 28,
                price = 150,
            )

        val rent: Int = utility.calculateRentWithDice(utilityCount = 2, diceRoll = 6)

        rent shouldBe 60 // 6 × 10
    }

    // TC-UTILITY-003: Calculate rent with 1 utility and dice roll 12
    "Rent with 1 utility and dice roll 12 should be $48 (12 × 4)" {
        val utility =
            UtilityProperty(
                name = "Electric Company",
                position = 12,
                price = 150,
            )

        val rent: Int = utility.calculateRentWithDice(utilityCount = 1, diceRoll = 12)

        rent shouldBe 48 // 12 × 4
    }

    // TC-UTILITY-004: Calculate rent with 2 utilities and dice roll 12
    "Rent with 2 utilities and dice roll 12 should be $120 (12 × 10)" {
        val utility =
            UtilityProperty(
                name = "Water Works",
                position = 28,
                price = 150,
            )

        val rent: Int = utility.calculateRentWithDice(utilityCount = 2, diceRoll = 12)

        rent shouldBe 120 // 12 × 10
    }

    // TC-UTILITY-005: Calculate rent with 1 utility and dice roll 2 (minimum)
    "Rent with 1 utility and dice roll 2 should be $8 (2 × 4)" {
        val utility =
            UtilityProperty(
                name = "Electric Company",
                position = 12,
                price = 150,
            )

        val rent: Int = utility.calculateRentWithDice(utilityCount = 1, diceRoll = 2)

        rent shouldBe 8 // 2 × 4
    }

    // TC-UTILITY-006: Calculate rent with 0 utilities (edge case)
    "Rent with 0 utilities should be $0" {
        val utility =
            UtilityProperty(
                name = "Electric Company",
                position = 12,
                price = 150,
            )

        val rent: Int = utility.calculateRentWithDice(utilityCount = 0, diceRoll = 6)

        rent shouldBe 0
    }

    // TC-UTILITY-007: Utility ownership
    "Utility can be owned by player" {
        val player = Player("Alice", AlwaysPlayerStrategy())
        val utility =
            UtilityProperty(
                name = "Electric Company",
                position = 12,
                price = 150,
            )

        utility.isOwned() shouldBe false

        val ownedUtility: UtilityProperty = utility.withOwner(player)

        ownedUtility.isOwned() shouldBe true
        ownedUtility.ownership shouldBe PropertyOwnership.OwnedByPlayer(player)
    }

    // TC-UTILITY-008: Utility can be released
    "Utility can be released from ownership" {
        val player = Player("Bob", AlwaysPlayerStrategy())
        val utility =
            UtilityProperty(
                name = "Electric Company",
                position = 12,
                price = 150,
            ).withOwner(player)

        utility.isOwned() shouldBe true

        val releasedUtility: UtilityProperty = utility.withoutOwner()

        releasedUtility.isOwned() shouldBe false
        releasedUtility.ownership shouldBe PropertyOwnership.Unowned
    }

    // TC-UTILITY-009: Utility price value
    "Utility price value should match price" {
        val utility =
            UtilityProperty(
                name = "Electric Company",
                position = 12,
                price = 150,
            )

        utility.priceValue shouldBe Money(150)
    }

    // TC-UTILITY-010: Mortgage utility (Phase 6)
    "Utility can be mortgaged" {
        val player = Player("Alice", AlwaysPlayerStrategy())
        val utility =
            UtilityProperty(
                name = "Electric Company",
                position = 12,
                price = 150,
            ).withOwner(player)

        utility.isMortgaged() shouldBe false

        val mortgagedUtility: UtilityProperty = utility.mortgage()

        mortgagedUtility.isMortgaged() shouldBe true
        mortgagedUtility.mortgageValue shouldBe Money(75) // 150 / 2
    }

    // TC-UTILITY-011: Cannot mortgage unowned utility
    "Cannot mortgage unowned utility" {
        val utility =
            UtilityProperty(
                name = "Electric Company",
                position = 12,
                price = 150,
            )

        val exception =
            io.kotest.assertions.throwables.shouldThrow<IllegalArgumentException> {
                utility.mortgage()
            }

        exception.message shouldBe "Cannot mortgage unowned property"
    }

    // TC-UTILITY-012: Cannot mortgage already mortgaged utility
    "Cannot mortgage already mortgaged utility" {
        val player = Player("Alice", AlwaysPlayerStrategy())
        val utility =
            UtilityProperty(
                name = "Electric Company",
                position = 12,
                price = 150,
            ).withOwner(player).mortgage()

        val exception =
            io.kotest.assertions.throwables.shouldThrow<IllegalArgumentException> {
                utility.mortgage()
            }

        exception.message shouldBe "Property is already mortgaged"
    }

    // TC-UTILITY-013: Unmortgage utility
    "Utility can be unmortgaged" {
        val player = Player("Bob", AlwaysPlayerStrategy())
        val utility =
            UtilityProperty(
                name = "Water Works",
                position = 28,
                price = 150,
            ).withOwner(player).mortgage()

        utility.isMortgaged() shouldBe true

        val unmortgagedUtility: UtilityProperty = utility.unmortgage()

        unmortgagedUtility.isMortgaged() shouldBe false
        unmortgagedUtility.unmortgageValue shouldBe Money(82) // (150 / 2) * 1.1 = 82.5 → 82
    }

    // TC-UTILITY-014: Cannot unmortgage unowned utility
    "Cannot unmortgage unowned utility" {
        val utility =
            UtilityProperty(
                name = "Electric Company",
                position = 12,
                price = 150,
            )

        val exception =
            io.kotest.assertions.throwables.shouldThrow<IllegalArgumentException> {
                utility.unmortgage()
            }

        exception.message shouldBe "Cannot unmortgage unowned property"
    }

    // TC-UTILITY-015: Cannot unmortgage non-mortgaged utility
    "Cannot unmortgage non-mortgaged utility" {
        val player = Player("Alice", AlwaysPlayerStrategy())
        val utility =
            UtilityProperty(
                name = "Electric Company",
                position = 12,
                price = 150,
            ).withOwner(player)

        val exception =
            io.kotest.assertions.throwables.shouldThrow<IllegalArgumentException> {
                utility.unmortgage()
            }

        exception.message shouldBe "Property is not mortgaged"
    }
})
