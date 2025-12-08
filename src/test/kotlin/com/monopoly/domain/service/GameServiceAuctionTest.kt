package com.monopoly.domain.service

import com.monopoly.domain.event.GameEvent
import com.monopoly.domain.model.game.Board
import com.monopoly.domain.model.game.Dice
import com.monopoly.domain.model.game.DiceRoll
import com.monopoly.domain.model.game.GameState
import com.monopoly.domain.model.game.Space
import com.monopoly.domain.model.game.SpaceType
import com.monopoly.domain.model.player.Player
import com.monopoly.domain.model.player.PlayerStrategy
import com.monopoly.domain.model.property.ColorGroup
import com.monopoly.domain.model.property.Property
import com.monopoly.domain.model.property.PropertyRent
import com.monopoly.domain.model.property.StreetProperty
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class GameServiceAuctionTest : StringSpec({

    class MockDice(private val rolls: List<DiceRoll>) : Dice {
        private var index = 0

        override fun roll(): DiceRoll {
            return rolls[index++ % rolls.size]
        }
    }

    /**
     * 常に購入を拒否し、オークションで入札する戦略
     */
    class AuctionBidderStrategy(private val bidAmount: Int?) : PlayerStrategy {
        override fun shouldBuy(
            property: Property,
            currentMoney: Int,
        ): Boolean = false

        override fun shouldBuildHouse(
            property: com.monopoly.domain.model.property.StreetProperty,
            currentMoney: Int,
        ): Boolean = false

        override fun shouldBuildHotel(
            property: com.monopoly.domain.model.property.StreetProperty,
            currentMoney: Int,
        ): Boolean = false

        override fun shouldPayToEscapeJail(currentMoney: Int): Boolean = false

        override fun decideAuctionBid(
            property: Property,
            currentBid: Int?,
            currentMoney: Int,
        ): Int? = bidAmount
    }

    /**
     * 常に購入を拒否し、オークションでパスする戦略
     */
    class AuctionPassStrategy : PlayerStrategy {
        override fun shouldBuy(
            property: Property,
            currentMoney: Int,
        ): Boolean = false

        override fun shouldBuildHouse(
            property: com.monopoly.domain.model.property.StreetProperty,
            currentMoney: Int,
        ): Boolean = false

        override fun shouldBuildHotel(
            property: com.monopoly.domain.model.property.StreetProperty,
            currentMoney: Int,
        ): Boolean = false

        override fun shouldPayToEscapeJail(currentMoney: Int): Boolean = false

        override fun decideAuctionBid(
            property: Property,
            currentBid: Int?,
            currentMoney: Int,
        ): Int? = null
    }

    // TC-AUCTION-INTEG-001: プレイヤーが購入を拒否した場合、オークションが開始される
    "when player declines to buy property, auction should start" {
        val alice: Player = Player("Alice", AuctionPassStrategy())
        val bob: Player = Player("Bob", AuctionPassStrategy())
        val players: List<Player> = listOf(alice, bob)

        val property: StreetProperty =
            StreetProperty(
                name = "Mediterranean Avenue",
                position = 3,
                price = 60,
                rent = PropertyRent(2, 10, 30, 90, 160, 250),
                houseCost = 50,
                hotelCost = 50,
                colorGroup = ColorGroup.BROWN,
            )

        val spaces: List<Space> =
            listOf(
                Space.Go(0),
                Space.Other(1, SpaceType.FREE_PARKING),
                Space.Other(2, SpaceType.FREE_PARKING),
                Space.PropertySpace(3, property),
            ) + List(36) { Space.Other(it + 4, SpaceType.FREE_PARKING) }

        val board: Board = Board(spaces)
        val gameState: GameState = GameState(players, board)

        // Alice is at position 0, rolls 3 to land on property at position 3
        val dice: MockDice = MockDice(listOf(DiceRoll(1, 2)))
        val gameService: GameService = GameService(BuildingService(MonopolyCheckerService()))

        gameService.executeTurn(gameState, dice)

        // オークション開始イベントが記録されているか確認
        val auctionStartedEvents: List<GameEvent.AuctionStarted> =
            gameState.events.filterIsInstance<GameEvent.AuctionStarted>()
        auctionStartedEvents.size shouldBe 1
        auctionStartedEvents[0].propertyName shouldBe "Mediterranean Avenue"
        auctionStartedEvents[0].eligiblePlayers shouldBe listOf("Alice", "Bob")
    }

    // TC-AUCTION-INTEG-002: オークションで1人が入札し、他全員がパスした場合、その人が落札
    "when one player bids and others pass, that player wins the auction" {
        val alice: Player = Player("Alice", AuctionBidderStrategy(bidAmount = 50))
        val bob: Player = Player("Bob", AuctionPassStrategy())
        val charlie: Player = Player("Charlie", AuctionPassStrategy())
        val players: List<Player> = listOf(alice, bob, charlie)

        val property: StreetProperty =
            StreetProperty(
                name = "Baltic Avenue",
                position = 3,
                price = 60,
                rent = PropertyRent(4, 20, 60, 180, 320, 450),
                houseCost = 50,
                hotelCost = 50,
                colorGroup = ColorGroup.BROWN,
            )

        val spaces: List<Space> =
            listOf(
                Space.Go(0),
                Space.Other(1, SpaceType.FREE_PARKING),
                Space.Other(2, SpaceType.FREE_PARKING),
                Space.PropertySpace(3, property),
            ) + List(36) { Space.Other(it + 4, SpaceType.FREE_PARKING) }

        val board: Board = Board(spaces)
        val gameState: GameState = GameState(players, board)

        val aliceInitialMoney: Int = alice.money

        // Alice rolls 3 to land on property at position 3
        val dice: MockDice = MockDice(listOf(DiceRoll(1, 2)))
        val gameService: GameService = GameService(BuildingService(MonopolyCheckerService()))

        gameService.executeTurn(gameState, dice)

        // オークション完了イベントが記録されているか確認
        val auctionCompletedEvents: List<GameEvent.AuctionCompleted> =
            gameState.events.filterIsInstance<GameEvent.AuctionCompleted>()
        auctionCompletedEvents.size shouldBe 1
        auctionCompletedEvents[0].propertyName shouldBe "Baltic Avenue"
        auctionCompletedEvents[0].winnerName shouldBe "Alice"
        auctionCompletedEvents[0].winningBid shouldBe 50

        // Aliceがプロパティを所有しているか確認
        alice.ownedProperties.size shouldBe 1
        alice.ownedProperties[0].name shouldBe "Baltic Avenue"

        // Aliceの所持金が減っているか確認
        alice.money shouldBe aliceInitialMoney - 50
    }

    // TC-AUCTION-INTEG-003: 全員がパスした場合、オークション不成立
    "when all players pass, auction fails with no winner" {
        val alice: Player = Player("Alice", AuctionPassStrategy())
        val bob: Player = Player("Bob", AuctionPassStrategy())
        val players: List<Player> = listOf(alice, bob)

        val property: StreetProperty =
            StreetProperty(
                name = "Oriental Avenue",
                position = 6,
                price = 100,
                rent = PropertyRent(6, 30, 90, 270, 400, 550),
                houseCost = 50,
                hotelCost = 50,
                colorGroup = ColorGroup.LIGHT_BLUE,
            )

        val spaces: List<Space> =
            listOf(
                Space.Go(0),
            ) + List(5) { Space.Other(it + 1, SpaceType.FREE_PARKING) } +
                listOf(
                    Space.PropertySpace(6, property),
                ) + List(33) { Space.Other(it + 7, SpaceType.FREE_PARKING) }

        val board: Board = Board(spaces)
        val gameState: GameState = GameState(players, board)

        // Alice rolls 6 to land on property at position 6
        val dice: MockDice = MockDice(listOf(DiceRoll(3, 3)))
        val gameService: GameService = GameService(BuildingService(MonopolyCheckerService()))

        gameService.executeTurn(gameState, dice)

        // オークション完了イベントが記録されているか確認
        val auctionCompletedEvents: List<GameEvent.AuctionCompleted> =
            gameState.events.filterIsInstance<GameEvent.AuctionCompleted>()
        auctionCompletedEvents.size shouldBe 1
        auctionCompletedEvents[0].propertyName shouldBe "Oriental Avenue"
        auctionCompletedEvents[0].winnerName shouldBe null
        auctionCompletedEvents[0].winningBid shouldBe 0

        // プロパティは未所有のまま
        alice.ownedProperties.size shouldBe 0
        bob.ownedProperties.size shouldBe 0
    }

    // TC-AUCTION-INTEG-004: 複数人が入札し合い、最後の1人が落札
    "when multiple players bid, highest bidder wins" {
        // Alice: 50で入札
        // Bob: 60で入札
        // Charlie: 70で入札
        // Alice: パス
        // Bob: パス
        // -> Charlieが70で落札

        class MultiBidderStrategy(private val bids: List<Int?>) : PlayerStrategy {
            private var bidIndex = 0

            override fun shouldBuy(
                property: Property,
                currentMoney: Int,
            ): Boolean = false

            override fun shouldBuildHouse(
                property: com.monopoly.domain.model.property.StreetProperty,
                currentMoney: Int,
            ): Boolean = false

            override fun shouldBuildHotel(
                property: com.monopoly.domain.model.property.StreetProperty,
                currentMoney: Int,
            ): Boolean = false

            override fun shouldPayToEscapeJail(currentMoney: Int): Boolean = false

            override fun decideAuctionBid(
                property: Property,
                currentBid: Int?,
                currentMoney: Int,
            ): Int? {
                if (bidIndex >= bids.size) return null
                return bids[bidIndex++]
            }
        }

        val alice: Player = Player("Alice", MultiBidderStrategy(listOf(50, null)))
        val bob: Player = Player("Bob", MultiBidderStrategy(listOf(60, null)))
        val charlie: Player = Player("Charlie", MultiBidderStrategy(listOf(70)))
        val players: List<Player> = listOf(alice, bob, charlie)

        val property: StreetProperty =
            StreetProperty(
                name = "Vermont Avenue",
                position = 8,
                price = 100,
                rent = PropertyRent(6, 30, 90, 270, 400, 550),
                houseCost = 50,
                hotelCost = 50,
                colorGroup = ColorGroup.LIGHT_BLUE,
            )

        val spaces: List<Space> =
            listOf(
                Space.Go(0),
            ) + List(7) { Space.Other(it + 1, SpaceType.FREE_PARKING) } +
                listOf(
                    Space.PropertySpace(8, property),
                ) + List(31) { Space.Other(it + 9, SpaceType.FREE_PARKING) }

        val board: Board = Board(spaces)
        val gameState: GameState = GameState(players, board)

        val charlieInitialMoney: Int = charlie.money

        // Alice rolls 8 to land on property at position 8
        val dice: MockDice = MockDice(listOf(DiceRoll(4, 4)))
        val gameService: GameService = GameService(BuildingService(MonopolyCheckerService()))

        gameService.executeTurn(gameState, dice)

        // オークション完了イベントが記録されているか確認
        val auctionCompletedEvents: List<GameEvent.AuctionCompleted> =
            gameState.events.filterIsInstance<GameEvent.AuctionCompleted>()
        auctionCompletedEvents.size shouldBe 1
        auctionCompletedEvents[0].propertyName shouldBe "Vermont Avenue"
        auctionCompletedEvents[0].winnerName shouldBe "Charlie"
        auctionCompletedEvents[0].winningBid shouldBe 70

        // Charlieがプロパティを所有しているか確認
        charlie.ownedProperties.size shouldBe 1
        charlie.ownedProperties[0].name shouldBe "Vermont Avenue"
        charlie.money shouldBe charlieInitialMoney - 70

        // 他のプレイヤーは所有していない
        alice.ownedProperties.size shouldBe 0
        bob.ownedProperties.size shouldBe 0
    }

    // TC-AUCTION-INTEG-005: 入札イベントとパスイベントが正しく記録される
    "auction events should be recorded correctly" {
        val alice: Player = Player("Alice", AuctionBidderStrategy(bidAmount = 30))
        val bob: Player = Player("Bob", AuctionPassStrategy())
        val players: List<Player> = listOf(alice, bob)

        val property: StreetProperty =
            StreetProperty(
                name = "Connecticut Avenue",
                position = 9,
                price = 120,
                rent = PropertyRent(8, 40, 100, 300, 450, 600),
                houseCost = 50,
                hotelCost = 50,
                colorGroup = ColorGroup.LIGHT_BLUE,
            )

        val spaces: List<Space> =
            listOf(
                Space.Go(0),
            ) + List(8) { Space.Other(it + 1, SpaceType.FREE_PARKING) } +
                listOf(
                    Space.PropertySpace(9, property),
                ) + List(30) { Space.Other(it + 10, SpaceType.FREE_PARKING) }

        val board: Board = Board(spaces)
        val gameState: GameState = GameState(players, board)

        val dice: MockDice = MockDice(listOf(DiceRoll(4, 5)))
        val gameService: GameService = GameService(BuildingService(MonopolyCheckerService()))

        gameService.executeTurn(gameState, dice)

        // イベントの順序を確認
        val auctionEvents: List<GameEvent> =
            gameState.events.filter {
                it is GameEvent.AuctionStarted ||
                    it is GameEvent.PlayerBidInAuction ||
                    it is GameEvent.PlayerPassedInAuction ||
                    it is GameEvent.AuctionCompleted
            }

        auctionEvents.size shouldBe 4

        // 1. AuctionStarted
        auctionEvents[0].shouldBeInstanceOf<GameEvent.AuctionStarted>()
        (auctionEvents[0] as GameEvent.AuctionStarted).propertyName shouldBe "Connecticut Avenue"

        // 2. Alice bids
        auctionEvents[1].shouldBeInstanceOf<GameEvent.PlayerBidInAuction>()
        val aliceBid: GameEvent.PlayerBidInAuction = auctionEvents[1] as GameEvent.PlayerBidInAuction
        aliceBid.playerName shouldBe "Alice"
        aliceBid.bidAmount shouldBe 30

        // 3. Bob passes
        auctionEvents[2].shouldBeInstanceOf<GameEvent.PlayerPassedInAuction>()
        val bobPass: GameEvent.PlayerPassedInAuction = auctionEvents[2] as GameEvent.PlayerPassedInAuction
        bobPass.playerName shouldBe "Bob"

        // 4. AuctionCompleted
        auctionEvents[3].shouldBeInstanceOf<GameEvent.AuctionCompleted>()
        val completed: GameEvent.AuctionCompleted = auctionEvents[3] as GameEvent.AuctionCompleted
        completed.winnerName shouldBe "Alice"
        completed.winningBid shouldBe 30
    }
})
