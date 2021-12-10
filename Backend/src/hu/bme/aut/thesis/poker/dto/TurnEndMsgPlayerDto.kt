package hu.bme.aut.thesis.poker.dto

import hu.bme.aut.thesis.poker.gamemodel.Card
import hu.bme.aut.thesis.poker.gamemodel.Hand
import hu.bme.aut.thesis.poker.network.TurnEndMessage

/**
 * DTO (data transfer object) class for [TurnEndMessage]
 * about necessary data of players at the end of a turn
 * @author Bognar, Gabor Bela
 */
data class TurnEndMsgPlayerDto(
    val userName: String,
    val inHandCards: List<Card>?,
    val hand: Hand?,
    val winAmount: Int
)