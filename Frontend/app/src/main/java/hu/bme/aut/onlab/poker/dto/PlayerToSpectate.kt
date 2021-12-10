package hu.bme.aut.onlab.poker.dto

import hu.bme.aut.onlab.poker.model.Card
import hu.bme.aut.onlab.poker.model.Player
import hu.bme.aut.onlab.poker.network.SpectatorGameStateMessage

/**
 * DTO (data transfer object) class for [SpectatorGameStateMessage] about a [Player]
 * @author Bognar, Gabor Bela
 */
data class PlayerToSpectate(
    val playerDto: Player,
    val inHandCards: List<Card>
)