package com.eywa.projectclava.main.mainActivity

import com.eywa.projectclava.main.mainActivity.drawer.DrawerIntent
import com.eywa.projectclava.main.model.Court
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
    data class Navigate(val destination: NavRoute) : MainEffect()
}

sealed class DatabaseIntent : CoreIntent {
    /*
     * Matches
     */
    data class DeleteMatch(val match: Match) : DatabaseIntent()
    object DeleteAllMatches : DatabaseIntent()
    data class AddMatch(val players: Iterable<Player>) : DatabaseIntent()

    /*
     * Update match
     */
    data class UpdateMatch(val match: Match) : DatabaseIntent()
    data class AddTimeToMatch(val matchId: Int, val secondsToAdd: Int) : DatabaseIntent()
    data class CompleteMatch(val matchId: Int) : DatabaseIntent()
    data class PauseMatch(val matchId: Int) : DatabaseIntent()
    data class ChangeMatchCourt(val matchId: Int, val court: Court) : DatabaseIntent()
    data class ResumeMatch(val matchId: Int, val court: Court, val resumeTimeSeconds: Int) : DatabaseIntent()

    /*
     * Players
     */
    data class UpdatePlayer(val player: Player) : DatabaseIntent()
    data class UpdatePlayers(val players: Iterable<Player>) : DatabaseIntent()
    data class AddPlayer(val name: String) : DatabaseIntent()
    data class DeletePlayer(val player: Player) : DatabaseIntent()

    /*
     * Courts
     */
    /**
     * Intent will prepend "Court " if the corresponding preference is set
     */
    data class AddCourt(val name: String) : DatabaseIntent()
    data class UpdateCourt(val court: Court) : DatabaseIntent()
    data class DeleteCourt(val court: Court) : DatabaseIntent()
}
