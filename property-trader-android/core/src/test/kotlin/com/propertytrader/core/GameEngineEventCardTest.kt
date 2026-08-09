package com.propertytrader.core

import com.propertytrader.core.cards.EventCard
import com.propertytrader.core.cards.EventCardEffect
import com.propertytrader.core.engine.Dice
import com.propertytrader.core.engine.GameEngine
import com.propertytrader.core.engine.GameEvent
import com.propertytrader.core.model.TurnPhase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameEngineEventCardTest {

    // Rolling (3, 4) = 7, non-double so it doesn't also trigger a bonus reroll, lands
    // player 0 (starting at index 0) on index 7, an Event space.
    private fun dice() = Dice(ScriptedRandom(2, 3))

    @Test
    fun `drawing a collect-money card pays the player and ends the turn`() {
        val engine = GameEngine(dice())
        val state = newGame().let { it.copy(eventDeck = listOf(EventCard("t", EventCardEffect.CollectMoney(50)))) }

        val (newState, events) = engine.rollAndMove(state)

        assertEquals(1550, newState.players[0].cash)
        assertEquals(TurnPhase.TURN_READY_TO_END, newState.phase)
        assertTrue(events.any { it is GameEvent.EventCardDrawn })
    }

    @Test
    fun `drawing a pay-money card charges the player and feeds the free parking pot`() {
        val engine = GameEngine(dice())
        val state = newGame().let { it.copy(eventDeck = listOf(EventCard("t", EventCardEffect.PayMoney(100)))) }

        val (newState, _) = engine.rollAndMove(state)

        assertEquals(1400, newState.players[0].cash)
        assertEquals(100, newState.freeParkingPot)
    }

    @Test
    fun `drawing a pay-money card bankrupts a player who cannot afford it`() {
        val engine = GameEngine(dice())
        val state = newGame().let {
            it.copy(
                players = it.players.map { p -> if (p.id == 0) p.copy(cash = 10) else p },
                eventDeck = listOf(EventCard("t", EventCardEffect.PayMoney(100))),
            )
        }

        val (newState, events) = engine.rollAndMove(state)

        assertTrue(newState.players[0].bankrupt)
        assertTrue(events.any { it is GameEvent.PlayerBankrupt })
    }

    @Test
    fun `drawing a move-to card teleports and resolves the new landing (with pass-go bonus)`() {
        val engine = GameEngine(dice())
        val state = newGame().let { it.copy(eventDeck = listOf(EventCard("t", EventCardEffect.MoveTo(0)))) }

        val (newState, events) = engine.rollAndMove(state)

        assertEquals(0, newState.players[0].position)
        assertEquals(1500 + 200, newState.players[0].cash) // pass-Go bonus from the card's own move
        assertEquals(TurnPhase.TURN_READY_TO_END, newState.phase)
        assertTrue(events.any { it is GameEvent.PassedGo })
    }

    @Test
    fun `drawing a go-to-jail card sends the player to jail`() {
        val engine = GameEngine(dice())
        val state = newGame().let { it.copy(eventDeck = listOf(EventCard("t", EventCardEffect.GoToJail))) }

        val (newState, events) = engine.rollAndMove(state)

        assertTrue(newState.players[0].inJail)
        assertTrue(events.any { it is GameEvent.SentToJail })
    }

    @Test
    fun `drawing a get-out-of-jail-free card grants a card to the player`() {
        val engine = GameEngine(dice())
        val state = newGame().let { it.copy(eventDeck = listOf(EventCard("t", EventCardEffect.GetOutOfJailFree))) }

        val (newState, _) = engine.rollAndMove(state)

        assertEquals(1, newState.players[0].getOutOfJailFreeCards)
    }

    @Test
    fun `drawing an extra-roll card grants a token`() {
        val engine = GameEngine(dice())
        val state = newGame().let { it.copy(eventDeck = listOf(EventCard("t", EventCardEffect.ExtraRoll))) }

        val (newState, events) = engine.rollAndMove(state)

        assertEquals(1, newState.players[0].extraRollTokens)
        assertTrue(events.any { it is GameEvent.ExtraRollGranted })
    }
}
