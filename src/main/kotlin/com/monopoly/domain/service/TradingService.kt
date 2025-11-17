package com.monopoly.domain.service

import com.monopoly.domain.model.*

/**
 * プレイヤー間取引システムのサービス
 *
 * Phase 19: プレイヤー間取引の実装
 */
class TradingService {
    /**
     * プロパティ同士を交換する
     */
    fun tradeProperties(
        player1: Player,
        player2: Player,
        property1: Property,
        property2: Property,
        gameState: GameState
    ) {
        // 所有権の検証
        require(property1.ownership is PropertyOwnership.Owned &&
            property1.ownership.ownerName == player1.name) {
            "${player1.name} does not own ${property1.name}"
        }
        require(property2.ownership is PropertyOwnership.Owned &&
            property2.ownership.ownerName == player2.name) {
            "${player2.name} does not own ${property2.name}"
        }

        // player1からproperty1を削除し、property2を追加（所有権を変更）
        val updatedProperty1ForPlayer2 = property1.copy(
            ownership = PropertyOwnership.Owned(player2.name)
        )
        val updatedProperty2ForPlayer1 = property2.copy(
            ownership = PropertyOwnership.Owned(player1.name)
        )

        // player1の処理
        player1.removeProperty(property1)
        player1.acquireProperty(updatedProperty2ForPlayer1)

        // player2の処理
        player2.removeProperty(property2)
        player2.acquireProperty(updatedProperty1ForPlayer2)

        // ボードの更新
        gameState.board.updateProperty(updatedProperty1ForPlayer2)
        gameState.board.updateProperty(updatedProperty2ForPlayer1)

        // イベント記録
        gameState.events.add(
            GameEvent.PropertyTraded(
                player1Name = player1.name,
                player2Name = player2.name,
                property1Name = property1.name,
                property2Name = property2.name
            )
        )
    }

    /**
     * プロパティを金銭で購入する
     */
    fun buyPropertyFromPlayer(
        buyer: Player,
        seller: Player,
        property: Property,
        agreedPrice: Int,
        gameState: GameState
    ) {
        // 所有権の検証
        require(property.ownership is PropertyOwnership.Owned &&
            property.ownership.ownerName == seller.name) {
            "${seller.name} does not own ${property.name}"
        }

        // 購入者の資金検証
        require(buyer.money >= agreedPrice) {
            "${buyer.name} does not have enough money to buy ${property.name}"
        }

        // 金銭の移動
        buyer.pay(Money(agreedPrice))
        seller.receiveMoney(Money(agreedPrice))

        // プロパティの所有権変更
        val updatedProperty = property.copy(
            ownership = PropertyOwnership.Owned(buyer.name)
        )

        seller.removeProperty(property)
        buyer.acquireProperty(updatedProperty)

        // ボードの更新
        gameState.board.updateProperty(updatedProperty)

        // イベント記録
        gameState.events.add(
            GameEvent.PropertySold(
                sellerName = seller.name,
                buyerName = buyer.name,
                propertyName = property.name,
                price = agreedPrice
            )
        )
    }

    /**
     * 複合取引（プロパティ + 金銭）
     */
    fun trade(
        player1: Player,
        player2: Player,
        player1Properties: List<Property>,
        player2Properties: List<Property>,
        player1Money: Int,
        player2Money: Int,
        gameState: GameState
    ) {
        // 所有権の検証
        player1Properties.forEach { property ->
            require(property.ownership is PropertyOwnership.Owned &&
                property.ownership.ownerName == player1.name) {
                "${player1.name} does not own ${property.name}"
            }
        }
        player2Properties.forEach { property ->
            require(property.ownership is PropertyOwnership.Owned &&
                property.ownership.ownerName == player2.name) {
                "${player2.name} does not own ${property.name}"
            }
        }

        // 資金の検証
        require(player1.money >= player1Money) {
            "${player1.name} does not have enough money"
        }
        require(player2.money >= player2Money) {
            "${player2.name} does not have enough money"
        }

        // 金銭の移動
        if (player1Money > 0) {
            player1.pay(Money(player1Money))
            player2.receiveMoney(Money(player1Money))
        }
        if (player2Money > 0) {
            player2.pay(Money(player2Money))
            player1.receiveMoney(Money(player2Money))
        }

        // プロパティの移動（player1 -> player2）
        player1Properties.forEach { property ->
            val updatedProperty = property.copy(
                ownership = PropertyOwnership.Owned(player2.name)
            )
            player1.removeProperty(property)
            player2.acquireProperty(updatedProperty)
            gameState.board.updateProperty(updatedProperty)
        }

        // プロパティの移動（player2 -> player1）
        player2Properties.forEach { property ->
            val updatedProperty = property.copy(
                ownership = PropertyOwnership.Owned(player1.name)
            )
            player2.removeProperty(property)
            player1.acquireProperty(updatedProperty)
            gameState.board.updateProperty(updatedProperty)
        }

        // イベント記録
        gameState.events.add(
            GameEvent.ComplexTrade(
                player1Name = player1.name,
                player2Name = player2.name,
                player1PropertiesCount = player1Properties.size,
                player2PropertiesCount = player2Properties.size,
                player1Money = player1Money,
                player2Money = player2Money
            )
        )
    }
}
