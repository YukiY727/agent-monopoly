package com.monopoly.domain.model.property

import com.monopoly.domain.model.core.BoardPosition
import com.monopoly.domain.model.core.Money
import com.monopoly.domain.model.player.Player

data class StreetProperty(
    override val name: String,
    override val position: Int,
    override val price: Int,
    val rent: PropertyRent,
    val houseCost: Int,
    val hotelCost: Int,
    val colorGroup: ColorGroup,
    override val ownership: PropertyOwnership = PropertyOwnership.Unowned,
    val buildings: PropertyBuildings = PropertyBuildings(),
) : Property {
    // Value object accessors
    val positionValue: BoardPosition
        get() = BoardPosition(position)

    override val priceValue: Money
        get() = Money(price)

    override val rentValue: Money
        get() = Money(calculateRent())

    override fun withOwner(newOwner: Player): StreetProperty = copy(ownership = PropertyOwnership.OwnedByPlayer(newOwner))

    override fun withoutOwner(): StreetProperty = copy(ownership = PropertyOwnership.Unowned)

    override fun isOwned(): Boolean = ownership is PropertyOwnership.OwnedByPlayer

    override fun mortgage(): StreetProperty {
        require(ownership is PropertyOwnership.OwnedByPlayer) { "Cannot mortgage unowned property" }
        require(!isMortgaged()) { "Property is already mortgaged" }
        require(buildings.houseCount == 0 && !buildings.hasHotel) { "Cannot mortgage property with buildings" }

        val ownerPlayer: PropertyOwnership.OwnedByPlayer = ownership
        return copy(ownership = PropertyOwnership.OwnedByPlayer(ownerPlayer.player, isMortgaged = true))
    }

    override fun unmortgage(): StreetProperty {
        require(ownership is PropertyOwnership.OwnedByPlayer) { "Cannot unmortgage unowned property" }
        require(isMortgaged()) { "Property is not mortgaged" }

        val ownerPlayer: PropertyOwnership.OwnedByPlayer = ownership
        return copy(ownership = PropertyOwnership.OwnedByPlayer(ownerPlayer.player, isMortgaged = false))
    }

    override fun isMortgaged(): Boolean = ownership is PropertyOwnership.OwnedByPlayer && ownership.isMortgaged

    /**
     * 現在の建物状態に基づいて家賃を計算
     * @return 適用される家賃額
     */
    @Suppress("MagicNumber")
    private fun calculateRent(): Int =
        when {
            buildings.hasHotel -> rent.withHotel
            buildings.houseCount == 4 -> rent.withHouse4
            buildings.houseCount == 3 -> rent.withHouse3
            buildings.houseCount == 2 -> rent.withHouse2
            buildings.houseCount == 1 -> rent.withHouse1
            else -> rent.base
        }
}
