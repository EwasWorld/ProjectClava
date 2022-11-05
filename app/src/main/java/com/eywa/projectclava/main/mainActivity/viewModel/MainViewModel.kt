package com.eywa.projectclava.main.mainActivity.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eywa.projectclava.main.database.ClavaDatabase
import com.eywa.projectclava.main.database.DatabaseIntent
import com.eywa.projectclava.main.datastore.ClavaDatastore
import com.eywa.projectclava.main.datastore.DataStoreIntent
import com.eywa.projectclava.main.features.drawer.DrawerIntent
import com.eywa.projectclava.main.features.screens.ScreenIntent
import com.eywa.projectclava.main.features.screens.ScreenState
import com.eywa.projectclava.main.features.screens.help.HelpState
import com.eywa.projectclava.main.mainActivity.MainNavRoute
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.model.ModelState
import com.eywa.projectclava.main.model.asCourt
import com.eywa.projectclava.main.model.asMatch
import com.eywa.projectclava.main.model.asPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


fun <T> SharedFlow<T>.latest() = replayCache.first()


@HiltViewModel
class MainViewModel @Inject constructor(
        private val db: ClavaDatabase,
        private val clavaDatastore: ClavaDatastore,
) : ViewModel() {
    val currentTime = MutableSharedFlow<Calendar>(1)
    private var screenState by mutableStateOf(mapOf<NavRoute, ScreenState>())

    private val _effects: MutableSharedFlow<MainEffect?> =
            MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val effects: Flow<MainEffect?> = _effects

    /*
     * Database state
     */
    val databaseState: SharedFlow<ModelState>

    init {
        val courtsFlow = db.courtRepo().getAll().map { it.map { dbMatch -> dbMatch.asCourt() } }
        val playersFlow = db.playerRepo().getAll().map { it.map { dbMatch -> dbMatch.asPlayer() } }
        val matchesFlow = db.matchRepo().getAll().map { it.map { dbMatch -> dbMatch.asMatch() } }

        databaseState = courtsFlow
                .combine(playersFlow) { courts, matches -> courts to matches }
                .combine(matchesFlow) { (courts, players), matches -> ModelState(courts, matches, players) }
                .shareIn(viewModelScope, SharingStarted.Eagerly, 1)
    }

    /*
     * Datastore
     */
    val preferences = clavaDatastore
            .getPreferences()
            .shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    init {
        viewModelScope.launch(context = Dispatchers.Default) {
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
            is MainEffect -> {
                if (intent is MainEffect.Navigate && intent.destination == MainNavRoute.HELP_SCREEN) {
                    val currentState = getScreenState(MainNavRoute.HELP_SCREEN) as HelpState
                    MainNavRoute.HELP_SCREEN.updateScreenState(currentState.copy(screen = intent.currentRoute))
                }

                viewModelScope.launch(context = Dispatchers.Default) { _effects.emit(intent) }
            }
            is DatabaseIntent -> viewModelScope.launch {
                intent.handle(
                        currentTime.latest(),
                        databaseState.latest(),
                        db,
                        preferences.latest().prependCourt,
                )
            }
            is DataStoreIntent -> viewModelScope.launch {
                clavaDatastore.handle(intent, preferences.latest())
            }

            /*
             * Screens
             */
            is DrawerIntent -> intent.handle { handleIntent(it) }
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
}

