package com.eywa.projectclava.main.common

import androidx.compose.ui.graphics.Color
import com.eywa.projectclava.main.model.MatchState
import com.eywa.projectclava.main.model.TimeRemaining
import com.eywa.projectclava.ui.theme.ClavaColor
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

fun Calendar.asDateString(): String = SimpleDateFormat("d MMM yy", Locale.getDefault()).format(this.time)
fun Calendar.asTimeString(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(this.time)

@Deprecated("Down with the current time!")
fun MatchState.asColor(currentTime: Calendar) = asColor(getTimeLeft(currentTime))

fun MatchState.asColor(timeLeft: TimeRemaining?): Color? {
    if (this is MatchState.Paused) return ClavaColor.MatchPaused
    if (this is MatchState.NotStarted) return ClavaColor.MatchQueued
    if (this !is MatchState.OnCourt) return null

    timeLeft!!
    if (timeLeft.isNegative) return ClavaColor.MatchOverrun
    return if (timeLeft.isEndingSoon()) ClavaColor.MatchFinishingSoon else ClavaColor.MatchInProgress
}

fun Int?.asTimeString() = this?.let { TimeRemaining(this) }.asTimeString()
fun TimeRemaining?.asTimeString() = asTimeString(this?.minutes, this?.seconds)
fun asTimeString(minutes: Int?, seconds: Int?): String {
    if (minutes == null || seconds == null) return "--:--"

    val initialSign = if (seconds < 0 || minutes < 0) "-" else ""
    return initialSign +
            abs(minutes) +
            ":" +
            abs(seconds).toString().padStart(2, '0')
}
