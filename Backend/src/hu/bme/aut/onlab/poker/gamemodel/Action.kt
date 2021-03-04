package hu.bme.aut.onlab.poker.gamemodel

import kotlinx.serialization.Serializable

@Serializable
enum class Action {
    FOLD,
    CHECK,
    CALL,
    RAISE,
    RE_RAISE,
    BET;
}