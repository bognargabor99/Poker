package hu.bme.aut.onlab.poker.gamemodel

import kotlin.test.*

class HandEvaluatorTest {
    @Test
    fun royalHandEvalTest() {
        // Arrange
        val handCards = mutableListOf(Card(2, Suit.HEARTS),
            Card(10, Suit.SPADES),
            Card(12, Suit.SPADES),
            Card(3, Suit.DIAMONDS),
            Card(13, Suit.SPADES),
            Card(11, Suit.SPADES),
            Card(14, Suit.SPADES))

        // Act
        val hand = HandEvaluator.evaluateHand(handCards)

        //Assert
        assertEquals(HandType.ROYAL_FLUSH, hand.type)
    }

    @Test
    fun straightFlushEvalTest() {
        // Arrange
        val handCards = mutableListOf(Card(2, Suit.HEARTS),
            Card(10, Suit.HEARTS),
            Card(12, Suit.HEARTS),
            Card(3, Suit.DIAMONDS),
            Card(9, Suit.HEARTS),
            Card(11, Suit.HEARTS),
            Card(8, Suit.HEARTS))

        // Act
        val hand = HandEvaluator.evaluateHand(handCards)

        // Assert
        assertEquals(HandType.STRAIGHT_FLUSH, hand.type)
        assertEquals(12, hand.values[0])
    }

    @Test
    fun fourOfKindTest() {
        // Arrange
        val handCards = mutableListOf(Card(8, Suit.HEARTS),
            Card(14, Suit.CLUBS),
            Card(11, Suit.HEARTS),
            Card(11, Suit.DIAMONDS),
            Card(14, Suit.SPADES),
            Card(14, Suit.HEARTS),
            Card(14, Suit.DIAMONDS))

        // Act
        val hand = HandEvaluator.evaluateHand(handCards)

        // Assert
        assertEquals(HandType.FOUR_OF_A_KIND, hand.type)
        assertEquals(14, hand.values[0])
        assertEquals(11, hand.values[1])
    }

    @Test
    fun fullHouseTest() {
        // Arrange
        val handCards = mutableListOf(Card(4, Suit.DIAMONDS),
            Card(11, Suit.DIAMONDS),
            Card(4, Suit.CLUBS),
            Card(4, Suit.SPADES),
            Card(7, Suit.SPADES),
            Card(7, Suit.DIAMONDS),
            Card(7, Suit.HEARTS))

        // Act
        val hand = HandEvaluator.evaluateHand(handCards)

        // Assert
        assertEquals(HandType.FULL_HOUSE, hand.type)
        assertEquals(7, hand.values[0])
        assertEquals(4, hand.values[1])
    }

    @Test
    fun flushTest() {
        // Arrange
        val handCards = mutableListOf(Card(10, Suit.CLUBS),
            Card(7, Suit.SPADES),
            Card(11, Suit.CLUBS),
            Card(12, Suit.HEARTS),
            Card(13, Suit.CLUBS),
            Card(6, Suit.CLUBS),
            Card(9, Suit.CLUBS))

        // Act
        val hand = HandEvaluator.evaluateHand(handCards)

        // Assert
        assertEquals(HandType.FLUSH, hand.type)
        assertEquals(13, hand.values[0])
        assertEquals(11, hand.values[1])
        assertEquals(10, hand.values[2])
        assertEquals(9, hand.values[3])
        assertEquals(6, hand.values[4])
    }

    @Test
    fun straightHandEvalTest() {
        // Arrange
        val handCards = mutableListOf(Card(9, Suit.HEARTS),
            Card(10, Suit.CLUBS),
            Card(12, Suit.SPADES),
            Card(9, Suit.SPADES),
            Card(13, Suit.HEARTS),
            Card(11, Suit.HEARTS),
            Card(9, Suit.CLUBS))

        // Act
        val hand = HandEvaluator.evaluateHand(handCards)

        // Assert
        assertEquals(HandType.STRAIGHT, hand.type)
        assertEquals(13, hand.values[0])
    }

    @Test
    fun lowStraightTest() {
        // Arrange
        val handCards = mutableListOf(Card(11, Suit.HEARTS),
            Card(14, Suit.CLUBS),
            Card(5, Suit.CLUBS),
            Card(2, Suit.SPADES),
            Card(9, Suit.HEARTS),
            Card(3, Suit.HEARTS),
            Card(4, Suit.CLUBS))

        // Act
        val hand = HandEvaluator.evaluateHand(handCards)

        // Assert
        assertEquals(HandType.STRAIGHT, hand.type)
        assertEquals(5, hand.values[0])
    }

    @Test
    fun threeOfKindTest() {
        // Arrange
        val handCards = mutableListOf(Card(5, Suit.DIAMONDS),
            Card(4, Suit.HEARTS),
            Card(10, Suit.DIAMONDS),
            Card(10, Suit.HEARTS),
            Card(10, Suit.CLUBS),
            Card(14, Suit.DIAMONDS),
            Card(8, Suit.CLUBS))

        // Act
        val hand = HandEvaluator.evaluateHand(handCards)

        // Assert
        assertEquals(HandType.THREE_OF_A_KIND, hand.type)
        assertEquals(10, hand.values[0])
        assertEquals(14, hand.values[1])
        assertEquals(8, hand.values[2])
    }

    @Test
    fun twoPairTest() {
        // Arrange
        val handCards = mutableListOf(Card(7, Suit.HEARTS),
            Card(8, Suit.SPADES),
            Card(11, Suit.SPADES),
            Card(8, Suit.CLUBS),
            Card(5, Suit.SPADES),
            Card(6, Suit.DIAMONDS),
            Card(5, Suit.CLUBS))

        // Act
        val hand = HandEvaluator.evaluateHand(handCards)

        // Assert
        assertEquals(HandType.TWO_PAIR, hand.type)
        assertEquals(8, hand.values[0])
        assertEquals(5, hand.values[1])
        assertEquals(11, hand.values[2])
    }

    @Test
    fun pairTest() {
        // Arrange
        val handCards = mutableListOf(Card(11, Suit.HEARTS),
            Card(4, Suit.CLUBS),
            Card(9, Suit.CLUBS),
            Card(3, Suit.HEARTS),
            Card(3, Suit.CLUBS),
            Card(12, Suit.SPADES),
            Card(10, Suit.HEARTS))

        // Act
        val hand = HandEvaluator.evaluateHand(handCards)

        // Assert
        assertEquals(HandType.PAIR, hand.type)
        assertEquals(3, hand.values[0])
        assertEquals(12, hand.values[1])
        assertEquals(11, hand.values[2])
        assertEquals(10, hand.values[3])
    }

    @Test
    fun highCardTest() {
        // Arrange
        val handCards = mutableListOf(Card(13, Suit.SPADES),
            Card(4, Suit.SPADES),
            Card(5, Suit.HEARTS),
            Card(7, Suit.SPADES),
            Card(6, Suit.DIAMONDS),
            Card(9, Suit.HEARTS),
            Card(11, Suit.SPADES))

        // Act
        val hand = HandEvaluator.evaluateHand(handCards)

        // Assert
        assertEquals(HandType.HIGH_CARD, hand.type)
        assertEquals(13, hand.values[0])
        assertEquals(11, hand.values[1])
        assertEquals(9, hand.values[2])
        assertEquals(7, hand.values[3])
        assertEquals(6, hand.values[4])
    }

    @Test
    fun straightEqualityTest() {
        //Arrange
        val handCards1 = mutableListOf(Card(9, Suit.SPADES),
            Card(8, Suit.SPADES),
            Card(7, Suit.SPADES),
            Card(10, Suit.CLUBS),
            Card(6, Suit.DIAMONDS),
            Card(10, Suit.DIAMONDS),
            Card(14, Suit.CLUBS))

        val handCards2 = mutableListOf(Card(9, Suit.SPADES),
            Card(8, Suit.SPADES),
            Card(7, Suit.SPADES),
            Card(10, Suit.CLUBS),
            Card(6, Suit.DIAMONDS),
            Card(9, Suit.CLUBS),
            Card(2, Suit.CLUBS))

        // Act
        val hand1 = HandEvaluator.evaluateHand(handCards1)
        val hand2 = HandEvaluator.evaluateHand(handCards2)

        // Assert
        assert(hand1 == hand2)
    }

    @Test
    fun handEqualityTest() {
        // Arrange
        val hand1 = Hand(HandType.THREE_OF_A_KIND, listOf(3, 14, 9))
        val hand2 = Hand(HandType.THREE_OF_A_KIND, listOf(3, 14, 9))
        val hand3 = Hand(HandType.FLUSH, listOf(12, 9, 8, 6, 3))
        val hand4 = Hand(HandType.FLUSH, listOf(12, 9, 8, 6, 3))
        val hand5 = Hand(HandType.STRAIGHT, listOf(7))
        val hand6 = Hand(HandType.STRAIGHT, listOf(7))

        // Assert
        assert(hand1 == hand2)
        assert(hand3 == hand4)
        assert(hand5 == hand6)
    }
}
