package com.eywa.projectclava.main.mainActivity

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.eywa.projectclava.main.database.ClavaDatabase
import com.eywa.projectclava.main.database.court.CourtRepo
import com.eywa.projectclava.main.database.court.DatabaseCourt
import com.eywa.projectclava.main.database.match.DatabaseMatchPlayer
import com.eywa.projectclava.main.database.match.MatchRepo
import com.eywa.projectclava.main.database.player.DatabasePlayer
import com.eywa.projectclava.main.database.player.PlayerRepo
import com.eywa.projectclava.main.mainActivity.drawer.DrawerIntent
import com.eywa.projectclava.main.mainActivity.screens.ScreenIntent
import com.eywa.projectclava.main.mainActivity.screens.ScreenState
import com.eywa.projectclava.main.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

fun <T> SharedFlow<T>.latest() = replayCache.first()

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val currentTime = MutableSharedFlow<Calendar>(1)
    private var screenState by mutableStateOf(mapOf<NavRoute, ScreenState>())

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
    val databaseState = courtRepo.getAll().map { it.map { dbMatch -> dbMatch.asCourt() } }
            .combine(playerRepo.getAll().map { it.map { dbMatch -> dbMatch.asPlayer() } }) { courts, matches ->
                courts to matches
            }
            .combine(matchRepo.getAll().map { it.map { dbMatch -> dbMatch.asMatch() } }) { (courts, players), matches ->
                DatabaseState(courts, matches, players)
            }
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

    /**
     * Retrieves the current state of the screen.
     * If it doesn't exist, creates a new one and saves it
     */
    fun getScreenState(screen: NavRoute) = screenState[screen]
            ?: screen.createInitialState().also { screen.updateScreenState(it) }

    private fun NavRoute.updateScreenState(newState: ScreenState) {
        screenState = screenState.plus(this to newState)
    }

    fun handleIntent(intent: MainIntent) {
        when (intent) {
            /*
             * CoreIntents
             */
            is MainEffect -> viewModelScope.launch { _effects.emit(intent) }
            is DatabaseIntent -> viewModelScope.launch { handleDatabaseIntent(intent) }
            is DataStoreIntent -> viewModelScope.launch {
                clavaDatastore.handle(intent, preferences.latest())
            }

            /*
             * Screens
             */
            is DrawerIntent -> handleIntent(intent.map())
            is ScreenIntent<*> -> {
                @Suppress("UNCHECKED_CAST")
                (intent as ScreenIntent<ScreenState>).handle(
                        currentState = getScreenState(intent.screen),
                        handle = { handleIntent(it) },
                        newStateListener = { intent.screen.updateScreenState(it) },
                )
            }
            else -> throw NotImplementedError()
        }
    }

    private suspend fun handleDatabaseIntent(intent: DatabaseIntent) {
        // Keep else so that when new DatabaseIntents are added, they cannot be ignored
        // Fool me once, shame on you >.>
        @Suppress("REDUNDANT_ELSE_IN_WHEN")
        when (intent) {
            /*
             * Matches
             */
            DatabaseIntent.DeleteAllMatches -> matchRepo.deleteAll()
            is DatabaseIntent.DeleteMatch -> matchRepo.delete(intent.match.asDatabaseMatch())
            is DatabaseIntent.UpdateMatch -> matchRepo.update(intent.match.asDatabaseMatch())
            is DatabaseIntent.AddMatch -> {
                check(intent.players.any()) { "No players in match" }
                val databaseMatch =
                        Match(0, intent.players, MatchState.NotStarted(currentTime.latest())).asDatabaseMatch()
                val matchId = matchRepo.insert(databaseMatch)
                matchRepo.insert(*intent.players.map { DatabaseMatchPlayer(matchId.toInt(), it.id) }.toTypedArray())
            }

            /*
             * Players
             */
            is DatabaseIntent.AddPlayer -> playerRepo.insertAll(DatabasePlayer(0, intent.name))
            is DatabaseIntent.DeletePlayer -> playerRepo.delete(intent.player.asDatabasePlayer())
            is DatabaseIntent.UpdatePlayer -> playerRepo.update(intent.player.asDatabasePlayer())
            is DatabaseIntent.UpdatePlayers -> playerRepo.update(
                    *intent.players.map { it.asDatabasePlayer() }.toTypedArray()
            )

            /*
             * Courts
             */
            is DatabaseIntent.AddCourt -> courtRepo.insertAll(DatabaseCourt(0, intent.name))
            is DatabaseIntent.DeleteCourt -> courtRepo.delete(intent.court.asDatabaseCourt())
            is DatabaseIntent.UpdateCourt -> courtRepo.update(intent.court.asDatabaseCourt())

            else -> throw NotImplementedError()
        }
    }

    fun updatePlayers(vararg player: Player) = viewModelScope.launch {
        playerRepo.update(*player.map { it.asDatabasePlayer() }.toTypedArray())
    }

    fun deletePlayer(player: Player) = viewModelScope.launch {
        playerRepo.delete(player.asDatabasePlayer())
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
