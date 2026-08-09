package com.propertytrader.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp

@Composable
fun SetupScreen(onStart: (List<String>, Int?) -> Unit) {
    var playerCount by remember { mutableIntStateOf(2) }
    val names = remember { mutableStateListOf("Gracz 1", "Gracz 2", "Gracz 3", "Gracz 4") }
    var roundLimitEnabled by remember { mutableStateOf(false) }
    var roundLimitText by remember { mutableStateOf("20") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Property Trader", style = MaterialTheme.typography.headlineMedium)
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

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 0 until playerCount) {
                OutlinedTextField(
                    value = names[i],
                    onValueChange = { names[i] = it },
                    label = { Text("Imie gracza ${i + 1}") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
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
                onStart(validNames, roundLimit)
            },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Rozpocznij gre")
        }
    }
}
