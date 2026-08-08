package com.mrozik279.pawclicker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mrozik279.pawclicker.ClickerViewModel
import com.mrozik279.pawclicker.R

@Composable
fun SettingsScreen(viewModel: ClickerViewModel, modifier: Modifier = Modifier) {
    val state = viewModel.state
    var showResetDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card {
                Column(Modifier.padding(16.dp)) {
                    SettingRow(
                        label = stringResource(R.string.settings_sound),
                        checked = state.soundEnabled,
                        onCheckedChange = viewModel::toggleSound
                    )
                    SettingRow(
                        label = stringResource(R.string.settings_vibration),
                        checked = state.vibrationEnabled,
                        onCheckedChange = viewModel::toggleVibration
                    )
                }
            }
        }

        item {
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.settings_stats_title),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(stringResource(R.string.settings_total_taps, state.tapCount))
                    Text(stringResource(R.string.settings_best_combo, state.bestCombo))
                    Text(stringResource(R.string.settings_daily_streak, state.dailyStreak))
                }
            }
        }

        item {
            Button(
                onClick = { showResetDialog = true },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB3261E)
                )
            ) {
                Text(stringResource(R.string.settings_reset))
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.settings_reset_confirm_title)) },
            text = { Text(stringResource(R.string.settings_reset_confirm_text)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetProgress()
                    showResetDialog = false
                }) {
                    Text(stringResource(R.string.settings_reset_confirm_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.settings_reset_confirm_cancel))
                }
            }
        )
    }
}

@Composable
private fun SettingRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
