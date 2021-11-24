package hu.bme.aut.thesis.poker.dto

import hu.bme.aut.thesis.poker.gamemodel.Card
import hu.bme.aut.thesis.poker.gamemodel.Hand

data class TurnEndMsgPlayerDto(
    val userName: String,
    val inHandCards: List<Card>?,
    val hand: Hand?,
    val winAmount: Int
)