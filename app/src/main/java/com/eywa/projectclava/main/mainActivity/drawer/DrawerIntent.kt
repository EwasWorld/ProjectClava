package com.eywa.projectclava.main.mainActivity.drawer

import com.eywa.projectclava.main.common.UpdateCalendarInfo
import com.eywa.projectclava.main.mainActivity.*
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.Player
import java.util.*

sealed class DrawerIntent : MainIntent {
    data class UpdateClubNightStartTime(val value: UpdateCalendarInfo) : DrawerIntent()
    data class UpdateClubNightStartTimeCalendar(val value: Calendar) : DrawerIntent()
    data class UpdateOverrunIndicatorThreshold(val value: Int) : DrawerIntent()
    data class UpdateDefaultMatchTime(val value: Int) : DrawerIntent()
    data class UpdateDefaultTimeToAdd(val value: Int) : DrawerIntent()
    object TogglePrependCourt : DrawerIntent()
    data class UpdatePlayers(val value: Iterable<Player>) : DrawerIntent()
    data class Navigate(val value: NavRoute) : DrawerIntent()
    data class UpdateMatch(val value: Match) : DrawerIntent()
    data class DeleteMatch(val value: Match) : DrawerIntent()
    object DeleteAllMatches : DrawerIntent()

    fun map(): CoreIntent {
        return when (this) {
            is Navigate -> MainEffect.Navigate(value)
            is UpdateClubNightStartTime -> DataStoreIntent.UpdateClubNightStartTime(value)
            is UpdateClubNightStartTimeCalendar -> DataStoreIntent.UpdateClubNightStartTimeCalendar(value)
            is UpdateDefaultMatchTime -> DataStoreIntent.UpdateDefaultMatchTime(value)
            is UpdateDefaultTimeToAdd -> DataStoreIntent.UpdateDefaultTimeToAdd(value)
            is UpdateOverrunIndicatorThreshold -> DataStoreIntent.UpdateOverrunIndicatorThreshold(value)
            is TogglePrependCourt -> DataStoreIntent.TogglePrependCourt
            DeleteAllMatches -> DatabaseIntent.DeleteAllMatches
            is DeleteMatch -> DatabaseIntent.DeleteMatch(value)
            is UpdatePlayers -> DatabaseIntent.UpdatePlayers(value)
            is UpdateMatch -> DatabaseIntent.UpdateMatch(value)
        }
    }
}