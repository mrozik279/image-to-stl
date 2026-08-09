package com.propertytrader.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.propertytrader.android.ui.GameViewModel
import com.propertytrader.android.ui.Screen
import com.propertytrader.android.ui.screens.BoardScreen
import com.propertytrader.android.ui.screens.BuildScreen
import com.propertytrader.android.ui.screens.GameOverScreen
import com.propertytrader.android.ui.screens.RulesScreen
import com.propertytrader.android.ui.screens.SetupScreen
import com.propertytrader.android.ui.screens.TradeScreen
import com.propertytrader.android.ui.theme.PropertyTraderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PropertyTraderTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot(viewModel: GameViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState.screen) {
        Screen.SETUP -> SetupScreen(
            onStart = { names, colors, roundLimit -> viewModel.startGame(names, colors, roundLimit = roundLimit) },
            onOpenRules = viewModel::openRules,
        )
        Screen.BOARD -> uiState.gameState?.let { gameState ->
            BoardScreen(
                gameState = gameState,
                eventLog = uiState.eventLog,
                onRollDice = viewModel::rollDice,
                onPurchaseDecision = viewModel::decidePurchase,
                onJailDecision = viewModel::decideJail,
                onEndTurn = viewModel::endTurn,
                onOpenTrade = viewModel::openTrade,
                onTradeResponse = viewModel::respondToTrade,
                onOpenBuild = viewModel::openBuild,
                onUseExtraRoll = viewModel::useExtraRoll,
                onOpenRules = viewModel::openRules,
            )
        }
        Screen.RULES -> RulesScreen(onBack = viewModel::closeRules)
        Screen.TRADE -> uiState.gameState?.let { gameState ->
            TradeScreen(
                gameState = gameState,
                onPropose = viewModel::proposeTrade,
                onCancel = viewModel::cancelTrade,
            )
        }
        Screen.BUILD -> uiState.gameState?.let { gameState ->
            BuildScreen(
                gameState = gameState,
                onBuild = viewModel::buildHouse,
                onCancel = viewModel::closeBuild,
            )
        }
        Screen.GAME_OVER -> uiState.gameState?.let { gameState ->
            GameOverScreen(gameState = gameState, onRestart = viewModel::restart)
        }
    }
}
