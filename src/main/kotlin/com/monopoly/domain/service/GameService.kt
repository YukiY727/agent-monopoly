package com.monopoly.domain.service

import com.monopoly.domain.model.GameEvent
import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Money
import com.monopoly.domain.model.Player
import com.monopoly.domain.model.Property
import com.monopoly.domain.model.PropertyOwnership
import com.monopoly.domain.model.Space

@Suppress("TooManyFunctions") // Phase 1の範囲内では許容
class GameService {
    fun checkGameEnd(gameState: GameState): Boolean {
        val activePlayerCount: Int = gameState.getActivePlayerCount()
        return activePlayerCount <= 1
    }

    fun executeTurn(
        gameState: GameState,
        dice: com.monopoly.domain.model.Dice,
    ) {
        val player: Player = gameState.currentPlayer

        // TurnStartedイベントを記録
        gameState.events.add(
            GameEvent.TurnStarted(
                turnNumber = gameState.turnNumber,
                playerName = player.name
            )
        )

        val roll: Int = dice.roll()

        // DiceRolledイベントを記録
        gameState.events.add(
            GameEvent.DiceRolled(
                turnNumber = gameState.turnNumber,
                playerName = player.name,
                die1 = dice.lastDie1,
                die2 = dice.lastDie2
            )
        )

        movePlayer(player, roll, gameState)
        processSpace(player, gameState)

        // TurnEndedイベントを記録
        gameState.events.add(
            GameEvent.TurnEnded(
                turnNumber = gameState.turnNumber,
                playerName = player.name
            )
        )

        gameState.incrementTurnNumber()
        gameState.nextPlayer()
    }

    fun runGame(
        gameState: GameState,
        dice: com.monopoly.domain.model.Dice,
        maxTurns: Int = 1000,
    ): Player {
        // GameStartedイベントを記録
        gameState.events.add(
            GameEvent.GameStarted(
                turnNumber = 0,
                playerNames = gameState.players.map { it.name }
            )
        )

        while (!checkGameEnd(gameState) && gameState.turnNumber < maxTurns) {
            executeTurn(gameState, dice)
        }
        gameState.endGame()

        // 最大ターン数に達した場合は、最も資産の多いプレイヤーを勝者とする
        val winner: Player = if (gameState.getActivePlayerCount() > 1) {
            gameState.players.filter { !it.isBankrupt }.maxByOrNull { it.getTotalAssets() }
                ?: gameState.players.first { !it.isBankrupt }
        } else {
            gameState.players.first { !it.isBankrupt }
        }

        // GameEndedイベントを記録
        gameState.events.add(
            GameEvent.GameEnded(
                turnNumber = gameState.turnNumber,
                winner = winner.name,
                totalTurns = gameState.turnNumber
            )
        )

        return winner
    }

    fun movePlayer(
        player: Player,
        steps: Int,
        gameState: GameState,
    ) {
        val fromPosition: Int = player.position
        val passedGo: Boolean = player.advance(steps)
        val toPosition: Int = player.position

        // PlayerMovedイベントを記録
        gameState.events.add(
            GameEvent.PlayerMoved(
                turnNumber = gameState.turnNumber,
                playerName = player.name,
                fromPosition = fromPosition,
                toPosition = toPosition,
                passedGo = passedGo
            )
        )
    }

    fun buyProperty(
        player: Player,
        property: Property,
        gameState: GameState,
    ): Property {
        player.pay(property.priceValue)
        val ownedProperty: Property = property.withOwner(player)
        player.acquireProperty(ownedProperty)

        // PropertyPurchasedイベントを記録
        gameState.events.add(
            GameEvent.PropertyPurchased(
                turnNumber = gameState.turnNumber,
                playerName = player.name,
                propertyName = property.name,
                price = property.price
            )
        )

        return ownedProperty
    }

    fun payRent(
        payer: Player,
        receiver: Player,
        rentAmount: Int,
        property: Property,
        gameState: GameState,
    ) {
        val amount: Money = Money(rentAmount)
        payer.pay(amount)
        receiver.receiveMoney(amount)

        // RentPaidイベントを記録
        gameState.events.add(
            GameEvent.RentPaid(
                turnNumber = gameState.turnNumber,
                payerName = payer.name,
                receiverName = receiver.name,
                propertyName = property.name,
                amount = rentAmount
            )
        )
    }

    fun bankruptPlayer(
        player: Player,
        gameState: GameState,
    ): List<Property> {
        val properties: List<Property> = player.ownedProperties.toList()
        val releasedProperties: List<Property> = properties.map { it.withoutOwner() }
        player.goBankrupt()

        // PlayerBankruptedイベントを記録
        gameState.events.add(
            GameEvent.PlayerBankrupted(
                turnNumber = gameState.turnNumber,
                playerName = player.name,
                finalMoney = player.money
            )
        )

        return releasedProperties
    }

    fun processSpace(
        player: Player,
        gameState: GameState,
    ) {
        val space: Space = gameState.board.getSpace(player.position)

        when (space) {
            is Space.PropertySpace -> processPropertySpace(player, gameState, space)
            is Space.Go -> {
                // GO: Phase 1では何もしない（GOボーナスはadvanceで処理済み）
            }
            is Space.Other -> {
                // その他のマス: Phase 1では何もしない
            }
        }
    }

    private fun processPropertySpace(
        player: Player,
        gameState: GameState,
        space: Space.PropertySpace,
    ) {
        val property: Property = gameState.board.getPropertyAt(space.position) ?: return

        when (val ownership: PropertyOwnership = property.ownership) {
            is PropertyOwnership.Unowned -> processUnownedProperty(player, gameState, property)
            is PropertyOwnership.OwnedByPlayer -> processOwnedProperty(player, ownership, property, gameState)
        }
    }

    private fun processUnownedProperty(
        player: Player,
        gameState: GameState,
        property: Property,
    ) {
        // BuyDecisionContextを作成
        val context = com.monopoly.domain.strategy.BuyDecisionContext(
            property = property,
            playerMoney = player.money,
            ownedProperties = player.ownedProperties,
            board = gameState.board,
            allPlayers = gameState.players,
            currentTurn = gameState.turnNumber,
        )

        // 戦略による購入判断（新しいコンテキスト版メソッドを呼ぶ）
        if (player.strategy.shouldBuy(context)) {
            val ownedProperty: Property = buyProperty(player, property, gameState)
            gameState.board.updateProperty(ownedProperty)
        }
    }

    private fun processOwnedProperty(
        player: Player,
        ownership: PropertyOwnership.OwnedByPlayer,
        property: Property,
        gameState: GameState,
    ) {
        val owner: Player = ownership.player
        if (owner != player) {
            payRent(player, owner, property.rent, property, gameState)
        }
    }
}
