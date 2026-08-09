package com.propertytrader.android.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.propertytrader.core.board.ColorGroup
import com.propertytrader.core.board.EventSpace
import com.propertytrader.core.board.FreeParkingSpace
import com.propertytrader.core.board.GoSpace
import com.propertytrader.core.board.GoToJailSpace
import com.propertytrader.core.board.JailSpace
import com.propertytrader.core.board.PropertySpace
import com.propertytrader.core.board.Space
import com.propertytrader.core.board.TaxSpace
import com.propertytrader.core.board.TransitSpace
import com.propertytrader.core.board.UtilitySpace
import com.propertytrader.core.model.GameState

private const val GRID = 11

@Composable
fun BoardCanvas(gameState: GameState, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cell = size.minDimension / GRID
        drawBoardCenter(cell)
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

private fun DrawScope.drawBoardCenter(cell: Float) {
    drawRect(
        color = Color(0xFFF7F2E7),
        topLeft = Offset(cell, cell),
        size = Size(cell * 9, cell * 9),
    )
}

private fun DrawScope.drawSpace(space: Space, gameState: GameState, topLeft: Offset, cell: Float) {
    val cellSize = Size(cell, cell)
    drawRect(color = spaceColor(space), topLeft = topLeft, size = cellSize)
    drawRect(color = Color.Black, topLeft = topLeft, size = cellSize, style = Stroke(width = 1f))

    val glyph = spaceGlyph(space)
    drawLabel(glyph, topLeft.x + cell / 2f, topLeft.y + cell * 0.32f, cell * 0.17f, bold = true)

    val label = spaceLabel(space)
    if (label.isNotEmpty()) {
        drawLabel(label, topLeft.x + cell / 2f, topLeft.y + cell * 0.5f, cell * 0.15f)
    }

    if (space is PropertySpace) {
        drawLabel("${space.price}", topLeft.x + cell / 2f, topLeft.y + cell * 0.68f, cell * 0.13f)
    }

    val ownerId = gameState.ownership[space.index]
    if (ownerId != null) {
        val ownerColor = Color(gameState.players.first { it.id == ownerId }.tokenColor)
        drawRect(color = ownerColor, topLeft = topLeft, size = cellSize, style = Stroke(width = 4f))
    }

    val houseLevel = gameState.houses[space.index] ?: 0
    if (houseLevel > 0) {
        drawHouseMarkers(topLeft, cell, houseLevel)
    }

    val playersHere = gameState.players.filter { it.position == space.index && !it.bankrupt }
    playersHere.forEachIndexed { i, player ->
        val offsetX = topLeft.x + cell * (0.25f + (i % 2) * 0.5f)
        val offsetY = topLeft.y + cell * (0.75f + (i / 2) * 0.15f)
        drawCircle(color = Color(player.tokenColor), radius = cell * 0.12f, center = Offset(offsetX, offsetY))
        drawCircle(
            color = Color.Black,
            radius = cell * 0.12f,
            center = Offset(offsetX, offsetY),
            style = Stroke(width = 1f),
        )
    }
}

private fun DrawScope.drawLabel(text: String, cx: Float, cy: Float, sizePx: Float, bold: Boolean = false) {
    if (text.isEmpty()) return
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = sizePx
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }
        canvas.nativeCanvas.drawText(text, cx, cy, paint)
    }
}

private fun DrawScope.drawHouseMarkers(topLeft: Offset, cell: Float, level: Int) {
    if (level >= 5) {
        val hotelSize = cell * 0.3f
        drawRect(
            color = Color(0xFFD32F2F),
            topLeft = Offset(topLeft.x + (cell - hotelSize) / 2, topLeft.y + cell * 0.05f),
            size = Size(hotelSize, hotelSize),
        )
        return
    }
    val pipSize = cell * 0.14f
    val gap = cell * 0.04f
    val totalWidth = level * pipSize + (level - 1) * gap
    var x = topLeft.x + (cell - totalWidth) / 2
    repeat(level) {
        drawRect(
            color = Color(0xFF2E7D32),
            topLeft = Offset(x, topLeft.y + cell * 0.05f),
            size = Size(pipSize, pipSize),
        )
        x += pipSize + gap
    }
}

private fun spaceGlyph(space: Space): String = when (space) {
    is GoSpace -> "START"
    is JailSpace -> "WIEZ."
    is GoToJailSpace -> "-> WIEZ."
    is FreeParkingSpace -> "PARK."
    is EventSpace -> "?"
    is TaxSpace -> "$"
    is TransitSpace -> "KOLEJ"
    is UtilitySpace -> "USLUGA"
    is PropertySpace -> ""
}

private fun spaceLabel(space: Space): String = when (space) {
    is PropertySpace -> abbreviate(space.name)
    is TransitSpace -> abbreviate(space.name)
    is UtilitySpace -> abbreviate(space.name)
    is TaxSpace -> "${space.amount}"
    else -> ""
}

private fun abbreviate(name: String): String {
    val lastWord = name.substringAfterLast(' ')
    return lastWord.take(8)
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
