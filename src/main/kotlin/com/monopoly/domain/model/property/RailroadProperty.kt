package com.monopoly.domain.model.property

import com.monopoly.domain.model.core.Money
import com.monopoly.domain.model.player.Player

/**
 * 鉄道プロパティ
 * 家賃は所有する鉄道の数に応じて変動する
 * - 1件: $25
 * - 2件: $50
 * - 3件: $100
 * - 4件: $200
 */
data class RailroadProperty(
    override val name: String,
    override val position: Int,
    override val price: Int,
    override val ownership: PropertyOwnership = PropertyOwnership.Unowned,
) : Property {
    override val priceValue: Money
        get() = Money(price)

    override val rentValue: Money
        get() = Money(calculateRent())

    override fun withOwner(newOwner: Player): RailroadProperty = copy(ownership = PropertyOwnership.OwnedByPlayer(newOwner))

    override fun withoutOwner(): RailroadProperty = copy(ownership = PropertyOwnership.Unowned)

    override fun isOwned(): Boolean = ownership is PropertyOwnership.OwnedByPlayer

    override fun mortgage(): RailroadProperty {
        require(ownership is PropertyOwnership.OwnedByPlayer) { "Cannot mortgage unowned property" }
        require(!isMortgaged()) { "Property is already mortgaged" }

        val ownerPlayer: PropertyOwnership.OwnedByPlayer = ownership
        return copy(ownership = PropertyOwnership.OwnedByPlayer(ownerPlayer.player, isMortgaged = true))
    }

    override fun unmortgage(): RailroadProperty {
        require(ownership is PropertyOwnership.OwnedByPlayer) { "Cannot unmortgage unowned property" }
        require(isMortgaged()) { "Property is not mortgaged" }

        val ownerPlayer: PropertyOwnership.OwnedByPlayer = ownership
        return copy(ownership = PropertyOwnership.OwnedByPlayer(ownerPlayer.player, isMortgaged = false))
    }

    override fun isMortgaged(): Boolean = ownership is PropertyOwnership.OwnedByPlayer && ownership.isMortgaged

    /**
     * 所有する鉄道の数に基づいて家賃を計算
     * @param railroadCount 同じプレイヤーが所有する鉄道の総数
     * @return 適用される家賃額
     */
    fun calculateRentWithCount(railroadCount: Int): Int =
        when (railroadCount) {
            1 -> 25
            2 -> 50
            3 -> 100
            4 -> 200
            else -> 0
        }

    /**
     * デフォルトの家賃計算（所有者情報が必要なため、実際には使用されない）
     */
    private fun calculateRent(): Int = 25 // Default to 1 railroad
}
