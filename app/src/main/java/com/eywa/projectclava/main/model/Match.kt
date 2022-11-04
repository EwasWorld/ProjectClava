package com.eywa.projectclava.main.model

import com.eywa.projectclava.main.database.match.DatabaseMatch
import com.eywa.projectclava.main.database.match.DatabaseMatchFull
import com.eywa.projectclava.main.features.ui.editNameDialog.NamedItem
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * Map player to their matches
 */
fun Iterable<Match>.getPlayerMatches() =
        flatMap { it.players.map { player -> player to it } }
                .groupBy { it.first.name }
                .map { (_, pairs) -> pairs.first().first.name to pairs.map { it.second } }
                .toMap()

/**
 * Gets the status of a player (whether they're on court, queued, etc.)
 */
fun Iterable<Match>.getPlayerStatus() =
        getPlayerMatches()
                .mapValues { (_, matches) ->
                    val unfinishedMatches = matches.filter { !it.isFinished }

                    when (unfinishedMatches.size) {
                        0 -> null
                        1 -> unfinishedMatches.first()
                        // Take the value with the largest remaining time
                        else -> unfinishedMatches.maxByOrNull { it.state }
                    }
                }
                .toMap()

/**
 * Returns max of ([Match.isCurrent] else [MatchState.NotStarted] else any)
 */
fun Iterable<Match>.getPlayerColouringMatch() = (
        filter { it.isCurrent }.takeIf { it.isNotEmpty() }
                ?: filter { it.state is MatchState.NotStarted }.takeIf { it.isNotEmpty() }
                ?: this
        ).maxByOrNull { it.state }

fun Iterable<Match>.getCourtsInUse() = filter { it.isOnCourt }.map { it.court!! }

fun Iterable<Match>.getLatestMatchForCourt(court: Court) =
        filter { it.court?.name == court.name }.getLatestFinishingMatch()

fun Iterable<Match>.getLatestFinishingMatch() = maxByOrNull { it.state }

fun DatabaseMatchFull.asMatch() = Match(
        id = match.id,
        players = players.map { it.asPlayer() },
        state = when (match.stateType) {
            MatchState.Paused::class.simpleName -> MatchState.Paused(
                    remainingTimeSeconds = match.stateSecondsLeft!!,
                    matchPausedAt = match.stateDate
            )
            MatchState.OnCourt::class.simpleName -> MatchState.OnCourt(
                    matchEndTime = match.stateDate,
                    court = court!!.asCourt(),
            )
            MatchState.NotStarted::class.simpleName -> MatchState.NotStarted(match.stateDate)
            MatchState.Completed::class.simpleName -> MatchState.Completed(match.stateDate)
            else -> throw IllegalStateException("Couldn't convert database match to match state")
        }
)

data class Match(
        val id: Int,
        val players: Iterable<Player>,
        val state: MatchState,
) : NamedItem {
    override val name: String
        get() = when {
            court != null -> "match on court ${court!!.name}"
            players.any() -> "match between " + playerNameString()
            else -> "match"
        }

    val isNotStarted
        get() = state is MatchState.NotStarted

    val isPaused
        get() = state is MatchState.Paused

    val isOnCourt
        get() = state is MatchState.OnCourt

    val isFinished
        get() = state is MatchState.Completed

    /**
     * true if the match is paused, in progress, or overrunning
     */
    val isCurrent
        get() = isPaused || isOnCourt

    /**
     * Gets the time associated with this match.
     * If the match is
     * - complete: returns the time it ended
     * - on court: returns when it will end
     * - paused: returns when it was paused
     * - not started: returns when it was created
     */
    fun getTime() = when (state) {
        is MatchState.Completed -> state.matchEndTime
        is MatchState.OnCourt -> state.matchEndTime
        is MatchState.Paused -> state.matchPausedAt
        is MatchState.NotStarted -> state.createdAt
    }

    fun playerNameString() = when {
        players.none() -> "No player data"
        else -> players.sortedBy { it.name }.joinToString(limit = 10) { it.name }
    }

    val court
        get() = if (state is MatchState.OnCourt) state.court else null

    fun asDatabaseMatch() = DatabaseMatch(
            id = id,
            stateType = state::class.simpleName!!,
            stateDate = getTime(),
            stateSecondsLeft = state
                    .takeIf { it is MatchState.Paused }
                    ?.let { (it as MatchState.Paused).remainingTimeSeconds },
            courtId = state
                    .takeIf { it is MatchState.OnCourt }
                    ?.let { (it as MatchState.OnCourt).court.id },
    )

    fun startMatch(currentTime: Calendar, court: Court, duration: Int? = null): Match {
        check(state is MatchState.NotStarted || state is MatchState.Paused) {
            "Match cannot be started - incorrect state"
        }
        require(state is MatchState.Paused || duration != null) {
            "Match cannot be started - no duration given"
        }

        return copy(
                state = MatchState.OnCourt(
                        matchEndTime = (currentTime.clone() as Calendar).apply {
                            add(Calendar.SECOND, duration ?: (state as MatchState.Paused).remainingTimeSeconds.toInt())
                        },
                        court = court,
                )
        )
    }

    fun completeMatch(currentTime: Calendar) = copy(
            state = MatchState.Completed(
                    matchEndTime = if (state is MatchState.Paused) state.matchPausedAt else currentTime,
            )
    )

    fun pauseMatch(currentTime: Calendar): Match {
        if (isPaused) return this

        check(state is MatchState.OnCourt) { "Match is not in progress" }

        return copy(
                state = MatchState.Paused(
                        remainingTimeSeconds = abs(
                                TimeUnit.MILLISECONDS.toSeconds(
                                        currentTime.timeInMillis - state.matchEndTime.timeInMillis
                                )
                        ),
                        matchPausedAt = currentTime,
                )
        )
    }

    fun resumeMatch(currentTime: Calendar, court: Court, resumeTime: Int): Match {
        if (!isPaused) return this
        state as MatchState.Paused

        return copy(
                state = MatchState.OnCourt(
                        matchEndTime = (currentTime.clone() as Calendar).apply { add(Calendar.SECOND, resumeTime) },
                        court = court,
                )
        )
    }

    /**
     * If the match is overrunning, will add the time to the current time rather than the matchEndTime.
     * If the match is complete, it will become paused with [timeToAdd] seconds remaining.
     */
    fun addTime(currentTime: Calendar, timeToAdd: Int) = when (state) {
        is MatchState.Paused -> copy(
                state = state.copy(remainingTimeSeconds = state.remainingTimeSeconds + timeToAdd)
        )
        is MatchState.OnCourt -> {
            val initialTime = if (state.matchEndTime.after(currentTime)) state.matchEndTime else currentTime
            copy(
                    state = state.copy(
                            matchEndTime = (initialTime.clone() as Calendar)
                                    .apply { add(Calendar.SECOND, timeToAdd) }
                    )
            )
        }
        is MatchState.Completed -> copy(
                state = MatchState.Paused(
                        matchPausedAt = state.matchEndTime,
                        remainingTimeSeconds = timeToAdd.toLong(),
                )
        )
        else -> this
    }

    fun changeCourt(court: Court): Match {
        if (state !is MatchState.OnCourt) return this
        return copy(state = state.copy(court = court))
    }
}

data class TimeRemaining(
        val minutes: Int,
        val seconds: Int,
) : Comparable<TimeRemaining> {
    constructor(totalSeconds: Int) : this((totalSeconds / 60L).toInt(), (totalSeconds % 60L).toInt())
    constructor(totalSeconds: Long) : this(totalSeconds.toInt())

    private val totalTimeInSeconds: Int
        get() = minutes * 60 + seconds

    val isNegative
        get() = totalTimeInSeconds < 0

    fun isEndingSoon(secondsThreshold: Int = 2 * 60) = totalTimeInSeconds < secondsThreshold

    override fun compareTo(other: TimeRemaining) = totalTimeInSeconds.compareTo(other.totalTimeInSeconds)
}

sealed class MatchState : Comparable<MatchState> {
    private val order = listOf(NotStarted::class, Paused::class, OnCourt::class, Completed::class)

    /**
     * Can be negative
     */
    open fun getTimeLeft(currentTime: Calendar?): TimeRemaining? = null

    override fun compareTo(other: MatchState): Int {
        val classComparison = order.indexOfFirst { it.isInstance(this) }
                .compareTo(order.indexOfFirst { it.isInstance(other) })
        if (classComparison != 0) return classComparison
        return sameClassCompare(other)
    }

    /**
     * Compare two [MatchState]s who have the same subclass
     * @throws ClassCastException if [other] is not the same [MatchState] subtype as this
     */
    abstract fun sameClassCompare(other: MatchState): Int

    data class NotStarted(
            val createdAt: Calendar
    ) : MatchState() {
        override fun sameClassCompare(other: MatchState): Int {
            other as NotStarted
            return createdAt.compareTo(other.createdAt)
        }
    }

    data class OnCourt(
            val matchEndTime: Calendar,
            val court: Court,
    ) : MatchState() {
        override fun getTimeLeft(currentTime: Calendar?): TimeRemaining {
            return TimeRemaining(
                    TimeUnit.MILLISECONDS.toSeconds(
                            matchEndTime.timeInMillis - currentTime!!.timeInMillis
                    )
            )
        }

        override fun sameClassCompare(other: MatchState): Int {
            other as OnCourt
            return matchEndTime.compareTo(other.matchEndTime)
        }
    }

    data class Paused(
            val remainingTimeSeconds: Long,
            val matchPausedAt: Calendar
    ) : MatchState() {
        override fun getTimeLeft(currentTime: Calendar?): TimeRemaining = TimeRemaining(remainingTimeSeconds)

        override fun sameClassCompare(other: MatchState): Int {
            other as Paused
            return remainingTimeSeconds.compareTo(other.remainingTimeSeconds)
        }
    }

    data class Completed(
            val matchEndTime: Calendar,
    ) : MatchState() {
        override fun sameClassCompare(other: MatchState): Int {
            other as Completed
            return matchEndTime.compareTo(other.matchEndTime)
        }
    }
}