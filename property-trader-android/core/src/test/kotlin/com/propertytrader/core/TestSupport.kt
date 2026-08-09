package com.propertytrader.core

import com.propertytrader.core.board.BoardFactory
import com.propertytrader.core.model.GameState
import com.propertytrader.core.model.Player
import com.propertytrader.core.model.TurnPhase
import kotlin.random.Random

/** Random source that plays back a scripted queue of nextInt(bound) results, one per call. */
class ScriptedRandom(private val values: MutableList<Int>) : Random() {
    constructor(vararg values: Int) : this(values.toMutableList())

    override fun nextBits(bitCount: Int): Int =
        throw UnsupportedOperationException("ScriptedRandom only supports nextInt(bound)")

    override fun nextInt(bound: Int): Int {
        check(values.isNotEmpty()) { "ScriptedRandom ran out of scripted values" }
        return values.removeAt(0)
    }
}

fun newGame(playerCount: Int = 2, startingCash: Int = 1500, roundLimit: Int? = null): GameState {
    val players = (0 until playerCount).map { id ->
        Player(id = id, name = "Gracz ${id + 1}", tokenColor = 0xFF000000L, cash = startingCash)
    }
    return GameState(
        board = BoardFactory.classicBoard(),
        players = players,
        currentPlayerIndex = 0,
        phase = TurnPhase.AWAITING_ROLL,
        roundLimit = roundLimit,
    )
}
