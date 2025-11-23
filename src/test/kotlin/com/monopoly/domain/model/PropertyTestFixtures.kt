package com.monopoly.domain.model

/**
 * テスト用のProperty作成ヘルパー関数
 */
object PropertyTestFixtures {
    /**
     * テスト用のPropertyRentを作成
     */
    fun createTestRent(
        base: Int = 2,
        withHouse1: Int = base * 5,
        withHouse2: Int = base * 15,
        withHouse3: Int = base * 45,
        withHouse4: Int = base * 80,
        withHotel: Int = base * 125,
    ): PropertyRent =
        PropertyRent(
            base = base,
            withHouse1 = withHouse1,
            withHouse2 = withHouse2,
            withHouse3 = withHouse3,
            withHouse4 = withHouse4,
            withHotel = withHotel,
        )

    /**
     * テスト用のPropertyを作成
     */
    fun createTestProperty(
        name: String = "Test Property",
        position: Int = 1,
        price: Int = 60,
        baseRent: Int = 2,
        houseCost: Int = 50,
        hotelCost: Int = 50,
        colorGroup: ColorGroup = ColorGroup.BROWN,
        ownership: PropertyOwnership = PropertyOwnership.Unowned,
        buildings: PropertyBuildings = PropertyBuildings(),
    ): Property =
        Property(
            name = name,
            position = position,
            price = price,
            rent = createTestRent(base = baseRent),
            houseCost = houseCost,
            hotelCost = hotelCost,
            colorGroup = colorGroup,
            ownership = ownership,
            buildings = buildings,
        )
}
