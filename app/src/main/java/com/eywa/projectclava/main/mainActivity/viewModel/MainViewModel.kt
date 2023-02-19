package com.eywa.projectclava.main.mainActivity.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eywa.projectclava.main.common.ClavaMediaPlayer
import com.eywa.projectclava.main.common.ClavaNotifications
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
import com.eywa.projectclava.main.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
    private val clavaMediaPlayer: ClavaMediaPlayer,
    private val clavaNotifications: ClavaNotifications,
) : ViewModel() {
    val currentTime = MutableSharedFlow<Calendar>(1)
    private var screenState by mutableStateOf(mapOf<NavRoute, ScreenState>())

    private val _effects: MutableStateFlow<MainEffect?> = MutableStateFlow(null)
    val effects: StateFlow<MainEffect?> = _effects

    private var isBackgrounded = false

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
        clavaNotifications.createNotificationChannel()

        viewModelScope.launch(context = Dispatchers.Default) {
            while (true) {
                currentTime.emit(Calendar.getInstance(Locale.getDefault()))
                delay(1000)
            }
        }

        viewModelScope.launch(context = Dispatchers.Default) {
            while (true) {
                try {
                    val time = currentTime.latest()
                    val modelState = databaseState.latest()

                    modelState.matches
                        .filter {
                            val state = (it.state as? MatchState.OnCourt) ?: return@filter false
                            !state.soundPlayed && state.getTimeLeft(time).isEndingSoon(10)
                        }
                        .takeIf { it.isNotEmpty() }
                        ?.let {
                            DatabaseIntent.SoundHappened(it).handle(time, modelState, db, true)
                            if (isBackgrounded) {
                                clavaNotifications.createNotification(
                                    it.first().id,
                                    it.first().court!!.id,
                                )
                            } else {
                                clavaMediaPlayer.playMatchFinishedSound()
                            }
                        }
                } catch (_: NoSuchElementException) {
                    // Thrown by latest()
                }

                delay(3000)
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
            is EffectHandledIntent -> {
                _effects.update { currentEffect -> currentEffect.takeIf { it != intent.effect } }
            }
            is MainEffect -> {
                if (intent is MainEffect.Navigate && intent.destination == MainNavRoute.HELP_SCREEN) {
                    val currentState = getScreenState(MainNavRoute.HELP_SCREEN) as HelpState
                    MainNavRoute.HELP_SCREEN.updateScreenState(currentState.copy(screen = intent.currentRoute))
                }

                viewModelScope.launch(context = Dispatchers.Default) { _effects.update { intent } }
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

    fun onPause() {
        Log.i("echDebug", "Pause")
        isBackgrounded = true
    }

    fun onResume() {
        Log.i("echDebug", "Resume")
        isBackgrounded = false
    }
}

