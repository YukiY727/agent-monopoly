package com.monopoly.domain.model

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
