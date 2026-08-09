package com.propertytrader.core.model

data class TradeOffer(
    val fromPlayerId: Int,
    val toPlayerId: Int,
    val offeredPropertyIndices: Set<Int> = emptySet(),
    val offeredCash: Int = 0,
    val requestedPropertyIndices: Set<Int> = emptySet(),
    val requestedCash: Int = 0,
)
