package com.monopoly.domain.service

import com.monopoly.domain.model.GameState
import com.monopoly.domain.model.Money
import com.monopoly.domain.model.Player
import com.monopoly.domain.model.Property

/**
 * Mortgage（抵当）システムのサービス
 *
 * Phase 18: 抵当システムの実装
 */
class MortgageService {
    /**
     * プロパティを抵当に入れる（価格の50%を受け取る）
     */
    fun mortgageProperty(
        player: Player,
        property: Property,
        @Suppress("UNUSED_PARAMETER") gameState: GameState,
    ): Property {
        require(!property.isMortgaged) { "Property is already mortgaged" }
        require(property.houses == 0 && !property.hasHotel) {
            "Must sell all buildings before mortgaging"
        }

        val mortgageValue = property.price / 2
        player.receiveMoney(Money(mortgageValue))

        val mortgaged = Property(
            name = property.name,
            position = property.position,
            price = property.price,
            baseRent = property.baseRent,
            colorGroup = property.colorGroup,
            ownership = property.ownership,
            houses = 0,
            hasHotel = false,
            isMortgaged = true,
        )

        player.updateProperty(mortgaged)
        return mortgaged
    }

    /**
     * プロパティの抵当を解除する（抵当価格の110%を支払う）
     */
    fun unmortgageProperty(
        player: Player,
        property: Property,
        @Suppress("UNUSED_PARAMETER") gameState: GameState,
    ): Property {
        require(property.isMortgaged) { "Property is not mortgaged" }

        val mortgageValue = property.price / 2
        val unmortgageCost = (mortgageValue * 1.1).toInt()

        require(player.money >= unmortgageCost) {
            "Not enough money to unmortgage property"
        }

        player.pay(Money(unmortgageCost))

        val unmortgaged = Property(
            name = property.name,
            position = property.position,
            price = property.price,
            baseRent = property.baseRent,
            colorGroup = property.colorGroup,
            ownership = property.ownership,
            houses = 0,
            hasHotel = false,
            isMortgaged = false,
        )

        player.updateProperty(unmortgaged)
        return unmortgaged
    }
}
