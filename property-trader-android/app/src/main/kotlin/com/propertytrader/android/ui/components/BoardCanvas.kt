package com.propertytrader.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.propertytrader.core.board.ColorGroup
import com.propertytrader.core.board.PropertySpace
import com.propertytrader.core.board.Space
import com.propertytrader.core.model.GameState

private const val GRID = 11

@Composable
fun BoardCanvas(gameState: GameState, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cell = size.minDimension / GRID
        gameState.board.forEach { space ->
            val (gx, gy) = gridPosition(space.index)
            val topLeft = Offset(gx * cell, gy * cell)
            drawSpace(space, gameState, topLeft, cell)
        }
    }
}

private fun gridPosition(index: Int): Pair<Int, Int> = when (index) {
    in 0..10 -> (10 - index) to 10
    in 11..20 -> 0 to (10 - (index - 10))
    in 21..30 -> (index - 20) to 0
    in 31..39 -> 10 to (index - 30)
    else -> 10 to 10
}

private fun DrawScope.drawSpace(space: Space, gameState: GameState, topLeft: Offset, cell: Float) {
    val cellSize = Size(cell, cell)
    drawRect(color = spaceColor(space), topLeft = topLeft, size = cellSize)
    drawRect(color = Color.Black, topLeft = topLeft, size = cellSize, style = Stroke(width = 1f))

    val ownerId = gameState.ownership[space.index]
    if (ownerId != null) {
        val ownerColor = Color(gameState.players.first { it.id == ownerId }.tokenColor)
        drawRect(color = ownerColor, topLeft = topLeft, size = cellSize, style = Stroke(width = 4f))
    }

    val playersHere = gameState.players.filter { it.position == space.index && !it.bankrupt }
    playersHere.forEachIndexed { i, player ->
        val offsetX = topLeft.x + cell * (0.25f + (i % 2) * 0.5f)
        val offsetY = topLeft.y + cell * (0.25f + (i / 2) * 0.5f)
        drawCircle(color = Color(player.tokenColor), radius = cell * 0.12f, center = Offset(offsetX, offsetY))
    }
}

private fun spaceColor(space: Space): Color = when (space) {
    is PropertySpace -> groupColor(space.group)
    else -> Color(0xFFDDDDDD)
}

private fun groupColor(group: ColorGroup): Color = when (group) {
    ColorGroup.A -> Color(0xFF8D6E63)
    ColorGroup.B -> Color(0xFF64B5F6)
    ColorGroup.C -> Color(0xFFBA68C8)
    ColorGroup.D -> Color(0xFFFFB74D)
    ColorGroup.E -> Color(0xFFE57373)
    ColorGroup.F -> Color(0xFFFFF176)
    ColorGroup.G -> Color(0xFF81C784)
    ColorGroup.H -> Color(0xFF4FC3F7)
}
