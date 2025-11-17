package com.monopoly.domain.model

/**
 * 鉄道プロパティ
 *
 * Phase 15: 所有数に応じた家賃計算
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
    /**
     * 鉄道の家賃を計算
     *
     * @param allRailroads 全ての鉄道プロパティ
     * @return 家賃
     */
    fun calculateRent(allRailroads: List<Property>): Int {
        if (ownership !is PropertyOwnership.OwnedByPlayer) {
            return 0
        }

        val owner = (ownership as PropertyOwnership.OwnedByPlayer).player
        val ownedRailroadCount = allRailroads
            .filterIsInstance<Railroad>()
            .count { railroad ->
                railroad.ownership is PropertyOwnership.OwnedByPlayer &&
                    (railroad.ownership as PropertyOwnership.OwnedByPlayer).player == owner
            }

        return when (ownedRailroadCount) {
            1 -> 25
            2 -> 50
            3 -> 100
            4 -> 200
            else -> 0
        }
    }

    override val rent: Int
        get() = baseRent // デフォルト値、calculateRent()を使うべき

    override fun withOwner(newOwner: Player): Railroad =
        Railroad(name, position, PropertyOwnership.OwnedByPlayer(newOwner))

    override fun withoutOwner(): Railroad =
        Railroad(name, position, PropertyOwnership.Unowned)
}
