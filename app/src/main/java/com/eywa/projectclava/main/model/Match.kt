package com.eywa.projectclava.main.model

import com.eywa.projectclava.main.database.match.DatabaseMatch
import com.eywa.projectclava.main.database.match.DatabaseMatchFull
import java.util.*
import java.util.concurrent.TimeUnit

fun Iterable<Match>.getPlayerStates() =
        map { it.players.map { player -> player to it } }
                .flatten()
                .groupBy { it.first.name }
                .mapNotNull { (_, pairs) ->
                    pairs.first().first.name to when (pairs.size) {
                        0 -> null
                        1 -> pairs.first().second
                        // Take the value with the largest remaining time
                        else -> pairs.map { it.second }.maxByOrNull { it.state }
                    }
                }
                .toMap()

fun Iterable<Match>.getCourtsInUse(currentTime: Calendar) =
        filter { it.isInProgress(currentTime) }.mapNotNull { it.court }

fun DatabaseMatchFull.asMatch() = Match(
        id = match.id,
        players = players.map { it.asPlayer() },
        state = when (match.stateType) {
            MatchState.Paused::class.simpleName -> MatchState.Paused(
                    remainingTimeSeconds = match.stateSecondsLeft!!,
                    matchPausedAt = match.stateDate
            )
            MatchState.InProgressOrComplete::class.simpleName -> MatchState.InProgressOrComplete(
                    matchEndTime = match.stateDate,
                    court = court!!.asCourt()
            )
            MatchState.NotStarted::class.simpleName -> MatchState.NotStarted(
                    match.stateDate
            )
            else -> throw IllegalStateException("Couldn't convert database match to match state")
        }
)

data class Match(
        val id: Int,
        val players: Iterable<Player>,
        val state: MatchState = MatchState.NotStarted(Calendar.getInstance()),
) {
    val isPaused
        get() = state is MatchState.Paused

    fun isInProgress(currentTime: Calendar) =
            state is MatchState.InProgressOrComplete && !state.isFinished(currentTime)

    /**
     * true if the match is paused or in progress
     */
    fun isCurrent(currentTime: Calendar) = isPaused || isInProgress(currentTime)

    val lastPlayedTime
        get() = when (state) {
            is MatchState.NotStarted -> null
            is MatchState.InProgressOrComplete -> state.matchEndTime
            is MatchState.Paused -> state.matchPausedAt
        }

    val court
        get() = if (state is MatchState.InProgressOrComplete) state.court else null

    fun asDatabaseMatch() = DatabaseMatch(
            id = id,
            stateType = state::class.simpleName!!,
            stateDate = when (state) {
                is MatchState.NotStarted -> state.createdAt
                is MatchState.InProgressOrComplete -> state.matchEndTime
                is MatchState.Paused -> state.matchPausedAt
            },
            stateSecondsLeft = state
                    .takeIf { it is MatchState.Paused }
                    ?.let { (it as MatchState.Paused).remainingTimeSeconds },
            courtId = state
                    .takeIf { it is MatchState.InProgressOrComplete }
                    ?.let { (it as MatchState.InProgressOrComplete).court.id },
    )

    fun startMatch(currentTime: Calendar, court: Court, duration: Int? = null): Match {
        check(state is MatchState.NotStarted || state is MatchState.Paused) {
            "Match cannot be started - incorrect state"
        }
        require(state is MatchState.Paused || duration != null) {
            "Match cannot be started - no duration given"
        }

        return copy(
                state = MatchState.InProgressOrComplete(
                        matchEndTime = (currentTime.clone() as Calendar).apply {
                            add(Calendar.SECOND, duration ?: (state as MatchState.Paused).remainingTimeSeconds.toInt())
                        },
                        court = court,
                )
        )
    }
}

data class TimeRemaining(
        val minutes: Int,
        val seconds: Int,
) : Comparable<TimeRemaining> {
    constructor(totalSeconds: Long) : this((totalSeconds / 60L).toInt(), (totalSeconds % 60L).toInt())

    private val totalTimeInSeconds: Int
        get() = minutes * 60 + seconds

    fun isEndingSoon(minutesThreshold: Int = 2) =
            minutes < minutesThreshold || (minutes == minutesThreshold && seconds == 0)

    override fun compareTo(other: TimeRemaining) = totalTimeInSeconds.compareTo(other.totalTimeInSeconds)
}

sealed class MatchState : Comparable<MatchState> {
    private val order = listOf(NotStarted::class, Paused::class, InProgressOrComplete::class)

    abstract fun getTimeLeft(currentTime: Calendar): TimeRemaining?
    abstract fun isFinished(currentTime: Calendar): Boolean

    fun compareClass(other: MatchState) =
            order.indexOfFirst { it.isInstance(this) }.compareTo(order.indexOfFirst { it.isInstance(other) })

    data class NotStarted(
            val createdAt: Calendar
    ) : MatchState() {
        override fun getTimeLeft(currentTime: Calendar): TimeRemaining? = null

        override fun compareTo(other: MatchState): Int {
            if (other is NotStarted) return createdAt.compareTo(other.createdAt)
            return compareClass(other)
        }

        override fun isFinished(currentTime: Calendar): Boolean = false
    }

    data class InProgressOrComplete(
            val matchEndTime: Calendar,
            val court: Court,
    ) : MatchState() {
        override fun getTimeLeft(currentTime: Calendar): TimeRemaining? {
            if (matchEndTime.before(currentTime)) return null

            val difference = matchEndTime.timeInMillis - currentTime.timeInMillis
            if (difference <= 0) return null

            return TimeRemaining(TimeUnit.MILLISECONDS.toSeconds(difference))
        }

        override fun compareTo(other: MatchState): Int {
            val classComparison = compareClass(other)
            if (classComparison != 0) return classComparison
            other as InProgressOrComplete

            return matchEndTime.compareTo(other.matchEndTime)
        }

        override fun isFinished(currentTime: Calendar): Boolean = matchEndTime.before(currentTime)
    }

    data class Paused(
            val remainingTimeSeconds: Long,
            val matchPausedAt: Calendar
    ) : MatchState() {
        override fun getTimeLeft(currentTime: Calendar): TimeRemaining = TimeRemaining(remainingTimeSeconds)

        override fun compareTo(other: MatchState): Int {
            val classComparison = compareClass(other)
            if (classComparison != 0) return classComparison
            other as Paused

            return remainingTimeSeconds.compareTo(other.remainingTimeSeconds)
        }

        override fun isFinished(currentTime: Calendar): Boolean = false
    }
}