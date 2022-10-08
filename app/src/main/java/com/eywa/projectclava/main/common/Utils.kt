package com.eywa.projectclava.main.common

import androidx.compose.ui.graphics.Color
import com.eywa.projectclava.main.model.MatchState
import com.eywa.projectclava.main.model.TimeRemaining
import com.eywa.projectclava.ui.theme.ClavaColor
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

fun Calendar.asString(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(this.time)
fun Calendar.isToday() = Calendar.getInstance(Locale.getDefault()).apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
}.before(this)

/**
 * @param generalInProgressColor color to use when the match is in progress, not finished, and not ending soon
 */
fun MatchState.asColor(currentTime: Calendar, generalInProgressColor: Color? = null): Color? {
    if (this is MatchState.Paused) return ClavaColor.MatchPaused
    if (this is MatchState.NotStarted) return ClavaColor.MatchQueued
    if (this !is MatchState.OnCourt) return null

    val timeLeft = getTimeLeft(currentTime)!!
    if (timeLeft.isNegative) return ClavaColor.MatchOverrun
    return if (timeLeft.isEndingSoon()) ClavaColor.MatchFinishingSoon else ClavaColor.MatchInProgress
}

fun TimeRemaining?.asString() = this?.let { "$minutes:" + abs(seconds).toString().padStart(2, '0') } ?: "--:--"

/**
 * For the purposes of sorting, treat null as NoTime
 */
fun MatchState?.transformForSorting(currentTime: Calendar) = this ?: MatchState.NotStarted(currentTime)