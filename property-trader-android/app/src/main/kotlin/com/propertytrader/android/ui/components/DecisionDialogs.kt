package com.propertytrader.android.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.propertytrader.core.board.PropertySpace
import com.propertytrader.core.board.TransitSpace
import com.propertytrader.core.board.UtilitySpace
import com.propertytrader.core.engine.GameEngine
import com.propertytrader.core.engine.JailAction
import com.propertytrader.core.model.GameState
import com.propertytrader.core.model.TurnPhase

@Composable
fun DecisionDialogs(
    gameState: GameState,
    onPurchaseDecision: (Boolean) -> Unit,
    onJailDecision: (JailAction) -> Unit,
    onTradeResponse: (Boolean) -> Unit,
) {
    val pendingTrade = gameState.pendingTrade
    if (pendingTrade != null) {
        val fromName = gameState.players.first { it.id == pendingTrade.fromPlayerId }.name
        val toName = gameState.players.first { it.id == pendingTrade.toPlayerId }.name
        val offeredNames = pendingTrade.offeredPropertyIndices.map { gameState.board[it].name }
        val requestedNames = pendingTrade.requestedPropertyIndices.map { gameState.board[it].name }
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Oferta handlowa dla $toName") },
            text = {
                Column {
                    Text("$fromName oferuje:")
                    if (offeredNames.isNotEmpty()) Text("- " + offeredNames.joinToString(", "))
                    if (pendingTrade.offeredCash > 0) Text("- gotowka: ${pendingTrade.offeredCash}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("W zamian za:")
                    if (requestedNames.isNotEmpty()) Text("- " + requestedNames.joinToString(", "))
                    if (pendingTrade.requestedCash > 0) Text("- gotowka: ${pendingTrade.requestedCash}")
                }
            },
            confirmButton = {
                TextButton(onClick = { onTradeResponse(true) }) { Text("Akceptuj") }
            },
            dismissButton = {
                TextButton(onClick = { onTradeResponse(false) }) { Text("Odrzuc") }
            },
        )
        return
    }

    when (gameState.phase) {
        TurnPhase.AWAITING_PURCHASE_DECISION -> {
            val space = gameState.board[gameState.currentPlayer.position]
            val price = when (space) {
                is PropertySpace -> space.price
                is TransitSpace -> space.price
                is UtilitySpace -> space.price
                else -> 0
            }
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Kupic ${space.name}?") },
                text = { Text("Cena: $price") },
                confirmButton = {
                    TextButton(onClick = { onPurchaseDecision(true) }) { Text("Kup") }
                },
                dismissButton = {
                    TextButton(onClick = { onPurchaseDecision(false) }) { Text("Pomin") }
                },
            )
        }

        TurnPhase.AWAITING_JAIL_DECISION -> {
            val hasCard = gameState.currentPlayer.getOutOfJailFreeCards > 0
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Jestes w wiezieniu") },
                text = {
                    Text(
                        "Zaplac kaucje (${GameEngine.BAIL_AMOUNT})" +
                            (if (hasCard) ", uzyj karty \"Wyjscie z wiezienia\"" else "") +
                            " albo sprobuj wyrzucic dublet.",
                    )
                },
                confirmButton = {
                    Row {
                        if (hasCard) {
                            TextButton(onClick = { onJailDecision(JailAction.USE_CARD) }) { Text("Uzyj karty") }
                        }
                        TextButton(onClick = { onJailDecision(JailAction.PAY_BAIL) }) { Text("Zaplac kaucje") }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onJailDecision(JailAction.TRY_ROLL) }) { Text("Rzuc koscmi") }
                },
            )
        }

        else -> Unit
    }
}
