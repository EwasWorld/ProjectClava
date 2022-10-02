package com.eywa.projectclava.main.model

import com.eywa.projectclava.main.ui.sharedUi.SetupListItem
import java.util.*

data class Court(
        val number: Int,
        val canBeUsed: Boolean = true,
        val currentMatch: Match? = null,
) : SetupListItem {
    override val name: String
        get() = "Court $number"
    override val enabled: Boolean
        get() = canBeUsed

    fun isAvailable(currentTime: Calendar) =
            canBeUsed && (currentMatch == null || currentMatch.state.isFinished(currentTime))
}

fun Iterable<Court>.getPlayerStatesFromCourts() = this
        .mapNotNull { it.currentMatch }
        .getPlayerStates()

fun Iterable<Match>.getPlayerStates() = this
        .map { it.players.map { player -> player to it } }
        .flatten()
        .groupBy { it.first }
        .mapNotNull { (player, pairs) ->
            player.name to when (pairs.size) {
                0 -> null
                1 -> pairs.first().second
                // Take the value with the largest remaining time
                else -> pairs.map { it.second }.maxByOrNull { it.state }
            }
        }
        .toMap()

fun Map<String, MatchState?>.plus(other: Map<String, MatchState?>) {
    val newMap = this.toMutableMap()
    other.forEach { (key, value) ->
        if (newMap.containsKey(key)) {
            newMap[key] = listOf(value, newMap[key]).maxOf { it ?: MatchState.NoTime }
        }
        else {
            newMap[key] = value
        }
    }
}