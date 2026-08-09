package com.propertytrader.core

import com.propertytrader.core.board.BoardFactory
import com.propertytrader.core.board.ColorGroup
import com.propertytrader.core.board.EventSpace
import com.propertytrader.core.board.FreeParkingSpace
import com.propertytrader.core.board.GoSpace
import com.propertytrader.core.board.GoToJailSpace
import com.propertytrader.core.board.JailSpace
import com.propertytrader.core.board.PropertySpace
import com.propertytrader.core.board.TaxSpace
import com.propertytrader.core.board.TransitSpace
import com.propertytrader.core.board.UtilitySpace
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BoardFactoryTest {

    private val board = BoardFactory.classicBoard()

    @Test
    fun `board has exactly 40 spaces with matching indices`() {
        assertEquals(40, board.size)
        board.forEachIndexed { i, space -> assertEquals(i, space.index) }
    }

    @Test
    fun `special spaces are at classic positions`() {
        assertIs<GoSpace>(board[0])
        assertIs<JailSpace>(board[10])
        assertIs<FreeParkingSpace>(board[20])
        assertIs<GoToJailSpace>(board[30])
    }

    @Test
    fun `space type counts add up to 40`() {
        val properties = board.filterIsInstance<PropertySpace>()
        val transit = board.filterIsInstance<TransitSpace>()
        val utilities = board.filterIsInstance<UtilitySpace>()
        val taxes = board.filterIsInstance<TaxSpace>()
        val events = board.filterIsInstance<EventSpace>()

        assertEquals(22, properties.size)
        assertEquals(4, transit.size)
        assertEquals(2, utilities.size)
        assertEquals(2, taxes.size)
        assertEquals(6, events.size)
        assertEquals(40, properties.size + transit.size + utilities.size + taxes.size + events.size + 4)
    }

    @Test
    fun `all eight color groups are used`() {
        val groups = board.filterIsInstance<PropertySpace>().map { it.group }.toSet()
        assertEquals(ColorGroup.entries.toSet(), groups)
    }

    @Test
    fun `no space uses the word Monopoly`() {
        assertTrue(board.none { it.name.contains("Monopoly", ignoreCase = true) })
    }
}
