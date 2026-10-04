package com.eywa.projectclava.main.common

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * @param month 1-12
 */
data class UpdateCalendarInfo(
        val day: Int? = null,
        val month: Int? = null,
        val year: Int? = null,
        val hours: Int? = null,
        val minutes: Int? = null,
) {
    init {
        require(
                day != null || month != null || year != null
                        || hours != null || minutes != null
        ) { "No new values set" }
    }

    /**
     * @return a copy of [initial] with the non-null values in this object replacing those in [initial]
     * (as seen in [timeZone]).
     * For example if only [day] is not null, [initial]'s day will be replaced with [day] and all other fields will be
     * retained
     */
    fun update(initial: Instant, timeZone: TimeZone = TimeZone.currentSystemDefault()): Instant {
        val current = initial.toLocalDateTime(timeZone)
        return LocalDateTime(
                year ?: current.year,
                month ?: current.month.number,
                day ?: current.day,
                hours ?: current.hour,
                minutes ?: current.minute,
                current.second,
                current.nanosecond,
        ).toInstant(timeZone)
    }
}
