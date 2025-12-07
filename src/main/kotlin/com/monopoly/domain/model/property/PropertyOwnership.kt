package com.monopoly.domain.model.property

import com.monopoly.domain.model.player.Player

sealed class PropertyOwnership {
    object Unowned : PropertyOwnership()

    data class OwnedByPlayer(
        val player: Player,
        val isMortgaged: Boolean = false,
    ) : PropertyOwnership()
}
