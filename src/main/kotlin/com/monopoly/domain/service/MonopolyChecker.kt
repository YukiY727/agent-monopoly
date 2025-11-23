package com.monopoly.domain.service

import com.monopoly.domain.model.ColorGroup
import com.monopoly.domain.model.Player

/**
 * モノポリー（特定の色グループ全てを所有している状態）をチェックするサービス
 */
class MonopolyChecker {
    companion object {
        // 各色グループに含まれるプロパティ数
        private val COLOR_GROUP_SIZES: Map<ColorGroup, Int> =
            mapOf(
                ColorGroup.BROWN to 2,
                ColorGroup.LIGHT_BLUE to 3,
                ColorGroup.PINK to 3,
                ColorGroup.ORANGE to 3,
                ColorGroup.RED to 3,
                ColorGroup.YELLOW to 3,
                ColorGroup.GREEN to 3,
                ColorGroup.DARK_BLUE to 2,
            )
    }

    /**
     * プレイヤーが指定した色グループのモノポリーを持っているかチェックする
     *
     * @param player チェック対象のプレイヤー
     * @param colorGroup チェックする色グループ
     * @return モノポリーを持っている場合true
     */
    fun hasMonopoly(
        player: Player,
        colorGroup: ColorGroup,
    ): Boolean {
        val requiredCount: Int = COLOR_GROUP_SIZES[colorGroup] ?: return false

        val ownedInGroupCount: Int =
            player.ownedProperties
                .count { property -> property.colorGroup == colorGroup }

        return ownedInGroupCount == requiredCount
    }
}
