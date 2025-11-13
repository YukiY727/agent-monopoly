package com.monopoly.domain.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PropertyCollectionTest : StringSpec({
    "should create empty collection" {
        val collection = PropertyCollection()

        collection.isEmpty shouldBe true
        collection.size shouldBe 0
    }

    "should add property to collection" {
        val collection = PropertyCollection()
        val property =
            Property(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                rent = 2,
                colorGroup = ColorGroup.BROWN,
            )

        val newCollection = collection.add(property)

        newCollection.size shouldBe 1
        newCollection.contains(property) shouldBe true
        newCollection.isEmpty shouldBe false
    }

    "should calculate total value of properties" {
        val collection = PropertyCollection()
        val property1 =
            Property(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                rent = 2,
                colorGroup = ColorGroup.BROWN,
            )
        val property2 =
            Property(
                name = "Baltic Avenue",
                position = 3,
                price = 60,
                rent = 4,
                colorGroup = ColorGroup.BROWN,
            )

        val newCollection = collection.add(property1).add(property2)

        newCollection.calculateTotalValue() shouldBe Money(120)
    }

    "should remove all properties" {
        val collection = PropertyCollection()
        val property =
            Property(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                rent = 2,
                colorGroup = ColorGroup.BROWN,
            )

        val withProperty = collection.add(property)
        val empty = withProperty.removeAll()

        empty.isEmpty shouldBe true
        empty.size shouldBe 0
    }

    "should convert to list" {
        val collection = PropertyCollection()
        val property1 =
            Property(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                rent = 2,
                colorGroup = ColorGroup.BROWN,
            )
        val property2 =
            Property(
                name = "Baltic Avenue",
                position = 3,
                price = 60,
                rent = 4,
                colorGroup = ColorGroup.BROWN,
            )

        val newCollection = collection.add(property1).add(property2)
        val list = newCollection.toList()

        list.size shouldBe 2
        list[0] shouldBe property1
        list[1] shouldBe property2
    }

    "should have EMPTY constant" {
        val empty = PropertyCollection.EMPTY

        empty.isEmpty shouldBe true
        empty.size shouldBe 0
    }

    "should check contains returns false for non-existent property" {
        val collection = PropertyCollection()
        val property1 =
            Property(
                name = "Mediterranean Avenue",
                position = 1,
                price = 60,
                rent = 2,
                colorGroup = ColorGroup.BROWN,
            )
        val property2 =
            Property(
                name = "Baltic Avenue",
                position = 3,
                price = 60,
                rent = 4,
                colorGroup = ColorGroup.BROWN,
            )

        val newCollection = collection.add(property1)

        newCollection.contains(property2) shouldBe false
    }

    "should calculate total value of empty collection as zero" {
        val collection = PropertyCollection()

        collection.calculateTotalValue() shouldBe Money(0)
    }
})
