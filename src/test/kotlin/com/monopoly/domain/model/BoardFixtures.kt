package com.monopoly.domain.model

@Suppress("MagicNumber")
object BoardFixtures {
    fun createStandardBoard(): Board = com.monopoly.cli.createStandardBoard()

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
