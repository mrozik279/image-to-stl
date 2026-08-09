package com.propertytrader.core

import com.propertytrader.core.engine.Dice
import com.propertytrader.core.engine.GameEngine
import com.propertytrader.core.engine.GameEvent
import com.propertytrader.core.engine.JailAction
import com.propertytrader.core.model.TurnPhase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameEngineJailTest {

    @Test
    fun `landing on go-to-jail sends the player straight to jail`() {
        val engine = GameEngine(Dice(ScriptedRandom(0, 0))) // rolls (1, 1) = 2
        val state = newGame().let {
            it.copy(players = it.players.map { p -> if (p.id == 0) p.copy(position = 28) else p })
        }

        val (newState, events) = engine.rollAndMove(state)

        val player = newState.players.first { it.id == 0 }
        assertEquals(10, player.position)
        assertTrue(player.inJail)
        assertEquals(TurnPhase.TURN_READY_TO_END, newState.phase)
        assertTrue(events.any { it is GameEvent.SentToJail })
    }

    @Test
    fun `paying bail releases the player and lets them roll`() {
        val engine = GameEngine()
        val state = jailedState()

        val (newState, events) = engine.decideJail(state, JailAction.PAY_BAIL)

        val player = newState.players.first { it.id == 0 }
        assertFalse(player.inJail)
        assertEquals(1500 - 50, player.cash)
        assertEquals(TurnPhase.AWAITING_ROLL, newState.phase)
        assertTrue(events.any { it is GameEvent.LeftJail })
    }

    @Test
    fun `rolling doubles in jail releases the player and moves them`() {
        val engine = GameEngine(Dice(ScriptedRandom(0, 0))) // rolls (1, 1) doubles, sum 2
        val state = jailedState()

        val (newState, events) = engine.decideJail(state, JailAction.TRY_ROLL)

        val player = newState.players.first { it.id == 0 }
        assertFalse(player.inJail)
        assertEquals(12, player.position) // 10 (jail) + 2
        assertTrue(events.any { it is GameEvent.LeftJail })
    }

    @Test
    fun `failing to roll doubles keeps the player in jail and ends the turn`() {
        val engine = GameEngine(Dice(ScriptedRandom(0, 1))) // rolls (1, 2), not doubles
        val state = jailedState()

        val (newState, events) = engine.decideJail(state, JailAction.TRY_ROLL)

        val player = newState.players.first { it.id == 0 }
        assertTrue(player.inJail)
        assertEquals(1, player.jailTurns)
        assertEquals(TurnPhase.TURN_READY_TO_END, newState.phase)
        assertTrue(events.none { it is GameEvent.LeftJail })
    }

    @Test
    fun `the third failed attempt forces bail payment and moves the player`() {
        val engine = GameEngine(Dice(ScriptedRandom(0, 1))) // rolls (1, 2), not doubles
        val state = jailedState(jailTurns = 2)

        val (newState, events) = engine.decideJail(state, JailAction.TRY_ROLL)

        val player = newState.players.first { it.id == 0 }
        assertFalse(player.inJail)
        assertEquals(0, player.jailTurns)
        assertEquals(1500 - 50, player.cash)
        assertEquals(13, player.position) // 10 (jail) + 3
        assertTrue(events.any { it is GameEvent.LeftJail })
    }

    @Test
    fun `using a get-out-of-jail-free card releases the player without paying`() {
        val engine = GameEngine()
        val state = jailedState().let {
            it.copy(players = it.players.map { p -> if (p.id == 0) p.copy(getOutOfJailFreeCards = 1) else p })
        }

        val (newState, events) = engine.decideJail(state, JailAction.USE_CARD)

        val player = newState.players.first { it.id == 0 }
        assertFalse(player.inJail)
        assertEquals(1500, player.cash) // no cost
        assertEquals(0, player.getOutOfJailFreeCards)
        assertEquals(TurnPhase.AWAITING_ROLL, newState.phase)
        assertTrue(events.single() is GameEvent.LeftJailWithCard)
    }

    @Test
    fun `using a get-out-of-jail-free card with none available is a no-op`() {
        val engine = GameEngine()
        val state = jailedState()

        val (newState, events) = engine.decideJail(state, JailAction.USE_CARD)

        assertTrue(newState.players.first { it.id == 0 }.inJail)
        assertTrue(events.isEmpty())
    }

    private fun jailedState(jailTurns: Int = 0) = newGame().let {
        it.copy(
            players = it.players.map { p ->
                if (p.id == 0) p.copy(position = 10, inJail = true, jailTurns = jailTurns) else p
            },
            phase = TurnPhase.AWAITING_JAIL_DECISION,
        )
    }
}
