package com.monopoly.domain.experiment

import kotlinx.serialization.Serializable

/**
 * プレイヤーごとの行動統計
 *
 * @property name プレイヤー名
 * @property propertiesPurchased 購入したプロパティ数
 * @property housesBuild 建設した家の数
 * @property hotelsBuilt 建設したホテルの数
 * @property rentPaid 支払った家賃総額
 * @property rentReceived 受け取った家賃総額
 * @property timesInJail 刑務所に入った回数
 * @property finalMoney 最終所持金
 */
@Serializable
data class PlayerStatistics(
    val name: String,
    val propertiesPurchased: Int,
    val housesBuild: Int,
    val hotelsBuilt: Int,
    val rentPaid: Int,
    val rentReceived: Int,
    val timesInJail: Int,
    val finalMoney: Int,
)
