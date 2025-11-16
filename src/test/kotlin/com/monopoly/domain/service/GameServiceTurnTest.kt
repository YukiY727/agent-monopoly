package com.monopoly.domain.service

import com.monopoly.domain.model.BoardFixtures
import com.monopoly.domain.model.Dice
import com.monopoly.domain.model.GameEvent
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Player
import com.monopoly.domain.strategy.AlwaysBuyStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.random.Random

class GameServiceTurnTest : StringSpec({
    val gameService = GameService()

    // TC-160: 1ターンの流れ
    // Given: GameState
    // When: executeTurn(gameState)
    // Then: サイコロが振られ、プレイヤーが移動し、マス目処理が実行され、ターン番号が増加
    "should execute one turn with dice roll, movement, space processing, and turn increment" {
        val player1 = Player("Alice", AlwaysBuyStrategy())
        val player2 = Player("Bob", AlwaysBuyStrategy())
        val gameState =
            GameState(
                players = listOf(player1, player2),
                board = BoardFixtures.createStandardBoard(),
            )

        val initialTurnNumber = gameState.turnNumber
        val initialPosition = player1.position
        val dice = Dice(Random(42)) // 固定シードで再現性を確保

        gameService.executeTurn(gameState, dice)

        // プレイヤーが移動した
        player1.position shouldBeGreaterThan initialPosition
        // ターン番号が増加した
        gameState.turnNumber shouldBe initialTurnNumber + 1
    }

    // TC-161: ターン後のプレイヤー交代
    // Given: GameStateでcurrentPlayerIndex=0、2人プレイヤー
    // When: executeTurn(gameState)
    // Then: currentPlayerIndexが1
    "should switch to next player after turn execution" {
        val player1 = Player("Alice", AlwaysBuyStrategy())
        val player2 = Player("Bob", AlwaysBuyStrategy())
        val gameState =
            GameState(
                players = listOf(player1, player2),
                board = BoardFixtures.createStandardBoard(),
            )

        val initialPlayer = gameState.currentPlayer
        initialPlayer shouldBe player1

        val dice = Dice(Random(42))
        gameService.executeTurn(gameState, dice)

        gameState.currentPlayer shouldBe player2
    }

    // TC-235: executeTurn実行後、TurnStarted, TurnEndedイベントが記録される
    // Given: GameState
    // When: executeTurn(gameState, dice)
    // Then: gameState.eventsにTurnStarted, TurnEndedイベントが追加されている
    "should record TurnStarted and TurnEnded events when executing turn" {
        val player1 = Player("Alice", AlwaysBuyStrategy())
        val player2 = Player("Bob", AlwaysBuyStrategy())
        val gameState =
            GameState(
                players = listOf(player1, player2),
                board = BoardFixtures.createStandardBoard(),
            )

        val dice = Dice(Random(42))
        val initialEventCount: Int = gameState.events.size

        gameService.executeTurn(gameState, dice)

        // イベントが追加されている
        gameState.events.size shouldBeGreaterThan initialEventCount

        // TurnStartedイベントが記録されている
        val turnStartedEvents: List<GameEvent.TurnStarted> =
            gameState.events.filterIsInstance<GameEvent.TurnStarted>()
        turnStartedEvents.size shouldBe 1
        val turnStartedEvent: GameEvent.TurnStarted = turnStartedEvents.first()
        turnStartedEvent.playerName shouldBe "Alice"
        turnStartedEvent.turnNumber shouldBe 1

        // TurnEndedイベントが記録されている
        val turnEndedEvents: List<GameEvent.TurnEnded> =
            gameState.events.filterIsInstance<GameEvent.TurnEnded>()
        turnEndedEvents.size shouldBe 1
        val turnEndedEvent: GameEvent.TurnEnded = turnEndedEvents.first()
        turnEndedEvent.playerName shouldBe "Alice"
        turnEndedEvent.turnNumber shouldBe 1
    }

    // TC-237: runGame実行後、GameStarted, GameEndedイベントが記録される
    // Given: GameState
    // When: runGame(gameState, dice, maxTurns)
    // Then: gameState.eventsの最初がGameStarted、最後がGameEnded
    "should record GameStarted and GameEnded events when running game" {
        val player1 = Player("Alice", AlwaysBuyStrategy())
        val player2 = Player("Bob", AlwaysBuyStrategy())
        val gameState =
            GameState(
                players = listOf(player1, player2),
                board = BoardFixtures.createStandardBoard(),
            )

        val dice = Dice(Random(42))
        gameService.runGame(gameState, dice, maxTurns = 10)

        // GameStartedイベントが最初に記録されている
        val firstEvent: GameEvent = gameState.events.first()
        firstEvent.shouldBeInstanceOf<GameEvent.GameStarted>()
        val gameStartedEvent: GameEvent.GameStarted = firstEvent as GameEvent.GameStarted
        gameStartedEvent.playerNames shouldBe listOf("Alice", "Bob")
        gameStartedEvent.turnNumber shouldBe 0

        // GameEndedイベントが最後に記録されている
        val lastEvent: GameEvent = gameState.events.last()
        lastEvent.shouldBeInstanceOf<GameEvent.GameEnded>()
    }

    // TC-238: GameEndedイベントに正しい勝者と総ターン数が記録される
    // Given: GameState
    // When: runGame(gameState, dice, maxTurns)
    // Then: GameEndedイベントのwinnerが破産していないプレイヤー、totalTurnsが正しい
    "should record correct winner and total turns in GameEnded event" {
        val player1 = Player("Alice", AlwaysBuyStrategy())
        val player2 = Player("Bob", AlwaysBuyStrategy())
        val gameState =
            GameState(
                players = listOf(player1, player2),
                board = BoardFixtures.createStandardBoard(),
            )

        val dice = Dice(Random(42))
        val winner: Player = gameService.runGame(gameState, dice, maxTurns = 100)

        // GameEndedイベントを取得
        val gameEndedEvents: List<GameEvent.GameEnded> =
            gameState.events.filterIsInstance<GameEvent.GameEnded>()
        gameEndedEvents.size shouldBe 1
        val gameEndedEvent: GameEvent.GameEnded = gameEndedEvents.first()

        // 勝者が記録されている
        gameEndedEvent.winner shouldBe winner.name

        // 総ターン数が記録されている（実際のターン数と一致）
        gameEndedEvent.totalTurns shouldBe gameState.turnNumber
    }
})
