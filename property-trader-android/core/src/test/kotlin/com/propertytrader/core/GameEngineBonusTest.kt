package com.propertytrader.core

import com.propertytrader.core.engine.Dice
import com.propertytrader.core.engine.GameEngine
import com.propertytrader.core.engine.GameEvent
import com.propertytrader.core.model.TurnPhase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameEngineBonusTest {

    @Test
    fun `rolling doubles grants another roll instead of ending the turn`() {
        // (3, 4) = 7, non-double, lands on the index 7 Event space (empty deck by default).
        val engine = GameEngine(Dice(ScriptedRandom(2, 2))) // rolls (3, 3) = 6 -> index 6, unowned, but cheap enough

        val state = newGame().let {
            it.copy(players = it.players.map { p -> if (p.id == 0) p.copy(cash = 0) else p })
        }

        val (newState, events) = engine.rollAndMove(state)

        // Player 0 has no cash, so the unowned property at index 6 can't trigger a purchase
        // decision - landing resolves straight to TURN_READY_TO_END, which the doubles bonus
        // then overrides back to AWAITING_ROLL.
        assertEquals(TurnPhase.AWAITING_ROLL, newState.phase)
        assertEquals(1, newState.players[0].consecutiveDoubles)
        assertTrue(events.any { it is GameEvent.RolledAgain })
    }

    @Test
    fun `a third consecutive double sends the player straight to jail without moving`() {
        val engine = GameEngine(Dice(ScriptedRandom(2, 2))) // rolls (3, 3), a double
        val state = newGame().let {
            it.copy(players = it.players.map { p -> if (p.id == 0) p.copy(position = 5, consecutiveDoubles = 2) else p })
        }

        val (newState, events) = engine.rollAndMove(state)

        val player = newState.players[0]
        assertEquals(10, player.position) // teleported straight to jail; the roll's move never applies
        assertTrue(player.inJail)
        assertEquals(0, player.consecutiveDoubles)
        assertEquals(TurnPhase.TURN_READY_TO_END, newState.phase)
        assertTrue(events.any { it is GameEvent.SentToJail })
        assertTrue(events.none { it is GameEvent.PlayerMoved })
    }

    @Test
    fun `ending a turn resets the current player's consecutive doubles counter`() {
        val engine = GameEngine()
        val state = newGame().let {
            it.copy(
                players = it.players.map { p -> if (p.id == 0) p.copy(consecutiveDoubles = 2) else p },
                phase = TurnPhase.TURN_READY_TO_END,
            )
        }

        val newState = engine.endTurn(state)

        assertEquals(0, newState.players[0].consecutiveDoubles)
    }

    @Test
    fun `landing on free parking with an empty pot does nothing`() {
        val engine = GameEngine(Dice(ScriptedRandom(0, 0))) // rolls (1, 1) = 2
        val state = newGame().let {
            it.copy(players = it.players.map { p -> if (p.id == 0) p.copy(position = 18) else p })
        }

        val (newState, events) = engine.rollAndMove(state)

        assertEquals(1500, newState.players[0].cash)
        assertTrue(events.none { it is GameEvent.FreeParkingJackpot })
    }

    @Test
    fun `landing on free parking collects and resets the accumulated pot`() {
        val engine = GameEngine(Dice(ScriptedRandom(0, 0))) // rolls (1, 1) = 2
        val state = newGame().let {
            it.copy(
                players = it.players.map { p -> if (p.id == 0) p.copy(position = 18) else p },
                freeParkingPot = 300,
            )
        }

        val (newState, events) = engine.rollAndMove(state)

        assertEquals(1500 + 300, newState.players[0].cash)
        assertEquals(0, newState.freeParkingPot)
        assertTrue(events.any { it is GameEvent.FreeParkingJackpot })
    }

    @Test
    fun `tax payments accumulate into the free parking pot`() {
        val engine = GameEngine(Dice(ScriptedRandom(0, 2))) // rolls (1, 3) = 4 -> index 4, tax 200
        val state = newGame()

        val (newState, _) = engine.rollAndMove(state)

        assertEquals(1500 - 200, newState.players[0].cash)
        assertEquals(200, newState.freeParkingPot)
    }

    @Test
    fun `using an extra roll token lets the player roll again`() {
        val engine = GameEngine()
        val state = newGame().let {
            it.copy(
                players = it.players.map { p -> if (p.id == 0) p.copy(extraRollTokens = 1) else p },
                phase = TurnPhase.TURN_READY_TO_END,
            )
        }

        val (newState, events) = engine.useExtraRollToken(state)

        assertEquals(TurnPhase.AWAITING_ROLL, newState.phase)
        assertEquals(0, newState.players[0].extraRollTokens)
        assertTrue(events.single() is GameEvent.ExtraRollUsed)
    }

    @Test
    fun `using an extra roll token with none available is a no-op`() {
        val engine = GameEngine()
        val state = newGame().copy(phase = TurnPhase.TURN_READY_TO_END)

        val (newState, events) = engine.useExtraRollToken(state)

        assertEquals(state, newState)
        assertTrue(events.isEmpty())
    }

    @Test
    fun `using an extra roll token outside TURN_READY_TO_END is a no-op`() {
        val engine = GameEngine()
        val state = newGame().let {
            it.copy(players = it.players.map { p -> if (p.id == 0) p.copy(extraRollTokens = 1) else p })
        }

        val (newState, events) = engine.useExtraRollToken(state)

        assertEquals(state, newState)
        assertTrue(events.isEmpty())
    }
}
