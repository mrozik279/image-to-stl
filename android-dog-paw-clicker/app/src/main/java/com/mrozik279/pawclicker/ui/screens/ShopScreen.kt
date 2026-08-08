package com.mrozik279.pawclicker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mrozik279.pawclicker.ClickerViewModel
import com.mrozik279.pawclicker.R
import com.mrozik279.pawclicker.data.BREEDS
import com.mrozik279.pawclicker.data.UpgradeConfig
import java.text.NumberFormat

@Composable
fun ShopScreen(viewModel: ClickerViewModel, modifier: Modifier = Modifier) {
    val state = viewModel.state
    val numberFormat = remember { NumberFormat.getIntegerInstance() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.shop_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            val cost = UpgradeConfig.costForLevel(
                UpgradeConfig.TREAT_BASE_COST, UpgradeConfig.TREAT_GROWTH, state.treatLevel
            )
            UpgradeCard(
                name = stringResource(R.string.shop_treat_name),
                description = stringResource(R.string.shop_treat_desc),
                level = state.treatLevel,
                cost = cost,
                canAfford = state.bones >= cost,
                onBuy = viewModel::buyTreat,
                numberFormat = numberFormat
            )
        }

        item {
            val cost = UpgradeConfig.costForLevel(
                UpgradeConfig.PUPPY_BASE_COST, UpgradeConfig.PUPPY_GROWTH, state.puppyLevel
            )
            UpgradeCard(
                name = stringResource(R.string.shop_puppy_name),
                description = stringResource(R.string.shop_puppy_desc),
                level = state.puppyLevel,
                cost = cost,
                canAfford = state.bones >= cost,
                onBuy = viewModel::buyPuppy,
                numberFormat = numberFormat
            )
        }

        item {
            Text(
                text = stringResource(R.string.shop_breeds_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(BREEDS) { breed ->
            val unlocked = breed.id in state.unlockedBreeds
            val selected = breed.id == state.selectedBreed
            Card(colors = CardDefaults.cardColors()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(breed.displayName, fontWeight = FontWeight.Bold)
                        if (!unlocked) {
                            Text(
                                stringResource(R.string.shop_breed_locked, breed.unlockAt),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    if (unlocked) {
                        if (selected) {
                            Text(
                                stringResource(R.string.shop_breed_selected),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            OutlinedButton(onClick = { viewModel.selectBreed(breed.id) }) {
                                Text(stringResource(R.string.shop_breed_select))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpgradeCard(
    name: String,
    description: String,
    level: Int,
    cost: Long,
    canAfford: Boolean,
    onBuy: () -> Unit,
    numberFormat: NumberFormat
) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodyMedium)
            Text(
                stringResource(R.string.shop_level, level),
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.shop_cost, numberFormat.format(cost)))
                Button(onClick = onBuy, enabled = canAfford) {
                    Text(stringResource(R.string.shop_buy))
                }
            }
        }
    }
}
