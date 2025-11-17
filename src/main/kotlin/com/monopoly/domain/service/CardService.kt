package com.monopoly.domain.service

import com.monopoly.domain.model.*

/**
 * カードシステムのサービス
 *
 * Phase 17: Chance/Community Chestカードの実装
 */
class CardService {
    /**
     * カードを1枚引いて効果を適用する
     * @return 新しいデッキ
     */
    fun drawAndApplyCard(
        player: Player,
        gameState: GameState,
        deck: CardDeck
    ): CardDeck {
        val (card, newDeck) = deck.draw()

        applyCardEffect(card, player, gameState)

        return newDeck
    }

    private fun applyCardEffect(card: Card, player: Player, gameState: GameState) {
        when (card) {
            is Card.GetOutOfJailFreeCard -> {
                player.addGetOutOfJailFreeCard()
                gameState.events.add(
                    GameEvent.CardDrawn(player.name, card.description)
                )
            }
            is Card.CollectMoney -> {
                player.receiveMoney(Money(card.amount))
                gameState.events.add(
                    GameEvent.CardDrawn(player.name, card.description)
                )
            }
            is Card.PayMoney -> {
                player.pay(Money(card.amount))
                gameState.events.add(
                    GameEvent.CardDrawn(player.name, card.description)
                )
            }
            is Card.AdvanceToGo -> {
                handleMoveToCard(card, player, gameState)
            }
            is Card.GoToJail -> {
                handleMoveToCard(card, player, gameState)
            }
            is Card.MoveToPosition -> {
                handleMoveToCard(card, player, gameState)
            }
            is Card.CollectFromEachPlayer -> {
                handleCollectFromEachPlayer(card, player, gameState)
            }
            is Card.PayEachPlayer -> {
                handlePayEachPlayer(card, player, gameState)
            }
            is Card.PayRepairs -> {
                handlePayRepairs(card, player, gameState)
            }
        }
    }

    private fun handleMoveToCard(card: Card.MoveTo, player: Player, gameState: GameState) {
        val currentPosition = player.position
        val targetPosition = card.position

        // GoToJail の場合は特別処理
        if (card is Card.GoToJail) {
            val jailService = JailService()
            jailService.sendToJail(player, gameState)
            gameState.events.add(
                GameEvent.CardDrawn(player.name, (card as Card).description)
            )
            return
        }

        // 通常の移動処理
        player.moveTo(BoardPosition(targetPosition))

        // GO を通過したかチェック
        if (targetPosition < currentPosition || targetPosition == 0) {
            player.receiveMoney(Money(200)) // GO通過ボーナス
            gameState.events.add(
                GameEvent.PassedGo(player.name)
            )
        }

        gameState.events.add(
            GameEvent.CardDrawn(player.name, (card as Card).description)
        )
    }

    private fun handleCollectFromEachPlayer(
        card: Card.CollectFromEachPlayer,
        player: Player,
        gameState: GameState
    ) {
        val otherPlayers = gameState.players.filter { it != player && !it.isBankrupt }
        val totalAmount = card.amount * otherPlayers.size

        otherPlayers.forEach { otherPlayer ->
            otherPlayer.pay(Money(card.amount))
        }
        player.receiveMoney(Money(totalAmount))

        gameState.events.add(
            GameEvent.CardDrawn(player.name, card.description)
        )
    }

    private fun handlePayEachPlayer(
        card: Card.PayEachPlayer,
        player: Player,
        gameState: GameState
    ) {
        val otherPlayers = gameState.players.filter { it != player && !it.isBankrupt }
        val totalAmount = card.amount * otherPlayers.size

        player.pay(Money(totalAmount))
        otherPlayers.forEach { otherPlayer ->
            otherPlayer.receiveMoney(Money(card.amount))
        }

        gameState.events.add(
            GameEvent.CardDrawn(player.name, card.description)
        )
    }

    private fun handlePayRepairs(
        card: Card.PayRepairs,
        player: Player,
        gameState: GameState
    ) {
        val properties = player.ownedProperties
        val totalHouses = properties.sumOf { it.houses }
        val totalHotels = properties.count { it.hasHotel }

        val totalCost = (totalHouses * card.perHouse) + (totalHotels * card.perHotel)
        player.pay(Money(totalCost))

        gameState.events.add(
            GameEvent.CardDrawn(player.name, card.description)
        )
    }
}
