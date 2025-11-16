package com.monopoly.domain.model

@Suppress("MagicNumber") // ボードは常に40マス
class Board(
    spaces: List<Space>,
) {
    private val spaces: List<Space> = spaces.toList()
    private val propertyMap: MutableMap<Int, Property> = mutableMapOf()

    init {
        require(spaces.size == 40) { "Board must have exactly 40 spaces" }

        // プロパティマップを初期化
        spaces.forEach { space ->
            if (space is Space.PropertySpace) {
                propertyMap[space.position] = space.property
            }
        }
    }

    fun getSpaceCount(): Int = spaces.size

    fun getSpace(position: Int): Space {
        require(position in 0..39) { "Position must be between 0 and 39" }
        return spaces[position]
    }

    fun getPropertyAt(position: Int): Property? = propertyMap[position]

    fun updateProperty(property: Property) {
        propertyMap[property.position] = property
    }

    /**
     * 全プロパティのリストを取得
     *
     * @return プロパティのリスト
     */
    fun getAllProperties(): List<Property> = propertyMap.values.toList()

    /**
     * 指定したカラーグループのプロパティを取得
     *
     * @param colorGroup カラーグループ名
     * @return 指定したカラーグループに属するプロパティのリスト
     */
    fun getPropertiesByColorGroup(colorGroup: String): List<Property> {
        return propertyMap.values.filter { it.colorGroup == colorGroup }
    }
}
