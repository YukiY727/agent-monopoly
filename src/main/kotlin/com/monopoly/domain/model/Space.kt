package com.monopoly.domain.model

sealed class Space(
    val position: Int,
    val spaceType: SpaceType,
) {
    class Go(position: Int) : Space(position, SpaceType.GO)

    class PropertySpace(
        position: Int,
        val property: Property,
    ) : Space(position, SpaceType.PROPERTY)

    // Phase 1では未実装のマスタイプ（将来の拡張用）
    class Other(
        position: Int,
        spaceType: SpaceType,
    ) : Space(position, spaceType)
}
