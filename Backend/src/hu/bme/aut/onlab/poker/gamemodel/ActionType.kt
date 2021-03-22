package hu.bme.aut.onlab.poker.gamemodel

import kotlinx.serialization.Serializable

@Serializable
enum class ActionType {
    FOLD,
    CHECK,
    CALL,
    RAISE,
    BET;
}