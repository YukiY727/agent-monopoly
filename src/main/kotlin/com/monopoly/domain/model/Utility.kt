package com.monopoly.domain.model

/**
 * 公共施設プロパティ（電力会社、水道会社）
 *
 * Phase 15: サイコロの目に応じた家賃計算
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
    /**
     * 公共施設の家賃を計算
     *
     * @param allUtilities 全ての公共施設プロパティ
     * @param diceRoll サイコロの目
     * @return 家賃
     */
    fun calculateRent(
        allUtilities: List<Property>,
        diceRoll: Int,
    ): Int {
        if (ownership !is PropertyOwnership.OwnedByPlayer) {
            return 0
        }

        val owner = (ownership as PropertyOwnership.OwnedByPlayer).player
        val ownedUtilityCount = allUtilities
            .filterIsInstance<Utility>()
            .count { utility ->
                utility.ownership is PropertyOwnership.OwnedByPlayer &&
                    (utility.ownership as PropertyOwnership.OwnedByPlayer).player == owner
            }

        val multiplier = when (ownedUtilityCount) {
            1 -> 4
            2 -> 10
            else -> 0
        }

        return diceRoll * multiplier
    }

    override val rent: Int
        get() = 0 // サイコロの目に依存するため固定値なし、calculateRent()を使うべき

    override fun withOwner(newOwner: Player): Utility =
        Utility(name, position, PropertyOwnership.OwnedByPlayer(newOwner))

    override fun withoutOwner(): Utility =
        Utility(name, position, PropertyOwnership.Unowned)
}
