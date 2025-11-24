package com.monopoly.domain.service

import com.monopoly.domain.model.Player
import com.monopoly.domain.model.Property
import com.monopoly.domain.model.PropertyBuildings

/**
 * 建物（家・ホテル）の建設を管理するサービス
 *
 * 主な責務:
 * - モノポリー（色グループ全て所有）の確認
 * - 建設コストのチェックと支払い
 * - 均等建設ルールの適用
 */
class BuildingService(
    private val monopolyChecker: MonopolyCheckerService,
) {
    /**
     * プロパティに家を建設する
     *
     * 建設条件:
     * 1. プレイヤーがプロパティの色グループ全て（モノポリー）を所有している
     * 2. プレイヤーが建設コストを支払える
     * 3. プロパティが家を建設可能（4つ未満、ホテルなし）
     * 4. 均等建設ルール: 同じ色グループの他のプロパティより2つ以上多く建てられない
     *
     * @param player プレイヤー
     * @param property 建設対象のプロパティ
     * @return 建設成功した場合true
     */
    fun buildHouse(
        player: Player,
        property: Property,
    ): Boolean {
        // 建設可能チェック
        val canBuild: Boolean =
            canBuildHouseImpl(player, property)

        if (!canBuild) {
            return false
        }

        // 建設実行: プロパティの建物状態を更新
        val newBuildings: PropertyBuildings = property.buildings.copy(houseCount = property.buildings.houseCount + 1)
        val updatedProperty: Property = property.copy(buildings = newBuildings)

        // プレイヤーの所有プロパティリストを更新
        player.removeProperty(property)
        player.acquireProperty(updatedProperty)

        // 建設コストを支払う
        player.subtractMoney(property.houseCost)

        return true
    }

    private fun canBuildHouseImpl(
        player: Player,
        property: Property,
    ): Boolean =
        monopolyChecker.hasMonopoly(player, property.colorGroup) &&
            property.buildings.canBuildHouse() &&
            player.money >= property.houseCost &&
            canBuildHouseEvenly(player, property)

    /**
     * プロパティにホテルを建設する
     *
     * 建設条件:
     * 1. プレイヤーがプロパティの色グループ全て（モノポリー）を所有している
     * 2. プレイヤーが建設コストを支払える
     * 3. プロパティが家を4つ持っている
     *
     * @param player プレイヤー
     * @param property 建設対象のプロパティ
     * @return 建設成功した場合true
     */
    fun buildHotel(
        player: Player,
        property: Property,
    ): Boolean {
        // 建設可能チェック
        val canBuild: Boolean =
            canBuildHotelImpl(player, property)

        if (!canBuild) {
            return false
        }

        // 建設実行: 家4つをホテル1つに置き換え
        val newBuildings: PropertyBuildings = PropertyBuildings(houseCount = 0, hasHotel = true)
        val updatedProperty: Property = property.copy(buildings = newBuildings)

        // プレイヤーの所有プロパティリストを更新
        player.removeProperty(property)
        player.acquireProperty(updatedProperty)

        // 建設コストを支払う
        player.subtractMoney(property.hotelCost)

        return true
    }

    private fun canBuildHotelImpl(
        player: Player,
        property: Property,
    ): Boolean =
        monopolyChecker.hasMonopoly(player, property.colorGroup) &&
            property.buildings.canBuildHotel() &&
            player.money >= property.hotelCost

    /**
     * 均等建設ルールをチェックする
     *
     * ルール: 同じ色グループ内で、他のプロパティより2つ以上多く家を建てることはできない
     *        = 建設後の家数が、グループ内の最小家数+1以下である必要がある
     *
     * @param player プレイヤー
     * @param targetProperty 建設対象のプロパティ
     * @return 均等建設ルールを満たす場合true
     */
    private fun canBuildHouseEvenly(
        player: Player,
        targetProperty: Property,
    ): Boolean {
        // 同じ色グループの全プロパティを取得
        val sameColorProperties: List<Property> =
            player.ownedProperties.filter { property ->
                property.colorGroup == targetProperty.colorGroup
            }

        // 他のプロパティの最小家数を取得
        val minHouseCount: Int =
            sameColorProperties
                .filter { property -> property != targetProperty }
                .minOfOrNull { property -> property.buildings.houseCount }
                ?: 0

        // 建設後の家数が最小家数+1以下である必要がある
        val afterBuildCount: Int = targetProperty.buildings.houseCount + 1
        return afterBuildCount <= minHouseCount + 1
    }
}
