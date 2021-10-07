package hu.bme.aut.onlab.poker.dto

import hu.bme.aut.onlab.poker.gamemodel.Card

data class InGamePlayerDto(
    val userName: String,
    val chipStack: Int,
    val inPotThisRound: Int,
    val isInTurn: Boolean
)

data class PlayerToSpectateDto(
    val playerDto: InGamePlayerDto,
    val inHandCards: List<Card>
)