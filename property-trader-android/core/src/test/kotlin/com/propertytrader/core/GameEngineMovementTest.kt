package com.propertytrader.core

import com.propertytrader.core.engine.GameEngine
import com.propertytrader.core.engine.Dice
import com.propertytrader.core.engine.GameEvent
import com.propertytrader.core.model.TurnPhase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameEngineMovementTest {

    @Test
    fun `rolling moves the player and lands on an unowned purchasable property`() {
        val engine = GameEngine(Dice(ScriptedRandom(0, 1))) // rolls (1, 2) = 3
        val state = newGame()

        val (newState, events) = engine.rollAndMove(state)

        assertEquals(3, newState.currentPlayer.position)
        assertEquals(TurnPhase.AWAITING_PURCHASE_DECISION, newState.phase)
        assertTrue(events.any { it is GameEvent.DiceRolled })
        assertTrue(events.any { it is GameEvent.PlayerMoved })
    }

    @Test
    fun `passing go awards the bonus`() {
        val engine = GameEngine(Dice(ScriptedRandom(0, 1))) // rolls (1, 2) = 3
        val base = newGame()
        val movedToEnd = base.copy(players = base.players.map { if (it.id == 0) it.copy(position = 38) else it })

        val (newState, events) = engine.rollAndMove(movedToEnd)

        assertEquals(1, newState.currentPlayer.position)
        assertEquals(1500 + 200, newState.currentPlayer.cash)
        assertTrue(events.any { it is GameEvent.PassedGo })
    }

    @Test
    fun `landing on an event space ends the turn with no purchase decision`() {
        // (3, 4) = 7, non-double so the doubles-reroll bonus doesn't apply here, lands on index 7 (Event).
        val engine = GameEngine(Dice(ScriptedRandom(2, 3)))
        val state = newGame()

        val (newState, _) = engine.rollAndMove(state)

        assertEquals(TurnPhase.TURN_READY_TO_END, newState.phase)
    }

    @Test
    fun `landing on own property ends the turn without a rent or purchase event`() {
        val engine = GameEngine(Dice(ScriptedRandom(0, 1))) // rolls (1, 2) = 3 -> index 3
        val state = newGame().let { it.copy(ownership = it.ownership + (3 to 0)) }

        val (newState, events) = engine.rollAndMove(state)

        assertEquals(TurnPhase.TURN_READY_TO_END, newState.phase)
        assertTrue(events.none { it is GameEvent.PropertyPurchased || it is GameEvent.RentPaid })
    }

    @Test
    fun `endTurn advances to the next player and resets the phase`() {
        val engine = GameEngine()
        val state = newGame()

        val nextState = engine.endTurn(state.copy(phase = TurnPhase.TURN_READY_TO_END))

        assertEquals(1, nextState.currentPlayerIndex)
        assertEquals(TurnPhase.AWAITING_ROLL, nextState.phase)
    }

    @Test
    fun `endTurn wraps around and increments the round number`() {
        val engine = GameEngine()
        val state = newGame().copy(currentPlayerIndex = 1, phase = TurnPhase.TURN_READY_TO_END)

        val nextState = engine.endTurn(state)

        assertEquals(0, nextState.currentPlayerIndex)
        assertEquals(2, nextState.roundNumber)
    }
}
