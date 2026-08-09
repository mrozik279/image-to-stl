package com.propertytrader.core

import com.propertytrader.core.engine.Dice
import com.propertytrader.core.engine.GameEngine
import com.propertytrader.core.engine.GameEvent
import com.propertytrader.core.model.TurnPhase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameEnginePurchaseRentTest {

    @Test
    fun `buying a property deducts cash and records ownership`() {
        val engine = GameEngine()
        val state = newGame().let {
            it.copy(
                players = it.players.map { p -> if (p.id == 0) p.copy(position = 3) else p },
                phase = TurnPhase.AWAITING_PURCHASE_DECISION,
            )
        }

        val (newState, events) = engine.decidePurchase(state, buy = true)

        assertEquals(1500 - 60, newState.currentPlayer.cash)
        assertEquals(0, newState.ownership[3])
        assertEquals(TurnPhase.TURN_READY_TO_END, newState.phase)
        assertTrue(events.single() is GameEvent.PropertyPurchased)
    }

    @Test
    fun `declining a purchase leaves the space unowned`() {
        val engine = GameEngine()
        val state = newGame().let {
            it.copy(
                players = it.players.map { p -> if (p.id == 0) p.copy(position = 3) else p },
                phase = TurnPhase.AWAITING_PURCHASE_DECISION,
            )
        }

        val (newState, events) = engine.decidePurchase(state, buy = false)

        assertEquals(1500, newState.currentPlayer.cash)
        assertTrue(3 !in newState.ownership)
        assertEquals(TurnPhase.TURN_READY_TO_END, newState.phase)
        assertTrue(events.isEmpty())
    }

    @Test
    fun `landing on a rival property charges base rent`() {
        val engine = GameEngine(Dice(ScriptedRandom(0, 1))) // rolls (1, 2) = 3 -> index 3, baseRent 8
        val state = newGame().let { it.copy(ownership = it.ownership + (3 to 1)) }

        val (newState, events) = engine.rollAndMove(state)

        assertEquals(1500 - 8, newState.players[0].cash)
        assertEquals(1500 + 8, newState.players[1].cash)
        assertTrue(events.any { it is GameEvent.RentPaid })
    }

    @Test
    fun `owning the full color group doubles the rent`() {
        val engine = GameEngine(Dice(ScriptedRandom(0, 1))) // rolls (1, 2) = 3 -> index 3, baseRent 8
        val state = newGame().let { it.copy(ownership = it.ownership + (1 to 1) + (3 to 1)) }

        val (newState, _) = engine.rollAndMove(state)

        assertEquals(1500 - 16, newState.players[0].cash)
        assertEquals(1500 + 16, newState.players[1].cash)
    }

    @Test
    fun `utility rent is dice sum times multiplier`() {
        val engine = GameEngine(Dice(ScriptedRandom(5, 5))) // rolls (6, 6) = 12 -> index 12
        val state = newGame().let { it.copy(ownership = it.ownership + (12 to 1)) }

        val (newState, _) = engine.rollAndMove(state)

        assertEquals(1500 - 48, newState.players[0].cash) // 12 * 4 (single utility owned)
    }

    @Test
    fun `transit rent scales with number of stations owned`() {
        val engine = GameEngine(Dice(ScriptedRandom(1, 2))) // rolls (2, 3) = 5 -> index 5 (station)
        val state = newGame().let { it.copy(ownership = it.ownership + (5 to 1) + (15 to 1)) }

        val (newState, _) = engine.rollAndMove(state)

        assertEquals(1500 - 50, newState.players[0].cash) // 2 stations owned -> rentByOwnedCount[1]
    }

    @Test
    fun `unaffordable rent bankrupts the debtor and transfers their properties to the creditor`() {
        val engine = GameEngine(Dice(ScriptedRandom(0, 1))) // rolls (1, 2) = 3 -> index 3, baseRent 8
        val state = newGame(playerCount = 2).let {
            it.copy(
                players = it.players.map { p -> if (p.id == 0) p.copy(cash = 5) else p },
                ownership = it.ownership + (1 to 0) + (3 to 1),
            )
        }

        val (newState, events) = engine.rollAndMove(state)

        val debtor = newState.players.first { it.id == 0 }
        assertTrue(debtor.bankrupt)
        assertEquals(0, debtor.cash)
        assertEquals(1, newState.ownership[1]) // transferred to the creditor
        assertTrue(events.any { it is GameEvent.PlayerBankrupt })
        assertTrue(events.any { it is GameEvent.GameOver })
        assertEquals(TurnPhase.GAME_OVER, newState.phase)
        assertEquals(1, newState.winnerId)
    }

    @Test
    fun `unaffordable tax bankrupts the player and returns properties to the bank`() {
        val engine = GameEngine(Dice(ScriptedRandom(1, 1))) // rolls (2, 2) = 4 -> index 4 (Tax, amount 200)
        val state = newGame(playerCount = 2).let {
            it.copy(
                players = it.players.map { p -> if (p.id == 0) p.copy(cash = 5) else p },
                ownership = it.ownership + (1 to 0),
            )
        }

        val (newState, events) = engine.rollAndMove(state)

        val player = newState.players.first { it.id == 0 }
        assertTrue(player.bankrupt)
        assertTrue(1 !in newState.ownership) // returned to the bank, not transferred
        assertTrue(events.any { it is GameEvent.PlayerBankrupt })
        assertEquals(TurnPhase.GAME_OVER, newState.phase)
    }
}
