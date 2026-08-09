package com.propertytrader.android.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

@Composable
fun DiceCup(dice: Pair<Int, Int>?, modifier: Modifier = Modifier) {
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(dice) {
        if (dice != null) {
            rotation.snapTo(0f)
            rotation.animateTo(360f, animationSpec = tween(450, easing = FastOutSlowInEasing))
        }
    }
    Row(
        modifier = modifier
            .background(Color(0xFF5D4037), RoundedCornerShape(18.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DieFace(dice?.first, rotation.value)
        DieFace(dice?.second, rotation.value)
    }
}

@Composable
private fun DieFace(value: Int?, rotationDegrees: Float) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .rotate(rotationDegrees)
            .background(Color.White, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (value != null) {
            Canvas(modifier = Modifier.size(34.dp)) { drawPips(value) }
        } else {
            Text("?", color = Color(0xFF9E9E9E), style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun DrawScope.drawPips(value: Int) {
    val radius = size.minDimension * 0.09f
    val pipColor = Color(0xFF212121)
    pipPositions(value).forEach { (fx, fy) ->
        drawCircle(color = pipColor, radius = radius, center = Offset(size.width * fx, size.height * fy))
    }
}

private fun pipPositions(value: Int): List<Pair<Float, Float>> {
    val left = 0.25f
    val center = 0.5f
    val right = 0.75f
    val top = 0.25f
    val mid = 0.5f
    val bottom = 0.75f
    return when (value) {
        1 -> listOf(center to mid)
        2 -> listOf(left to top, right to bottom)
        3 -> listOf(left to top, center to mid, right to bottom)
        4 -> listOf(left to top, right to top, left to bottom, right to bottom)
        5 -> listOf(left to top, right to top, center to mid, left to bottom, right to bottom)
        6 -> listOf(left to top, right to top, left to mid, right to mid, left to bottom, right to bottom)
        else -> emptyList()
    }
}
