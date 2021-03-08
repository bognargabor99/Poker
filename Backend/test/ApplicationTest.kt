package hu.bme.aut.onlab.poker

import hu.bme.aut.onlab.poker.gamemodel.Card
import hu.bme.aut.onlab.poker.gamemodel.HandEvaluator
import hu.bme.aut.onlab.poker.gamemodel.HandType
import hu.bme.aut.onlab.poker.gamemodel.Suit
import io.ktor.http.*
import kotlin.test.*
import io.ktor.server.testing.*

class ApplicationTest {
    //@Test
    fun testRoot() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("HELLO WORLD!", response.content)
            }
        }
    }

    @Test
    fun royalHandEvalTest() {
        val handCards = mutableListOf(Card(2, Suit.HEARTS),
            Card(10, Suit.SPADES),
            Card(12, Suit.SPADES),
            Card(3, Suit.DIAMONDS),
            Card(13, Suit.SPADES),
            Card(11, Suit.SPADES),
            Card(14, Suit.SPADES))
        val hand = HandEvaluator.evaluateHand(handCards)
        assertEquals(HandType.ROYAL_FLUSH, hand.type)
    }

    @Test
    fun straightFlushEvalTest() {
        val handCards = mutableListOf(Card(2, Suit.HEARTS),
            Card(10, Suit.HEARTS),
            Card(12, Suit.HEARTS),
            Card(3, Suit.DIAMONDS),
            Card(9, Suit.HEARTS),
            Card(11, Suit.HEARTS),
            Card(8, Suit.HEARTS))
        val hand = HandEvaluator.evaluateHand(handCards)
        assertEquals(HandType.STRAIGHT_FLUSH, hand.type)
        assertEquals(12, hand.values[0])
    }

    @Test
    fun fourOfKindTest() {
        val handCards = mutableListOf(Card(8, Suit.HEARTS),
            Card(14, Suit.CLUBS),
            Card(11, Suit.HEARTS),
            Card(11, Suit.DIAMONDS),
            Card(14, Suit.SPADES),
            Card(14, Suit.HEARTS),
            Card(14, Suit.DIAMONDS))
        val hand = HandEvaluator.evaluateHand(handCards)
        assertEquals(HandType.FOUR_OF_A_KIND, hand.type)
        assertEquals(14, hand.values[0])
        assertEquals(11, hand.values[1])
    }

    @Test
    fun fullHouseTest() {
        val handCards = mutableListOf(Card(4, Suit.DIAMONDS),
            Card(11, Suit.DIAMONDS),
            Card(4, Suit.CLUBS),
            Card(4, Suit.SPADES),
            Card(7, Suit.SPADES),
            Card(7, Suit.DIAMONDS),
            Card(7, Suit.HEARTS))
        val hand = HandEvaluator.evaluateHand(handCards)
        assertEquals(HandType.FULL_HOUSE, hand.type)
        assertEquals(7, hand.values[0])
        assertEquals(4, hand.values[1])
    }

    @Test
    fun flushTest() {
        val handCards = mutableListOf(Card(10, Suit.CLUBS),
            Card(7, Suit.SPADES),
            Card(11, Suit.CLUBS),
            Card(13, Suit.HEARTS),
            Card(13, Suit.CLUBS),
            Card(6, Suit.CLUBS),
            Card(9, Suit.CLUBS))
        val hand = HandEvaluator.evaluateHand(handCards)
        assertEquals(HandType.FLUSH, hand.type)
        assertEquals(13, hand.values[0])
        assertEquals(11, hand.values[1])
        assertEquals(10, hand.values[2])
        assertEquals(9, hand.values[3])
        assertEquals(6, hand.values[4])
    }

    @Test
    fun straightHandEvalTest() {
        val handCards = mutableListOf(Card(2, Suit.HEARTS),
            Card(10, Suit.CLUBS),
            Card(12, Suit.SPADES),
            Card(3, Suit.SPADES),
            Card(13, Suit.HEARTS),
            Card(11, Suit.HEARTS),
            Card(9, Suit.CLUBS))
        val hand = HandEvaluator.evaluateHand(handCards)
        assertEquals(HandType.STRAIGHT, hand.type)
        assertEquals(13, hand.values[0])
    }

    @Test
    fun threeOfKindTest() {
        val handCards = mutableListOf(Card(5, Suit.DIAMONDS),
            Card(4, Suit.HEARTS),
            Card(10, Suit.DIAMONDS),
            Card(10, Suit.HEARTS),
            Card(10, Suit.CLUBS),
            Card(14, Suit.DIAMONDS),
            Card(8, Suit.CLUBS))
        val hand = HandEvaluator.evaluateHand(handCards)
        assertEquals(HandType.THREE_OF_A_KIND, hand.type)
        assertEquals(10, hand.values[0])
        assertEquals(14, hand.values[1])
        assertEquals(8, hand.values[2])
    }

    @Test
    fun twoPairTest() {
        val handCards = mutableListOf(Card(7, Suit.HEARTS),
            Card(8, Suit.SPADES),
            Card(11, Suit.SPADES),
            Card(8, Suit.CLUBS),
            Card(5, Suit.SPADES),
            Card(6, Suit.DIAMONDS),
            Card(5, Suit.CLUBS))
        val hand = HandEvaluator.evaluateHand(handCards)
        assertEquals(HandType.TWO_PAIR, hand.type)
        assertEquals(8, hand.values[0])
        assertEquals(5, hand.values[1])
        assertEquals(11, hand.values[2])
    }

    @Test
    fun pairTest() {
        val handCards = mutableListOf(Card(11, Suit.HEARTS),
            Card(4, Suit.CLUBS),
            Card(9, Suit.CLUBS),
            Card(3, Suit.HEARTS),
            Card(3, Suit.CLUBS),
            Card(12, Suit.SPADES),
            Card(10, Suit.HEARTS))
        val hand = HandEvaluator.evaluateHand(handCards)
        assertEquals(HandType.PAIR, hand.type)
        assertEquals(3, hand.values[0])
        assertEquals(12, hand.values[1])
        assertEquals(11, hand.values[2])
        assertEquals(10, hand.values[3])
    }

    @Test
    fun highCardTest() {
        val handCards = mutableListOf(Card(13, Suit.SPADES),
            Card(4, Suit.SPADES),
            Card(5, Suit.HEARTS),
            Card(7, Suit.SPADES),
            Card(6, Suit.DIAMONDS),
            Card(9, Suit.HEARTS),
            Card(11, Suit.SPADES))
        val hand = HandEvaluator.evaluateHand(handCards)
        assertEquals(HandType.HIGH_CARD, hand.type)
        assertEquals(13, hand.values[0])
        assertEquals(11, hand.values[1])
        assertEquals(9, hand.values[2])
        assertEquals(7, hand.values[3])
        assertEquals(6, hand.values[4])
    }
}
