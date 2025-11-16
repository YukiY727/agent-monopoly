package com.monopoly.domain.model

/**
 * 基本的な不動産クラス
 *
 * Phase 13: baseRentを追加し、rent算出の基礎とする
 * Phase 14: 家・ホテルによる家賃変動を実装
 */
open class Property(
    val name: String,
    val position: Int,
    val price: Int,
    val baseRent: Int,
    val colorGroup: ColorGroup,
    val ownership: PropertyOwnership = PropertyOwnership.Unowned,
    val houses: Int = 0,
    val hasHotel: Boolean = false,
) {
    // 後方互換性のため、rentプロパティを提供（Phase 14で家賃計算ロジックに置き換え）
    open val rent: Int
        get() = baseRent

    // Value object accessors
    val positionValue: BoardPosition
        get() = BoardPosition(position)

    val priceValue: Money
        get() = Money(price)

    val rentValue: Money
        get() = Money(rent)

    open fun withOwner(newOwner: Player): Property =
        Property(name, position, price, baseRent, colorGroup, PropertyOwnership.OwnedByPlayer(newOwner), houses, hasHotel)

    open fun withoutOwner(): Property =
        Property(name, position, price, baseRent, colorGroup, PropertyOwnership.Unowned, houses, hasHotel)

    fun isOwned(): Boolean = ownership is PropertyOwnership.OwnedByPlayer

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Property) return false
        return position == other.position
    }

    override fun hashCode(): Int = position
}
