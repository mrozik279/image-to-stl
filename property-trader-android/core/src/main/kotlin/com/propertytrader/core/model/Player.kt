package com.propertytrader.core.model

data class Player(
    val id: Int,
    val name: String,
    val tokenColor: Long,
    val cash: Int,
    val position: Int = 0,
    val inJail: Boolean = false,
    val jailTurns: Int = 0,
    val bankrupt: Boolean = false,
)
