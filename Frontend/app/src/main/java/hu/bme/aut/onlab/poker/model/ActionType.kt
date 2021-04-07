package hu.bme.aut.onlab.poker.model

import kotlinx.serialization.Serializable

@Serializable
enum class ActionType {
    FOLD,
    CHECK,
    CALL,
    RAISE;
}