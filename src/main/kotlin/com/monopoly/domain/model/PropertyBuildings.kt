package com.monopoly.domain.model

/**
 * プロパティの建物（家とホテル）を管理するデータクラス
 *
 * @property houseCount 家の数（0-4）
 * @property hasHotel ホテルの有無
 */
data class PropertyBuildings(
    val houseCount: Int = 0,
    val hasHotel: Boolean = false,
) {
    init {
        require(houseCount in MIN_HOUSES..MAX_HOUSES) {
            "House count must be between $MIN_HOUSES and $MAX_HOUSES, but was $houseCount"
        }
        require(!(hasHotel && houseCount > 0)) { "Cannot have both hotel and houses" }
    }

    /**
     * 家を建設できるか判定
     * @return 家を建設できる場合true
     */
    fun canBuildHouse(): Boolean = houseCount < MAX_HOUSES && !hasHotel

    /**
     * ホテルを建設できるか判定
     * @return ホテルを建設できる場合true
     */
    fun canBuildHotel(): Boolean = houseCount == MAX_HOUSES && !hasHotel

    /**
     * 建物の総数を取得
     * ホテルは家5件分として扱う
     * @return 建物の総数
     */
    fun getTotalBuildingCount(): Int = if (hasHotel) HOTEL_EQUIVALENT_HOUSES else houseCount

    companion object {
        /** 最小家数 */
        private const val MIN_HOUSES = 0

        /** 最大家数 */
        const val MAX_HOUSES = 4

        /** ホテルの家換算数 */
        private const val HOTEL_EQUIVALENT_HOUSES = 5
    }
}
