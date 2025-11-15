package com.monopoly.domain.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class GameEventTest : StringSpec({
    // TC-201: GameStarted初期化
    // Given: プレイヤー名リスト["Alice", "Bob"]
    // When: GameStartedイベントを作成
    // Then: playerNamesが正しく設定されている、turnNumberが0
    "GameStarted should be initialized with player names" {
        val event = GameEvent.GameStarted(
            turnNumber = 0,
            timestamp = System.currentTimeMillis(),
            playerNames = listOf("Alice", "Bob")
        )

        event.playerNames shouldBe listOf("Alice", "Bob")
        event.turnNumber shouldBe 0
    }

    // TC-202: DiceRolled初期化
    // Given: ターン番号1、プレイヤー名"Alice"、サイコロ3と4
    // When: DiceRolledイベントを作成
    // Then: die1が3、die2が4、totalが7、playerNameが"Alice"
    "DiceRolled should be initialized with dice values" {
        val event = GameEvent.DiceRolled(
            turnNumber = 1,
            timestamp = System.currentTimeMillis(),
            playerName = "Alice",
            die1 = 3,
            die2 = 4,
            total = 7
        )

        event.die1 shouldBe 3
        event.die2 shouldBe 4
        event.total shouldBe 7
        event.playerName shouldBe "Alice"
    }

    // TC-203: PlayerMoved初期化
    // Given: ターン番号1、プレイヤー名"Alice"、位置0→7、GO通過なし
    // When: PlayerMovedイベントを作成
    // Then: fromPositionが0、toPositionが7、passedGoがfalse
    "PlayerMoved should be initialized without passing GO" {
        val event = GameEvent.PlayerMoved(
            turnNumber = 1,
            timestamp = System.currentTimeMillis(),
            playerName = "Alice",
            fromPosition = 0,
            toPosition = 7,
            passedGo = false
        )

        event.fromPosition shouldBe 0
        event.toPosition shouldBe 7
        event.passedGo shouldBe false
    }

    // TC-204: PlayerMoved（GO通過あり）
    // Given: ターン番号1、プレイヤー名"Alice"、位置38→3、GO通過あり
    // When: PlayerMovedイベントを作成
    // Then: fromPositionが38、toPositionが3、passedGoがtrue
    "PlayerMoved should be initialized with passing GO" {
        val event = GameEvent.PlayerMoved(
            turnNumber = 1,
            timestamp = System.currentTimeMillis(),
            playerName = "Alice",
            fromPosition = 38,
            toPosition = 3,
            passedGo = true
        )

        event.fromPosition shouldBe 38
        event.toPosition shouldBe 3
        event.passedGo shouldBe true
    }

    // TC-205: PropertyPurchased初期化
    // Given: ターン番号2、プレイヤー名"Bob"、プロパティ"Park Place"、価格350
    // When: PropertyPurchasedイベントを作成
    // Then: playerNameが"Bob"、propertyNameが"Park Place"、priceが350
    "PropertyPurchased should be initialized with property details" {
        val event = GameEvent.PropertyPurchased(
            turnNumber = 2,
            timestamp = System.currentTimeMillis(),
            playerName = "Bob",
            propertyName = "Park Place",
            price = 350
        )

        event.playerName shouldBe "Bob"
        event.propertyName shouldBe "Park Place"
        event.price shouldBe 350
    }

    // TC-206: RentPaid初期化
    // Given: ターン番号3、支払者"Alice"、受取者"Bob"、プロパティ"Boardwalk"、レント50
    // When: RentPaidイベントを作成
    // Then: payerNameが"Alice"、receiverNameが"Bob"、amountが50
    "RentPaid should be initialized with rent details" {
        val event = GameEvent.RentPaid(
            turnNumber = 3,
            timestamp = System.currentTimeMillis(),
            payerName = "Alice",
            receiverName = "Bob",
            propertyName = "Boardwalk",
            amount = 50
        )

        event.payerName shouldBe "Alice"
        event.receiverName shouldBe "Bob"
        event.amount shouldBe 50
    }

    // TC-207: PlayerBankrupted初期化
    // Given: ターン番号10、プレイヤー名"Alice"、最終所持金-50
    // When: PlayerBankruptedイベントを作成
    // Then: playerNameが"Alice"、finalMoneyが-50
    "PlayerBankrupted should be initialized with final money" {
        val event = GameEvent.PlayerBankrupted(
            turnNumber = 10,
            timestamp = System.currentTimeMillis(),
            playerName = "Alice",
            finalMoney = -50
        )

        event.playerName shouldBe "Alice"
        event.finalMoney shouldBe -50
    }

    // TC-208: TurnStarted初期化
    // Given: ターン番号5、プレイヤー名"Bob"
    // When: TurnStartedイベントを作成
    // Then: turnNumberが5、playerNameが"Bob"
    "TurnStarted should be initialized with turn number and player" {
        val event = GameEvent.TurnStarted(
            turnNumber = 5,
            timestamp = System.currentTimeMillis(),
            playerName = "Bob"
        )

        event.turnNumber shouldBe 5
        event.playerName shouldBe "Bob"
    }

    // TC-209: TurnEnded初期化
    // Given: ターン番号5、プレイヤー名"Bob"
    // When: TurnEndedイベントを作成
    // Then: turnNumberが5、playerNameが"Bob"
    "TurnEnded should be initialized with turn number and player" {
        val event = GameEvent.TurnEnded(
            turnNumber = 5,
            timestamp = System.currentTimeMillis(),
            playerName = "Bob"
        )

        event.turnNumber shouldBe 5
        event.playerName shouldBe "Bob"
    }

    // TC-210: GameEnded初期化
    // Given: ターン番号50、勝者"Alice"、総ターン数50
    // When: GameEndedイベントを作成
    // Then: winnerが"Alice"、totalTurnsが50
    "GameEnded should be initialized with winner and total turns" {
        val event = GameEvent.GameEnded(
            turnNumber = 50,
            timestamp = System.currentTimeMillis(),
            winner = "Alice",
            totalTurns = 50
        )

        event.winner shouldBe "Alice"
        event.totalTurns shouldBe 50
    }
})
