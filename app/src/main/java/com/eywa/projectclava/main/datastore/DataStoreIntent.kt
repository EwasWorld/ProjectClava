package com.eywa.projectclava.main.datastore

import com.eywa.projectclava.main.common.UpdateCalendarInfo
import com.eywa.projectclava.main.mainActivity.viewModel.CoreIntent
import kotlin.time.Instant

/**
 * Handled in [ClavaDatastore]
 */
sealed class DataStoreIntent : CoreIntent {
    data class UpdateClubNightStartTime(val value: UpdateCalendarInfo) : DataStoreIntent()
    data class UpdateClubNightStartTimeInstant(val value: Instant) : DataStoreIntent()
    data class UpdateOverrunIndicatorThreshold(val value: Int) : DataStoreIntent()
    data class UpdateDefaultMatchTime(val value: Int) : DataStoreIntent()
    data class UpdateDefaultTimeToAdd(val value: Int) : DataStoreIntent()
    object TogglePrependCourt : DataStoreIntent()
    object ToggleMuteSounds : DataStoreIntent()
    object ResetClubNightStartTime : DataStoreIntent()
    object ClearDatastore : DataStoreIntent()
}
