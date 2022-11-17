package com.eywa.projectclava.main.common

import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.MatchState
import com.eywa.projectclava.main.model.TimeRemaining
import java.util.concurrent.TimeUnit
import kotlin.math.abs

fun Match?.stateSemanticsText(
        isPlayer: Boolean = false,
        getTimeRemaining: Match.() -> TimeRemaining? = { null },
): String {
    return when (this?.state) {
        null -> "Not played"
        is MatchState.NotStarted -> "Queued"
        is MatchState.Paused -> "Paused " + getTimeRemaining()!!.asSemanticsTimeString()
        is MatchState.OnCourt -> "Playing for another " + getTimeRemaining()!!.asSemanticsTimeString()
        is MatchState.Completed -> when {
            isPlayer -> "Last played"
            else -> "Finished"
        } + " at " + getTime().asTimeString()

    }
}

fun Int?.asSemanticsTimeString() = this?.let { TimeRemaining(this) }.asSemanticsTimeString()
fun TimeRemaining?.asSemanticsTimeString() = asSemanticsTimeString(this?.minutes, this?.seconds)
fun asSemanticsTimeString(minutes: Int?, seconds: Int?): String {
    if (minutes == null || seconds == null) return "unknown"

    val initialSign = if (seconds < 0 || minutes < 0) "overrunning by " else ""

    return initialSign + when {
        // Over a day
        abs(minutes) >= 60 * 24 -> "more than 1 day"
        // Over an hour
        abs(minutes) >= 60 -> {
            val hours = TimeUnit.MINUTES.toHours(abs(minutes).toLong())
            "" + hours + " hour" + (if (hours == 1L) "" else "s")
        }
        else -> "" + abs(minutes) + "minutes " + abs(seconds).toString().padStart(2, '0') + "seconds"
    }
}