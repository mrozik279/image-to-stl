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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.propertytrader.core.board.PropertySpace
import com.propertytrader.core.engine.GameEngine
import com.propertytrader.core.model.GameState

@Composable
fun BuildScreen(gameState: GameState, onBuild: (Int) -> Unit, onCancel: () -> Unit) {
    val player = gameState.currentPlayer
    val allProperties = gameState.board.filterIsInstance<PropertySpace>()
    val fullyOwnedGroups = allProperties.groupBy { it.group }
        .filterValues { spaces -> spaces.all { gameState.ownership[it.index] == player.id } }
    val buildableProperties = fullyOwnedGroups.values.flatten().sortedBy { it.index }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Buduj - ${player.name} (gotowka: ${player.cash})", style = MaterialTheme.typography.headlineSmall)

        if (buildableProperties.isEmpty()) {
            Text(
                "Nie masz jeszcze pelnej grupy kolorow - zbuduj dopiero, gdy posiadasz wszystkie " +
                    "nieruchomosci danego koloru.",
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            buildableProperties.forEach { space ->
                val level = gameState.houses[space.index] ?: 0
                val isHotel = level >= GameEngine.MAX_HOUSE_LEVEL
                val canBuild = !isHotel && player.cash >= space.houseCost
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(space.name, style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (isHotel) "Hotel" else "Domy: $level/4",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Button(onClick = { onBuild(space.index) }, enabled = canBuild) {
                        Text(if (isHotel) "Max" else "Buduj (${space.houseCost})")
                    }
                }
            }
        }

        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
            Text("Zamknij")
        }
    }
}
