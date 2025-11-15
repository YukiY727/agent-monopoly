package com.monopoly.domain.model

class Board(
    spaces: List<Space>,
) {
    private val spaces: List<Space> = spaces.toList()
    private val propertyMap: MutableMap<Int, Property> = mutableMapOf()

    init {
        require(spaces.size == BOARD_SIZE) { "Board must have exactly $BOARD_SIZE spaces" }

        // プロパティマップを初期化
        spaces.forEach { space ->
            if (space is Space.PropertySpace) {
                propertyMap[space.position] = space.property
            }
        }
    }

    fun getSpaceCount(): Int = spaces.size

    fun getSpace(position: Int): Space {
        require(position in 0 until BOARD_SIZE) { "Position must be between 0 and ${BOARD_SIZE - 1}" }
        return spaces[position]
    }

    fun getPropertyAt(position: Int): Property? = propertyMap[position]

    fun updateProperty(property: Property) {
        propertyMap[property.position] = property
    }

    companion object {
        const val BOARD_SIZE: Int = 40
    }
}
