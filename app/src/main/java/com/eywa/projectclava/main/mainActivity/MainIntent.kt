package com.eywa.projectclava.main.mainActivity

import com.eywa.projectclava.main.common.UpdateCalendarInfo
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.Player
import java.util.*

// TODO Can I split this into files?
interface MainIntent

sealed class DrawerIntent : MainIntent {
    data class UpdateClubNightStartTime(val value: UpdateCalendarInfo) : DrawerIntent()
    data class UpdateClubNightStartTimeCalendar(val value: Calendar) : DrawerIntent()
    data class UpdateOverrunIndicatorThreshold(val value: Int) : DrawerIntent()
    data class UpdateDefaultMatchTime(val value: Int) : DrawerIntent()
    data class UpdateDefaultTimeToAdd(val value: Int) : DrawerIntent()
    object TogglePrependCourt : DrawerIntent()
    data class UpdatePlayers(val players: Iterable<Player>) : DrawerIntent()
    data class Navigate(val route: NavRoute) : DrawerIntent()
    data class UpdateMatch(val match: Match) : DrawerIntent()
    data class DeleteMatch(val match: Match) : DrawerIntent()
    object DeleteAllMatches : DrawerIntent()
}