package com.eywa.projectclava.main.datastore

import java.util.*

data class DatastoreState(
        val overrunIndicatorThreshold: Int = 10,
        val defaultMatchTime: Int = 15 * 60,
        val defaultTimeToAdd: Int = 2 * 60,
        // Default: winds back from now to 4am
        val clubNightStartTime: Calendar = Calendar.getInstance(Locale.getDefault()).apply {
            if (get(Calendar.HOUR_OF_DAY) < 4) {
                add(Calendar.DAY_OF_MONTH, -1)
            }
            set(Calendar.HOUR_OF_DAY, 4)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        },
        val isDefaultClubNightStartTime: Boolean = true,
        val prependCourt: Boolean = true,
)