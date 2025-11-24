package com.monopoly.domain.service

import com.monopoly.domain.event.GameEvent
import com.monopoly.domain.model.Board
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

        // 破産したプレイヤーはスキップ
        if (player.isBankrupt) {
            gameState.nextPlayer()
            return
        }

        // TurnStartedイベントを記録
        gameState.incrementTurnNumber()
        gameState.events.add(
            GameEvent.TurnStarted(
                turnNumber = gameState.turnNumber,
                timestamp = System.currentTimeMillis(),
                playerName = player.name,
            ),
        )

        val roll: Int = dice.roll()
        val lastRoll: Pair<Int, Int> = dice.getLastRoll()

        // DiceRolledイベントを記録
        gameState.events.add(
            GameEvent.DiceRolled(
                turnNumber = gameState.turnNumber,
                timestamp = System.currentTimeMillis(),
                playerName = player.name,
                die1 = lastRoll.first,
                die2 = lastRoll.second,
                total = roll,
            ),
        )

        movePlayer(player, roll, gameState)
        processSpace(player, gameState)

        // TurnEndedイベントを記録
        gameState.events.add(
            GameEvent.TurnEnded(
                turnNumber = gameState.turnNumber,
                timestamp = System.currentTimeMillis(),
                playerName = player.name,
            ),
        )

        gameState.nextPlayer()
    }

    fun runGame(
        gameState: GameState,
        dice: com.monopoly.domain.model.Dice,
        maxTurns: Int = Int.MAX_VALUE,
    ): Player {
        // GameStartedイベントを記録
        gameState.events.add(
            GameEvent.GameStarted(
                turnNumber = 0,
                timestamp = System.currentTimeMillis(),
                playerNames = gameState.players.map { it.name },
            ),
        )

        var turns: Int = 0
        while (!checkGameEnd(gameState) && turns < maxTurns) {
            executeTurn(gameState, dice)
            turns++
        }
        gameState.endGame()

        // ターン数上限に達した場合は、最も資産の多いプレイヤーを勝者とする
        val activePlayers: List<Player> = gameState.players.filter { !it.isBankrupt }
        val winner: Player =
            if (activePlayers.size == 1) {
                activePlayers.first()
            } else {
                // 複数のアクティブプレイヤーがいる場合は資産で判定
                activePlayers.maxByOrNull { it.calculateTotalAssets().amount }
                    ?: gameState.players.first()
            }

        // GameEndedイベントを記録
        gameState.events.add(
            GameEvent.GameEnded(
                turnNumber = gameState.turnNumber,
                timestamp = System.currentTimeMillis(),
                winner = winner.name,
                totalTurns = gameState.turnNumber,
            ),
        )

        return winner
    }

    fun movePlayer(
        player: Player,
        steps: Int,
        gameState: GameState,
    ) {
        val fromPosition: Int = player.position
        player.advance(steps)
        val toPosition: Int = player.position
        val passedGo: Boolean = fromPosition + steps >= Board.BOARD_SIZE

        gameState.events.add(
            GameEvent.PlayerMoved(
                turnNumber = gameState.turnNumber,
                timestamp = System.currentTimeMillis(),
                playerName = player.name,
                fromPosition = fromPosition,
                toPosition = toPosition,
                passedGo = passedGo,
            ),
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
                price = property.price,
            ),
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
        propertyName: String,
        gameState: GameState,
    ) {
        val amount: Money = Money(rentAmount)
        payer.pay(amount)
        receiver.receiveMoney(amount)

        gameState.events.add(
            GameEvent.RentPaid(
                turnNumber = gameState.turnNumber,
                timestamp = System.currentTimeMillis(),
                payerName = payer.name,
                receiverName = receiver.name,
                propertyName = propertyName,
                amount = rentAmount,
            ),
        )
    }

    // Phase 1のテストとの互換性のための overload
    fun payRent(
        payer: Player,
        receiver: Player,
        rentAmount: Int,
    ) {
        val amount: Money = Money(rentAmount)
        payer.pay(amount)
        receiver.receiveMoney(amount)
    }

    fun bankruptPlayer(
        player: Player,
        gameState: GameState,
    ): List<Property> {
        val properties: List<Property> = player.ownedProperties.toList()
        val releasedProperties: List<Property> = properties.map { it.withoutOwner() }
        player.goBankrupt()

        gameState.events.add(
            GameEvent.PlayerBankrupted(
                turnNumber = gameState.turnNumber,
                timestamp = System.currentTimeMillis(),
                playerName = player.name,
                finalMoney = player.money,
            ),
        )

        return releasedProperties
    }

    // Phase 1のテストとの互換性のための overload
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
            is PropertyOwnership.OwnedByPlayer -> processOwnedProperty(player, gameState, ownership, property)
        }
    }

    private fun processUnownedProperty(
        player: Player,
        gameState: GameState,
        property: Property,
    ) {
        if (player.strategy.shouldBuy(property, player.money)) {
            val ownedProperty: Property = buyProperty(player, property, gameState)
            gameState.updateProperty(ownedProperty)
        }
    }

    private fun processOwnedProperty(
        player: Player,
        gameState: GameState,
        ownership: PropertyOwnership.OwnedByPlayer,
        property: Property,
    ) {
        val owner: Player = ownership.player

        // 破産したプレイヤーが所有するプロパティは解放済みのはず
        if (owner.isBankrupt) {
            val releasedProperty: Property = property.withoutOwner()
            gameState.releaseProperty(releasedProperty)
            return
        }

        if (owner != player) {
            // レント支払い前にプロパティリストを保存（pay()内でgoBankrupt()が呼ばれると空になるため）
            val propertiesBeforePayment: List<Property> = player.ownedProperties.toList()

            payRent(player, owner, property.rentValue.amount, property.name, gameState)

            // レント支払い後にプレイヤーが破産したかチェック
            if (player.isBankrupt) {
                releasePlayerPropertiesOnBoard(propertiesBeforePayment, gameState)
            }
        }
    }

    private fun releasePlayerPropertiesOnBoard(
        properties: List<Property>,
        gameState: GameState,
    ) {
        properties.forEach { property ->
            val releasedProperty: Property = property.withoutOwner()
            gameState.releaseProperty(releasedProperty)
        }
    }
}
