package com.eywa.projectclava.main.datastore

import com.eywa.projectclava.main.common.UpdateCalendarInfo
import com.eywa.projectclava.main.mainActivity.viewModel.CoreIntent
import java.util.*

/**
 * Handled in [ClavaDatastore]
 */
sealed class DataStoreIntent : CoreIntent {
    data class UpdateClubNightStartTime(val value: UpdateCalendarInfo) : DataStoreIntent()
    data class UpdateClubNightStartTimeCalendar(val value: Calendar) : DataStoreIntent()
    data class UpdateOverrunIndicatorThreshold(val value: Int) : DataStoreIntent()
    data class UpdateDefaultMatchTime(val value: Int) : DataStoreIntent()
    data class UpdateDefaultTimeToAdd(val value: Int) : DataStoreIntent()
    object TogglePrependCourt : DataStoreIntent()
    object ResetClubNightStartTime : DataStoreIntent()
    object ClearDatastore : DataStoreIntent()
}