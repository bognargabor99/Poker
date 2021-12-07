package hu.bme.aut.thesis.poker.gamemodel

import kotlin.test.*

class TableRulesTest {
    @Test
    fun createTableRulesSuccessTest() {
        val rules = TableRules(
            isOpen = true,
            playerCount = 3,
            bigBlindStartingAmount = 80,
            doubleBlindsAfterTurnCount = 4,
            playerStartingStack =  4000,
            isRoyal = false
        )
        assertEquals(true, rules.isOpen)
        assertEquals(3, rules.playerCount)
        assertEquals(80, rules.bigBlindStartingAmount)
        assertEquals(4, rules.doubleBlindsAfterTurnCount)
        assertEquals(4000, rules.playerStartingStack)
        assertEquals(false, rules.isRoyal)
    }

    @Test
    fun createTableRulesFailureTest() {
        assertFailsWith<IllegalArgumentException> {
            TableRules(isOpen = true,
                playerCount = 1,
                bigBlindStartingAmount = 80,
                doubleBlindsAfterTurnCount = 4,
                4000,
                false)
        }
    }

    @Test
    fun defaultTableRulesTest() {
        val rules = TableRules.defaultRules
        assertEquals(true, rules.isOpen)
        assertEquals(2, rules.playerCount)
        assertEquals(80, rules.bigBlindStartingAmount)
        assertEquals(6, rules.doubleBlindsAfterTurnCount)
        assertEquals(3000, rules.playerStartingStack)
        assertEquals(false, rules.isRoyal)
    }
}