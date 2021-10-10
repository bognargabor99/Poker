package hu.bme.aut.onlab.poker.gamemodel

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DelicateCoroutinesApi
class TableTest {
    @Test
    fun checkRulesTest() {
        val table = Table(TableRules(
            isOpen = false,
            playerCount = 3,
            bigBlindStartingAmount = 60,
            doubleBlindsAfterTurnCount = 3,
            playerStartingStack = 5000,
            isRoyal = false
        ))
        assertEquals(false, table.isOpen())
        assertEquals(3, table.rules.playerCount)
        assertEquals(60, table.rules.bigBlindStartingAmount)
        assertEquals(3, table.rules.doubleBlindsAfterTurnCount)
        assertEquals(5000, table.rules.playerStartingStack)
        assertEquals(false, table.rules.isRoyal)
    }

    @Test
    fun addMorePlayersThanAvailableSeatsTest() {
        val playerCount = Random.nextInt(2, 10)
        val table = Table(TableRules(
            isOpen = false,
            playerCount = playerCount,
            bigBlindStartingAmount = 60,
            doubleBlindsAfterTurnCount = 3,
            playerStartingStack = 5000,
            isRoyal = false
        ))
        for (i in 1..playerCount)
            assertTrue(table.addInGamePlayer("guest$i"))
        assertFalse(table.addInGamePlayer("guest${ playerCount + 1 }"))
    }

    @Test
    fun addPersonAsPlayerAndSpectatorTest() {
        val table = Table(TableRules(
            isOpen = false,
            playerCount = 3,
            bigBlindStartingAmount = 60,
            doubleBlindsAfterTurnCount = 3,
            playerStartingStack = 5000,
            isRoyal = false
        ))
        assertTrue(table.addInGamePlayer("guest1"))
        assertFalse(table.addSpectator("guest1"))
        assertTrue(table.addSpectator("guest2"))
        assertFalse(table.addInGamePlayer("guest2"))
    }
}