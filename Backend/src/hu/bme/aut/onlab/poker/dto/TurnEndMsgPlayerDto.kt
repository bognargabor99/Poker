package hu.bme.aut.onlab.poker.dto

import hu.bme.aut.onlab.poker.gamemodel.Card
import hu.bme.aut.onlab.poker.gamemodel.Hand

data class TurnEndMsgPlayerDto(
    val userName: String,
    val inHandCards: List<Card>?,
    val hand: Hand?,
    val winAmount: Int
)