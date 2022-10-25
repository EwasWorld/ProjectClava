package com.eywa.projectclava.main.mainActivity

import com.eywa.projectclava.main.mainActivity.drawer.DrawerIntent
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.Player

/**
 * Things that must be handled by the viewmodel like [DatabaseIntent], [DataStoreIntent], [MainEffect].
 * This is separate to [MainIntent] as screen's intents should map to one of these
 *
 * @see [DrawerIntent.map]
 */
interface CoreIntent : MainIntent

sealed class MainEffect : CoreIntent {
    data class Navigate(val value: NavRoute) : MainEffect()
}

sealed class DatabaseIntent : CoreIntent {
    /*
     * Matches
     */
    data class UpdateMatch(val value: Match) : DatabaseIntent()
    data class DeleteMatch(val value: Match) : DatabaseIntent()
    object DeleteAllMatches : DatabaseIntent()

    /*
     * Players
     */
    data class UpdatePlayers(val value: Iterable<Player>) : DatabaseIntent()
}
