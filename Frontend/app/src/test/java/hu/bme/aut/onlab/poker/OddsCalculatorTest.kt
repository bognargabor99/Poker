package hu.bme.aut.onlab.poker

import hu.bme.aut.onlab.poker.calculation.OddsCalculator
import hu.bme.aut.onlab.poker.dto.PlayerToSpectate
import hu.bme.aut.onlab.poker.model.Card
import hu.bme.aut.onlab.poker.model.Player
import hu.bme.aut.onlab.poker.model.Suit
import org.junit.Test

import org.junit.Assert.*

class OddsCalculatorTest {
    private val messageWin = "Difference between expected and calculated win odds id too big"
    private val messageTie = "Difference between expected and calculated tie odds id too big"
    
    @Test
    fun riverTwoPlayers() {
        val tableCards = mutableListOf(
            Card(6, Suit.DIAMONDS),
            Card(13, Suit.DIAMONDS),
            Card(10, Suit.HEARTS),
            Card(9, Suit.HEARTS),
            Card(12, Suit.SPADES),
        )
        val players = listOf(
            PlayerToSpectate(Player("player1", 3000, 200, true), listOf(Card(11, Suit.CLUBS), Card(11, Suit.HEARTS))),
            PlayerToSpectate(Player("player2", 3000, 200, true), listOf(Card(14, Suit.DIAMONDS), Card(14, Suit.SPADES))),
        )

        val odds = OddsCalculator.calculateOdds(tableCards, players)

        assertEquals(100.0, odds["player1"]?.winChance)
        assertEquals(0.0, odds["player1"]?.tieChance)
        assertEquals(0.0, odds["player2"]?.winChance)
        assertEquals(0.0, odds["player2"]?.tieChance)
    }

    @Test
    fun riverThreePlayers() {
        val tableCards = mutableListOf(
            Card(7, Suit.HEARTS),
            Card(10, Suit.DIAMONDS),
            Card(11, Suit.DIAMONDS),
            Card(5, Suit.SPADES),
            Card(13, Suit.DIAMONDS),
        )
        val players = listOf(
            PlayerToSpectate(Player("player1", 3000, 200, true), listOf(Card(14, Suit.DIAMONDS), Card(12, Suit.DIAMONDS))),
            PlayerToSpectate(Player("player2", 3000, 200, true), listOf(Card(7, Suit.DIAMONDS), Card(7, Suit.CLUBS))),
            PlayerToSpectate(Player("player3", 3000, 200, true), listOf(Card(2, Suit.SPADES), Card(3, Suit.SPADES)))
        )

        val odds = OddsCalculator.calculateOdds(tableCards, players)

        assertEquals(100.0, odds["player1"]?.winChance)
        assertEquals(0.0, odds["player1"]?.tieChance)
        assertEquals(0.0, odds["player2"]?.winChance)
        assertEquals(0.0, odds["player2"]?.tieChance)
        assertEquals(0.0, odds["player3"]?.winChance)
        assertEquals(0.0, odds["player3"]?.tieChance)
    }

    @Test
    fun riverFourPlayers() {
        val tableCards = mutableListOf(
            Card(3, Suit.SPADES),
            Card(9, Suit.DIAMONDS),
            Card(8, Suit.CLUBS),
            Card(4, Suit.HEARTS),
            Card(6, Suit.DIAMONDS),
        )
        val players = listOf(
            PlayerToSpectate(Player("player1", 3000, 200, true), listOf(Card(7, Suit.HEARTS), Card(12, Suit.DIAMONDS))),
            PlayerToSpectate(Player("player2", 3000, 200, true), listOf(Card(8, Suit.SPADES), Card(6, Suit.CLUBS))),
            PlayerToSpectate(Player("player3", 3000, 200, true), listOf(Card(13, Suit.HEARTS), Card(5, Suit.DIAMONDS))),
            PlayerToSpectate(Player("player4", 3000, 200, true), listOf(Card(5, Suit.SPADES), Card(14, Suit.SPADES)))
        )

        val odds = OddsCalculator.calculateOdds(tableCards, players)

        assertEquals(0.0, odds["player1"]?.winChance)
        assertEquals(0.0, odds["player1"]?.tieChance)
        assertEquals(100.0, odds["player2"]?.winChance)
        assertEquals(0.0, odds["player2"]?.tieChance)
        assertEquals(0.0, odds["player3"]?.winChance)
        assertEquals(0.0, odds["player3"]?.tieChance)
        assertEquals(0.0, odds["player4"]?.winChance)
        assertEquals(0.0, odds["player4"]?.tieChance)
    }

    @Test
    fun riverFivePlayers() {
        val tableCards = mutableListOf(
            Card(10, Suit.SPADES),
            Card(5, Suit.DIAMONDS),
            Card(9, Suit.HEARTS),
            Card(14, Suit.HEARTS),
            Card(12, Suit.DIAMONDS),
        )
        val players = listOf(
            PlayerToSpectate(Player("player1", 3000, 200, true), listOf(Card(9, Suit.CLUBS), Card(4, Suit.HEARTS))),
            PlayerToSpectate(Player("player2", 3000, 200, true), listOf(Card(5, Suit.CLUBS), Card(4, Suit.SPADES))),
            PlayerToSpectate(Player("player3", 3000, 200, true), listOf(Card(2, Suit.SPADES), Card(8, Suit.DIAMONDS))),
            PlayerToSpectate(Player("player4", 3000, 200, true), listOf(Card(13, Suit.DIAMONDS), Card(11, Suit.CLUBS))),
            PlayerToSpectate(Player("player5", 3000, 200, true), listOf(Card(14, Suit.CLUBS), Card(14, Suit.SPADES)))
        )

        val odds = OddsCalculator.calculateOdds(tableCards, players)

        assertEquals(0.0, odds["player1"]?.winChance)
        assertEquals(0.0, odds["player1"]?.tieChance)
        assertEquals(0.0, odds["player2"]?.winChance)
        assertEquals(0.0, odds["player2"]?.tieChance)
        assertEquals(0.0, odds["player3"]?.winChance)
        assertEquals(0.0, odds["player3"]?.tieChance)
        assertEquals(100.0, odds["player4"]?.winChance)
        assertEquals(0.0, odds["player4"]?.tieChance)
        assertEquals(0.0, odds["player5"]?.tieChance)
        assertEquals(0.0, odds["player5"]?.tieChance)
    }

    @Test
    fun riverThreePlayersTie() {
        val tableCards = mutableListOf(
            Card(13, Suit.CLUBS),
            Card(14, Suit.HEARTS),
            Card(12, Suit.DIAMONDS),
            Card(11, Suit.CLUBS),
            Card(10, Suit.SPADES),
        )
        val players = listOf(
            PlayerToSpectate(Player("player1", 3000, 200, true), listOf(Card(5, Suit.HEARTS), Card(9, Suit.HEARTS))),
            PlayerToSpectate(Player("player2", 3000, 200, true), listOf(Card(9, Suit.DIAMONDS), Card(2, Suit.CLUBS))),
            PlayerToSpectate(Player("player3", 3000, 200, true), listOf(Card(8, Suit.SPADES), Card(8, Suit.DIAMONDS)))
        )

        val odds = OddsCalculator.calculateOdds(tableCards, players)

        assertEquals(0.0, odds["player1"]?.winChance)
        assertEquals(100.0, odds["player1"]?.tieChance)
        assertEquals(0.0, odds["player2"]?.winChance)
        assertEquals(100.0, odds["player2"]?.tieChance)
        assertEquals(0.0, odds["player3"]?.winChance)
        assertEquals(100.0, odds["player3"]?.tieChance)
    }

    @Test
    fun turnTwoPlayers() {
        val tableCards = mutableListOf(
            Card(13, Suit.CLUBS),
            Card(14, Suit.HEARTS),
            Card(12, Suit.DIAMONDS),
            Card(11, Suit.CLUBS)
        )
        val players = listOf(
            PlayerToSpectate(Player("player1", 3000, 200, true), listOf(Card(5, Suit.HEARTS), Card(9, Suit.HEARTS))),
            PlayerToSpectate(Player("player2", 3000, 200, true), listOf(Card(7, Suit.CLUBS), Card(2, Suit.CLUBS)))
        )

        val odds = OddsCalculator.calculateOdds(tableCards, players)

        assertEquals(36.36, odds["player1"]?.winChance)
        assertEquals(29.55, odds["player1"]?.tieChance)
        assertEquals(34.09, odds["player2"]?.winChance)
        assertEquals(29.55, odds["player2"]?.tieChance)
    }

    @Test
    fun turnThreePlayers() {
        val tableCards = mutableListOf(
            Card(5, Suit.HEARTS),
            Card(5, Suit.CLUBS),
            Card(9, Suit.HEARTS),
            Card(11, Suit.DIAMONDS)
        )
        val players = listOf(
            PlayerToSpectate(Player("player1", 3000, 200, true), listOf(Card(8, Suit.SPADES), Card(14, Suit.CLUBS))),
            PlayerToSpectate(Player("player2", 3000, 200, true), listOf(Card(4, Suit.SPADES), Card(13, Suit.SPADES))),
            PlayerToSpectate(Player("player3", 3000, 200, true), listOf(Card(7, Suit.CLUBS), Card(13, Suit.DIAMONDS)))
        )

        val odds = OddsCalculator.calculateOdds(tableCards, players)

        assertEquals(80.95, odds["player1"]?.winChance)
        assertEquals(0.0, odds["player1"]?.tieChance)
        assertEquals(7.14, odds["player2"]?.winChance)
        assertEquals(4.76, odds["player2"]?.tieChance)
        assertEquals(7.14, odds["player3"]?.winChance)
        assertEquals(4.76, odds["player3"]?.tieChance)
    }

    @Test
    fun turnFourPlayers() {
        val tableCards = mutableListOf(
            Card(11, Suit.DIAMONDS),
            Card(6, Suit.SPADES),
            Card(11, Suit.CLUBS),
            Card(13, Suit.SPADES)
        )
        val players = listOf(
            PlayerToSpectate(Player("player1", 3000, 200, true), listOf(Card(8, Suit.DIAMONDS), Card(13, Suit.CLUBS))),
            PlayerToSpectate(Player("player2", 3000, 200, true), listOf(Card(13, Suit.DIAMONDS), Card(4, Suit.HEARTS))),
            PlayerToSpectate(Player("player3", 3000, 200, true), listOf(Card(4, Suit.CLUBS), Card(9, Suit.HEARTS))),
            PlayerToSpectate(Player("player4", 3000, 200, true), listOf(Card(6, Suit.DIAMONDS), Card(14, Suit.SPADES)))
        )

        val odds = OddsCalculator.calculateOdds(tableCards, players)

        assertEquals(45.00, odds["player1"]?.winChance)
        assertEquals(42.50, odds["player1"]?.tieChance)
        assertEquals(0.0, odds["player2"]?.winChance)
        assertEquals(42.50, odds["player2"]?.tieChance)
        assertEquals(0.0, odds["player3"]?.winChance)
        assertEquals(0.0, odds["player3"]?.tieChance)
        assertEquals(12.50, odds["player4"]?.winChance)
        assertEquals(0.0, odds["player4"]?.tieChance)
    }

    @Test
    fun turnFivePlayers() {
        val tableCards = mutableListOf(
            Card(11, Suit.SPADES),
            Card(5, Suit.HEARTS),
            Card(4, Suit.SPADES),
            Card(11, Suit.DIAMONDS)
        )
        val players = listOf(
            PlayerToSpectate(Player("player1", 3000, 200, true), listOf(Card(8, Suit.SPADES), Card(13, Suit.DIAMONDS))),
            PlayerToSpectate(Player("player2", 3000, 200, true), listOf(Card(6, Suit.CLUBS), Card(11, Suit.HEARTS))),
            PlayerToSpectate(Player("player3", 3000, 200, true), listOf(Card(13, Suit.CLUBS), Card(6, Suit.DIAMONDS))),
            PlayerToSpectate(Player("player4", 3000, 200, true), listOf(Card(4, Suit.HEARTS), Card(5, Suit.CLUBS))),
            PlayerToSpectate(Player("player5", 3000, 200, true), listOf(Card(7, Suit.CLUBS), Card(6, Suit.HEARTS)))
        )

        val odds = OddsCalculator.calculateOdds(tableCards, players)

        assertEquals(0.0, odds["player1"]?.winChance)
        assertEquals(0.0, odds["player1"]?.tieChance)
        assertEquals(81.58, odds["player2"]?.winChance)
        assertEquals(0.0, odds["player2"]?.tieChance)
        assertEquals(0.0, odds["player3"]?.winChance)
        assertEquals(0.0, odds["player3"]?.tieChance)
        assertEquals(0.0, odds["player4"]?.winChance)
        assertEquals(0.0, odds["player4"]?.tieChance)
        assertEquals(18.42, odds["player5"]?.winChance)
        assertEquals(0.0, odds["player5"]?.tieChance)
    }

    @Test
    fun flopTwoPlayers() {
        val tableCards = mutableListOf(
            Card(13, Suit.CLUBS),
            Card(12, Suit.DIAMONDS),
            Card(11, Suit.CLUBS)
        )
        val players = listOf(
            PlayerToSpectate(Player("player1", 3000, 200, true), listOf(Card(5, Suit.HEARTS), Card(9, Suit.HEARTS))),
            PlayerToSpectate(Player("player2", 3000, 200, true), listOf(Card(7, Suit.CLUBS), Card(2, Suit.CLUBS)))
        )

        val odds = OddsCalculator.calculateOdds(tableCards, players)

        assertEquals(39.29, odds["player1"]?.winChance)
        assertEquals(8.28, odds["player1"]?.tieChance)
        assertEquals(52.42, odds["player2"]?.winChance)
        assertEquals(8.28, odds["player2"]?.tieChance)
    }
    
    @Test
    fun flopFivePlayers() {
        val tableCards = mutableListOf(
            Card(11, Suit.SPADES),
            Card(5, Suit.HEARTS),
            Card(4, Suit.SPADES)
        )
        val players = listOf(
            PlayerToSpectate(Player("player1", 3000, 200, true), listOf(Card(8, Suit.SPADES), Card(13, Suit.DIAMONDS))),
            PlayerToSpectate(Player("player2", 3000, 200, true), listOf(Card(6, Suit.CLUBS), Card(11, Suit.HEARTS))),
            PlayerToSpectate(Player("player3", 3000, 200, true), listOf(Card(13, Suit.CLUBS), Card(6, Suit.DIAMONDS))),
            PlayerToSpectate(Player("player4", 3000, 200, true), listOf(Card(4, Suit.HEARTS), Card(5, Suit.CLUBS))),
            PlayerToSpectate(Player("player5", 3000, 200, true), listOf(Card(7, Suit.CLUBS), Card(6, Suit.HEARTS)))
        )

        val odds = OddsCalculator.calculateOdds(tableCards, players)

        assertEquals(5.26, odds["player1"]?.winChance)
        assertEquals(0.0, odds["player1"]?.tieChance)
        assertEquals(14.30, odds["player2"]?.winChance)
        assertEquals(2.70, odds["player2"]?.tieChance)
        assertEquals(0.13, odds["player3"]?.winChance)
        assertEquals(2.70, odds["player3"]?.tieChance)
        assertEquals(51.69, odds["player4"]?.winChance)
        assertEquals(0.0, odds["player4"]?.tieChance)
        assertEquals(25.91, odds["player5"]?.winChance)
        assertEquals(2.70, odds["player5"]?.tieChance)
    }

    @Test
    fun preFlopTwoPlayers() {
        val players = listOf(
            PlayerToSpectate(Player("player1", 3000, 200, true), listOf(Card(13, Suit.HEARTS), Card(5, Suit.CLUBS))),
            PlayerToSpectate(Player("player2", 3000, 200, true), listOf(Card(11, Suit.DIAMONDS), Card(9, Suit.SPADES)))
        )

        val odds = OddsCalculator.calculateOdds(mutableListOf(), players)

        assertEquals(messageWin, 55.73, odds["player1"]?.winChance!!, 2.5)
        assertEquals(messageTie, 0.60, odds["player1"]?.tieChance!!, 1.2)
        assertEquals(messageWin, 43.67, odds["player2"]?.winChance!!, 2.5)
        assertEquals(messageTie, 0.60, odds["player2"]?.tieChance!!, 1.2)
    }

    @Test
    fun preFlopThreePlayers() {
        val players = listOf(
            PlayerToSpectate(Player("player1", 3000, 200, true), listOf(Card(13, Suit.SPADES), Card(2, Suit.DIAMONDS))),
            PlayerToSpectate(Player("player2", 3000, 200, true), listOf(Card(9, Suit.CLUBS), Card(9, Suit.HEARTS))),
            PlayerToSpectate(Player("player3", 3000, 200, true), listOf(Card(14, Suit.DIAMONDS), Card(11, Suit.DIAMONDS)))
        )

        val odds = OddsCalculator.calculateOdds(mutableListOf(), players)

        assertEquals(messageWin, 19.42, odds["player1"]?.winChance!!, 2.5)
        assertEquals(messageTie, 0.26, odds["player1"]?.tieChance!!, 1.0)
        assertEquals(messageWin, 40.83, odds["player2"]?.winChance!!, 2.5)
        assertEquals(messageTie, 0.26, odds["player2"]?.tieChance!!, 1.0)
        assertEquals(messageWin, 39.49, odds["player3"]?.winChance!!, 2.5)
        assertEquals(messageTie, 0.26, odds["player3"]?.tieChance!!, 1.0)
    }

    @Test
    fun preFlopFourPlayers() {
        val players = listOf(
            PlayerToSpectate(Player("player1", 3000, 200, true), listOf(Card(4, Suit.SPADES), Card(4, Suit.DIAMONDS))),
            PlayerToSpectate(Player("player2", 3000, 200, true), listOf(Card(7, Suit.SPADES), Card(7, Suit.DIAMONDS))),
            PlayerToSpectate(Player("player3", 3000, 200, true), listOf(Card(6, Suit.CLUBS), Card(8, Suit.SPADES))),
            PlayerToSpectate(Player("player4", 3000, 200, true), listOf(Card(7, Suit.CLUBS), Card(12, Suit.DIAMONDS)))
        )

        val odds = OddsCalculator.calculateOdds(mutableListOf(), players)

        assertEquals(messageWin, 19.40, odds["player1"]?.winChance!!, 2.5)
        assertEquals(messageTie, 0.60, odds["player1"]?.tieChance!!, 1.5)
        assertEquals(messageWin, 33.71, odds["player2"]?.winChance!!, 2.5)
        assertEquals(messageTie, 1.79, odds["player2"]?.tieChance!!, 1.5)
        assertEquals(messageWin, 21.25, odds["player3"]?.winChance!!, 2.5)
        assertEquals(messageTie, 0.60, odds["player3"]?.tieChance!!, 1.5)
        assertEquals(messageWin, 23.85, odds["player4"]?.winChance!!, 2.5)
        assertEquals(messageTie, 1.79, odds["player4"]?.tieChance!!, 1.5)
    }

    @Test
    fun preFlopFivePlayers() {
        val players = listOf(
            PlayerToSpectate(Player("player1", 3000, 200, true), listOf(Card(11, Suit.SPADES), Card(5, Suit.DIAMONDS))),
            PlayerToSpectate(Player("player2", 3000, 200, true), listOf(Card(6, Suit.CLUBS), Card(12, Suit.DIAMONDS))),
            PlayerToSpectate(Player("player3", 3000, 200, true), listOf(Card(5, Suit.CLUBS), Card(2, Suit.SPADES))),
            PlayerToSpectate(Player("player4", 3000, 200, true), listOf(Card(6, Suit.SPADES), Card(6, Suit.DIAMONDS))),
            PlayerToSpectate(Player("player5", 3000, 200, true), listOf(Card(13, Suit.SPADES), Card(14, Suit.DIAMONDS)))
        )

        val odds = OddsCalculator.calculateOdds(mutableListOf(), players)

        assertEquals(messageWin, 14.50, odds["player1"]?.winChance!!, 2.5)
        assertEquals(messageTie, 1.67, odds["player1"]?.tieChance!!, 1.5)
        assertEquals(messageWin, 14.01, odds["player2"]?.winChance!!, 2.5)
        assertEquals(messageTie, 1.88, odds["player2"]?.tieChance!!, 1.5)
        assertEquals(messageWin, 8.29, odds["player3"]?.winChance!!, 2.5)
        assertEquals(messageTie, 1.67, odds["player3"]?.tieChance!!, 1.5)
        assertEquals(messageWin, 19.59, odds["player4"]?.winChance!!, 2.5)
        assertEquals(messageTie, 1.88, odds["player4"]?.tieChance!!, 1.5)
        assertEquals(messageWin, 40.37, odds["player5"]?.winChance!!, 2.5)
        assertEquals(messageTie, 0.31, odds["player5"]?.tieChance!!, 1.5)
    }
}