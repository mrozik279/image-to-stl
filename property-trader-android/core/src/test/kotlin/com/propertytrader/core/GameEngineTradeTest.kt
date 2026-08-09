package com.propertytrader.core

import com.propertytrader.core.engine.GameEngine
import com.propertytrader.core.engine.GameEvent
import com.propertytrader.core.model.TradeOffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameEngineTradeTest {

    private val engine = GameEngine()

    @Test
    fun `proposing a valid trade sets the pending offer`() {
        val state = newGame().let { it.copy(ownership = it.ownership + (1 to 0) + (3 to 1)) }
        val offer = TradeOffer(fromPlayerId = 0, toPlayerId = 1, offeredPropertyIndices = setOf(1), offeredCash = 100)

        val (newState, events) = engine.proposeTrade(state, offer)

        assertEquals(offer, newState.pendingTrade)
        assertTrue(events.single() is GameEvent.TradeProposed)
    }

    @Test
    fun `proposing a trade with a property you do not own is a no-op`() {
        val state = newGame()
        val offer = TradeOffer(fromPlayerId = 0, toPlayerId = 1, offeredPropertyIndices = setOf(1))

        val (newState, events) = engine.proposeTrade(state, offer)

        assertNull(newState.pendingTrade)
        assertTrue(events.isEmpty())
    }

    @Test
    fun `proposing a trade with more cash than you have is a no-op`() {
        val state = newGame()
        val offer = TradeOffer(fromPlayerId = 0, toPlayerId = 1, offeredCash = 2000)

        val (newState, _) = engine.proposeTrade(state, offer)

        assertNull(newState.pendingTrade)
    }

    @Test
    fun `proposing a trade with yourself is a no-op`() {
        val state = newGame()
        val offer = TradeOffer(fromPlayerId = 0, toPlayerId = 0, offeredCash = 10)

        val (newState, _) = engine.proposeTrade(state, offer)

        assertNull(newState.pendingTrade)
    }

    @Test
    fun `accepting a valid trade swaps properties and cash both ways`() {
        val state = newGame().let { it.copy(ownership = it.ownership + (1 to 0) + (3 to 1)) }
        val offer = TradeOffer(
            fromPlayerId = 0,
            toPlayerId = 1,
            offeredPropertyIndices = setOf(1),
            offeredCash = 100,
            requestedPropertyIndices = setOf(3),
            requestedCash = 50,
        )
        val proposed = engine.proposeTrade(state, offer).first

        val (newState, events) = engine.respondToTrade(proposed, accept = true)

        assertEquals(0, newState.ownership[3]) // fromPlayer receives the requested property
        assertEquals(1, newState.ownership[1]) // toPlayer receives the offered property
        assertEquals(1500 - 100 + 50, newState.players[0].cash)
        assertEquals(1500 - 50 + 100, newState.players[1].cash)
        assertNull(newState.pendingTrade)
        assertTrue(events.single() is GameEvent.TradeAccepted)
    }

    @Test
    fun `declining a trade clears it without moving anything`() {
        val state = newGame().let { it.copy(ownership = it.ownership + (1 to 0)) }
        val offer = TradeOffer(fromPlayerId = 0, toPlayerId = 1, offeredPropertyIndices = setOf(1), offeredCash = 100)
        val proposed = engine.proposeTrade(state, offer).first

        val (newState, events) = engine.respondToTrade(proposed, accept = false)

        assertNull(newState.pendingTrade)
        assertEquals(0, newState.ownership[1])
        assertEquals(1500, newState.players[0].cash)
        assertEquals(1500, newState.players[1].cash)
        assertTrue(events.single() is GameEvent.TradeDeclined)
    }

    @Test
    fun `accepting a stale offer that is no longer valid auto-declines`() {
        val state = newGame().let {
            it.copy(
                ownership = it.ownership + (1 to 1), // property changed hands since the offer was made
                pendingTrade = TradeOffer(fromPlayerId = 0, toPlayerId = 1, offeredPropertyIndices = setOf(1)),
            )
        }

        val (newState, events) = engine.respondToTrade(state, accept = true)

        assertNull(newState.pendingTrade)
        assertEquals(1, newState.ownership[1]) // unchanged
        assertTrue(events.single() is GameEvent.TradeDeclined)
    }

    @Test
    fun `responding with no pending trade is a no-op`() {
        val state = newGame()

        val (newState, events) = engine.respondToTrade(state, accept = true)

        assertEquals(state, newState)
        assertTrue(events.isEmpty())
    }
}
