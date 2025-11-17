package com.monopoly.domain.service

import com.monopoly.domain.model.Board
import com.monopoly.domain.model.ColorGroup
import com.monopoly.domain.model.Player
import com.monopoly.domain.model.Property
import com.monopoly.domain.model.Railroad
import com.monopoly.domain.model.Utility

/**
 * プロパティ開発サービス（家・ホテル建設）
 *
 * Phase 14: 家・ホテルシステムの実装
 */
@Suppress("MagicNumber")
class PropertyDevelopmentService {
    companion object {
        const val HOUSE_COST = 100 // 簡略化のため固定（実際は物件ごとに異なる）
        const val HOTEL_COST = 100 // 簡略化のため固定
    }

    /**
     * カラーグループの独占判定
     */
    fun hasColorGroupMonopoly(
        player: Player,
        colorGroup: ColorGroup,
        board: Board,
    ): Boolean {
        val groupProperties = board.getPropertiesByColorGroup(colorGroup)
            .filterIsInstance<Property>() // RailroadとUtilityを除外

        if (groupProperties.isEmpty()) return false

        val ownedByPlayer = groupProperties.filter { property ->
            player.ownedProperties.any { it.position == property.position }
        }

        return ownedByPlayer.size == groupProperties.size
    }

    /**
     * 家賃計算
     *
     * - カラーグループ独占（家なし）: 基本家賃×2
     * - 家1軒: baseRent × 5
     * - 家2軒: baseRent × 15
     * - 家3軒: baseRent × 40
     * - 家4軒: baseRent × 50
     * - ホテル: baseRent × 60（またはより高い値）
     *
     * @param property プロパティ
     * @param board ボード
     * @param owner オーナー
     * @param diceRoll サイコロの目（Utility用、ここでは未使用）
     */
    fun calculateRent(
        property: Property,
        board: Board,
        owner: Player,
        diceRoll: Int,
    ): Int {
        // Railroad と Utilityは別ロジック（Phase 15で実装予定）
        if (property is Railroad || property is Utility) {
            return property.rent
        }

        // ホテルがある場合
        if (property.hasHotel) {
            return when (property.colorGroup) {
                ColorGroup.BROWN, ColorGroup.LIGHT_BLUE -> property.baseRent * 25
                ColorGroup.PINK, ColorGroup.ORANGE -> property.baseRent * 25
                ColorGroup.RED, ColorGroup.YELLOW -> property.baseRent * 25
                ColorGroup.GREEN, ColorGroup.DARK_BLUE -> property.baseRent * 25
            }
        }

        // 家がある場合
        if (property.houses > 0) {
            return when (property.houses) {
                1 -> property.baseRent * 5
                2 -> property.baseRent * 15
                3 -> property.baseRent * 40
                4 -> property.baseRent * 50
                else -> property.baseRent
            }
        }

        // カラーグループ独占（家なし）の場合は基本家賃×2
        if (hasColorGroupMonopoly(owner, property.colorGroup, board)) {
            return property.baseRent * 2
        }

        // 通常の基本家賃
        return property.baseRent
    }

    /**
     * 家を建設
     *
     * @param player プレイヤー
     * @param property プロパティ
     * @param board ボード
     * @return 更新されたプロパティ
     */
    fun buildHouse(
        player: Player,
        property: Property,
        board: Board,
    ): Property {
        // カラーグループ独占チェック
        require(hasColorGroupMonopoly(player, property.colorGroup, board)) {
            "Must own all properties in color group to build"
        }

        // 既に4軒の家がある場合はエラー
        require(property.houses < 4) {
            "Cannot build more than 4 houses. Build a hotel instead."
        }

        // 均等建設ルールチェック
        val groupProperties = board.getPropertiesByColorGroup(property.colorGroup)
            .filterIsInstance<Property>()
        val ownedGroupProperties = groupProperties.filter { prop ->
            player.ownedProperties.any { it.position == prop.position }
        }

        val minHouses = ownedGroupProperties.minOfOrNull { it.houses } ?: 0
        require(property.houses <= minHouses) {
            "Must build evenly. All properties must have $minHouses houses before building another."
        }

        // 建設費用を支払う
        player.subtractMoney(HOUSE_COST)

        // 新しいプロパティを作成
        val updatedProperty = Property(
            name = property.name,
            position = property.position,
            price = property.price,
            baseRent = property.baseRent,
            colorGroup = property.colorGroup,
            ownership = property.ownership,
            houses = property.houses + 1,
            hasHotel = false,
        )

        // プレイヤーとボードのプロパティを更新
        updatePlayerProperty(player, updatedProperty)
        board.updateProperty(updatedProperty)

        return updatedProperty
    }

    /**
     * ホテルを建設
     *
     * @param player プレイヤー
     * @param property プロパティ（4軒の家が必要）
     * @param board ボード
     * @return 更新されたプロパティ
     */
    fun buildHotel(
        player: Player,
        property: Property,
        board: Board,
    ): Property {
        // 4軒の家が必要
        require(property.houses == 4) {
            "Must have 4 houses before building a hotel"
        }

        // カラーグループ独占チェック
        require(hasColorGroupMonopoly(player, property.colorGroup, board)) {
            "Must own all properties in color group to build"
        }

        // ホテル建設費用を支払う
        player.subtractMoney(HOTEL_COST)

        // 新しいプロパティを作成（4軒の家→ホテル）
        val updatedProperty = Property(
            name = property.name,
            position = property.position,
            price = property.price,
            baseRent = property.baseRent,
            colorGroup = property.colorGroup,
            ownership = property.ownership,
            houses = 0,
            hasHotel = true,
        )

        // プレイヤーとボードのプロパティを更新
        updatePlayerProperty(player, updatedProperty)
        board.updateProperty(updatedProperty)

        return updatedProperty
    }

    /**
     * 家を売却（半額で銀行に戻る）
     *
     * @param player プレイヤー
     * @param property プロパティ
     * @return 更新されたプロパティ
     */
    fun sellHouse(
        player: Player,
        property: Property,
    ): Property {
        require(property.houses > 0) {
            "No houses to sell"
        }

        // 半額を受け取る
        player.addMoney(HOUSE_COST / 2)

        // 新しいプロパティを作成
        val updatedProperty = Property(
            name = property.name,
            position = property.position,
            price = property.price,
            baseRent = property.baseRent,
            colorGroup = property.colorGroup,
            ownership = property.ownership,
            houses = property.houses - 1,
            hasHotel = false,
        )

        // プレイヤーのプロパティを更新
        updatePlayerProperty(player, updatedProperty)

        return updatedProperty
    }

    /**
     * ホテルを売却（半額で銀行に戻り、4軒の家に戻る）
     *
     * @param player プレイヤー
     * @param property プロパティ
     * @return 更新されたプロパティ
     */
    fun sellHotel(
        player: Player,
        property: Property,
    ): Property {
        require(property.hasHotel) {
            "No hotel to sell"
        }

        // 半額を受け取る
        player.addMoney(HOTEL_COST / 2)

        // 新しいプロパティを作成（ホテル→4軒の家）
        val updatedProperty = Property(
            name = property.name,
            position = property.position,
            price = property.price,
            baseRent = property.baseRent,
            colorGroup = property.colorGroup,
            ownership = property.ownership,
            houses = 4,
            hasHotel = false,
        )

        // プレイヤーのプロパティを更新
        updatePlayerProperty(player, updatedProperty)

        return updatedProperty
    }

    /**
     * プレイヤーの所有プロパティを更新
     */
    private fun updatePlayerProperty(
        player: Player,
        updatedProperty: Property,
    ) {
        player.updateProperty(updatedProperty)
    }
}
