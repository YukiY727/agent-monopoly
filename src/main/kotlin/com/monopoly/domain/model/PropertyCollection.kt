package com.monopoly.domain.model

class PropertyCollection(
    private val properties: List<Property> = emptyList(),
) {
    fun add(property: Property): PropertyCollection = PropertyCollection(properties + property)

    fun remove(property: Property): PropertyCollection = PropertyCollection(properties.filter { it.position != property.position })

    fun removeAll(): PropertyCollection = PropertyCollection(emptyList())

    fun update(property: Property): PropertyCollection {
        val updated = properties.filter { it.position != property.position } + property
        return PropertyCollection(updated)
    }

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
