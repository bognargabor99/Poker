package hu.bme.aut.onlab.poker.dto

import hu.bme.aut.onlab.poker.model.Card
import hu.bme.aut.onlab.poker.model.Player

data class PlayerToSpectate(
    val playerDto: Player,
    val inHandCards: List<Card>
)