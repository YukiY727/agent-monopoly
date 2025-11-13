package com.monopoly.domain.model

sealed class PropertyOwnership {
    object Unowned : PropertyOwnership()

    data class OwnedByPlayer(val player: Player) : PropertyOwnership()
}
