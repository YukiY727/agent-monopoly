package com.monopoly.domain.model

class Property(
    val name: String,
    val position: Int,
    val price: Int,
    val rent: Int,
    val colorGroup: ColorGroup,
) {
    private var owner: Player? = null

    fun getOwner(): Player? = owner

    fun setOwner(player: Player?) {
        owner = player
    }

    fun isOwned(): Boolean = owner != null
}
