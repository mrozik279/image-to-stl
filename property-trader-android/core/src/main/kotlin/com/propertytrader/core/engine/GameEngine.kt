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
import com.propertytrader.core.cards.EventCardEffect
import com.propertytrader.core.model.GameState
import com.propertytrader.core.model.Player
import com.propertytrader.core.model.TradeOffer
import com.propertytrader.core.model.TurnPhase

class GameEngine(private val dice: Dice = Dice()) {

    fun rollAndMove(state: GameState): Pair<GameState, List<GameEvent>> {
        require(state.phase == TurnPhase.AWAITING_ROLL) { "Cannot roll dice outside of AWAITING_ROLL phase" }
        val player = state.currentPlayer
        val (a, b) = dice.roll()
        val events = mutableListOf<GameEvent>(GameEvent.DiceRolled(a, b))
        val isDouble = a == b
        val newConsecutiveDoubles = if (isDouble) player.consecutiveDoubles + 1 else 0
        val stateWithDice = updatePlayer(state.copy(lastDice = a to b), player.id) {
            it.copy(consecutiveDoubles = newConsecutiveDoubles)
        }

        if (isDouble && newConsecutiveDoubles >= MAX_CONSECUTIVE_DOUBLES) {
            val resetState = updatePlayer(stateWithDice, player.id) { it.copy(consecutiveDoubles = 0) }
            val (jailedState, jailEvents) = sendToJail(resetState, player.id)
            return jailedState to (events + jailEvents)
        }

        val (movedState, moveEvents) = moveAndResolveLanding(stateWithDice, player.id, a + b)
        events += moveEvents

        val movedPlayer = movedState.players.first { it.id == player.id }
        val grantsReroll = isDouble && movedState.phase == TurnPhase.TURN_READY_TO_END && !movedPlayer.inJail
        return if (grantsReroll) {
            events += GameEvent.RolledAgain(player.id)
            movedState.copy(phase = TurnPhase.AWAITING_ROLL) to events
        } else {
            movedState to events
        }
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
            JailAction.USE_CARD -> useCardAndStay(state, player)
        }
    }

    fun buildHouse(state: GameState, spaceIndex: Int): Pair<GameState, List<GameEvent>> {
        val player = state.currentPlayer
        val space = state.board.getOrNull(spaceIndex) as? PropertySpace ?: return state to emptyList()
        if (state.ownership[spaceIndex] != player.id) return state to emptyList()
        val groupSpaces = state.board.filterIsInstance<PropertySpace>().filter { it.group == space.group }
        val ownsFullGroup = groupSpaces.all { state.ownership[it.index] == player.id }
        if (!ownsFullGroup) return state to emptyList()
        val currentLevel = state.houses[spaceIndex] ?: 0
        if (currentLevel >= MAX_HOUSE_LEVEL) return state to emptyList()
        if (player.cash < space.houseCost) return state to emptyList()

        val newState = updatePlayer(state, player.id) { it.copy(cash = it.cash - space.houseCost) }
            .copy(houses = state.houses + (spaceIndex to currentLevel + 1))
        return newState to listOf(GameEvent.HouseBuilt(player.id, spaceIndex, currentLevel + 1))
    }

    fun useExtraRollToken(state: GameState): Pair<GameState, List<GameEvent>> {
        if (state.phase != TurnPhase.TURN_READY_TO_END) return state to emptyList()
        val player = state.currentPlayer
        if (player.extraRollTokens <= 0) return state to emptyList()
        val newState = updatePlayer(state, player.id) { it.copy(extraRollTokens = it.extraRollTokens - 1) }
            .copy(phase = TurnPhase.AWAITING_ROLL)
        return newState to listOf(GameEvent.ExtraRollUsed(player.id))
    }

    fun endTurn(state: GameState): GameState {
        if (state.phase == TurnPhase.GAME_OVER) return state
        val resetState = updatePlayer(state, state.currentPlayer.id) { it.copy(consecutiveDoubles = 0) }

        val activePlayers = resetState.players.filterNot { it.bankrupt }
        if (activePlayers.size <= 1) {
            val winner = activePlayers.firstOrNull() ?: resetState.players.first()
            return resetState.copy(phase = TurnPhase.GAME_OVER, winnerId = winner.id)
        }

        var nextIndex = resetState.currentPlayerIndex
        var roundNumber = resetState.roundNumber
        var attempts = 0
        do {
            val prev = nextIndex
            nextIndex = (nextIndex + 1) % resetState.players.size
            if (nextIndex <= prev) roundNumber++
            attempts++
        } while (resetState.players[nextIndex].bankrupt && attempts <= resetState.players.size)

        val roundLimit = resetState.roundLimit
        if (roundLimit != null && roundNumber > roundLimit) {
            val winner = activePlayers.maxWithOrNull(compareBy<Player> { netWorth(resetState, it) }.thenBy { -it.id })!!
            return resetState.copy(phase = TurnPhase.GAME_OVER, winnerId = winner.id, roundNumber = roundNumber)
        }

        val nextPlayer = resetState.players[nextIndex]
        val nextPhase = if (nextPlayer.inJail) TurnPhase.AWAITING_JAIL_DECISION else TurnPhase.AWAITING_ROLL
        return resetState.copy(
            currentPlayerIndex = nextIndex,
            roundNumber = roundNumber,
            phase = nextPhase,
            lastDice = null,
        )
    }

    fun proposeTrade(state: GameState, offer: TradeOffer): Pair<GameState, List<GameEvent>> {
        if (!isTradeValid(state, offer)) return state to emptyList()
        return state.copy(pendingTrade = offer) to listOf(GameEvent.TradeProposed(offer.fromPlayerId, offer.toPlayerId))
    }

    fun respondToTrade(state: GameState, accept: Boolean): Pair<GameState, List<GameEvent>> {
        val offer = state.pendingTrade ?: return state to emptyList()
        if (!accept || !isTradeValid(state, offer)) {
            return state.copy(pendingTrade = null) to listOf(GameEvent.TradeDeclined(offer.fromPlayerId, offer.toPlayerId))
        }

        val newOwnership = state.ownership.toMutableMap()
        offer.offeredPropertyIndices.forEach { newOwnership[it] = offer.toPlayerId }
        offer.requestedPropertyIndices.forEach { newOwnership[it] = offer.fromPlayerId }

        val newState = state.copy(ownership = newOwnership, pendingTrade = null)
            .let { updatePlayer(it, offer.fromPlayerId) { p -> p.copy(cash = p.cash - offer.offeredCash + offer.requestedCash) } }
            .let { updatePlayer(it, offer.toPlayerId) { p -> p.copy(cash = p.cash - offer.requestedCash + offer.offeredCash) } }
        return newState to listOf(GameEvent.TradeAccepted(offer.fromPlayerId, offer.toPlayerId))
    }

    private fun isTradeValid(state: GameState, offer: TradeOffer): Boolean {
        if (offer.fromPlayerId == offer.toPlayerId) return false
        val from = state.players.firstOrNull { it.id == offer.fromPlayerId } ?: return false
        val to = state.players.firstOrNull { it.id == offer.toPlayerId } ?: return false
        if (from.bankrupt || to.bankrupt) return false
        if (offer.offeredCash < 0 || offer.requestedCash < 0) return false
        if (from.cash < offer.offeredCash || to.cash < offer.requestedCash) return false
        if (offer.offeredPropertyIndices.any { state.ownership[it] != from.id }) return false
        if (offer.requestedPropertyIndices.any { state.ownership[it] != to.id }) return false
        val isEmpty = offer.offeredPropertyIndices.isEmpty() && offer.offeredCash == 0 &&
            offer.requestedPropertyIndices.isEmpty() && offer.requestedCash == 0
        return !isEmpty
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

    private fun useCardAndStay(state: GameState, player: Player): Pair<GameState, List<GameEvent>> {
        if (player.getOutOfJailFreeCards <= 0) return state to emptyList()
        val releasedState = updatePlayer(state, player.id) {
            it.copy(inJail = false, jailTurns = 0, getOutOfJailFreeCards = it.getOutOfJailFreeCards - 1)
        }.copy(phase = TurnPhase.AWAITING_ROLL)
        return releasedState to listOf(GameEvent.LeftJailWithCard(player.id))
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
            is GoSpace, is JailSpace ->
                state.copy(phase = TurnPhase.TURN_READY_TO_END) to emptyList()

            is FreeParkingSpace -> collectFreeParkingPot(state, playerId)

            is EventSpace -> drawEventCard(state, playerId)

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

    private fun collectFreeParkingPot(state: GameState, playerId: Int): Pair<GameState, List<GameEvent>> {
        val pot = state.freeParkingPot
        if (pot <= 0) {
            return state.copy(phase = TurnPhase.TURN_READY_TO_END) to emptyList()
        }
        val newState = updatePlayer(state, playerId) { it.copy(cash = it.cash + pot) }
            .copy(freeParkingPot = 0, phase = TurnPhase.TURN_READY_TO_END)
        return newState to listOf(GameEvent.FreeParkingJackpot(playerId, pot))
    }

    private fun drawEventCard(state: GameState, playerId: Int): Pair<GameState, List<GameEvent>> {
        if (state.eventDeck.isEmpty()) return state.copy(phase = TurnPhase.TURN_READY_TO_END) to emptyList()
        val card = state.eventDeck[state.eventDeckPosition % state.eventDeck.size]
        val stateWithCard = state.copy(eventDeckPosition = state.eventDeckPosition + 1, lastDrawnCard = card)
        val events = mutableListOf<GameEvent>(GameEvent.EventCardDrawn(playerId, card.description))

        return when (val effect = card.effect) {
            is EventCardEffect.CollectMoney -> {
                val newState = updatePlayer(stateWithCard, playerId) { it.copy(cash = it.cash + effect.amount) }
                    .copy(phase = TurnPhase.TURN_READY_TO_END)
                newState to events
            }

            is EventCardEffect.PayMoney -> {
                val (chargedState, chargeEvents) = chargePlayer(stateWithCard, playerId, effect.amount, creditorId = null)
                events += chargeEvents
                if (chargedState.players.first { it.id == playerId }.bankrupt) {
                    checkGameOver(chargedState, events)
                } else {
                    chargedState.copy(phase = TurnPhase.TURN_READY_TO_END) to events
                }
            }

            is EventCardEffect.MoveTo -> {
                val player = stateWithCard.players.first { it.id == playerId }
                val boardSize = stateWithCard.board.size
                val spacesToMove = ((effect.spaceIndex - player.position) + boardSize) % boardSize
                val (movedState, moveEvents) = moveAndResolveLanding(stateWithCard, playerId, spacesToMove)
                movedState to (events + moveEvents)
            }

            EventCardEffect.GoToJail -> {
                val (jailedState, jailEvents) = sendToJail(stateWithCard, playerId)
                jailedState to (events + jailEvents)
            }

            EventCardEffect.GetOutOfJailFree -> {
                val newState = updatePlayer(stateWithCard, playerId) {
                    it.copy(getOutOfJailFreeCards = it.getOutOfJailFreeCards + 1)
                }.copy(phase = TurnPhase.TURN_READY_TO_END)
                newState to events
            }

            EventCardEffect.ExtraRoll -> {
                val newState = updatePlayer(stateWithCard, playerId) { it.copy(extraRollTokens = it.extraRollTokens + 1) }
                    .copy(phase = TurnPhase.TURN_READY_TO_END)
                newState to (events + GameEvent.ExtraRollGranted(playerId))
            }
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
                val houseLevel = state.houses[spaceIndex] ?: 0
                if (houseLevel > 0) {
                    space.rentWithHouses[(houseLevel - 1).coerceIn(0, space.rentWithHouses.lastIndex)]
                } else {
                    val groupSpaces = state.board.filterIsInstance<PropertySpace>().filter { it.group == space.group }
                    val ownsFullGroup = groupSpaces.all { state.ownership[it.index] == ownerId }
                    if (ownsFullGroup) space.baseRent * space.fullGroupRentMultiplier else space.baseRent
                }
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
            newState = if (creditorId != null) {
                updatePlayer(newState, creditorId) { it.copy(cash = it.cash + amount) }
            } else {
                newState.copy(freeParkingPot = newState.freeParkingPot + amount)
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
        val debtorSpaces = state.ownership.filterValues { it == debtorId }.keys
        val newHouses = state.houses.filterKeys { it !in debtorSpaces }
        val newState = updatePlayer(state, debtorId) { it.copy(bankrupt = true, cash = 0) }
            .copy(ownership = newOwnership, houses = newHouses)
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
        val houseValue = state.houses.entries.sumOf { (spaceIndex, level) ->
            if (state.ownership[spaceIndex] == player.id) {
                val space = state.board[spaceIndex] as? PropertySpace
                (space?.houseCost ?: 0) * level
            } else {
                0
            }
        }
        return player.cash + propertyValue + houseValue
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
        const val MAX_CONSECUTIVE_DOUBLES = 3
        const val MAX_HOUSE_LEVEL = 5
    }
}
