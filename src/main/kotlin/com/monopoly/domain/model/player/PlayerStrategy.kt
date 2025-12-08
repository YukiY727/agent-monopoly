package com.monopoly.domain.model.player

import com.monopoly.domain.model.property.Property
import com.monopoly.domain.model.property.StreetProperty

interface PlayerStrategy {
    fun shouldBuy(
        property: Property,
        currentMoney: Int,
    ): Boolean

    fun shouldBuildHouse(
        property: StreetProperty,
        currentMoney: Int,
    ): Boolean

    fun shouldBuildHotel(
        property: StreetProperty,
        currentMoney: Int,
    ): Boolean

    // Phase 3: 刑務所関連
    fun shouldPayToEscapeJail(currentMoney: Int): Boolean

    // Phase 7: オークション関連
    /**
     * オークションでの入札を決定
     *
     * @param property オークション対象のプロパティ
     * @param currentBid 現在の最高入札額（null = まだ誰も入札していない）
     * @param currentMoney プレイヤーの現在の所持金
     * @return 入札額（null = パス）
     */
    fun decideAuctionBid(
        property: Property,
        currentBid: Int?,
        currentMoney: Int,
    ): Int?
}
