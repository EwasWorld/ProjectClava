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

fun Iterable<Court>.getPlayerStates() = this
        .mapNotNull { it.currentMatch?.players?.map { player -> player to it.currentMatch.state } }
        .flatten()
        .groupBy { it.first }
        .mapNotNull { (player, pairs) ->
            player.name to when (pairs.size) {
                0 -> null
                1 -> pairs.first().second
                // Take the value with the largest remaining time
                else -> pairs.map { it.second }.maxOf { it }
            }
        }
        .toMap()
