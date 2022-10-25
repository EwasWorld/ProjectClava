package com.eywa.projectclava.main.mainActivity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.eywa.projectclava.main.database.ClavaDatabase
import com.eywa.projectclava.main.database.court.CourtRepo
import com.eywa.projectclava.main.database.match.DatabaseMatchPlayer
import com.eywa.projectclava.main.database.match.MatchRepo
import com.eywa.projectclava.main.database.player.PlayerRepo
import com.eywa.projectclava.main.mainActivity.drawer.DrawerIntent
import com.eywa.projectclava.main.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val currentTime = MutableSharedFlow<Calendar>(1)

    private val _effects: MutableStateFlow<MainEffect?> = MutableStateFlow(null)
    val effects: Flow<MainEffect?> = _effects

    /*
     * Repos
     */
    private val db = ClavaDatabase.getInstance(application)
    private val playerRepo: PlayerRepo = db.playerRepo()
    private val courtRepo: CourtRepo = db.courtRepo()
    private val matchRepo: MatchRepo = db.matchRepo()

    /*
     * Main state
     */
    private val courts = courtRepo.getAll().map { it.map { dbMatch -> dbMatch.asCourt() } }
    private val players = playerRepo.getAll().map { it.map { dbMatch -> dbMatch.asPlayer() } }
    private val matches = matchRepo.getAll().map { it.map { dbMatch -> dbMatch.asMatch() } }
    val databaseState = courts
            .combine(players) { a, b -> a to b }
            .combine(matches) { (courts, players), matches -> DatabaseState(courts, matches, players) }
            .shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    /*
     * Datastore
     */
    private val clavaDatastore = ClavaDatastore((application.applicationContext).dataStore)
    val preferences = clavaDatastore
            .getPreferences()
            .shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    init {
        viewModelScope.launch {
            while (true) {
                currentTime.emit(Calendar.getInstance(Locale.getDefault()))
                delay(1000)
            }
        }
    }

    fun mainHandle(action: MainIntent) {
        when (action) {
            /*
             * CoreIntents
             */
            is MainEffect -> viewModelScope.launch { _effects.emit(action) }
            is DatabaseIntent -> viewModelScope.launch { handleDatabaseAction(action) }
            is DataStoreIntent -> viewModelScope.launch {
                clavaDatastore.handle(action, preferences.replayCache.first())
            }

            /*
             * Screens
             */
            is DrawerIntent -> mainHandle(action.map())

            else -> throw NotImplementedError()
        }
    }

    private suspend fun handleDatabaseAction(action: DatabaseIntent) {
        when (action) {
            /*
             * Matches
             */
            DatabaseIntent.DeleteAllMatches -> matchRepo.deleteAll()
            is DatabaseIntent.DeleteMatch -> matchRepo.delete(action.value.asDatabaseMatch())
            is DatabaseIntent.UpdateMatch -> matchRepo.update(action.value.asDatabaseMatch())

            /*
             * Players
             */
            is DatabaseIntent.UpdatePlayers -> playerRepo.update(
                    *action.value.map { it.asDatabasePlayer() }.toTypedArray()
            )
        }
    }

    fun addPlayer(playerName: String) = viewModelScope.launch {
        playerRepo.insertAll(Player(0, playerName).asDatabasePlayer())
    }

    fun updatePlayers(vararg player: Player) = viewModelScope.launch {
        playerRepo.update(*player.map { it.asDatabasePlayer() }.toTypedArray())
    }

    fun deletePlayer(player: Player) = viewModelScope.launch {
        playerRepo.delete(player.asDatabasePlayer())
    }

    fun addCourt(courtName: String) = viewModelScope.launch {
        courtRepo.insertAll(Court(0, courtName).asDatabaseCourt())
    }

    fun updateCourt(court: Court) = viewModelScope.launch {
        courtRepo.update(court.asDatabaseCourt())
    }

    fun deleteCourt(court: Court) = viewModelScope.launch {
        courtRepo.delete(court.asDatabaseCourt())
    }

    fun addMatch(players: Iterable<Player>, createdTime: Calendar) = viewModelScope.launch {
        val databaseMatch = Match(0, players, MatchState.NotStarted(createdTime)).asDatabaseMatch()
        val matchId = matchRepo.insert(databaseMatch)
        matchRepo.insert(*players.map { DatabaseMatchPlayer(matchId.toInt(), it.id) }.toTypedArray())
    }

    fun updateMatch(match: Match) = viewModelScope.launch {
        matchRepo.update(match.asDatabaseMatch())
    }

    fun deleteMatch(match: Match) = viewModelScope.launch {
        matchRepo.delete(match.asDatabaseMatch())
    }
}

/**
 * Top level for any intents.
 * Screen's intents should map instead to a [CoreIntent]
 */
interface MainIntent