package com.propertytrader.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.propertytrader.android.ui.components.BoardCanvas
import com.propertytrader.android.ui.components.DecisionDialogs
import com.propertytrader.android.ui.components.PlayerHudRow
import com.propertytrader.core.engine.JailAction
import com.propertytrader.core.model.GameState
import com.propertytrader.core.model.TurnPhase

@Composable
fun BoardScreen(
    gameState: GameState,
    eventLog: List<String>,
    onRollDice: () -> Unit,
    onPurchaseDecision: (Boolean) -> Unit,
    onJailDecision: (JailAction) -> Unit,
    onEndTurn: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
    ) {
        PlayerHudRow(gameState = gameState)

        Spacer(modifier = Modifier.height(8.dp))

        BoardCanvas(
            gameState = gameState,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Tura: ${gameState.currentPlayer.name} (runda ${gameState.roundNumber})",
            style = MaterialTheme.typography.titleMedium,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            Button(onClick = onRollDice, enabled = gameState.phase == TurnPhase.AWAITING_ROLL) {
                Text("Rzuc koscmi")
            }
            Button(onClick = onEndTurn, enabled = gameState.phase == TurnPhase.TURN_READY_TO_END) {
                Text("Zakoncz ture")
            }
        }

        gameState.lastDice?.let { (a, b) -> Text("Ostatni rzut: $a + $b = ${a + b}") }

        Text(
            "Log zdarzen",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 8.dp),
        )
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(eventLog.asReversed()) { message ->
                Text(
                    message,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }
        }
    }

    DecisionDialogs(
        gameState = gameState,
        onPurchaseDecision = onPurchaseDecision,
        onJailDecision = onJailDecision,
    )
}
