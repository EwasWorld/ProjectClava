package com.eywa.projectclava.main.features.ui.timePicker

import com.eywa.projectclava.main.common.parseInt

data class TimePickerState(
        internal val minutes: String,
        internal val seconds: String,
        internal val initialMinutes: String = minutes,
        internal val initialSeconds: String = seconds,
        private val minutesIsDirty: Boolean = false,
        private val secondsIsDirty: Boolean = false,
) {
    constructor(initialTotalSeconds: Int) : this(
            minutes = (initialTotalSeconds / 60).takeIf { it != 0 }?.toString() ?: "",
            seconds = (initialTotalSeconds % 60).toString().padStart(2, '0'),
    ) {
        check(initialTotalSeconds >= 0) { "Initial time cannot be less than 0" }
    }

    val totalSeconds
        get() = seconds.parseInt() + minutes.parseInt() * 60

    val isValid
        get() = error == null

    val error
        get() = minutesError ?: secondsError ?: generalError

    internal val secondsError
        get() = when {
            seconds.parseInt() < 0 -> "Cannot be less than 0"
            seconds.parseInt() >= 60 -> "Must be less than 60"
            else -> null
        }

    internal val minutesError
        get() = when {
            minutes.parseInt() < 0 -> "Cannot be less than 0"
            else -> null
        }

    internal val generalError
        get() = when {
            dirty && totalSeconds == 0 -> "Cannot be 0"
            else -> null
        }

    private val dirty
        get() = minutesIsDirty || secondsIsDirty
}