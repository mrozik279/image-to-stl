package com.propertytrader.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = PropertyTraderPrimary,
    secondary = PropertyTraderSecondary,
    background = PropertyTraderBackground,
)

@Composable
fun PropertyTraderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}
