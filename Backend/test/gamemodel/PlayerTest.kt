package hu.bme.aut.thesis.poker.gamemodel

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class PlayerTest {
    @Test
    fun creationTest() {
        val player = Player(3000)
        assertEquals(3000, player.chipStack)
    }

    @Test
    fun handCardsTest() {
        val player = Player(3000)
        assertEquals(mutableListOf(), player.inHandCards)
        player.handCards(listOf(Card(14, Suit.HEARTS), Card(10, Suit.CLUBS)))
        assertContains(player.inHandCards, Card(14, Suit.HEARTS))
        assertContains(player.inHandCards, Card(10, Suit.CLUBS))
    }

    @Test
    fun nextRoundTest() {
        val player = Player(3000)

        player.putInPot(200)
        player.actedThisRound = true
        assertEquals(2800, player.chipStack)
        assertEquals(200, player.inPot)
        assertEquals(200, player.inPotThisRound)

        player.nextRound(TurnState.AFTER_FLOP)
        assertEquals(false, player.actedThisRound)
        assertEquals(2800, player.chipStack)
        assertEquals(200, player.inPot)
        assertEquals(0, player.inPotThisRound)

        player.putInPot(500)
        assertEquals(2300, player.chipStack)
        assertEquals(700, player.inPot)
        assertEquals(500, player.inPotThisRound)

        player.nextRound(TurnState.AFTER_TURN)
        assertEquals(2300, player.chipStack)
        assertEquals(700, player.inPot)
        assertEquals(0, player.inPotThisRound)
    }

    @Test
    fun foldTest() {
        val player = Player(3000)
        player.fold()
        assertEquals(false, player.isInTurn)
    }

    @Test
    fun newTurnTest() {
        val player = Player(3000)

        player.putInPot(500)
        assertEquals(2500, player.chipStack)
        assertEquals(500, player.inPot)
        assertEquals(500, player.inPotThisRound)

        player.nextRound(TurnState.AFTER_FLOP)
        player.actedThisRound = true
        player.newTurn()
        assertEquals(false, player.actedThisRound)
        assertEquals(2500, player.chipStack)
        assertEquals(0, player.inPot)
        assertEquals(0, player.inPotThisRound)

    }

    @Test
    fun putMoreThanAllin() {
        val player = Player(3000)

        player.putInPot(500)
        assertEquals(2500, player.chipStack)
        assertEquals(500, player.inPot)
        assertEquals(500, player.inPotThisRound)

        player.nextRound(TurnState.AFTER_FLOP)
        player.putInPot(3000)
        assertEquals(0, player.chipStack)
        assertEquals(3000, player.inPot)
        assertEquals(2500, player.inPotThisRound)
    }
}