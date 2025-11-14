package com.monopoly.domain.service

import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Money
import com.monopoly.domain.model.Player
import com.monopoly.domain.model.Property
import com.monopoly.domain.model.PropertyOwnership
import com.monopoly.domain.model.Space

class GameService {
    fun movePlayer(
        player: Player,
        steps: Int,
    ) {
        player.advance(steps)
    }

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
            is Space.PropertySpace -> {
                val property: Property = gameState.board.getPropertyAt(space.position) ?: return

                when (val ownership: PropertyOwnership = property.ownership) {
                    is PropertyOwnership.Unowned -> {
                        // 未所有プロパティ: 購入判定
                        if (player.strategy.shouldBuy(property, player.money)) {
                            val ownedProperty: Property = buyProperty(player, property)
                            gameState.board.updateProperty(ownedProperty)
                        }
                    }
                    is PropertyOwnership.OwnedByPlayer -> {
                        val owner: Player = ownership.player
                        // 他人のプロパティ: レント支払い
                        if (owner != player) {
                            payRent(player, owner, property.rent)
                        }
                        // 自分のプロパティ: 何もしない
                    }
                }
            }
            else -> {
                // GO、その他のマス: Phase 1では何もしない
            }
        }
    }
}
