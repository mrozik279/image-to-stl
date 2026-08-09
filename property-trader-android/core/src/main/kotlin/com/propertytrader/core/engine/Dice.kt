package com.propertytrader.core.engine

import kotlin.random.Random

class Dice(private val random: Random = Random.Default) {
    fun roll(): Pair<Int, Int> = (1 + random.nextInt(6)) to (1 + random.nextInt(6))
}
