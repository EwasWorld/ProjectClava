package com.eywa.projectclava.main.model

import java.util.*
import java.util.concurrent.TimeUnit

data class Match(
        val players: Iterable<Player>,
        val state: MatchState = MatchState.NoTime,
) {
    val isPaused
        get() = state is MatchState.Paused

    fun isCourtAvailable(currentTime: Calendar) = state is MatchState.Paused || state.isFinished(currentTime)

    val lastPlayedTime
        get() = when (state) {
            MatchState.NoTime -> null
            is MatchState.InProgress -> state.endTime
            is MatchState.Paused -> state.pausedAt
        }
}

data class TimeRemaining(
        val minutes: Int,
        val seconds: Int,
) : Comparable<TimeRemaining> {
    constructor(totalSeconds: Long) : this((totalSeconds % 60L).toInt(), (totalSeconds / 60L).toInt())

    private val totalTimeInSeconds: Int
        get() = minutes * 60 + seconds

    fun isEndingSoon(minutesThreshold: Int = 2) =
            minutes < minutesThreshold || (minutes == minutesThreshold && seconds == 0)

    override fun compareTo(other: TimeRemaining) = totalTimeInSeconds.compareTo(other.totalTimeInSeconds)
}

sealed class MatchState : Comparable<MatchState> {
    private val order = listOf(NoTime::class, Paused::class, InProgress::class)

    abstract fun getTimeLeft(currentTime: Calendar): TimeRemaining?
    abstract fun isFinished(currentTime: Calendar): Boolean

    fun compareClass(other: MatchState) =
            order.indexOfFirst { it.isInstance(this) }.compareTo(order.indexOfFirst { it.isInstance(other) })

    object NoTime : MatchState() {
        override fun getTimeLeft(currentTime: Calendar): TimeRemaining? = null

        override fun compareTo(other: MatchState): Int {
            if (other is NoTime) return 0
            return compareClass(other)
        }

        override fun isFinished(currentTime: Calendar): Boolean = true
    }

    data class InProgress(val endTime: Calendar) : MatchState() {
        override fun getTimeLeft(currentTime: Calendar): TimeRemaining? {
            if (endTime.before(currentTime)) return null

            val difference = endTime.timeInMillis - currentTime.timeInMillis
            if (difference <= 0) return null

            return TimeRemaining(TimeUnit.MILLISECONDS.toSeconds(difference))
        }

        override fun compareTo(other: MatchState): Int {
            val classComparison = compareClass(other)
            if (classComparison != 0) return classComparison
            other as InProgress

            return endTime.compareTo(other.endTime)
        }

        override fun isFinished(currentTime: Calendar): Boolean = endTime.before(currentTime)
    }

    data class Paused(val remainingTimeSeconds: Long, val pausedAt: Calendar) : MatchState() {
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