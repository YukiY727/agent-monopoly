package com.monopoly.domain.model

@Suppress("MagicNumber")
object BoardFixtures {
    fun createStandardBoard(): Board {
        val spaceList = mutableListOf<Space>()

        // 位置0: GO
        spaceList.add(Space.Go(0))

        // 位置1-39: 簡易版では一部のプロパティのみ配置
        for (i in 1..39) {
            val space =
                when (i) {
                    1 ->
                        Space.PropertySpace(
                            position = 1,
                            property = Property("Mediterranean Avenue", 1, 60, 2, ColorGroup.BROWN),
                        )
                    3 ->
                        Space.PropertySpace(
                            position = 3,
                            property = Property("Baltic Avenue", 3, 60, 4, ColorGroup.BROWN),
                        )
                    6 ->
                        Space.PropertySpace(
                            position = 6,
                            property = Property("Oriental Avenue", 6, 100, 6, ColorGroup.LIGHT_BLUE),
                        )
                    8 ->
                        Space.PropertySpace(
                            position = 8,
                            property = Property("Vermont Avenue", 8, 100, 6, ColorGroup.LIGHT_BLUE),
                        )
                    9 ->
                        Space.PropertySpace(
                            position = 9,
                            property = Property("Connecticut Avenue", 9, 120, 8, ColorGroup.LIGHT_BLUE),
                        )
                    else -> Space.Other(i, SpaceType.CHANCE)
                }
            spaceList.add(space)
        }

        return Board(spaceList)
    }

    fun createBoardWithProperties(properties: List<Property>): Board {
        val spaceList = mutableListOf<Space>()

        // 位置0: GO
        spaceList.add(Space.Go(0))

        // プロパティマップを作成
        val propertyMap = properties.associateBy { it.position }

        // 位置1-39
        for (i in 1..39) {
            val space =
                if (propertyMap.containsKey(i)) {
                    Space.PropertySpace(position = i, property = propertyMap[i]!!)
                } else {
                    Space.Other(i, SpaceType.CHANCE)
                }
            spaceList.add(space)
        }

        return Board(spaceList)
    }
}
