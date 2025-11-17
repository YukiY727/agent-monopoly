package com.monopoly.domain.model

sealed class PropertyOwnership {
    object Unowned : PropertyOwnership()

    data class OwnedByPlayer(val player: Player) : PropertyOwnership()

    /**
     * 所有者の名前で所有を表す（Phase 19: プレイヤー間取引用）
     */
    data class Owned(val ownerName: String) : PropertyOwnership()
}
