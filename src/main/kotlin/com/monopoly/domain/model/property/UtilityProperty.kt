package com.monopoly.domain.model.property

import com.monopoly.domain.model.core.Money
import com.monopoly.domain.model.player.Player

/**
 * 公共施設プロパティ（電力会社、水道会社）
 * 家賃はサイコロの目と所有する公共施設の数に応じて変動する
 * - 1件所有: サイコロの目 × 4
 * - 2件所有: サイコロの目 × 10
 */
data class UtilityProperty(
    override val name: String,
    override val position: Int,
    override val price: Int,
    override val ownership: PropertyOwnership = PropertyOwnership.Unowned,
) : Property {
    override val priceValue: Money
        get() = Money(price)

    override val rentValue: Money
        get() = Money(calculateRent())

    override fun withOwner(newOwner: Player): UtilityProperty = copy(ownership = PropertyOwnership.OwnedByPlayer(newOwner))

    override fun withoutOwner(): UtilityProperty = copy(ownership = PropertyOwnership.Unowned)

    override fun isOwned(): Boolean = ownership is PropertyOwnership.OwnedByPlayer

    override fun mortgage(): UtilityProperty {
        require(ownership is PropertyOwnership.OwnedByPlayer) { "Cannot mortgage unowned property" }
        require(!isMortgaged()) { "Property is already mortgaged" }

        val ownerPlayer: PropertyOwnership.OwnedByPlayer = ownership
        return copy(ownership = PropertyOwnership.OwnedByPlayer(ownerPlayer.player, isMortgaged = true))
    }

    override fun unmortgage(): UtilityProperty {
        require(ownership is PropertyOwnership.OwnedByPlayer) { "Cannot unmortgage unowned property" }
        require(isMortgaged()) { "Property is not mortgaged" }

        val ownerPlayer: PropertyOwnership.OwnedByPlayer = ownership
        return copy(ownership = PropertyOwnership.OwnedByPlayer(ownerPlayer.player, isMortgaged = false))
    }

    override fun isMortgaged(): Boolean = ownership is PropertyOwnership.OwnedByPlayer && ownership.isMortgaged

    /**
     * 所有する公共施設の数とサイコロの目に基づいて家賃を計算
     * @param utilityCount 同じプレイヤーが所有する公共施設の総数
     * @param diceRoll サイコロの合計値
     * @return 適用される家賃額
     */
    fun calculateRentWithDice(
        utilityCount: Int,
        diceRoll: Int,
    ): Int {
        val multiplier =
            when (utilityCount) {
                1 -> 4
                2 -> 10
                else -> 0
            }
        return diceRoll * multiplier
    }

    /**
     * デフォルトの家賃計算（サイコロ情報が必要なため、実際には使用されない）
     */
    private fun calculateRent(): Int = 0 // Cannot calculate without dice roll
}
