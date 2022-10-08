package com.eywa.projectclava.main.model

import com.eywa.projectclava.main.database.match.DatabaseMatch
import com.eywa.projectclava.main.database.match.DatabaseMatchFull
import java.util.*
import java.util.concurrent.TimeUnit

fun Iterable<Match>.getPlayerMatches() =
        map { it.players.map { player -> player to it } }
                .flatten()
                .groupBy { it.first.name }
                .map { (_, pairs) -> pairs.first().first.name to pairs.map { it.second } }
                .toMap()

fun Iterable<Match>.getPlayerStates() =
        getPlayerMatches()
                .mapValues { (_, matches) ->
                    when (matches.size) {
                        0 -> null
                        1 -> matches.first()
                        // Take the value with the largest remaining time
                        else -> matches.maxByOrNull { it.state }
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

fun Iterable<Match>.getCourtsInUse() = filter { it.isInProgress }.map { it.court!! }

fun Iterable<Match>.getNextMatchToFinish() = filter { it.isInProgress }.minByOrNull { it.state }

fun Iterable<Match>.getLatestMatchForCourt(court: Court) =
        filter { it.court?.name == court.name }.getLatestFinishingMatch()

fun Iterable<Match>.getLatestFinishingMatch() = maxByOrNull { it.state }

fun DatabaseMatchFull.asMatch(currentTime: Calendar) = Match(
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
            MatchState.InProgressOrComplete::class.simpleName -> {
                if (match.stateDate.before(currentTime)) {
                    MatchState.OnCourt(
                            matchEndTime = match.stateDate,
                            court = court!!.asCourt(),
                    )
                }
                else {
                    MatchState.Completed(match.stateDate)
                }
            }
            else -> throw IllegalStateException("Couldn't convert database match to match state")
        }
)

data class Match(
        val id: Int,
        val players: Iterable<Player>,
        val state: MatchState = MatchState.NotStarted(Calendar.getInstance(Locale.getDefault())),
) {
    val isPaused
        get() = state is MatchState.Paused

    val isInProgress
        get() = state is MatchState.OnCourt

    val isFinished
        get() = state is MatchState.Completed

    /**
     * true if the match is paused, in progress, or overrunning
     */
    val isCurrent
        get() = isPaused || isInProgress

    fun containsPlayer(player: Player) = players.any { it.name == player.name }

    fun getFinishTime() = when (state) {
        is MatchState.Completed -> state.matchEndTime
        is MatchState.OnCourt -> state.matchEndTime
        else -> null
    }

    fun getLastPlayedTime(currentTime: Calendar) = when (state) {
        is MatchState.NotStarted -> null
        is MatchState.OnCourt -> if (state.matchEndTime.before(currentTime)) currentTime else state.matchEndTime
        is MatchState.Completed -> state.matchEndTime
        is MatchState.Paused -> state.matchPausedAt
        is MatchState.InProgressOrComplete -> throw NotImplementedError()
    }

    val court
        get() = if (state is MatchState.OnCourt) state.court else null

    fun asDatabaseMatch() = DatabaseMatch(
            id = id,
            stateType = state::class.simpleName!!,
            stateDate = when (state) {
                is MatchState.NotStarted -> state.createdAt
                is MatchState.OnCourt -> state.matchEndTime
                is MatchState.Completed -> state.matchEndTime
                is MatchState.Paused -> state.matchPausedAt
                is MatchState.InProgressOrComplete -> throw NotImplementedError()
            },
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
                        remainingTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(
                                currentTime.timeInMillis - state.matchEndTime.timeInMillis
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
     * If the match is finished, will transform to a paused state with [timeToAdd] remaining
     */
    fun addTime(currentTime: Calendar, timeToAdd: Int) = when (state) {
        is MatchState.Paused -> copy(
                state = state.copy(remainingTimeSeconds = state.remainingTimeSeconds + timeToAdd)
        )
        is MatchState.OnCourt -> {
            copy(
                    state = if (!state.isFinished(currentTime)) {
                        state.copy(
                                matchEndTime = (state.matchEndTime.clone() as Calendar)
                                        .apply { add(Calendar.SECOND, timeToAdd) }
                        )
                    }
                    else {
                        MatchState.Paused(
                                matchPausedAt = state.matchEndTime,
                                remainingTimeSeconds = timeToAdd.toLong(),
                        )
                    }
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
    constructor(totalSeconds: Long) : this((totalSeconds / 60L).toInt(), (totalSeconds % 60L).toInt())

    private val totalTimeInSeconds: Int
        get() = minutes * 60 + seconds

    val isNegative
        get() = totalTimeInSeconds < 0

    fun isEndingSoon(minutesThreshold: Int = 2) =
            minutes < minutesThreshold || (minutes == minutesThreshold && seconds == 0)

    override fun compareTo(other: TimeRemaining) = totalTimeInSeconds.compareTo(other.totalTimeInSeconds)
}

sealed class MatchState : Comparable<MatchState> {
    private val order = listOf(NotStarted::class, Paused::class, OnCourt::class, Completed::class)

    /**
     * Can be negative
     */
    abstract fun getTimeLeft(currentTime: Calendar): TimeRemaining?
    abstract fun isFinished(currentTime: Calendar): Boolean
    open fun getFinishedTime(): Calendar? = null

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

    // TODO Delete InProgressOrComplete
    @Deprecated("Use OnCourt instead, keeping for database conversions")
    data class InProgressOrComplete(
            val matchEndTime: Calendar,
            val court: Court,
    ) : MatchState() {
        override fun compareTo(other: MatchState) = throw NotImplementedError()
        override fun getTimeLeft(currentTime: Calendar) = throw NotImplementedError()
        override fun isFinished(currentTime: Calendar) = throw NotImplementedError()
    }

    data class OnCourt(
            val matchEndTime: Calendar,
            val court: Court,
    ) : MatchState() {
        override fun getTimeLeft(currentTime: Calendar): TimeRemaining? {
            return TimeRemaining(
                    TimeUnit.MILLISECONDS.toSeconds(
                            matchEndTime.timeInMillis - currentTime.timeInMillis
                    )
            )
        }

        override fun compareTo(other: MatchState): Int {
            val classComparison = compareClass(other)
            if (classComparison != 0) return classComparison
            other as OnCourt

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

    data class Completed(
            val matchEndTime: Calendar,
    ) : MatchState() {
        override fun getTimeLeft(currentTime: Calendar): TimeRemaining? = null

        override fun compareTo(other: MatchState) =
                when (other) {
                    is Completed -> other.matchEndTime
                    is OnCourt -> other.matchEndTime
                    else -> null
                }?.let { matchEndTime.compareTo(it) }
                        ?: compareClass(other)

        override fun isFinished(currentTime: Calendar): Boolean = true

        override fun getFinishedTime() = matchEndTime
    }
}