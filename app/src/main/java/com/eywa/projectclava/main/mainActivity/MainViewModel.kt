package com.eywa.projectclava.main.mainActivity

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.eywa.projectclava.main.database.ClavaDatabase
import com.eywa.projectclava.main.database.court.CourtRepo
import com.eywa.projectclava.main.database.match.DatabaseMatchPlayer
import com.eywa.projectclava.main.database.match.MatchRepo
import com.eywa.projectclava.main.database.player.PlayerRepo
import com.eywa.projectclava.main.mainActivity.drawer.DrawerIntent
import com.eywa.projectclava.main.mainActivity.screens.ScreenIntent
import com.eywa.projectclava.main.mainActivity.screens.ScreenState
import com.eywa.projectclava.main.mainActivity.screens.createMatch.CreateMatchState
import com.eywa.projectclava.main.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.cast

fun <T> SharedFlow<T>.latest() = replayCache.first()

fun <T : ScreenState> createState(clazz: KClass<T>): T {
    @Suppress("UNCHECKED_CAST")
    return when (clazz) {
        CreateMatchState::class -> CreateMatchState()
        else -> throw NotImplementedError()
    } as T
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val currentTime = MutableSharedFlow<Calendar>(1)
    private var screenState by mutableStateOf(mapOf<KClass<out ScreenState>, ScreenState>())

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
     * Retrieves the current state of type T or creates a new one if it doesn't exist.
     * Kind of annoying to pass the class around rather than using reified T, but didn't want to expose
     * [updateScreenState] or [screenState]
     */
    fun <T : ScreenState> getScreenState(clazz: KClass<T>) = screenState[clazz]?.let { clazz.cast(it) }
            ?: createState(clazz).apply { updateScreenState() }

    private fun ScreenState.updateScreenState() {
        screenState = screenState.plus(this::class to this)
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
                // TODO Things went a bit funny with the types... ngl
                @Suppress("UNCHECKED_CAST")
                (intent as ScreenIntent<ScreenState>).handle(
                        currentState = getScreenState(intent.getStateClass()),
                        handle = { handleIntent(it) },
                        newStateListener = { it.updateScreenState() },
                )
            }
            else -> throw NotImplementedError()
        }
    }

    private suspend fun handleDatabaseIntent(intent: DatabaseIntent) {
        when (intent) {
            /*
             * Matches
             */
            DatabaseIntent.DeleteAllMatches -> matchRepo.deleteAll()
            is DatabaseIntent.DeleteMatch -> matchRepo.delete(intent.value.asDatabaseMatch())
            is DatabaseIntent.UpdateMatch -> matchRepo.update(intent.value.asDatabaseMatch())
            is DatabaseIntent.AddMatch -> {
                check(intent.value.any()) { "No players in match" }
                val databaseMatch =
                        Match(0, intent.value, MatchState.NotStarted(currentTime.latest())).asDatabaseMatch()
                val matchId = matchRepo.insert(databaseMatch)
                matchRepo.insert(*intent.value.map { DatabaseMatchPlayer(matchId.toInt(), it.id) }.toTypedArray())
            }

            /*
             * Players
             */
            is DatabaseIntent.UpdatePlayers -> playerRepo.update(
                    *intent.value.map { it.asDatabasePlayer() }.toTypedArray()
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
