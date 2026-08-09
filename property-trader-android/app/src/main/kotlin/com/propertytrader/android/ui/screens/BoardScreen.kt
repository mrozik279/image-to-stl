package com.propertytrader.android.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.propertytrader.android.ui.components.BoardCanvas
import com.propertytrader.android.ui.components.DecisionDialogs
import com.propertytrader.android.ui.components.DiceCup
import com.propertytrader.android.ui.components.EventBanner
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
    onOpenTrade: () -> Unit,
    onTradeResponse: (Boolean) -> Unit,
    onOpenBuild: () -> Unit,
    onUseExtraRoll: () -> Unit,
    onOpenRules: () -> Unit,
) {
    val noBlockingDecision = gameState.pendingTrade == null
    val canActNow = noBlockingDecision &&
        (gameState.phase == TurnPhase.AWAITING_ROLL || gameState.phase == TurnPhase.TURN_READY_TO_END)
    val canOpenTrade = canActNow && gameState.players.count { !it.bankrupt } >= 2
    val canUseExtraRoll = noBlockingDecision &&
        gameState.phase == TurnPhase.TURN_READY_TO_END &&
        gameState.currentPlayer.extraRollTokens > 0

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Property Trader", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onOpenRules) { Text("Regulamin") }
            }

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
            Text(
                "Pula Darmowego Parkingu: ${gameState.freeParkingPot}",
                style = MaterialTheme.typography.bodySmall,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                DiceCup(dice = gameState.lastDice)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .horizontalScroll(rememberScrollState()),
            ) {
                Button(
                    onClick = onRollDice,
                    enabled = gameState.phase == TurnPhase.AWAITING_ROLL && gameState.pendingTrade == null,
                ) {
                    Text("Rzuc koscmi")
                }
                Button(
                    onClick = onEndTurn,
                    enabled = gameState.phase == TurnPhase.TURN_READY_TO_END && gameState.pendingTrade == null,
                ) {
                    Text("Zakoncz ture")
                }
                Button(onClick = onOpenTrade, enabled = canOpenTrade) {
                    Text("Handel")
                }
                Button(onClick = onOpenBuild, enabled = canActNow) {
                    Text("Buduj")
                }
                Button(onClick = onUseExtraRoll, enabled = canUseExtraRoll) {
                    Text("Dodatkowy rzut (${gameState.currentPlayer.extraRollTokens})")
                }
            }

            Text(
                "Log zdarzen",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 4.dp),
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

        EventBanner(
            latestMessage = eventLog.lastOrNull(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp),
        )
    }

    DecisionDialogs(
        gameState = gameState,
        onPurchaseDecision = onPurchaseDecision,
        onJailDecision = onJailDecision,
        onTradeResponse = onTradeResponse,
    )
}
