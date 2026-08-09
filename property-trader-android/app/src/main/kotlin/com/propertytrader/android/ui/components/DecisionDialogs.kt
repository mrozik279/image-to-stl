package com.propertytrader.android.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
) {
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
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Jestes w wiezieniu") },
                text = { Text("Zaplac kaucje (${GameEngine.BAIL_AMOUNT}) albo sprobuj wyrzucic dublet.") },
                confirmButton = {
                    TextButton(onClick = { onJailDecision(JailAction.PAY_BAIL) }) { Text("Zaplac kaucje") }
                },
                dismissButton = {
                    TextButton(onClick = { onJailDecision(JailAction.TRY_ROLL) }) { Text("Rzuc koscmi") }
                },
            )
        }

        else -> Unit
    }
}
