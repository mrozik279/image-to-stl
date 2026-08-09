package com.propertytrader.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val TOKEN_PALETTE = listOf(
    0xFFE53935L, // czerwony
    0xFF1E88E5L, // niebieski
    0xFF43A047L, // zielony
    0xFFFDD835L, // zolty
    0xFF8E24AAL, // fioletowy
    0xFFFB8C00L, // pomaranczowy
)

@Composable
fun SetupScreen(onStart: (List<String>, List<Long>, Int?) -> Unit, onOpenRules: () -> Unit) {
    var playerCount by remember { mutableIntStateOf(2) }
    val names = remember { mutableStateListOf("Gracz 1", "Gracz 2", "Gracz 3", "Gracz 4") }
    val colorAssignment = remember { mutableStateListOf(0, 1, 2, 3) }
    var roundLimitEnabled by remember { mutableStateOf(false) }
    var roundLimitText by remember { mutableStateOf("20") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Property Trader", style = MaterialTheme.typography.headlineMedium)
        OutlinedButton(onClick = onOpenRules) {
            Text("Regulamin")
        }

        Text("Liczba graczy: $playerCount", style = MaterialTheme.typography.titleMedium)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (2..4).forEach { count ->
                FilterChip(
                    selected = playerCount == count,
                    onClick = { playerCount = count },
                    label = { Text(count.toString()) },
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for (i in 0 until playerCount) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = names[i],
                        onValueChange = { names[i] = it },
                        label = { Text("Imie gracza ${i + 1}") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TOKEN_PALETTE.forEachIndexed { paletteIndex, colorLong ->
                            val isSelected = colorAssignment[i] == paletteIndex
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorLong))
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        shape = CircleShape,
                                    )
                                    .clickable {
                                        val previousPaletteIndex = colorAssignment[i]
                                        val otherPlayer = colorAssignment.indexOfFirst { it == paletteIndex }
                                        if (otherPlayer != -1 && otherPlayer != i) {
                                            colorAssignment[otherPlayer] = previousPaletteIndex
                                        }
                                        colorAssignment[i] = paletteIndex
                                    },
                            )
                        }
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Checkbox(checked = roundLimitEnabled, onCheckedChange = { roundLimitEnabled = it })
            Text("Limit rund")
            if (roundLimitEnabled) {
                OutlinedTextField(
                    value = roundLimitText,
                    onValueChange = { roundLimitText = it.filter(Char::isDigit) },
                    modifier = Modifier.width(80.dp),
                    singleLine = true,
                )
            }
        }

        val validNames = (0 until playerCount).map { names[it].trim() }
        val isValid = validNames.all { it.isNotEmpty() } && validNames.toSet().size == playerCount

        Button(
            onClick = {
                val roundLimit = if (roundLimitEnabled) roundLimitText.toIntOrNull() else null
                val colors = (0 until playerCount).map { TOKEN_PALETTE[colorAssignment[it]] }
                onStart(validNames, colors, roundLimit)
            },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Rozpocznij gre")
        }
    }
}
