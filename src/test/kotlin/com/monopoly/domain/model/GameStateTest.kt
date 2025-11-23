package com.monopoly.domain.model

import com.monopoly.domain.event.GameEvent
import com.monopoly.domain.strategy.AlwaysBuyStrategy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class GameStateTest : StringSpec({
    // TC-030: GameState初期化
    // Given: 2人のPlayer
    // When: 新しいGameStateを作成
    // Then: プレイヤーが登録され、currentPlayerIndexが0、gameOverがfalse
    "game state should be initialized correctly" {
        val player1 = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val player2 = Player(name = "Bob", strategy = AlwaysBuyStrategy())
        val board = BoardFixtures.createStandardBoard()

        val gameState = GameState(listOf(player1, player2), board)

        gameState.players.size shouldBe 2
        gameState.currentPlayer shouldBe player1
        gameState.isGameOver shouldBe false
    }

    // TC-031: 現在のプレイヤー取得
    // Given: GameStateでcurrentPlayerIndex=1
    // When: getCurrentPlayer()
    // Then: プレイヤー2を返す
    "should return current player" {
        val player1 = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val player2 = Player(name = "Bob", strategy = AlwaysBuyStrategy())
        val board = BoardFixtures.createStandardBoard()
        val gameState = GameState(listOf(player1, player2), board)

        // 最初のプレイヤーをスキップして次へ
        gameState.nextPlayer()

        gameState.currentPlayer shouldBe player2
    }

    // TC-032: 次のプレイヤーに交代
    // Given: GameStateでcurrentPlayerIndex=0、プレイヤー3人
    // When: nextPlayer()
    // Then: currentPlayerがplayer2
    "should move to next player" {
        val player1 = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val player2 = Player(name = "Bob", strategy = AlwaysBuyStrategy())
        val player3 = Player(name = "Charlie", strategy = AlwaysBuyStrategy())
        val board = BoardFixtures.createStandardBoard()
        val gameState = GameState(listOf(player1, player2, player3), board)

        gameState.nextPlayer()

        gameState.currentPlayer shouldBe player2
    }

    // TC-033: 破産プレイヤーをスキップ
    // Given: GameStateでプレイヤー2が破産、currentPlayerIndex=0
    // When: nextPlayer()
    // Then: currentPlayerがplayer3
    "should skip bankrupt player" {
        val player1 = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val player2 = Player(name = "Bob", strategy = AlwaysBuyStrategy())
        val player3 = Player(name = "Charlie", strategy = AlwaysBuyStrategy())
        val board = BoardFixtures.createStandardBoard()
        val gameState = GameState(listOf(player1, player2, player3), board)

        // プレイヤー2を破産させる
        player2.markAsBankrupt()

        gameState.nextPlayer()

        gameState.currentPlayer shouldBe player3
    }

    // TC-034: アクティブプレイヤー数
    // Given: GameStateで3人中1人が破産
    // When: getActivePlayerCount()
    // Then: 2
    "should count active players correctly" {
        val player1 = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val player2 = Player(name = "Bob", strategy = AlwaysBuyStrategy())
        val player3 = Player(name = "Charlie", strategy = AlwaysBuyStrategy())
        val board = BoardFixtures.createStandardBoard()
        val gameState = GameState(listOf(player1, player2, player3), board)

        // プレイヤー2を破産させる
        player2.markAsBankrupt()

        gameState.getActivePlayerCount() shouldBe 2
    }

    // TC-035: ゲームオーバーフラグの設定
    "should set game over flag" {
        val player1 = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val player2 = Player(name = "Bob", strategy = AlwaysBuyStrategy())
        val board = BoardFixtures.createStandardBoard()
        val gameState = GameState(listOf(player1, player2), board)

        gameState.endGame()

        gameState.isGameOver shouldBe true
    }

    // TC-036: ターン番号の取得
    "should get turn number" {
        val player1 = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val player2 = Player(name = "Bob", strategy = AlwaysBuyStrategy())
        val board = BoardFixtures.createStandardBoard()
        val gameState = GameState(listOf(player1, player2), board)

        gameState.turnNumber shouldBe 0
    }

    // TC-037: ターン番号の増加
    "should increment turn number" {
        val player1 = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val player2 = Player(name = "Bob", strategy = AlwaysBuyStrategy())
        val board = BoardFixtures.createStandardBoard()
        val gameState = GameState(listOf(player1, player2), board)

        gameState.incrementTurnNumber()
        gameState.incrementTurnNumber()

        gameState.turnNumber shouldBe 2
    }

    // TC-038: プレイヤーが1周する
    "should cycle through all players" {
        val player1 = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val player2 = Player(name = "Bob", strategy = AlwaysBuyStrategy())
        val board = BoardFixtures.createStandardBoard()
        val gameState = GameState(listOf(player1, player2), board)

        gameState.nextPlayer()
        gameState.nextPlayer()

        gameState.currentPlayer shouldBe player1
    }

    // TC-039: 最後の1人が残った場合
    "should handle when only one player is active" {
        val player1 = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val player2 = Player(name = "Bob", strategy = AlwaysBuyStrategy())
        val board = BoardFixtures.createStandardBoard()
        val gameState = GameState(listOf(player1, player2), board)

        player2.markAsBankrupt()

        gameState.getActivePlayerCount() shouldBe 1
    }

    // TC-040: 全プレイヤーが破産していない状態でのアクティブプレイヤー数
    "should count all players as active when no one is bankrupt" {
        val player1 = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val player2 = Player(name = "Bob", strategy = AlwaysBuyStrategy())
        val player3 = Player(name = "Charlie", strategy = AlwaysBuyStrategy())
        val board = BoardFixtures.createStandardBoard()
        val gameState = GameState(listOf(player1, player2, player3), board)

        gameState.getActivePlayerCount() shouldBe 3
    }

    // Phase 2: イベントログ機能のテスト

    // TC-220: eventsフィールドが初期化される
    // Given: なし
    // When: GameStateを作成（デフォルト引数）
    // Then: events.size()が0、eventsがMutableList
    "events field should be initialized as empty mutable list" {
        val player1 = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val player2 = Player(name = "Bob", strategy = AlwaysBuyStrategy())
        val board = BoardFixtures.createStandardBoard()

        val gameState = GameState(listOf(player1, player2), board)

        gameState.events.size shouldBe 0
        gameState.events::class.simpleName shouldBe "ArrayList"
    }

    // TC-221: イベントを追加できる
    // Given: GameState
    // When: events.add(GameStarted(...))
    // Then: events.size()が1、eventsにGameStartedが含まれる
    "should be able to add event" {
        val player1 = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val player2 = Player(name = "Bob", strategy = AlwaysBuyStrategy())
        val board = BoardFixtures.createStandardBoard()
        val gameState = GameState(listOf(player1, player2), board)

        val event =
            GameEvent.GameStarted(
                turnNumber = 0,
                timestamp = System.currentTimeMillis(),
                playerNames = listOf("Alice", "Bob"),
            )
        gameState.events.add(event)

        gameState.events.size shouldBe 1
        gameState.events[0] shouldBe event
    }

    // TC-222: イベントが追加順に記録される
    // Given: GameState
    // When: events.add(GameStarted(...))、events.add(TurnStarted(...))、events.add(DiceRolled(...))
    // Then: events[0]がGameStarted、events[1]がTurnStarted、events[2]がDiceRolled
    "events should be recorded in order" {
        val player1 = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val player2 = Player(name = "Bob", strategy = AlwaysBuyStrategy())
        val board = BoardFixtures.createStandardBoard()
        val gameState = GameState(listOf(player1, player2), board)

        val gameStarted =
            GameEvent.GameStarted(
                turnNumber = 0,
                timestamp = System.currentTimeMillis(),
                playerNames = listOf("Alice", "Bob"),
            )
        val turnStarted =
            GameEvent.TurnStarted(
                turnNumber = 1,
                timestamp = System.currentTimeMillis(),
                playerName = "Alice",
            )
        val diceRolled =
            GameEvent.DiceRolled(
                turnNumber = 1,
                timestamp = System.currentTimeMillis(),
                playerName = "Alice",
                die1 = 3,
                die2 = 4,
                total = 7,
            )

        gameState.events.add(gameStarted)
        gameState.events.add(turnStarted)
        gameState.events.add(diceRolled)

        gameState.events.size shouldBe 3
        gameState.events[0] shouldBe gameStarted
        gameState.events[1] shouldBe turnStarted
        gameState.events[2] shouldBe diceRolled
    }

    // TC-041: プロパティを解放する（デメテルの法則）
    // Given: GameState、ボード上に所有されているプロパティ
    // When: releaseProperty(property)
    // Then: ボード上のプロパティが未所有になる
    "should release property on board" {
        val player1: Player = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val property: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Mediterranean Avenue",
                position = 1,
                price = 200,
                baseRent = 20,
                colorGroup = ColorGroup.BROWN,
            )
        val board = BoardFixtures.createBoardWithProperties(listOf(property))
        val gameState = GameState(listOf(player1), board)

        // プロパティを所有状態にする
        val ownedProperty: Property = property.withOwner(player1)
        board.updateProperty(ownedProperty)

        // プロパティを解放
        val releasedProperty: Property = ownedProperty.withoutOwner()
        gameState.releaseProperty(releasedProperty)

        // ボード上のプロパティが未所有になっている
        val propertyOnBoard: Property? = board.getPropertyAt(1)
        propertyOnBoard shouldBe releasedProperty
    }

    // TC-042: プロパティを更新する（デメテルの法則）
    // Given: GameState、ボード上の未所有プロパティ
    // When: updateProperty(ownedProperty)
    // Then: ボード上のプロパティが所有状態になる
    "should update property on board" {
        val player1: Player = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val property: Property =
            PropertyTestFixtures.createTestProperty(
                name = "Mediterranean Avenue",
                position = 1,
                price = 200,
                baseRent = 20,
                colorGroup = ColorGroup.BROWN,
            )
        val board = BoardFixtures.createBoardWithProperties(listOf(property))
        val gameState = GameState(listOf(player1), board)

        // プロパティを所有状態にする
        val ownedProperty: Property = property.withOwner(player1)
        gameState.updateProperty(ownedProperty)

        // ボード上のプロパティが所有状態になっている
        val propertyOnBoard: Property? = board.getPropertyAt(1)
        propertyOnBoard shouldBe ownedProperty
    }
})
