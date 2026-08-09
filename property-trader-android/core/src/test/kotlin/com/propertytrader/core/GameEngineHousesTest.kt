package com.propertytrader.core

import com.propertytrader.core.engine.Dice
import com.propertytrader.core.engine.GameEngine
import com.propertytrader.core.engine.GameEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameEngineHousesTest {

    @Test
    fun `building a house on a fully-owned color group deducts cost and increments level`() {
        val engine = GameEngine()
        val state = newGame().let { it.copy(ownership = it.ownership + (1 to 0) + (3 to 0)) } // full group A

        val (newState, events) = engine.buildHouse(state, 1)

        assertEquals(1, newState.houses[1])
        assertEquals(1500 - 50, newState.players[0].cash) // group A house cost
        assertTrue(events.single() is GameEvent.HouseBuilt)
    }

    @Test
    fun `building without owning the full color group is a no-op`() {
        val engine = GameEngine()
        val state = newGame().let { it.copy(ownership = it.ownership + (1 to 0)) } // missing index 3

        val (newState, events) = engine.buildHouse(state, 1)

        assertNull(newState.houses[1])
        assertTrue(events.isEmpty())
    }

    @Test
    fun `building past hotel level is a no-op`() {
        val engine = GameEngine()
        val state = newGame().let {
            it.copy(ownership = it.ownership + (1 to 0) + (3 to 0), houses = mapOf(1 to 5))
        }

        val (newState, events) = engine.buildHouse(state, 1)

        assertEquals(5, newState.houses[1])
        assertTrue(events.isEmpty())
    }

    @Test
    fun `building without enough cash is a no-op`() {
        val engine = GameEngine()
        val state = newGame().let {
            it.copy(
                players = it.players.map { p -> if (p.id == 0) p.copy(cash = 10) else p },
                ownership = it.ownership + (1 to 0) + (3 to 0),
            )
        }

        val (newState, events) = engine.buildHouse(state, 1)

        assertNull(newState.houses[1])
        assertTrue(events.isEmpty())
    }

    @Test
    fun `rent scales with the number of houses built`() {
        // Player 0 starts at index 39 so a roll of 2 wraps to index 1 (Ulica Wierzbowa, baseRent 4).
        val engine = GameEngine(Dice(ScriptedRandom(0, 0))) // rolls (1, 1) = 2
        val state = newGame().let {
            it.copy(
                players = it.players.map { p -> if (p.id == 0) p.copy(position = 39) else p },
                ownership = it.ownership + (1 to 1) + (3 to 1),
                houses = mapOf(1 to 2), // 2 houses -> rentWithHouses[1]
            )
        }

        val (newState, _) = engine.rollAndMove(state)

        // rentLadder(4) = [20, 60, 120, 200, 280]; 2 houses -> 60. Plus a pass-Go bonus (39 -> 1).
        assertEquals(1500 + 200 - 60, newState.players[0].cash)
        assertEquals(1500 + 60, newState.players[1].cash)
    }
}
