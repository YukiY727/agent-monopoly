package com.monopoly.domain.model

/**
 * Monopolyボードのマス
 *
 * Phase 13: 完全な40マスボードのために各種マスタイプを追加
 */
sealed class Space(
    val position: Int,
    val spaceType: SpaceType,
) {
    class Go(position: Int) : Space(position, SpaceType.GO)

    class PropertySpace(
        position: Int,
        val property: Property,
    ) : Space(position, SpaceType.PROPERTY)

    /**
     * 刑務所マス（位置10）
     * "Just Visiting" または収監中のプレイヤーが留まる場所
     */
    class Jail(position: Int) : Space(position, SpaceType.JAIL)

    /**
     * 無料駐車場マス（位置20）
     */
    class FreeParking(position: Int) : Space(position, SpaceType.FREE_PARKING)

    /**
     * 刑務所行きマス（位置30）
     */
    class GoToJail(position: Int) : Space(position, SpaceType.GO_TO_JAIL)

    /**
     * 税金マス
     * - 位置4: Income Tax ($200)
     * - 位置38: Luxury Tax ($100)
     */
    class Tax(
        position: Int,
        val amount: Int,
    ) : Space(position, SpaceType.TAX)

    /**
     * Chanceマス（位置7, 22, 36）
     */
    class ChanceSpace(position: Int) : Space(position, SpaceType.CHANCE)

    /**
     * Community Chestマス（位置2, 17, 33）
     */
    class CommunityChestSpace(position: Int) : Space(position, SpaceType.COMMUNITY_CHEST)

    // Phase 1で定義された汎用的なOtherマス（後方互換性のため残す）
    class Other(
        position: Int,
        spaceType: SpaceType,
    ) : Space(position, spaceType)
}
