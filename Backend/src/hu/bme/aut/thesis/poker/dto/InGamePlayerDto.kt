package hu.bme.aut.thesis.poker.dto

import hu.bme.aut.thesis.poker.gamemodel.Card
import hu.bme.aut.thesis.poker.gamemodel.Player
import hu.bme.aut.thesis.poker.network.GameStateMessage
import hu.bme.aut.thesis.poker.network.SpectatorGameStateMessage

/**
 * DTO (data transfer object) class for [GameStateMessage] about a [Player]
 * @author Bognar, Gabor Bela
 */
data class InGamePlayerDto(
    val userName: String,
    val chipStack: Int,
    val inPotThisRound: Int,
    val isInTurn: Boolean
)

/**
 * DTO (data transfer object) class for [SpectatorGameStateMessage] about a [Player]
 * @author Bognar, Gabor Bela
 */
data class PlayerToSpectateDto(
    val playerDto: InGamePlayerDto,
    val inHandCards: List<Card>
)