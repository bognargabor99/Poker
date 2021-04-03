package hu.bme.aut.onlab.poker.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlayerDto(
    val userName: String,
    val chipStack: Int,
    val inPotThisRound: Int
)