package com.propertytrader.android.ui

import androidx.lifecycle.ViewModel
import com.propertytrader.core.board.BoardFactory
import com.propertytrader.core.engine.GameEngine
import com.propertytrader.core.engine.GameEvent
import com.propertytrader.core.engine.JailAction
import com.propertytrader.core.model.GameState
import com.propertytrader.core.model.Player
import com.propertytrader.core.model.TurnPhase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class Screen { SETUP, BOARD, GAME_OVER }

data class GameUiState(
    val screen: Screen = Screen.SETUP,
    val gameState: GameState? = null,
    val eventLog: List<String> = emptyList(),
)

class GameViewModel : ViewModel() {
    private val engine = GameEngine()
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun startGame(playerNames: List<String>, startingCash: Int = DEFAULT_STARTING_CASH, roundLimit: Int? = null) {
        val players = playerNames.mapIndexed { index, name ->
            Player(id = index, name = name, tokenColor = TOKEN_COLORS[index % TOKEN_COLORS.size], cash = startingCash)
        }
        val state = GameState(
            board = BoardFactory.classicBoard(),
            players = players,
            currentPlayerIndex = 0,
            phase = TurnPhase.AWAITING_ROLL,
            roundLimit = roundLimit,
        )
        _uiState.value = GameUiState(
            screen = Screen.BOARD,
            gameState = state,
            eventLog = listOf("Gra rozpoczeta. Tura: ${state.currentPlayer.name}."),
        )
    }

    fun rollDice() = applyAction { engine.rollAndMove(it) }

    fun decidePurchase(buy: Boolean) = applyAction { engine.decidePurchase(it, buy) }

    fun decideJail(action: JailAction) = applyAction { engine.decideJail(it, action) }

    fun endTurn() {
        val current = _uiState.value.gameState ?: return
        applyNewState(engine.endTurn(current), emptyList())
    }

    fun restart() {
        _uiState.value = GameUiState()
    }

    private fun applyAction(action: (GameState) -> Pair<GameState, List<GameEvent>>) {
        val current = _uiState.value.gameState ?: return
        val (newState, events) = action(current)
        applyNewState(newState, events)
    }

    private fun applyNewState(newState: GameState, events: List<GameEvent>) {
        val messages = events.mapNotNull { describeEvent(it, newState) }
        _uiState.update { state ->
            val screen = if (newState.phase == TurnPhase.GAME_OVER) Screen.GAME_OVER else state.screen
            state.copy(
                screen = screen,
                gameState = newState,
                eventLog = (state.eventLog + messages).takeLast(50),
            )
        }
    }

    private fun describeEvent(event: GameEvent, state: GameState): String? {
        fun playerName(id: Int) = state.players.first { it.id == id }.name
        fun spaceName(index: Int) = state.board[index].name
        return when (event) {
            is GameEvent.DiceRolled -> "Rzut koscmi: ${event.a} + ${event.b} = ${event.a + event.b}"
            is GameEvent.PlayerMoved -> null
            is GameEvent.PassedGo -> "${playerName(event.playerId)} minal Start i otrzymuje ${event.bonus}."
            is GameEvent.PropertyPurchased ->
                "${playerName(event.playerId)} kupuje ${spaceName(event.spaceIndex)} za ${event.price}."
            is GameEvent.RentPaid ->
                "${playerName(event.payerId)} placi czynsz ${event.amount} graczowi " +
                    "${playerName(event.ownerId)} za ${spaceName(event.spaceIndex)}."
            is GameEvent.TaxPaid -> "${playerName(event.playerId)} placi podatek ${event.amount}."
            is GameEvent.SentToJail -> "${playerName(event.playerId)} trafia do wiezienia."
            is GameEvent.LeftJail -> if (event.paidBail) {
                "${playerName(event.playerId)} placi kaucje i wychodzi z wiezienia."
            } else {
                "${playerName(event.playerId)} wyrzucil dublet i wychodzi z wiezienia."
            }
            is GameEvent.PlayerBankrupt -> "${playerName(event.playerId)} oglasza bankructwo."
            is GameEvent.GameOver -> "Koniec gry! Zwyciezca: ${playerName(event.winnerId)}."
        }
    }

    companion object {
        const val DEFAULT_STARTING_CASH = 1500
        val TOKEN_COLORS = listOf(0xFFE53935L, 0xFF1E88E5L, 0xFF43A047L, 0xFFFDD835L)
    }
}
