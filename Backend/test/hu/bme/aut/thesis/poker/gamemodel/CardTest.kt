package hu.bme.aut.thesis.poker.gamemodel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class CardTest {
    @Test
    fun creationTest() {
        val card1 = Card(7, Suit.HEARTS)
        val card2 = Card(13, Suit.SPADES)
        val card3 = Card(10, Suit.DIAMONDS)
        val card4 = Card(3, Suit.CLUBS)

        assertEquals(7, card1.value)
        assertEquals(Suit.HEARTS, card1.suit)
        assertEquals(13, card2.value)
        assertEquals(Suit.SPADES, card2.suit)
        assertEquals(10, card3.value)
        assertEquals(Suit.DIAMONDS, card3.suit)
        assertEquals(3, card4.value)
        assertEquals(Suit.CLUBS, card4.suit)
    }

    @Test
    fun creationFailureTest() {
        val cardMessage = "Card creation should have failed"
        val values = listOf(-10, -1, 1, 0, 15, 100, 60)
        values.forEach { value ->
            assertFails(cardMessage) { Card(value, Suit.DIAMONDS) }
        }
    }
}