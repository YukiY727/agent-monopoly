package com.monopoly.domain.model

/**
 * プロパティの家賃構造を表現するデータクラス
 * 建物の数に応じた家賃を保持する
 *
 * @property base 素地（建物なし）の家賃
 * @property withHouse1 家1件の家賃
 * @property withHouse2 家2件の家賃
 * @property withHouse3 家3件の家賃
 * @property withHouse4 家4件の家賃
 * @property withHotel ホテルの家賃
 */
data class PropertyRent(
    val base: Int,
    val withHouse1: Int,
    val withHouse2: Int,
    val withHouse3: Int,
    val withHouse4: Int,
    val withHotel: Int,
)
