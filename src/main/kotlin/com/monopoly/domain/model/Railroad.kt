package com.monopoly.domain.model

/**
 * 鉄道プロパティ
 *
 * Phase 15で家賃計算ロジックを実装予定：
 * - 1つ所有: $25
 * - 2つ所有: $50
 * - 3つ所有: $100
 * - 4つ所有: $200
 */
class Railroad(
    name: String,
    position: Int,
    ownership: PropertyOwnership = PropertyOwnership.Unowned,
) : Property(
    name = name,
    position = position,
    price = 200,
    baseRent = 25,
    colorGroup = ColorGroup.BROWN, // 鉄道には色グループがないが、便宜上BROWNを使用
    ownership = ownership,
) {
    override fun withOwner(newOwner: Player): Railroad =
        Railroad(name, position, PropertyOwnership.OwnedByPlayer(newOwner))

    override fun withoutOwner(): Railroad =
        Railroad(name, position, PropertyOwnership.Unowned)
}
