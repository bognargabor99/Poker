package hu.bme.aut.onlab.poker.gamemodel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TurnStateTest {
    @Test
    fun incrementationTest() {
        var state = TurnState.PREFLOP
        assertEquals(TurnState.AFTER_FLOP, ++state)
        assertEquals(TurnState.AFTER_TURN, ++state)
        assertEquals(TurnState.AFTER_RIVER, ++state)
        assertEquals(TurnState.PREFLOP, ++state)
    }

    @Test
    fun fromCardCountTest() {
        assertEquals(TurnState.PREFLOP, TurnState.fromCardCount(0))
        assertEquals(TurnState.AFTER_FLOP, TurnState.fromCardCount(3))
        assertEquals(TurnState.AFTER_TURN, TurnState.fromCardCount(4))
        assertEquals(TurnState.AFTER_RIVER, TurnState.fromCardCount(5))

        assertFailsWith<IllegalArgumentException> { TurnState.fromCardCount(-1) }
        assertFailsWith<IllegalArgumentException> { TurnState.fromCardCount(1) }
        assertFailsWith<IllegalArgumentException> { TurnState.fromCardCount(2) }
        assertFailsWith<IllegalArgumentException> { TurnState.fromCardCount(7) }
    }
}