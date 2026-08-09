package com.propertytrader.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.propertytrader.core.model.GameState

@Composable
fun GameOverScreen(gameState: GameState, onRestart: () -> Unit) {
    val winner = gameState.players.firstOrNull { it.id == gameState.winnerId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Koniec gry", style = MaterialTheme.typography.headlineMedium)
        if (winner != null) {
            Text("Zwyciezca: ${winner.name}", style = MaterialTheme.typography.titleLarge)
        }

        Text("Wyniki koncowe", style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(gameState.players.sortedByDescending { it.cash }) { player ->
                val propertyCount = gameState.ownership.values.count { it == player.id }
                val suffix = if (player.bankrupt) " (bankrut)" else ""
                Text(
                    "${player.name}: ${player.cash} | nieruchomosci: $propertyCount$suffix",
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }

        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
            Text("Nowa gra")
        }
    }
}
