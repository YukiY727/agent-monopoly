package com.monopoly.domain.model

data class Property(
    val name: String,
    val position: Int,
    val price: Int,
    val rent: Int,
    val colorGroup: ColorGroup,
)
