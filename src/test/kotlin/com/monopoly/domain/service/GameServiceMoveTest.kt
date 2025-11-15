package com.monopoly.domain.service

import com.monopoly.domain.model.Board
import com.monopoly.domain.model.BoardFixtures
import com.monopoly.domain.model.GameEvent
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Player
import com.monopoly.domain.strategy.AlwaysBuyStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

class GameServiceMoveTest : StringSpec({
    // TC-101: 通常の移動
    // Given: 位置0のPlayer、GameState
    // When: movePlayer(player, 7, gameState)
    // Then: プレイヤーの位置が7
    "should move player to new position normally" {
        // Given
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val gameService = GameService()

        // When
        gameService.movePlayer(player, 7)

        // Then
        player.position shouldBe 7
    }

    // TC-102: GO通過（$200獲得）
    // Given: 位置38のPlayer、所持金$1500
    // When: movePlayer(player, 5, gameState)
    // Then: 位置が3、所持金が$1700
    "should pass GO and receive $200" {
        // Given
        val player = Player(name = "Bob", strategy = AlwaysBuyStrategy())
        player.setPosition(38)
        val gameService = GameService()
        val initialMoney = player.money

        // When
        gameService.movePlayer(player, 5)

        // Then
        player.position shouldBe 3
        player.money shouldBe initialMoney + 200
    }

    // TC-103: 位置の循環（mod 40）
    // Given: 位置39のPlayer
    // When: movePlayer(player, 1, gameState)
    // Then: 位置が0
    "should wrap around board when reaching position 40" {
        // Given
        val player = Player(name = "Carol", strategy = AlwaysBuyStrategy())
        player.setPosition(39)
        val gameService = GameService()

        // When
        gameService.movePlayer(player, 1)

        // Then
        player.position shouldBe 0
    }

    // Phase 2: イベント記録のテスト

    // TC-230: movePlayer実行後、PlayerMovedイベントが記録される
    // Given: 位置0のPlayer、GameState
    // When: movePlayer(player, 7, gameState)
    // Then: gameState.eventsにPlayerMovedイベントが追加されている、fromPositionが0、toPositionが7
    "should record PlayerMoved event when moving player" {
        // Given
        val player = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val board = BoardFixtures.createStandardBoard()
        val gameState = GameState(listOf(player), board)
        val gameService = GameService()

        // When
        gameService.movePlayer(player, 7, gameState)

        // Then
        player.position shouldBe 7
        val movedEvent = gameState.events.filterIsInstance<GameEvent.PlayerMoved>().firstOrNull()
        movedEvent shouldNotBe null
        movedEvent?.fromPosition shouldBe 0
        movedEvent?.toPosition shouldBe 7
        movedEvent?.passedGo shouldBe false
        movedEvent?.playerName shouldBe "Alice"
    }

    // TC-231: movePlayerでGO通過時、passedGoがtrue
    // Given: 位置38のPlayer、GameState
    // When: movePlayer(player, 5, gameState)
    // Then: PlayerMovedイベントのpassedGoがtrue
    "should record PlayerMoved event with passedGo=true when passing GO" {
        // Given
        val player = Player(name = "Bob", strategy = AlwaysBuyStrategy())
        player.setPosition(38)
        val board = BoardFixtures.createStandardBoard()
        val gameState = GameState(listOf(player), board)
        val gameService = GameService()

        // When
        gameService.movePlayer(player, 5, gameState)

        // Then
        player.position shouldBe 3
        val movedEvent = gameState.events.filterIsInstance<GameEvent.PlayerMoved>().firstOrNull()
        movedEvent shouldNotBe null
        movedEvent?.fromPosition shouldBe 38
        movedEvent?.toPosition shouldBe 3
        movedEvent?.passedGo shouldBe true
        movedEvent?.playerName shouldBe "Bob"
    }
})
