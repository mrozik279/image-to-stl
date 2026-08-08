package com.mrozik279.pawclicker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PawBrown = Color(0xFF8D5A3C)
val PawBrownDark = Color(0xFF5E3B26)
val PawCream = Color(0xFFFFF7EE)
val PawGold = Color(0xFFE0B267)

private val LightColors = lightColorScheme(
    primary = PawBrown,
    onPrimary = Color.White,
    secondary = PawGold,
    background = PawCream,
    surface = Color.White
)

private val DarkColors = darkColorScheme(
    primary = PawGold,
    onPrimary = Color.Black,
    secondary = PawBrown,
    background = Color(0xFF241A14),
    surface = Color(0xFF322419)
)

@Composable
fun PawClickerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
