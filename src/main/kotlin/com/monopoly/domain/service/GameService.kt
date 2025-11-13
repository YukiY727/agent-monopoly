package com.monopoly.domain.service

import com.monopoly.domain.model.Player
import com.monopoly.domain.model.Property

class GameService {
    companion object {
        private const val BOARD_SIZE = 40
        private const val GO_BONUS = 200
    }

    fun movePlayer(
        player: Player,
        steps: Int,
    ) {
        val currentPosition = player.position
        val newPosition = (currentPosition + steps) % BOARD_SIZE

        // Check if player passed GO (position 0)
        if (currentPosition + steps >= BOARD_SIZE) {
            player.addMoney(GO_BONUS)
        }

        player.setPosition(newPosition)
    }

    fun buyProperty(
        player: Player,
        property: Property,
    ): Property {
        player.subtractMoney(property.price)
        val ownedProperty = property.withOwner(player)
        player.addProperty(ownedProperty)
        return ownedProperty
    }

    fun payRent(
        payer: Player,
        receiver: Player,
        rentAmount: Int,
    ) {
        payer.subtractMoney(rentAmount)
        receiver.addMoney(rentAmount)

        if (payer.money < 0) {
            payer.markAsBankrupt()
        }
    }
}
