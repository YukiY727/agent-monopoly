package com.monopoly.cli

import com.monopoly.domain.model.Board
import com.monopoly.domain.model.ColorGroup
import com.monopoly.domain.model.Property
import com.monopoly.domain.model.StreetProperty
import com.monopoly.domain.model.PropertyRent
import com.monopoly.domain.model.Space
import com.monopoly.domain.model.SpaceType

@Suppress("MagicNumber")
private fun createPropertyRent(base: Int): PropertyRent =
    PropertyRent(
        base = base,
        withHouse1 = base * 5,
        withHouse2 = base * 15,
        withHouse3 = base * 45,
        withHouse4 = base * 80,
        withHotel = base * 125,
    )

@Suppress("MagicNumber")
private fun getSpaceForPosition(position: Int): Space =
    when (position) {
        1, 3 -> createBrownProperty(position)
        6, 8, 9 -> createLightBlueProperty(position)
        11, 13, 14 -> createPinkProperty(position)
        16, 18, 19 -> createOrangeProperty(position)
        21, 23, 24 -> createRedProperty(position)
        26, 27, 29 -> createYellowProperty(position)
        31, 32, 34 -> createGreenProperty(position)
        37, 39 -> createDarkBlueProperty(position)
        else -> Space.Other(position, SpaceType.CHANCE)
    }

@Suppress("MagicNumber")
private fun createBrownProperty(position: Int): Space.PropertySpace =
    when (position) {
        1 ->
            Space.PropertySpace(
                position = 1,
                property =
                    StreetProperty(
                        name = "Mediterranean Avenue",
                        position = 1,
                        price = 60,
                        rent = createPropertyRent(2),
                        houseCost = 50,
                        hotelCost = 50,
                        colorGroup = ColorGroup.BROWN,
                    ),
            )
        else ->
            Space.PropertySpace(
                position = 3,
                property =
                    StreetProperty(
                        name = "Baltic Avenue",
                        position = 3,
                        price = 60,
                        rent = createPropertyRent(4),
                        houseCost = 50,
                        hotelCost = 50,
                        colorGroup = ColorGroup.BROWN,
                    ),
            )
    }

@Suppress("MagicNumber")
private fun createLightBlueProperty(position: Int): Space.PropertySpace =
    when (position) {
        6 ->
            Space.PropertySpace(
                position = 6,
                property =
                    StreetProperty(
                        name = "Oriental Avenue",
                        position = 6,
                        price = 100,
                        rent = createPropertyRent(6),
                        houseCost = 50,
                        hotelCost = 50,
                        colorGroup = ColorGroup.LIGHT_BLUE,
                    ),
            )
        8 ->
            Space.PropertySpace(
                position = 8,
                property =
                    StreetProperty(
                        name = "Vermont Avenue",
                        position = 8,
                        price = 100,
                        rent = createPropertyRent(6),
                        houseCost = 50,
                        hotelCost = 50,
                        colorGroup = ColorGroup.LIGHT_BLUE,
                    ),
            )
        else ->
            Space.PropertySpace(
                position = 9,
                property =
                    StreetProperty(
                        name = "Connecticut Avenue",
                        position = 9,
                        price = 120,
                        rent = createPropertyRent(8),
                        houseCost = 50,
                        hotelCost = 50,
                        colorGroup = ColorGroup.LIGHT_BLUE,
                    ),
            )
    }

@Suppress("MagicNumber")
private fun createPinkProperty(position: Int): Space.PropertySpace =
    when (position) {
        11 ->
            Space.PropertySpace(
                position = 11,
                property =
                    StreetProperty(
                        name = "St. Charles Place",
                        position = 11,
                        price = 140,
                        rent = createPropertyRent(10),
                        houseCost = 100,
                        hotelCost = 100,
                        colorGroup = ColorGroup.PINK,
                    ),
            )
        13 ->
            Space.PropertySpace(
                position = 13,
                property =
                    StreetProperty(
                        name = "States Avenue",
                        position = 13,
                        price = 140,
                        rent = createPropertyRent(10),
                        houseCost = 100,
                        hotelCost = 100,
                        colorGroup = ColorGroup.PINK,
                    ),
            )
        else ->
            Space.PropertySpace(
                position = 14,
                property =
                    StreetProperty(
                        name = "Virginia Avenue",
                        position = 14,
                        price = 160,
                        rent = createPropertyRent(12),
                        houseCost = 100,
                        hotelCost = 100,
                        colorGroup = ColorGroup.PINK,
                    ),
            )
    }

@Suppress("MagicNumber")
private fun createOrangeProperty(position: Int): Space.PropertySpace =
    when (position) {
        16 ->
            Space.PropertySpace(
                position = 16,
                property =
                    StreetProperty(
                        name = "St. James Place",
                        position = 16,
                        price = 180,
                        rent = createPropertyRent(14),
                        houseCost = 100,
                        hotelCost = 100,
                        colorGroup = ColorGroup.ORANGE,
                    ),
            )
        18 ->
            Space.PropertySpace(
                position = 18,
                property =
                    StreetProperty(
                        name = "Tennessee Avenue",
                        position = 18,
                        price = 180,
                        rent = createPropertyRent(14),
                        houseCost = 100,
                        hotelCost = 100,
                        colorGroup = ColorGroup.ORANGE,
                    ),
            )
        else ->
            Space.PropertySpace(
                position = 19,
                property =
                    StreetProperty(
                        name = "New York Avenue",
                        position = 19,
                        price = 200,
                        rent = createPropertyRent(16),
                        houseCost = 100,
                        hotelCost = 100,
                        colorGroup = ColorGroup.ORANGE,
                    ),
            )
    }

@Suppress("MagicNumber")
private fun createRedProperty(position: Int): Space.PropertySpace =
    when (position) {
        21 ->
            Space.PropertySpace(
                position = 21,
                property =
                    StreetProperty(
                        name = "Kentucky Avenue",
                        position = 21,
                        price = 220,
                        rent = createPropertyRent(18),
                        houseCost = 150,
                        hotelCost = 150,
                        colorGroup = ColorGroup.RED,
                    ),
            )
        23 ->
            Space.PropertySpace(
                position = 23,
                property =
                    StreetProperty(
                        name = "Indiana Avenue",
                        position = 23,
                        price = 220,
                        rent = createPropertyRent(18),
                        houseCost = 150,
                        hotelCost = 150,
                        colorGroup = ColorGroup.RED,
                    ),
            )
        else ->
            Space.PropertySpace(
                position = 24,
                property =
                    StreetProperty(
                        name = "Illinois Avenue",
                        position = 24,
                        price = 240,
                        rent = createPropertyRent(20),
                        houseCost = 150,
                        hotelCost = 150,
                        colorGroup = ColorGroup.RED,
                    ),
            )
    }

@Suppress("MagicNumber")
private fun createYellowProperty(position: Int): Space.PropertySpace =
    when (position) {
        26 ->
            Space.PropertySpace(
                position = 26,
                property =
                    StreetProperty(
                        name = "Atlantic Avenue",
                        position = 26,
                        price = 260,
                        rent = createPropertyRent(22),
                        houseCost = 150,
                        hotelCost = 150,
                        colorGroup = ColorGroup.YELLOW,
                    ),
            )
        27 ->
            Space.PropertySpace(
                position = 27,
                property =
                    StreetProperty(
                        name = "Ventnor Avenue",
                        position = 27,
                        price = 260,
                        rent = createPropertyRent(22),
                        houseCost = 150,
                        hotelCost = 150,
                        colorGroup = ColorGroup.YELLOW,
                    ),
            )
        else ->
            Space.PropertySpace(
                position = 29,
                property =
                    StreetProperty(
                        name = "Marvin Gardens",
                        position = 29,
                        price = 280,
                        rent = createPropertyRent(24),
                        houseCost = 150,
                        hotelCost = 150,
                        colorGroup = ColorGroup.YELLOW,
                    ),
            )
    }

@Suppress("MagicNumber")
private fun createGreenProperty(position: Int): Space.PropertySpace =
    when (position) {
        31 ->
            Space.PropertySpace(
                position = 31,
                property =
                    StreetProperty(
                        name = "Pacific Avenue",
                        position = 31,
                        price = 300,
                        rent = createPropertyRent(26),
                        houseCost = 200,
                        hotelCost = 200,
                        colorGroup = ColorGroup.GREEN,
                    ),
            )
        32 ->
            Space.PropertySpace(
                position = 32,
                property =
                    StreetProperty(
                        name = "North Carolina Avenue",
                        position = 32,
                        price = 300,
                        rent = createPropertyRent(26),
                        houseCost = 200,
                        hotelCost = 200,
                        colorGroup = ColorGroup.GREEN,
                    ),
            )
        else ->
            Space.PropertySpace(
                position = 34,
                property =
                    StreetProperty(
                        name = "Pennsylvania Avenue",
                        position = 34,
                        price = 320,
                        rent = createPropertyRent(28),
                        houseCost = 200,
                        hotelCost = 200,
                        colorGroup = ColorGroup.GREEN,
                    ),
            )
    }

@Suppress("MagicNumber")
private fun createDarkBlueProperty(position: Int): Space.PropertySpace =
    when (position) {
        37 ->
            Space.PropertySpace(
                position = 37,
                property =
                    StreetProperty(
                        name = "Park Place",
                        position = 37,
                        price = 350,
                        rent = createPropertyRent(35),
                        houseCost = 200,
                        hotelCost = 200,
                        colorGroup = ColorGroup.DARK_BLUE,
                    ),
            )
        else ->
            Space.PropertySpace(
                position = 39,
                property =
                    StreetProperty(
                        name = "Boardwalk",
                        position = 39,
                        price = 400,
                        rent = createPropertyRent(50),
                        houseCost = 200,
                        hotelCost = 200,
                        colorGroup = ColorGroup.DARK_BLUE,
                    ),
            )
    }

@Suppress("MagicNumber")
fun createStandardBoard(): Board {
    val spaceList = mutableListOf<Space>()
    spaceList.add(Space.Go(0))
    for (i in 1..39) {
        spaceList.add(getSpaceForPosition(i))
    }
    return Board(spaceList)
}
