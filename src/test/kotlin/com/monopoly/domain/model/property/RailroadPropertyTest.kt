package com.monopoly.domain.model.property

import com.monopoly.domain.model.core.Money
import com.monopoly.domain.model.player.Player
import com.monopoly.domain.strategy.AlwaysPlayerStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class RailroadPropertyTest : StringSpec({

    // TC-RAILROAD-001: Calculate rent with 1 railroad
    "Rent with 1 railroad should be $25" {
        val railroad = RailroadProperty(
            name = "Reading Railroad",
            position = 5,
            price = 200
        )

        val rent: Int = railroad.calculateRentWithCount(1)

        rent shouldBe 25
    }

    // TC-RAILROAD-002: Calculate rent with 2 railroads
    "Rent with 2 railroads should be $50" {
        val railroad = RailroadProperty(
            name = "Pennsylvania Railroad",
            position = 15,
            price = 200
        )

        val rent: Int = railroad.calculateRentWithCount(2)

        rent shouldBe 50
    }

    // TC-RAILROAD-003: Calculate rent with 3 railroads
    "Rent with 3 railroads should be $100" {
        val railroad = RailroadProperty(
            name = "B&O Railroad",
            position = 25,
            price = 200
        )

        val rent: Int = railroad.calculateRentWithCount(3)

        rent shouldBe 100
    }

    // TC-RAILROAD-004: Calculate rent with 4 railroads
    "Rent with 4 railroads should be $200" {
        val railroad = RailroadProperty(
            name = "Short Line",
            position = 35,
            price = 200
        )

        val rent: Int = railroad.calculateRentWithCount(4)

        rent shouldBe 200
    }

    // TC-RAILROAD-005: Calculate rent with 0 railroads (edge case)
    "Rent with 0 railroads should be $0" {
        val railroad = RailroadProperty(
            name = "Reading Railroad",
            position = 5,
            price = 200
        )

        val rent: Int = railroad.calculateRentWithCount(0)

        rent shouldBe 0
    }

    // TC-RAILROAD-006: Railroad ownership
    "Railroad can be owned by player" {
        val player = Player("Alice", AlwaysPlayerStrategy())
        val railroad = RailroadProperty(
            name = "Reading Railroad",
            position = 5,
            price = 200
        )

        railroad.isOwned() shouldBe false

        val ownedRailroad: RailroadProperty = railroad.withOwner(player)

        ownedRailroad.isOwned() shouldBe true
        ownedRailroad.ownership shouldBe PropertyOwnership.OwnedByPlayer(player)
    }

    // TC-RAILROAD-007: Railroad can be released
    "Railroad can be released from ownership" {
        val player = Player("Bob", AlwaysPlayerStrategy())
        val railroad = RailroadProperty(
            name = "Reading Railroad",
            position = 5,
            price = 200
        ).withOwner(player)

        railroad.isOwned() shouldBe true

        val releasedRailroad: RailroadProperty = railroad.withoutOwner()

        releasedRailroad.isOwned() shouldBe false
        releasedRailroad.ownership shouldBe PropertyOwnership.Unowned
    }

    // TC-RAILROAD-008: Railroad price value
    "Railroad price value should match price" {
        val railroad = RailroadProperty(
            name = "Reading Railroad",
            position = 5,
            price = 200
        )

        railroad.priceValue shouldBe Money(200)
    }
})
