package com.monopoly.domain.model

/**
 * 標準的なMonopolyボード（40マス）
 *
 * Phase 13: 実際のMonopolyボードの全40マスを正確に実装
 */
@Suppress("MagicNumber", "LongMethod")
object StandardBoard {
    /**
     * 標準的な40マスのMonopolyボードを生成
     */
    fun create(): Board {
        val spaces = (0..39).map { position ->
            createSpaceAt(position)
        }
        return Board(spaces)
    }

    /**
     * 各位置のマスを生成
     */
    private fun createSpaceAt(position: Int): Space =
        when (position) {
            0 -> Space.Go(0)

            // BROWN (2 properties)
            1 -> Space.PropertySpace(
                position = 1,
                property = Property(
                    name = "Mediterranean Avenue",
                    position = 1,
                    price = 60,
                    baseRent = 2,
                    colorGroup = ColorGroup.BROWN,
                ),
            )
            3 -> Space.PropertySpace(
                position = 3,
                property = Property(
                    name = "Baltic Avenue",
                    position = 3,
                    price = 60,
                    baseRent = 4,
                    colorGroup = ColorGroup.BROWN,
                ),
            )

            // LIGHT_BLUE (3 properties)
            6 -> Space.PropertySpace(
                position = 6,
                property = Property(
                    name = "Oriental Avenue",
                    position = 6,
                    price = 100,
                    baseRent = 6,
                    colorGroup = ColorGroup.LIGHT_BLUE,
                ),
            )
            8 -> Space.PropertySpace(
                position = 8,
                property = Property(
                    name = "Vermont Avenue",
                    position = 8,
                    price = 100,
                    baseRent = 6,
                    colorGroup = ColorGroup.LIGHT_BLUE,
                ),
            )
            9 -> Space.PropertySpace(
                position = 9,
                property = Property(
                    name = "Connecticut Avenue",
                    position = 9,
                    price = 120,
                    baseRent = 8,
                    colorGroup = ColorGroup.LIGHT_BLUE,
                ),
            )

            // PINK (3 properties)
            11 -> Space.PropertySpace(
                position = 11,
                property = Property(
                    name = "St. Charles Place",
                    position = 11,
                    price = 140,
                    baseRent = 10,
                    colorGroup = ColorGroup.PINK,
                ),
            )
            13 -> Space.PropertySpace(
                position = 13,
                property = Property(
                    name = "States Avenue",
                    position = 13,
                    price = 140,
                    baseRent = 10,
                    colorGroup = ColorGroup.PINK,
                ),
            )
            14 -> Space.PropertySpace(
                position = 14,
                property = Property(
                    name = "Virginia Avenue",
                    position = 14,
                    price = 160,
                    baseRent = 12,
                    colorGroup = ColorGroup.PINK,
                ),
            )

            // ORANGE (3 properties)
            16 -> Space.PropertySpace(
                position = 16,
                property = Property(
                    name = "St. James Place",
                    position = 16,
                    price = 180,
                    baseRent = 14,
                    colorGroup = ColorGroup.ORANGE,
                ),
            )
            18 -> Space.PropertySpace(
                position = 18,
                property = Property(
                    name = "Tennessee Avenue",
                    position = 18,
                    price = 180,
                    baseRent = 14,
                    colorGroup = ColorGroup.ORANGE,
                ),
            )
            19 -> Space.PropertySpace(
                position = 19,
                property = Property(
                    name = "New York Avenue",
                    position = 19,
                    price = 200,
                    baseRent = 16,
                    colorGroup = ColorGroup.ORANGE,
                ),
            )

            // RED (3 properties)
            21 -> Space.PropertySpace(
                position = 21,
                property = Property(
                    name = "Kentucky Avenue",
                    position = 21,
                    price = 220,
                    baseRent = 18,
                    colorGroup = ColorGroup.RED,
                ),
            )
            23 -> Space.PropertySpace(
                position = 23,
                property = Property(
                    name = "Indiana Avenue",
                    position = 23,
                    price = 220,
                    baseRent = 18,
                    colorGroup = ColorGroup.RED,
                ),
            )
            24 -> Space.PropertySpace(
                position = 24,
                property = Property(
                    name = "Illinois Avenue",
                    position = 24,
                    price = 240,
                    baseRent = 20,
                    colorGroup = ColorGroup.RED,
                ),
            )

            // YELLOW (3 properties)
            26 -> Space.PropertySpace(
                position = 26,
                property = Property(
                    name = "Atlantic Avenue",
                    position = 26,
                    price = 260,
                    baseRent = 22,
                    colorGroup = ColorGroup.YELLOW,
                ),
            )
            27 -> Space.PropertySpace(
                position = 27,
                property = Property(
                    name = "Ventnor Avenue",
                    position = 27,
                    price = 260,
                    baseRent = 22,
                    colorGroup = ColorGroup.YELLOW,
                ),
            )
            29 -> Space.PropertySpace(
                position = 29,
                property = Property(
                    name = "Marvin Gardens",
                    position = 29,
                    price = 280,
                    baseRent = 24,
                    colorGroup = ColorGroup.YELLOW,
                ),
            )

            // GREEN (3 properties)
            31 -> Space.PropertySpace(
                position = 31,
                property = Property(
                    name = "Pacific Avenue",
                    position = 31,
                    price = 300,
                    baseRent = 26,
                    colorGroup = ColorGroup.GREEN,
                ),
            )
            32 -> Space.PropertySpace(
                position = 32,
                property = Property(
                    name = "North Carolina Avenue",
                    position = 32,
                    price = 300,
                    baseRent = 26,
                    colorGroup = ColorGroup.GREEN,
                ),
            )
            34 -> Space.PropertySpace(
                position = 34,
                property = Property(
                    name = "Pennsylvania Avenue",
                    position = 34,
                    price = 320,
                    baseRent = 28,
                    colorGroup = ColorGroup.GREEN,
                ),
            )

            // DARK_BLUE (2 properties)
            37 -> Space.PropertySpace(
                position = 37,
                property = Property(
                    name = "Park Place",
                    position = 37,
                    price = 350,
                    baseRent = 35,
                    colorGroup = ColorGroup.DARK_BLUE,
                ),
            )
            39 -> Space.PropertySpace(
                position = 39,
                property = Property(
                    name = "Boardwalk",
                    position = 39,
                    price = 400,
                    baseRent = 50,
                    colorGroup = ColorGroup.DARK_BLUE,
                ),
            )

            // Railroads (4 total)
            5 -> Space.PropertySpace(
                position = 5,
                property = Railroad(
                    name = "Reading Railroad",
                    position = 5,
                ),
            )
            15 -> Space.PropertySpace(
                position = 15,
                property = Railroad(
                    name = "Pennsylvania Railroad",
                    position = 15,
                ),
            )
            25 -> Space.PropertySpace(
                position = 25,
                property = Railroad(
                    name = "B&O Railroad",
                    position = 25,
                ),
            )
            35 -> Space.PropertySpace(
                position = 35,
                property = Railroad(
                    name = "Short Line Railroad",
                    position = 35,
                ),
            )

            // Utilities (2 total)
            12 -> Space.PropertySpace(
                position = 12,
                property = Utility(
                    name = "Electric Company",
                    position = 12,
                ),
            )
            28 -> Space.PropertySpace(
                position = 28,
                property = Utility(
                    name = "Water Works",
                    position = 28,
                ),
            )

            // Special spaces
            2 -> Space.CommunityChestSpace(2)
            7 -> Space.ChanceSpace(7)
            10 -> Space.Jail(10)
            17 -> Space.CommunityChestSpace(17)
            20 -> Space.FreeParking(20)
            22 -> Space.ChanceSpace(22)
            30 -> Space.GoToJail(30)
            33 -> Space.CommunityChestSpace(33)
            36 -> Space.ChanceSpace(36)

            // Tax spaces
            4 -> Space.Tax(position = 4, amount = 200) // Income Tax
            38 -> Space.Tax(position = 38, amount = 100) // Luxury Tax

            else -> error("Invalid position: $position")
        }
}
