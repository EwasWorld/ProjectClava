package com.eywa.projectclava.main.common

import androidx.compose.ui.graphics.Color
import com.eywa.projectclava.main.model.MatchState
import com.eywa.projectclava.main.model.TimeRemaining
import com.eywa.projectclava.ui.theme.ClavaColor
import java.text.SimpleDateFormat
import java.util.*

fun Calendar.asString(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(this.time)
fun Calendar.isToday() = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
}.before(this)

/**
 * @param generalInProgressColor color to use when the match is in progress, not finished, and not ending soon
 */
fun MatchState.asColor(currentTime: Calendar, generalInProgressColor: Color? = null): Color? {
    if (this is MatchState.Paused) return ClavaColor.MatchPaused
    if (this is MatchState.NotStarted) return ClavaColor.MatchQueued
    if (this !is MatchState.InProgressOrComplete) return null

    val timeLeft = getTimeLeft(currentTime) ?: return null
    return if (timeLeft.isEndingSoon()) ClavaColor.MatchFinishingSoon else generalInProgressColor
}

fun TimeRemaining?.asString() = this?.let { "$minutes:" + seconds.toString().padStart(2, '0') } ?: "--:--"

/**
 * For the purposes of sorting, treat (InProgress && isFinished) == NoTime
 */
fun MatchState?.transformForSorting(currentTime: Calendar) =
        if (this == null || this is MatchState.InProgressOrComplete && isFinished(currentTime)) {
            MatchState.NotStarted(currentTime)
        }
        else {
            this
        }