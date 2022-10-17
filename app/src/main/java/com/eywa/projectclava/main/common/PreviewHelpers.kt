package com.eywa.projectclava.main.common

import com.eywa.projectclava.main.model.*
import java.util.*

fun generatePlayers(count: Int): List<Player> {
    require(count > 0) { "Count should be > 0" }

    val defaultNames = listOf("James", "Jack", "Jason", "Julien", "Jeremy")
    val defaultSurnames = 'A'..'Z'
    val names = List(count) { defaultNames[it % defaultNames.size] }
    return names
            .mapIndexed { index, name ->
                Player(
                        id = 1,
                        name = name + " " + defaultSurnames.elementAt(index % defaultSurnames.count()),
                        isPresent = index % 3 != 1,
                )
            }
            .shuffled()
}

fun generateCourt(courtNumber: Int, courtEnabled: Boolean = true) = Court(1, courtNumber.toString(), courtEnabled)

fun generateCourts(count: Int) = List(count) { generateCourt(it + 1, it % 6 != 5) }.shuffled()

fun generateMatchTimeRemaining(
        matches: List<Match>,
        currentTime: Calendar,
): Map<Int, TimeRemaining?> = matches.associate { it.id to it.state.getTimeLeft(currentTime) }

fun generateMatches(
        count: Int,
        currentTime: Calendar,
        forceState: GeneratableMatchState? = null,
        finishingSoonThresholdSeconds: Int = 120
): List<Match> {
    require(count > 0) { "Count should be > 0" }

    val states = GeneratableMatchState.values().plus(GeneratableMatchState.NOT_STARTED)
    var allPlayers = generatePlayers(2 * (count + count / 7 + if (count % 7 >= 2) 1 else 0))
    var courtNumber = 1
    return List(count) { index ->
        val players = allPlayers.take(if (index % 7 == 2) 4 else 2)
        allPlayers = allPlayers.drop(players.size)
        val state = forceState ?: states[index % states.size]

        val court = if (state.needsCourt) generateCourt(courtNumber++) else null
        Match(
                id = 1,
                players = players,
                state = generateMatchState(
                        type = state,
                        court = court,
                        currentTime = currentTime,
                        finishingSoonThresholdSeconds = finishingSoonThresholdSeconds
                )
        )
    }
}

fun generateMatchState(
        type: GeneratableMatchState,
        court: Court?,
        currentTime: Calendar,
        finishingSoonThresholdSeconds: Int = 120,
): MatchState {
    val time = when (type) {
        GeneratableMatchState.COMPLETE -> -5
        GeneratableMatchState.FINISHING_SOON -> (20 + Random().nextInt(finishingSoonThresholdSeconds - 25))
                .coerceIn(20 until finishingSoonThresholdSeconds)
        GeneratableMatchState.IN_PROGRESS -> finishingSoonThresholdSeconds + 1 + Random().nextInt(100)
        else -> null
    }?.let { (currentTime.clone() as Calendar).apply { add(Calendar.SECOND, it) } }

    return when (type) {
        GeneratableMatchState.NOT_STARTED -> MatchState.NotStarted(currentTime)
        GeneratableMatchState.PAUSED -> MatchState.Paused(Random().nextInt(60 * 10).toLong() + 20, currentTime)
        GeneratableMatchState.COMPLETE -> MatchState.Completed(time!!)
        else -> {
            MatchState.OnCourt(
                    matchEndTime = time!!,
                    court = court!!
            )
        }
    }
}

enum class GeneratableMatchState(val needsCourt: Boolean = true) {
    NOT_STARTED(false), IN_PROGRESS, FINISHING_SOON, COMPLETE, PAUSED(false)
}