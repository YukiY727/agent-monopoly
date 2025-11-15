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
        val roll: Int = dice.roll()

        movePlayer(player, roll, gameState)
        processSpace(player, gameState)

        gameState.incrementTurnNumber()
        gameState.nextPlayer()
    }

    fun runGame(
        gameState: GameState,
        dice: com.monopoly.domain.model.Dice,
    ): Player {
        while (!checkGameEnd(gameState)) {
            executeTurn(gameState, dice)
        }
        gameState.endGame()
        return gameState.players.first { !it.isBankrupt }
    }

    fun movePlayer(
        player: Player,
        steps: Int,
        gameState: GameState,
    ) {
        val fromPosition: Int = player.position
        player.advance(steps)
        val toPosition: Int = player.position
        val passedGo: Boolean = fromPosition + steps >= 40

        gameState.events.add(
            GameEvent.PlayerMoved(
                turnNumber = gameState.turnNumber,
                timestamp = System.currentTimeMillis(),
                playerName = player.name,
                fromPosition = fromPosition,
                toPosition = toPosition,
                passedGo = passedGo
            )
        )
    }

    // Phase 1のテストとの互換性のための overload
    fun movePlayer(
        player: Player,
        steps: Int,
    ) {
        player.advance(steps)
    }

    fun buyProperty(
        player: Player,
        property: Property,
        gameState: GameState,
    ): Property {
        player.pay(property.priceValue)
        val ownedProperty: Property = property.withOwner(player)
        player.acquireProperty(ownedProperty)

        gameState.events.add(
            GameEvent.PropertyPurchased(
                turnNumber = gameState.turnNumber,
                timestamp = System.currentTimeMillis(),
                playerName = player.name,
                propertyName = property.name,
                price = property.price
            )
        )

        return ownedProperty
    }

    // Phase 1のテストとの互換性のための overload
    fun buyProperty(
        player: Player,
        property: Property,
    ): Property {
        player.pay(property.priceValue)
        val ownedProperty: Property = property.withOwner(player)
        player.acquireProperty(ownedProperty)
        return ownedProperty
    }

    fun payRent(
        payer: Player,
        receiver: Player,
        rentAmount: Int,
    ) {
        val amount: Money = Money(rentAmount)
        payer.pay(amount)
        receiver.receiveMoney(amount)
    }

    fun bankruptPlayer(player: Player): List<Property> {
        val properties: List<Property> = player.ownedProperties.toList()
        val releasedProperties: List<Property> = properties.map { it.withoutOwner() }
        player.goBankrupt()
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
            is PropertyOwnership.OwnedByPlayer -> processOwnedProperty(player, ownership, property)
        }
    }

    private fun processUnownedProperty(
        player: Player,
        gameState: GameState,
        property: Property,
    ) {
        if (player.strategy.shouldBuy(property, player.money)) {
            val ownedProperty: Property = buyProperty(player, property, gameState)
            gameState.board.updateProperty(ownedProperty)
        }
    }

    private fun processOwnedProperty(
        player: Player,
        ownership: PropertyOwnership.OwnedByPlayer,
        property: Property,
    ) {
        val owner: Player = ownership.player
        if (owner != player) {
            payRent(player, owner, property.rent)
        }
    }
}
