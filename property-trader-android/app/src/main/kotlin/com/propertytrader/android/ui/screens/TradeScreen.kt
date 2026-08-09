package com.propertytrader.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.propertytrader.core.model.GameState
import com.propertytrader.core.model.TradeOffer

@Composable
fun TradeScreen(gameState: GameState, onPropose: (TradeOffer) -> Unit, onCancel: () -> Unit) {
    val currentPlayer = gameState.currentPlayer
    val counterparts = gameState.players.filter { it.id != currentPlayer.id && !it.bankrupt }

    var counterpartId by remember { mutableStateOf(counterparts.firstOrNull()?.id) }
    val offeredProps = remember { mutableStateListOf<Int>() }
    val requestedProps = remember { mutableStateListOf<Int>() }
    var offeredCashText by remember { mutableStateOf("0") }
    var requestedCashText by remember { mutableStateOf("0") }

    LaunchedEffect(counterpartId) {
        requestedProps.clear()
    }

    val ownedByCurrent = gameState.ownership.filterValues { it == currentPlayer.id }.keys.sorted()
    val ownedByCounterpart = gameState.ownership.filterValues { it == counterpartId }.keys.sorted()

    val offeredCash = offeredCashText.toIntOrNull() ?: 0
    val requestedCash = requestedCashText.toIntOrNull() ?: 0
    val counterpart = counterparts.firstOrNull { it.id == counterpartId }

    val isValid = counterpart != null &&
        (offeredProps.isNotEmpty() || offeredCash > 0 || requestedProps.isNotEmpty() || requestedCash > 0) &&
        offeredCash in 0..currentPlayer.cash &&
        requestedCash in 0..counterpart.cash

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Handel — ${currentPlayer.name}", style = MaterialTheme.typography.headlineSmall)

        Text("Z kim?", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            counterparts.forEach { player ->
                FilterChip(
                    selected = counterpartId == player.id,
                    onClick = { counterpartId = player.id },
                    label = { Text(player.name) },
                )
            }
        }

        if (counterpart == null) {
            Text("Brak innych graczy do wymiany.", style = MaterialTheme.typography.bodyMedium)
        } else {
            Text("Oferujesz (${currentPlayer.name})", style = MaterialTheme.typography.titleSmall)
            Column {
                ownedByCurrent.forEach { spaceIndex ->
                    val checked = spaceIndex in offeredProps
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = {
                                if (it) offeredProps.add(spaceIndex) else offeredProps.remove(spaceIndex)
                            },
                        )
                        Text(gameState.board[spaceIndex].name)
                    }
                }
            }
            OutlinedTextField(
                value = offeredCashText,
                onValueChange = { offeredCashText = it.filter(Char::isDigit) },
                label = { Text("Gotowka do oddania (masz ${currentPlayer.cash})") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Chcesz w zamian (${counterpart.name})", style = MaterialTheme.typography.titleSmall)
            Column {
                ownedByCounterpart.forEach { spaceIndex ->
                    val checked = spaceIndex in requestedProps
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = {
                                if (it) requestedProps.add(spaceIndex) else requestedProps.remove(spaceIndex)
                            },
                        )
                        Text(gameState.board[spaceIndex].name)
                    }
                }
            }
            OutlinedTextField(
                value = requestedCashText,
                onValueChange = { requestedCashText = it.filter(Char::isDigit) },
                label = { Text("Gotowka do otrzymania (${counterpart.name} ma ${counterpart.cash})") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Anuluj")
            }
            Button(
                onClick = {
                    counterpart?.let { target ->
                        onPropose(
                            TradeOffer(
                                fromPlayerId = currentPlayer.id,
                                toPlayerId = target.id,
                                offeredPropertyIndices = offeredProps.toSet(),
                                offeredCash = offeredCash,
                                requestedPropertyIndices = requestedProps.toSet(),
                                requestedCash = requestedCash,
                            ),
                        )
                    }
                },
                enabled = isValid,
                modifier = Modifier.weight(1f),
            ) {
                Text("Zaproponuj")
            }
        }
    }
}
