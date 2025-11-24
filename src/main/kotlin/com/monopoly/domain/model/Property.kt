package com.monopoly.domain.model

data class Property(
    val name: String,
    val position: Int,
    val price: Int,
    val rent: PropertyRent,
    val houseCost: Int,
    val hotelCost: Int,
    val colorGroup: ColorGroup,
    val ownership: PropertyOwnership = PropertyOwnership.Unowned,
    val buildings: PropertyBuildings = PropertyBuildings(),
) {
    // Value object accessors
    val positionValue: BoardPosition
        get() = BoardPosition(position)

    val priceValue: Money
        get() = Money(price)

    val rentValue: Money
        get() = Money(calculateRent())

    fun withOwner(newOwner: Player): Property = copy(ownership = PropertyOwnership.OwnedByPlayer(newOwner))

    fun withoutOwner(): Property = copy(ownership = PropertyOwnership.Unowned)

    fun isOwned(): Boolean = ownership is PropertyOwnership.OwnedByPlayer

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
