package com.eywa.projectclava.main.features.drawer

import com.eywa.projectclava.main.common.UpdateCalendarInfo
import com.eywa.projectclava.main.database.DatabaseIntent
import com.eywa.projectclava.main.datastore.DataStoreIntent
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.mainActivity.viewModel.CoreIntent
import com.eywa.projectclava.main.mainActivity.viewModel.MainEffect
import com.eywa.projectclava.main.mainActivity.viewModel.MainIntent
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.Player
import kotlin.time.Instant

sealed class DrawerIntent : MainIntent {
    object ClearDatastore : DrawerIntent()
    data class UpdateClubNightStartTime(val value: UpdateCalendarInfo) : DrawerIntent()
    data class UpdateClubNightStartTimeInstant(val value: Instant) : DrawerIntent()
    data class UpdateOverrunIndicatorThreshold(val value: Int) : DrawerIntent()
    data class UpdateDefaultMatchTime(val value: Int) : DrawerIntent()
    data class UpdateDefaultTimeToAdd(val value: Int) : DrawerIntent()
    object ResetClubNightStartTime : DrawerIntent()
    object TogglePrependCourt : DrawerIntent()
    object ToggleMuteSounds : DrawerIntent()
    data class UpdatePlayers(val value: Iterable<Player>) : DrawerIntent()
    data class Navigate(val value: NavRoute) : DrawerIntent()
    data class UpdateMatch(val value: Match) : DrawerIntent()
    data class DeleteMatch(val value: Match) : DrawerIntent()
    object DeleteAllMatches : DrawerIntent()
    object DeleteAllData : DrawerIntent()

    fun handle(handle: (CoreIntent) -> Unit) {
        when (this) {
            DeleteAllData -> {
                handle(DatabaseIntent.DeleteAllData)
                handle(DataStoreIntent.ClearDatastore)
            }
            else -> handle(
                    when (this) {
                        is Navigate -> MainEffect.Navigate(value)
                        is UpdateClubNightStartTime -> DataStoreIntent.UpdateClubNightStartTime(value)
                        is UpdateClubNightStartTimeInstant -> DataStoreIntent.UpdateClubNightStartTimeInstant(value)
                        is UpdateDefaultMatchTime -> DataStoreIntent.UpdateDefaultMatchTime(value)
                        is UpdateDefaultTimeToAdd -> DataStoreIntent.UpdateDefaultTimeToAdd(value)
                        is UpdateOverrunIndicatorThreshold -> DataStoreIntent.UpdateOverrunIndicatorThreshold(value)
                        is TogglePrependCourt -> DataStoreIntent.TogglePrependCourt
                        is ToggleMuteSounds -> DataStoreIntent.ToggleMuteSounds
                        is ResetClubNightStartTime -> DataStoreIntent.ResetClubNightStartTime
                        DeleteAllMatches -> DatabaseIntent.DeleteAllMatches
                        is DeleteMatch -> DatabaseIntent.DeleteMatch(value)
                        is UpdatePlayers -> DatabaseIntent.UpdatePlayers(value)
                        is UpdateMatch -> DatabaseIntent.UpdateMatch(value)
                        ClearDatastore -> DataStoreIntent.ClearDatastore
                        else -> throw NotImplementedError()
                    }
            )
        }
    }
}
