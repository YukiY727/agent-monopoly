package com.monopoly.domain.model.game

import com.monopoly.domain.model.player.Player
import com.monopoly.domain.model.property.ColorGroup
import com.monopoly.domain.model.property.PropertyRent
import com.monopoly.domain.model.property.StreetProperty
import com.monopoly.domain.strategy.AlwaysPlayerStrategy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class AuctionTest : StringSpec({

    // TC-AUCTION-001: Start a new auction
    "Start a new auction with property and players" {
        val property: StreetProperty = StreetProperty(
            name = "Mediterranean Avenue",
            position = 1,
            price = 60,
            rent = PropertyRent(2, 10, 30, 90, 160, 250),
            houseCost = 50,
            hotelCost = 50,
            colorGroup = ColorGroup.BROWN
        )
        val players: List<Player> = listOf(
            Player("Alice", AlwaysPlayerStrategy()),
            Player("Bob", AlwaysPlayerStrategy()),
            Player("Charlie", AlwaysPlayerStrategy())
        )

        val auction: Auction = Auction.start(property, players)

        auction.shouldBeInstanceOf<Auction.InProgress>()
        val inProgress: Auction.InProgress = auction as Auction.InProgress
        inProgress.property.name shouldBe "Mediterranean Avenue"
        inProgress.eligiblePlayers shouldBe players
        inProgress.currentBid shouldBe null
        inProgress.passedPlayers shouldBe emptySet()
    }

    // TC-AUCTION-002: Player makes a bid
    "Player can make a bid" {
        val property: StreetProperty = StreetProperty(
            name = "Baltic Avenue",
            position = 3,
            price = 60,
            rent = PropertyRent(4, 20, 60, 180, 320, 450),
            houseCost = 50,
            hotelCost = 50,
            colorGroup = ColorGroup.BROWN
        )
        val alice: Player = Player("Alice", AlwaysPlayerStrategy())
        val bob: Player = Player("Bob", AlwaysPlayerStrategy())
        val players: List<Player> = listOf(alice, bob)

        val auction: Auction = Auction.start(property, players)
        val afterBid: Auction = auction.placeBid(alice, 10)

        afterBid.shouldBeInstanceOf<Auction.InProgress>()
        val inProgress: Auction.InProgress = afterBid as Auction.InProgress
        inProgress.currentBid shouldBe Bid(alice, 10)
        inProgress.passedPlayers shouldBe emptySet()
    }

    // TC-AUCTION-003: Player passes
    "Player can pass" {
        val property: StreetProperty = StreetProperty(
            name = "Oriental Avenue",
            position = 6,
            price = 100,
            rent = PropertyRent(6, 30, 90, 270, 400, 550),
            houseCost = 50,
            hotelCost = 50,
            colorGroup = ColorGroup.LIGHT_BLUE
        )
        val alice: Player = Player("Alice", AlwaysPlayerStrategy())
        val bob: Player = Player("Bob", AlwaysPlayerStrategy())
        val charlie: Player = Player("Charlie", AlwaysPlayerStrategy())
        val players: List<Player> = listOf(alice, bob, charlie)

        val auction: Auction = Auction.start(property, players)
        val afterPass: Auction = auction.pass(alice)

        afterPass.shouldBeInstanceOf<Auction.InProgress>()
        val inProgress: Auction.InProgress = afterPass as Auction.InProgress
        inProgress.passedPlayers shouldBe setOf(alice)
        inProgress.currentBid shouldBe null
    }

    // TC-AUCTION-004: All players except one pass, that player wins
    "When all players except one pass, that player wins the auction" {
        val property: StreetProperty = StreetProperty(
            name = "Vermont Avenue",
            position = 8,
            price = 100,
            rent = PropertyRent(6, 30, 90, 270, 400, 550),
            houseCost = 50,
            hotelCost = 50,
            colorGroup = ColorGroup.LIGHT_BLUE
        )
        val alice: Player = Player("Alice", AlwaysPlayerStrategy())
        val bob: Player = Player("Bob", AlwaysPlayerStrategy())
        val charlie: Player = Player("Charlie", AlwaysPlayerStrategy())
        val players: List<Player> = listOf(alice, bob, charlie)

        val auction: Auction = Auction.start(property, players)
            .placeBid(alice, 20)
            .pass(bob)
            .pass(charlie)

        auction.shouldBeInstanceOf<Auction.Completed>()
        val completed: Auction.Completed = auction as Auction.Completed
        completed.winner shouldBe alice
        completed.winningBid shouldBe 20
    }

    // TC-AUCTION-005: Minimum bid is $1
    "Minimum bid must be at least $1" {
        val property: StreetProperty = StreetProperty(
            name = "Connecticut Avenue",
            position = 9,
            price = 120,
            rent = PropertyRent(8, 40, 100, 300, 450, 600),
            houseCost = 50,
            hotelCost = 50,
            colorGroup = ColorGroup.LIGHT_BLUE
        )
        val alice: Player = Player("Alice", AlwaysPlayerStrategy())
        val bob: Player = Player("Bob", AlwaysPlayerStrategy())
        val players: List<Player> = listOf(alice, bob)

        val auction: Auction = Auction.start(property, players)

        shouldThrow<IllegalArgumentException> {
            auction.placeBid(alice, 0)
        }
    }

    // TC-AUCTION-006: Bid must be higher than current bid
    "Bid must be higher than current bid" {
        val property: StreetProperty = StreetProperty(
            name = "St. Charles Place",
            position = 11,
            price = 140,
            rent = PropertyRent(10, 50, 150, 450, 625, 750),
            houseCost = 100,
            hotelCost = 100,
            colorGroup = ColorGroup.PINK
        )
        val alice: Player = Player("Alice", AlwaysPlayerStrategy())
        val bob: Player = Player("Bob", AlwaysPlayerStrategy())
        val players: List<Player> = listOf(alice, bob)

        val auction: Auction = Auction.start(property, players)
            .placeBid(alice, 20)

        shouldThrow<IllegalArgumentException> {
            auction.placeBid(bob, 20)
        }

        shouldThrow<IllegalArgumentException> {
            auction.placeBid(bob, 15)
        }
    }

    // TC-AUCTION-007: Cannot bid more than player's money
    "Cannot bid more than player's money" {
        val property: StreetProperty = StreetProperty(
            name = "States Avenue",
            position = 13,
            price = 140,
            rent = PropertyRent(10, 50, 150, 450, 625, 750),
            houseCost = 100,
            hotelCost = 100,
            colorGroup = ColorGroup.PINK
        )
        val alice: Player = Player("Alice", AlwaysPlayerStrategy())
        val bob: Player = Player("Bob", AlwaysPlayerStrategy())
        val players: List<Player> = listOf(alice, bob)

        val auction: Auction = Auction.start(property, players)

        shouldThrow<IllegalArgumentException> {
            auction.placeBid(alice, alice.money + 1)
        }
    }

    // TC-AUCTION-008: Player who passed cannot bid
    "Player who passed cannot bid again" {
        val property: StreetProperty = StreetProperty(
            name = "Virginia Avenue",
            position = 14,
            price = 160,
            rent = PropertyRent(12, 60, 180, 500, 700, 900),
            houseCost = 100,
            hotelCost = 100,
            colorGroup = ColorGroup.PINK
        )
        val alice: Player = Player("Alice", AlwaysPlayerStrategy())
        val bob: Player = Player("Bob", AlwaysPlayerStrategy())
        val players: List<Player> = listOf(alice, bob)

        val auction: Auction = Auction.start(property, players)
            .pass(alice)

        shouldThrow<IllegalArgumentException> {
            auction.placeBid(alice, 10)
        }
    }

    // TC-AUCTION-009: No bids means auction fails
    "When all players pass without bidding, auction fails" {
        val property: StreetProperty = StreetProperty(
            name = "St. James Place",
            position = 16,
            price = 180,
            rent = PropertyRent(14, 70, 200, 550, 750, 950),
            houseCost = 100,
            hotelCost = 100,
            colorGroup = ColorGroup.ORANGE
        )
        val alice: Player = Player("Alice", AlwaysPlayerStrategy())
        val bob: Player = Player("Bob", AlwaysPlayerStrategy())
        val players: List<Player> = listOf(alice, bob)

        val auction: Auction = Auction.start(property, players)
            .pass(alice)
            .pass(bob)

        auction.shouldBeInstanceOf<Auction.Completed>()
        val completed: Auction.Completed = auction as Auction.Completed
        completed.winner shouldBe null
        completed.winningBid shouldBe 0
    }

    // TC-AUCTION-010: Cannot operate on completed auction
    "Cannot place bid or pass on completed auction" {
        val property: StreetProperty = StreetProperty(
            name = "Tennessee Avenue",
            position = 18,
            price = 180,
            rent = PropertyRent(14, 70, 200, 550, 750, 950),
            houseCost = 100,
            hotelCost = 100,
            colorGroup = ColorGroup.ORANGE
        )
        val alice: Player = Player("Alice", AlwaysPlayerStrategy())
        val bob: Player = Player("Bob", AlwaysPlayerStrategy())
        val players: List<Player> = listOf(alice, bob)

        val auction: Auction = Auction.start(property, players)
            .placeBid(alice, 50)
            .pass(bob)

        auction.shouldBeInstanceOf<Auction.Completed>()

        shouldThrow<IllegalStateException> {
            auction.placeBid(alice, 60)
        }

        shouldThrow<IllegalStateException> {
            auction.pass(bob)
        }
    }
})
