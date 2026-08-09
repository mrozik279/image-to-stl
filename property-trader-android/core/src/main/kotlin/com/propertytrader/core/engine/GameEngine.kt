package com.propertytrader.core.engine

import com.propertytrader.core.board.GoSpace
import com.propertytrader.core.board.GoToJailSpace
import com.propertytrader.core.board.EventSpace
import com.propertytrader.core.board.FreeParkingSpace
import com.propertytrader.core.board.JailSpace
import com.propertytrader.core.board.PropertySpace
import com.propertytrader.core.board.Space
import com.propertytrader.core.board.TaxSpace
import com.propertytrader.core.board.TransitSpace
import com.propertytrader.core.board.UtilitySpace
import com.propertytrader.core.model.GameState
import com.propertytrader.core.model.Player
import com.propertytrader.core.model.TurnPhase

class GameEngine(private val dice: Dice = Dice()) {

    fun rollAndMove(state: GameState): Pair<GameState, List<GameEvent>> {
        require(state.phase == TurnPhase.AWAITING_ROLL) { "Cannot roll dice outside of AWAITING_ROLL phase" }
        val player = state.currentPlayer
        val (a, b) = dice.roll()
        val events = mutableListOf<GameEvent>(GameEvent.DiceRolled(a, b))
        val stateWithDice = state.copy(lastDice = a to b)
        val (movedState, moveEvents) = moveAndResolveLanding(stateWithDice, player.id, a + b)
        events += moveEvents
        return movedState to events
    }

    fun decidePurchase(state: GameState, buy: Boolean): Pair<GameState, List<GameEvent>> {
        require(state.phase == TurnPhase.AWAITING_PURCHASE_DECISION) { "Not awaiting purchase decision" }
        val player = state.currentPlayer
        val space = state.board[player.position]
        val price = spacePrice(space)
        if (!buy || player.cash < price) {
            return state.copy(phase = TurnPhase.TURN_READY_TO_END) to emptyList()
        }
        val newState = updatePlayer(state, player.id) { it.copy(cash = it.cash - price) }
            .let { it.copy(ownership = it.ownership + (player.position to player.id), phase = TurnPhase.TURN_READY_TO_END) }
        return newState to listOf(GameEvent.PropertyPurchased(player.id, player.position, price))
    }

    fun decideJail(state: GameState, action: JailAction): Pair<GameState, List<GameEvent>> {
        require(state.phase == TurnPhase.AWAITING_JAIL_DECISION) { "Not awaiting jail decision" }
        val player = state.currentPlayer
        return when (action) {
            JailAction.PAY_BAIL -> payBailAndStay(state, player)
            JailAction.TRY_ROLL -> tryRollForJail(state, player)
        }
    }

    fun endTurn(state: GameState): GameState {
        if (state.phase == TurnPhase.GAME_OVER) return state
        val activePlayers = state.players.filterNot { it.bankrupt }
        if (activePlayers.size <= 1) {
            val winner = activePlayers.firstOrNull() ?: state.players.first()
            return state.copy(phase = TurnPhase.GAME_OVER, winnerId = winner.id)
        }

        var nextIndex = state.currentPlayerIndex
        var roundNumber = state.roundNumber
        var attempts = 0
        do {
            val prev = nextIndex
            nextIndex = (nextIndex + 1) % state.players.size
            if (nextIndex <= prev) roundNumber++
            attempts++
        } while (state.players[nextIndex].bankrupt && attempts <= state.players.size)

        val roundLimit = state.roundLimit
        if (roundLimit != null && roundNumber > roundLimit) {
            val winner = activePlayers.maxWithOrNull(compareBy<Player> { netWorth(state, it) }.thenBy { -it.id })!!
            return state.copy(phase = TurnPhase.GAME_OVER, winnerId = winner.id, roundNumber = roundNumber)
        }

        val nextPlayer = state.players[nextIndex]
        val nextPhase = if (nextPlayer.inJail) TurnPhase.AWAITING_JAIL_DECISION else TurnPhase.AWAITING_ROLL
        return state.copy(
            currentPlayerIndex = nextIndex,
            roundNumber = roundNumber,
            phase = nextPhase,
            lastDice = null,
        )
    }

    private fun payBailAndStay(state: GameState, player: Player): Pair<GameState, List<GameEvent>> {
        val (chargedState, chargeEvents) = chargePlayer(state, player.id, BAIL_AMOUNT, creditorId = null)
        if (chargedState.players.first { it.id == player.id }.bankrupt) {
            return checkGameOver(chargedState, chargeEvents)
        }
        val releasedState = updatePlayer(chargedState, player.id) { it.copy(inJail = false, jailTurns = 0) }
            .copy(phase = TurnPhase.AWAITING_ROLL)
        return releasedState to (chargeEvents + GameEvent.LeftJail(player.id, paidBail = true))
    }

    private fun tryRollForJail(state: GameState, player: Player): Pair<GameState, List<GameEvent>> {
        val (a, b) = dice.roll()
        val events = mutableListOf<GameEvent>(GameEvent.DiceRolled(a, b))
        val stateWithDice = state.copy(lastDice = a to b)

        if (a == b) {
            val freedState = updatePlayer(stateWithDice, player.id) { it.copy(inJail = false, jailTurns = 0) }
            events += GameEvent.LeftJail(player.id, paidBail = false)
            val (movedState, moveEvents) = moveAndResolveLanding(freedState, player.id, a + b)
            return movedState to (events + moveEvents)
        }

        val newJailTurns = player.jailTurns + 1
        val stateWithMoreJailTurns = updatePlayer(stateWithDice, player.id) { it.copy(jailTurns = newJailTurns) }

        if (newJailTurns < MAX_JAIL_TURNS) {
            return stateWithMoreJailTurns.copy(phase = TurnPhase.TURN_READY_TO_END) to events
        }

        val (chargedState, chargeEvents) = chargePlayer(stateWithMoreJailTurns, player.id, BAIL_AMOUNT, creditorId = null)
        events += chargeEvents
        if (chargedState.players.first { it.id == player.id }.bankrupt) {
            return checkGameOver(chargedState, events)
        }
        val freedState = updatePlayer(chargedState, player.id) { it.copy(inJail = false, jailTurns = 0) }
        events += GameEvent.LeftJail(player.id, paidBail = true)
        val (movedState, moveEvents) = moveAndResolveLanding(freedState, player.id, a + b)
        return movedState to (events + moveEvents)
    }

    private fun moveAndResolveLanding(state: GameState, playerId: Int, spaces: Int): Pair<GameState, List<GameEvent>> {
        val events = mutableListOf<GameEvent>()
        val player = state.players.first { it.id == playerId }
        val from = player.position
        val boardSize = state.board.size
        val to = (from + spaces) % boardSize
        val passedGo = to < from

        var working = updatePlayer(state, playerId) { it.copy(position = to) }
        events += GameEvent.PlayerMoved(playerId, from, to)
        if (passedGo) {
            working = updatePlayer(working, playerId) { it.copy(cash = it.cash + PASS_GO_BONUS) }
            events += GameEvent.PassedGo(playerId, PASS_GO_BONUS)
        }

        val (resolvedState, resolveEvents) = resolveLanding(working, playerId)
        events += resolveEvents
        return resolvedState to events
    }

    private fun resolveLanding(state: GameState, playerId: Int): Pair<GameState, List<GameEvent>> {
        val player = state.players.first { it.id == playerId }
        return when (val space = state.board[player.position]) {
            is GoSpace, is JailSpace, is FreeParkingSpace, is EventSpace ->
                state.copy(phase = TurnPhase.TURN_READY_TO_END) to emptyList()

            is GoToJailSpace -> sendToJail(state, playerId)

            is TaxSpace -> {
                val (chargedState, chargeEvents) = chargePlayer(state, playerId, space.amount, creditorId = null)
                if (chargedState.players.first { it.id == playerId }.bankrupt) {
                    checkGameOver(chargedState, chargeEvents)
                } else {
                    chargedState.copy(phase = TurnPhase.TURN_READY_TO_END) to
                        (chargeEvents + GameEvent.TaxPaid(playerId, space.amount))
                }
            }

            is PropertySpace -> resolveOwnableLanding(state, playerId, space.index, space.price)
            is TransitSpace -> resolveOwnableLanding(state, playerId, space.index, space.price)
            is UtilitySpace -> resolveOwnableLanding(state, playerId, space.index, space.price)
        }
    }

    private fun resolveOwnableLanding(
        state: GameState,
        playerId: Int,
        spaceIndex: Int,
        price: Int,
    ): Pair<GameState, List<GameEvent>> {
        val ownerId = state.ownership[spaceIndex]
        return when {
            ownerId == null -> {
                val player = state.players.first { it.id == playerId }
                val phase = if (player.cash >= price) TurnPhase.AWAITING_PURCHASE_DECISION else TurnPhase.TURN_READY_TO_END
                state.copy(phase = phase) to emptyList()
            }
            ownerId == playerId -> state.copy(phase = TurnPhase.TURN_READY_TO_END) to emptyList()
            else -> {
                val rent = calculateRent(state, spaceIndex, ownerId)
                val (chargedState, chargeEvents) = chargePlayer(state, playerId, rent, creditorId = ownerId)
                val bankrupted = chargedState.players.first { it.id == playerId }.bankrupt
                if (bankrupted) {
                    checkGameOver(chargedState, chargeEvents)
                } else {
                    chargedState.copy(phase = TurnPhase.TURN_READY_TO_END) to
                        (chargeEvents + GameEvent.RentPaid(playerId, ownerId, rent, spaceIndex))
                }
            }
        }
    }

    private fun calculateRent(state: GameState, spaceIndex: Int, ownerId: Int): Int =
        when (val space = state.board[spaceIndex]) {
            is PropertySpace -> {
                val groupSpaces = state.board.filterIsInstance<PropertySpace>().filter { it.group == space.group }
                val ownsFullGroup = groupSpaces.all { state.ownership[it.index] == ownerId }
                if (ownsFullGroup) space.baseRent * space.fullGroupRentMultiplier else space.baseRent
            }
            is TransitSpace -> {
                val ownedCount = state.board.filterIsInstance<TransitSpace>().count { state.ownership[it.index] == ownerId }
                space.rentByOwnedCount[(ownedCount - 1).coerceIn(0, space.rentByOwnedCount.lastIndex)]
            }
            is UtilitySpace -> {
                val ownedCount = state.board.filterIsInstance<UtilitySpace>().count { state.ownership[it.index] == ownerId }
                val diceSum = (state.lastDice?.first ?: 0) + (state.lastDice?.second ?: 0)
                val multiplier = if (ownedCount >= 2) space.diceMultiplierBothOwned else space.diceMultiplierOneOwned
                diceSum * multiplier
            }
            else -> 0
        }

    private fun sendToJail(state: GameState, playerId: Int): Pair<GameState, List<GameEvent>> {
        val jailIndex = state.board.indexOfFirst { it is JailSpace }
        val newState = updatePlayer(state, playerId) { it.copy(position = jailIndex, inJail = true, jailTurns = 0) }
            .copy(phase = TurnPhase.TURN_READY_TO_END)
        return newState to listOf(GameEvent.SentToJail(playerId))
    }

    private fun chargePlayer(
        state: GameState,
        playerId: Int,
        amount: Int,
        creditorId: Int?,
    ): Pair<GameState, List<GameEvent>> {
        val player = state.players.first { it.id == playerId }
        return if (player.cash < amount) {
            handleBankruptcy(state, playerId, creditorId)
        } else {
            var newState = updatePlayer(state, playerId) { it.copy(cash = it.cash - amount) }
            if (creditorId != null) {
                newState = updatePlayer(newState, creditorId) { it.copy(cash = it.cash + amount) }
            }
            newState to emptyList()
        }
    }

    private fun handleBankruptcy(state: GameState, debtorId: Int, creditorId: Int?): Pair<GameState, List<GameEvent>> {
        val newOwnership = state.ownership.mapNotNull { (spaceIndex, ownerId) ->
            when {
                ownerId != debtorId -> spaceIndex to ownerId
                creditorId != null -> spaceIndex to creditorId
                else -> null
            }
        }.toMap()
        val newState = updatePlayer(state, debtorId) { it.copy(bankrupt = true, cash = 0) }
            .copy(ownership = newOwnership)
        return newState to listOf(GameEvent.PlayerBankrupt(debtorId, creditorId))
    }

    private fun checkGameOver(state: GameState, events: List<GameEvent>): Pair<GameState, List<GameEvent>> {
        val activePlayers = state.players.filterNot { it.bankrupt }
        return if (activePlayers.size <= 1) {
            val winner = activePlayers.firstOrNull() ?: state.players.maxBy { it.cash }
            val finalState = state.copy(phase = TurnPhase.GAME_OVER, winnerId = winner.id)
            finalState to (events + GameEvent.GameOver(winner.id, "bankructwo przeciwnikow"))
        } else {
            state.copy(phase = TurnPhase.TURN_READY_TO_END) to events
        }
    }

    private fun netWorth(state: GameState, player: Player): Int {
        val propertyValue = state.ownership.filterValues { it == player.id }.keys.sumOf { spacePrice(state.board[it]) }
        return player.cash + propertyValue
    }

    private fun spacePrice(space: Space): Int = when (space) {
        is PropertySpace -> space.price
        is TransitSpace -> space.price
        is UtilitySpace -> space.price
        else -> 0
    }

    private fun updatePlayer(state: GameState, playerId: Int, transform: (Player) -> Player): GameState =
        state.copy(players = state.players.map { if (it.id == playerId) transform(it) else it })

    companion object {
        const val PASS_GO_BONUS = 200
        const val BAIL_AMOUNT = 50
        const val MAX_JAIL_TURNS = 3
    }
}
