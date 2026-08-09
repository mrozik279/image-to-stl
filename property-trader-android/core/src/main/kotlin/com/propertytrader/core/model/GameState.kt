package com.propertytrader.core.model

import com.propertytrader.core.board.Space

enum class TurnPhase {
    AWAITING_ROLL,
    AWAITING_PURCHASE_DECISION,
    AWAITING_JAIL_DECISION,
    TURN_READY_TO_END,
    GAME_OVER,
}

data class GameState(
    val board: List<Space>,
    val players: List<Player>,
    val currentPlayerIndex: Int,
    val ownership: Map<Int, Int> = emptyMap(),
    val phase: TurnPhase = TurnPhase.AWAITING_ROLL,
    val lastDice: Pair<Int, Int>? = null,
    val roundNumber: Int = 1,
    val roundLimit: Int? = null,
    val winnerId: Int? = null,
) {
    val currentPlayer: Player get() = players[currentPlayerIndex]
}
