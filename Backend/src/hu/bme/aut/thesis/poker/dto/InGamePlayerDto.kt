package hu.bme.aut.thesis.poker.dto

import hu.bme.aut.thesis.poker.gamemodel.Card

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