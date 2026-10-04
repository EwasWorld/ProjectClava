package com.eywa.projectclava.main.datastore

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

data class DatastoreState(
        val overrunIndicatorThreshold: Int = 10,
        val defaultMatchTime: Int = 15 * 60,
        val defaultTimeToAdd: Int = 2 * 60,
        val clubNightStartTime: Instant = defaultClubNightStartTime(),
        val isDefaultClubNightStartTime: Boolean = true,
        val prependCourt: Boolean = true,
        val muteSounds: Boolean = false,
)

/**
 * Winds back from now to 4am
 */
private fun defaultClubNightStartTime(): Instant {
    val timeZone = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toLocalDateTime(timeZone)
    val date = if (now.hour < 4) now.date.minus(1, DateTimeUnit.DAY) else now.date
    return date.atTime(4, 0).toInstant(timeZone)
}
