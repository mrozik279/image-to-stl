package com.mrozik279.pawclicker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrozik279.pawclicker.ClickerViewModel
import com.mrozik279.pawclicker.R
import com.mrozik279.pawclicker.ui.screens.AchievementsScreen
import com.mrozik279.pawclicker.ui.screens.ClickerScreen
import com.mrozik279.pawclicker.ui.screens.SettingsScreen
import com.mrozik279.pawclicker.ui.screens.ShopScreen

private enum class Tab(val labelRes: Int) {
    CLICKER(R.string.nav_clicker),
    SHOP(R.string.nav_shop),
    ACHIEVEMENTS(R.string.nav_achievements),
    SETTINGS(R.string.nav_settings)
}

@Composable
fun PawClickerApp(viewModel: ClickerViewModel = viewModel()) {
    var currentTab by remember { mutableStateOf(Tab.CLICKER) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar { Text(data.visuals.message) }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == Tab.CLICKER,
                    onClick = { currentTab = Tab.CLICKER },
                    icon = { Icon(Icons.Filled.Pets, contentDescription = null) },
                    label = { Text(stringRes(Tab.CLICKER)) }
                )
                NavigationBarItem(
                    selected = currentTab == Tab.SHOP,
                    onClick = { currentTab = Tab.SHOP },
                    icon = { Icon(Icons.Filled.Storefront, contentDescription = null) },
                    label = { Text(stringRes(Tab.SHOP)) }
                )
                NavigationBarItem(
                    selected = currentTab == Tab.ACHIEVEMENTS,
                    onClick = { currentTab = Tab.ACHIEVEMENTS },
                    icon = { Icon(Icons.Filled.EmojiEvents, contentDescription = null) },
                    label = { Text(stringRes(Tab.ACHIEVEMENTS)) }
                )
                NavigationBarItem(
                    selected = currentTab == Tab.SETTINGS,
                    onClick = { currentTab = Tab.SETTINGS },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    label = { Text(stringRes(Tab.SETTINGS)) }
                )
            }
        }
    ) { padding ->
        val contentModifier = Modifier.padding(padding)
        when (currentTab) {
            Tab.CLICKER -> ClickerScreen(viewModel, contentModifier)
            Tab.SHOP -> ShopScreen(viewModel, contentModifier)
            Tab.ACHIEVEMENTS -> AchievementsScreen(viewModel, contentModifier)
            Tab.SETTINGS -> SettingsScreen(viewModel, contentModifier)
        }
    }
}

@Composable
private fun stringRes(tab: Tab): String = androidx.compose.ui.res.stringResource(tab.labelRes)
