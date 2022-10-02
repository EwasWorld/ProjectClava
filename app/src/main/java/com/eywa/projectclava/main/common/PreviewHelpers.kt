package com.eywa.projectclava.main.common

import com.eywa.projectclava.main.model.Court
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.MatchState
import com.eywa.projectclava.main.model.Player
import java.util.*

fun generatePlayers(count: Int): List<Player> {
    require(count > 0) { "Count should be > 0" }

    val defaultNames = listOf("James", "Jack", "Jason", "Julien", "Jeremy")
    val names = List(count) { defaultNames[it % defaultNames.size] }
    return names
            .mapIndexed { index, name ->
                Player(
                        name = name + " " + (index / defaultNames.size),
                        isPresent = index % 3 != 1,
                        lastPlayed = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 18)
                            set(Calendar.MINUTE, names.lastIndex - index)
                        },
                )
            }
            .shuffled()
}

fun generateCourts(count: Int) = List(count) { index ->
    Court(
            number = index + 1,
            canBeUsed = index % 3 != 2
    )
}.shuffled()

fun generateCourts(withMatchCount: Int = 0, availableCount: Int = 0): Iterable<Court>? {
    val totalToMake = (withMatchCount + availableCount).takeIf { it > 0 } ?: return null

    val courts = generateCourts(totalToMake).toMutableList()
    if (withMatchCount > 0) {
        val matches = generateMatches(withMatchCount)
        matches.forEach {
            courts.add(courts.removeFirst().copy(currentMatch = it))
        }
    }
    return courts.shuffled()
}

fun generateMatches(count: Int, finishingSoonThresholdSeconds: Int = 120): List<Match> {
    require(count > 0) { "Count should be > 0" }

    var allPlayers = generatePlayers(2 * (1 + count + count / 7))
    return List(count) { index ->
        val rollSeconds = when (index % 5) {
            // Finished
            1 -> -5
            // Finishing soon
            3 -> 20 + Random().nextInt(finishingSoonThresholdSeconds - 25)
            // Ongoing
            else -> finishingSoonThresholdSeconds + 1 + Random().nextInt(100)
        }
        val isPaused = index % 6 == 2

        val time = when {
            isPaused -> MatchState.Paused(Random().nextInt(600).toLong() + 20, Calendar.getInstance())
            else -> MatchState.InProgress(Calendar.getInstance().apply { add(Calendar.SECOND, rollSeconds) })
        }

        val players = allPlayers.take(if (index % 7 == 2) 4 else 2)
        allPlayers = allPlayers.drop(players.size)
        Match(
                players = players,
                state = time
        )
    }
}