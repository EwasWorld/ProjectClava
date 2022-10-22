package com.eywa.projectclava.main.mainActivity

import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.Player
import java.util.*

// TODO Can I split this into files?
sealed class MainIntent {
    sealed class DrawerIntent : MainIntent() {
        data class UpdateClubNightStartTime(
                val day: Int? = null,
                val month: Int? = null,
                val year: Int? = null,
                val hours: Int? = null,
                val minutes: Int? = null,
        ) : DrawerIntent() {
            init {
                require(
                        day != null || month != null || year != null
                                || hours != null || minutes != null
                ) { "No new values set" }
            }
        }

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
}