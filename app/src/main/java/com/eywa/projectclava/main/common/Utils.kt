package com.eywa.projectclava.main.common

import androidx.compose.ui.graphics.Color
import com.eywa.projectclava.main.model.Court
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.MatchState
import com.eywa.projectclava.main.model.TimeRemaining
import com.eywa.projectclava.ui.theme.ClavaColor
import java.text.SimpleDateFormat
import java.util.*

fun Calendar.asString() = SimpleDateFormat("HH:mm", Locale.getDefault()).format(this)
fun Calendar.isToday() = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
}.before(this)

/**
 * @param generalInProgressColor color to use when the match is in progress, not finished, and not ending soon
 */
fun MatchState.asColor(currentTime: Calendar, generalInProgressColor: Color? = null): Color? {
    if (this is MatchState.Paused) return ClavaColor.MatchPaused
    if (this !is MatchState.InProgress) return null

    val timeLeft = getTimeLeft(currentTime)
            ?: return ClavaColor.MatchFinished
    return if (timeLeft.isEndingSoon()) ClavaColor.MatchFinished else generalInProgressColor
}

fun TimeRemaining?.asString() = this?.let { "$minutes:" + seconds.toString().padStart(2, '0') } ?: "--:--"

fun Iterable<Court>?.filterAvailable(currentTime: Calendar) = this?.takeIf { !it.none() }
        ?.filter { it.isAvailable(currentTime) }
        ?.takeIf { it.isNotEmpty() }
        ?.sortedBy { it.name }

/**
 * @see MatchState.transformForSorting
 */
fun Match.transformForSorting(currentTime: Calendar) = copy(state = state.transformForSorting(currentTime))

/**
 * For the purposes of sorting, treat (InProgress && isFinished) == NoTime
 */
fun MatchState.transformForSorting(currentTime: Calendar) =
        if (this is MatchState.InProgress && isFinished(currentTime)) MatchState.NoTime else this