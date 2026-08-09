package com.propertytrader.android.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.propertytrader.core.model.GameState

@Composable
fun PlayerHudRow(gameState: GameState) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(gameState.players) { player ->
            val isActive = player.id == gameState.currentPlayer.id
            val propertyCount = gameState.ownership.values.count { it == player.id }
            Card(
                modifier = Modifier
                    .padding(4.dp)
                    .border(
                        width = if (isActive) 3.dp else 0.dp,
                        color = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                    ),
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    val nameSuffix = if (player.bankrupt) " (bankrut)" else ""
                    Text(player.name + nameSuffix, style = MaterialTheme.typography.titleSmall)
                    Text("Gotowka: ${player.cash}", style = MaterialTheme.typography.bodySmall)
                    Text("Nieruchomosci: $propertyCount", style = MaterialTheme.typography.bodySmall)
                    if (player.extraRollTokens > 0) {
                        Text("Dodatkowe rzuty: ${player.extraRollTokens}", style = MaterialTheme.typography.bodySmall)
                    }
                    if (player.getOutOfJailFreeCards > 0) {
                        Text(
                            "Karty \"Wyjscie z wiezienia\": ${player.getOutOfJailFreeCards}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}
