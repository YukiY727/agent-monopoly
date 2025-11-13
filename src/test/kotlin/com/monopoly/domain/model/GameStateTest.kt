package com.monopoly.domain.model

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
        val board = Board()

        val gameState = GameState(listOf(player1, player2), board)

        gameState.getPlayers().size shouldBe 2
        gameState.getCurrentPlayerIndex() shouldBe 0
        gameState.isGameOver() shouldBe false
    }

    // TC-031: 現在のプレイヤー取得
    // Given: GameStateでcurrentPlayerIndex=1
    // When: getCurrentPlayer()
    // Then: プレイヤー2を返す
    "should return current player" {
        val player1 = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val player2 = Player(name = "Bob", strategy = AlwaysBuyStrategy())
        val board = Board()
        val gameState = GameState(listOf(player1, player2), board)

        // 最初のプレイヤーをスキップして次へ
        gameState.nextPlayer()

        gameState.getCurrentPlayer() shouldBe player2
    }

    // TC-032: 次のプレイヤーに交代
    // Given: GameStateでcurrentPlayerIndex=0、プレイヤー3人
    // When: nextPlayer()
    // Then: currentPlayerIndexが1
    "should move to next player" {
        val player1 = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val player2 = Player(name = "Bob", strategy = AlwaysBuyStrategy())
        val player3 = Player(name = "Charlie", strategy = AlwaysBuyStrategy())
        val board = Board()
        val gameState = GameState(listOf(player1, player2, player3), board)

        gameState.nextPlayer()

        gameState.getCurrentPlayerIndex() shouldBe 1
    }

    // TC-033: 破産プレイヤーをスキップ
    // Given: GameStateでプレイヤー2が破産、currentPlayerIndex=0
    // When: nextPlayer()
    // Then: currentPlayerIndexが2（プレイヤー3）
    "should skip bankrupt player" {
        val player1 = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val player2 = Player(name = "Bob", strategy = AlwaysBuyStrategy())
        val player3 = Player(name = "Charlie", strategy = AlwaysBuyStrategy())
        val board = Board()
        val gameState = GameState(listOf(player1, player2, player3), board)

        // プレイヤー2を破産させる
        player2.markAsBankrupt()

        gameState.nextPlayer()

        gameState.getCurrentPlayerIndex() shouldBe 2
        gameState.getCurrentPlayer() shouldBe player3
    }

    // TC-034: アクティブプレイヤー数
    // Given: GameStateで3人中1人が破産
    // When: getActivePlayerCount()
    // Then: 2
    "should count active players correctly" {
        val player1 = Player(name = "Alice", strategy = AlwaysBuyStrategy())
        val player2 = Player(name = "Bob", strategy = AlwaysBuyStrategy())
        val player3 = Player(name = "Charlie", strategy = AlwaysBuyStrategy())
        val board = Board()
        val gameState = GameState(listOf(player1, player2, player3), board)

        // プレイヤー2を破産させる
        player2.markAsBankrupt()

        gameState.getActivePlayerCount() shouldBe 2
    }
})
