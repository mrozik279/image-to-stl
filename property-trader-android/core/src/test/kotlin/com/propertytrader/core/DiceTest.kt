package com.propertytrader.core

import com.propertytrader.core.engine.Dice
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DiceTest {

    @Test
    fun `roll returns values in 1 to 6 range`() {
        val dice = Dice()
        repeat(200) {
            val (a, b) = dice.roll()
            assertTrue(a in 1..6, "a=$a out of range")
            assertTrue(b in 1..6, "b=$b out of range")
        }
    }

    @Test
    fun `roll uses the injected random source`() {
        val dice = Dice(ScriptedRandom(2, 5))
        assertEquals(3 to 6, dice.roll())
    }
}
