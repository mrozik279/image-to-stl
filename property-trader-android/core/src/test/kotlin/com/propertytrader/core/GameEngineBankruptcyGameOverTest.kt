package com.propertytrader.core

import com.propertytrader.core.engine.GameEngine
import com.propertytrader.core.engine.GameEvent
import com.propertytrader.core.engine.JailAction
import com.propertytrader.core.model.TurnPhase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameEngineBankruptcyGameOverTest {

    @Test
    fun `endTurn ends the game when only one active player remains`() {
        val engine = GameEngine()
        val state = newGame(playerCount = 2).let {
            it.copy(
                players = it.players.map { p -> if (p.id == 1) p.copy(bankrupt = true) else p },
                phase = TurnPhase.TURN_READY_TO_END,
            )
        }

        val newState = engine.endTurn(state)

        assertEquals(TurnPhase.GAME_OVER, newState.phase)
        assertEquals(0, newState.winnerId)
    }

    @Test
    fun `endTurn skips a bankrupt player in the rotation`() {
        val engine = GameEngine()
        val state = newGame(playerCount = 3).let {
            it.copy(
                players = it.players.map { p -> if (p.id == 1) p.copy(bankrupt = true) else p },
                currentPlayerIndex = 0,
                phase = TurnPhase.TURN_READY_TO_END,
            )
        }

        val newState = engine.endTurn(state)

        assertEquals(2, newState.currentPlayerIndex)
        assertEquals(TurnPhase.AWAITING_ROLL, newState.phase)
    }

    @Test
    fun `reaching the round limit ends the game with the highest net worth as winner`() {
        val engine = GameEngine()
        val state = newGame(playerCount = 2, roundLimit = 1).let {
            it.copy(
                players = it.players.map { p ->
                    when (p.id) {
                        0 -> p.copy(cash = 1000)
                        else -> p.copy(cash = 2000)
                    }
                },
                ownership = it.ownership + (1 to 0), // player 0 owns a 60-price property: net worth 1060 < 2000
                currentPlayerIndex = 1,
                phase = TurnPhase.TURN_READY_TO_END,
            )
        }

        val newState = engine.endTurn(state)

        assertEquals(TurnPhase.GAME_OVER, newState.phase)
        assertEquals(1, newState.winnerId)
    }

    @Test
    fun `a net worth tie at the round limit is broken by the lowest player id`() {
        val engine = GameEngine()
        val state = newGame(playerCount = 2, roundLimit = 1).let {
            it.copy(currentPlayerIndex = 1, phase = TurnPhase.TURN_READY_TO_END)
        }

        val newState = engine.endTurn(state)

        assertEquals(TurnPhase.GAME_OVER, newState.phase)
        assertEquals(0, newState.winnerId)
    }

    @Test
    fun `failing to afford bail bankrupts the player and can end the game`() {
        val engine = GameEngine()
        val state = newGame(playerCount = 2).let {
            it.copy(
                players = it.players.map { p ->
                    if (p.id == 0) p.copy(position = 10, inJail = true, cash = 10) else p
                },
                phase = TurnPhase.AWAITING_JAIL_DECISION,
            )
        }

        val (newState, events) = engine.decideJail(state, JailAction.PAY_BAIL)

        val debtor = newState.players.first { it.id == 0 }
        assertTrue(debtor.bankrupt)
        assertEquals(0, debtor.cash)
        assertEquals(TurnPhase.GAME_OVER, newState.phase)
        assertEquals(1, newState.winnerId)
        assertTrue(events.any { it is GameEvent.PlayerBankrupt })
        assertTrue(events.any { it is GameEvent.GameOver })
    }
}
