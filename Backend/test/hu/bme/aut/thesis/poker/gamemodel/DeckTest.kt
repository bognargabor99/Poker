package hu.bme.aut.thesis.poker.gamemodel

import hu.bme.aut.thesis.poker.testutils.*
import kotlin.test.*

class DeckTest {
    @Test
    fun creationTest() {
        val deck = TraditionalDeck()
        assertIs<Deck>(deck)
        assertIs<TraditionalDeck>(deck)
        assertIsNot<RoyalDeck>(deck)
    }

    @Test
    fun handCardsUntilEmptyFromTraditionalDeckTest() {
        val deck = TraditionalDeck()
        deck.shuffle()
        val cards: MutableList<Card> = mutableListOf()

        for (i in 0 until 52 step 2) {
            val newCards = deck.getCards(2)
            assertNotContains(cards, newCards[0])
            assertNotContains(cards, newCards[1])
            cards.addAll(newCards)
        }
        val newCards = deck.getCards(2)
        assertContentEquals(listOf(), newCards)
    }

    @Test
    fun handCardsUntilEmptyFromRoyalDeckTest() {
        val deck = RoyalDeck()
        deck.shuffle()
        val cards: MutableList<Card> = mutableListOf()

        for (i in 0 until 20 step 2) {
            val newCards = deck.getCards(2)
            assertNotContains(cards, newCards[0])
            assertNotContains(cards, newCards[1])
            assert(newCards[0].value in 10..14)
            assert(newCards[1].value in 10..14)
            cards.addAll(newCards)
        }
        val newCards = deck.getCards(2)
        assertContentEquals(listOf(), newCards)
    }

    @Test
    fun getMoreCardsThanExistsInTraditionalDeckTest() {
        val deck = TraditionalDeck()
        val cards = deck.getCards(200)
        assertEquals(52, cards.size)
    }

    @Test
    fun getMoreCardsThanExistsInRoyalDeckTest() {
        val deck = RoyalDeck()
        val cards = deck.getCards(200)
        assertEquals(20, cards.size)
    }

    @Test
    fun resetDeckTest() {
        val deck = TraditionalDeck()
        val cards = deck.getCards(200) as MutableList<Card>
        assertEquals(52, cards.size)
        deck.reset()
        cards.clear()
        cards.addAll(deck.getCards(200))
        assertEquals(52, cards.size)
    }

    @Test
    fun shuffleDeckTest() {
        val deck = TraditionalDeck()
        val ordered = deck.getCards(52)
        deck.reset()
        deck.shuffle()
        val shuffled = deck.getCards(52)
        assertContentNotEquals(ordered, shuffled)
        assertContentEquals(ordered.sorted(), shuffled.sorted())
    }

    @Test
    fun distinctionTest() {
        val traditionalDeck = TraditionalDeck().getCards(52)
        val royalDeck = RoyalDeck().getCards(20)
        assertContentEquals(traditionalDeck.sorted(), traditionalDeck.sorted().distinct())
        assertContentEquals(royalDeck.sorted(), royalDeck.sorted().distinct())
    }
}