package com.monopoly.domain.model

class PropertyCollection(
    private val properties: List<Property> = emptyList(),
) {
    fun add(property: Property): PropertyCollection = PropertyCollection(properties + property)

    fun removeAll(): PropertyCollection = PropertyCollection(emptyList())

    fun calculateTotalValue(): Money = Money(properties.sumOf { it.price })

    fun contains(property: Property): Boolean = properties.contains(property)

    fun toList(): List<Property> = properties.toList()

    val size: Int
        get() = properties.size

    val isEmpty: Boolean
        get() = properties.isEmpty()

    companion object {
        val EMPTY = PropertyCollection(emptyList())
    }
}
