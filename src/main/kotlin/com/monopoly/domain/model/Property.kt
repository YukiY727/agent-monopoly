package com.monopoly.domain.model

data class Property(
    val name: String,
    val position: Int,
    val price: Int,
    val rent: Int,
    val colorGroup: ColorGroup,
    val ownership: PropertyOwnership = PropertyOwnership.Unowned,
) {
    fun withOwner(newOwner: Player): Property = copy(ownership = PropertyOwnership.OwnedByPlayer(newOwner))

    fun withoutOwner(): Property = copy(ownership = PropertyOwnership.Unowned)

    fun isOwned(): Boolean = ownership is PropertyOwnership.OwnedByPlayer
}
