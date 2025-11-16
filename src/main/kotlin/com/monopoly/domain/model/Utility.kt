package com.monopoly.domain.model

/**
 * 公共施設プロパティ（電力会社、水道会社）
 *
 * Phase 15で家賃計算ロジックを実装予定：
 * - 1つ所有: サイコロの目 × 4
 * - 2つ所有: サイコロの目 × 10
 */
class Utility(
    name: String,
    position: Int,
    ownership: PropertyOwnership = PropertyOwnership.Unowned,
) : Property(
    name = name,
    position = position,
    price = 150,
    baseRent = 0, // サイコロの目に依存するため基本家賃は0
    colorGroup = ColorGroup.BROWN, // 公共施設には色グループがないが、便宜上BROWNを使用
    ownership = ownership,
) {
    override fun withOwner(newOwner: Player): Utility =
        Utility(name, position, PropertyOwnership.OwnedByPlayer(newOwner))

    override fun withoutOwner(): Utility =
        Utility(name, position, PropertyOwnership.Unowned)
}
