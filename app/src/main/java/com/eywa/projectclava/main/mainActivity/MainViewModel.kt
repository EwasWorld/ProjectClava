package com.eywa.projectclava.main.mainActivity

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.eywa.projectclava.main.common.asCalendar
import com.eywa.projectclava.main.database.ClavaDatabase
import com.eywa.projectclava.main.database.court.CourtRepo
import com.eywa.projectclava.main.database.match.DatabaseMatchPlayer
import com.eywa.projectclava.main.database.match.MatchRepo
import com.eywa.projectclava.main.database.player.PlayerRepo
import com.eywa.projectclava.main.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*

private const val USER_PREFERENCES_NAME = "clava_user_preferences"

// TODO Dependency injection?
private val Context.dataStore by preferencesDataStore(name = USER_PREFERENCES_NAME)

private object PreferencesKeys {
    val OVERRUN_INDICATOR_THRESHOLD = intPreferencesKey("overrun_indicator_threshold")
    val DEFAULT_MATCH_TIME = intPreferencesKey("default_match_time")
    val DEFAULT_TIME_TO_ADD = intPreferencesKey("default_time_to_add")
    val CLUB_NIGHT_START_TIME = longPreferencesKey("club_night_start_time")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = (application.applicationContext).dataStore

    private val playerRepo: PlayerRepo
    private val courtRepo: CourtRepo
    private val matchRepo: MatchRepo

    val currentTime = MutableSharedFlow<Calendar>(1)

    var overrunIndicatorThreshold by mutableStateOf(10)
        private set
    var defaultMatchTime by mutableStateOf(15 * 60)
        private set
    var defaultTimeToAdd by mutableStateOf(2 * 60)
        private set
    var clubNightStartTime: Calendar by mutableStateOf(
            // Default to 4am today
            Calendar.getInstance(Locale.getDefault()).apply {
                set(Calendar.HOUR_OF_DAY, 4)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
    )
        private set

    init {
        val db = ClavaDatabase.getInstance(application)
        playerRepo = db.playerRepo()
        courtRepo = db.courtRepo()
        matchRepo = db.matchRepo()

        viewModelScope.launch {
            dataStore.data.collect { preference ->
                preference[PreferencesKeys.OVERRUN_INDICATOR_THRESHOLD]?.let {
                    overrunIndicatorThreshold = it
                }
                preference[PreferencesKeys.DEFAULT_MATCH_TIME]?.let {
                    defaultMatchTime = it
                }
                preference[PreferencesKeys.DEFAULT_TIME_TO_ADD]?.let {
                    defaultTimeToAdd = it
                }
                preference[PreferencesKeys.CLUB_NIGHT_START_TIME]?.let {
                    clubNightStartTime = it.asCalendar()!!
                }
            }
        }

        viewModelScope.launch {
            while (true) {
                currentTime.emit(Calendar.getInstance(Locale.getDefault()))
                delay(1000)
            }
        }
    }

    var courts = courtRepo.getAll().map { it.map { dbMatch -> dbMatch.asCourt() } }
    var players = playerRepo.getAll().map { it.map { dbMatch -> dbMatch.asPlayer() } }
    val matches = matchRepo.getAll().map { it.map { dbMatch -> dbMatch.asMatch() } }

    fun mainHandle(action: MainIntent) {
        when (action) {
            is MainIntent.DrawerIntent -> handle(action)
        }
    }

    private fun handle(action: MainIntent.DrawerIntent) {
        when (action) {
            is MainIntent.DrawerIntent.Navigate -> throw NotImplementedError()
            is MainIntent.DrawerIntent.UpdateClubNightStartTime -> updateClubNightStartTime(action)
            is MainIntent.DrawerIntent.UpdateClubNightStartTimeCalendar -> {
                clubNightStartTime = action.value
                viewModelScope.launch {
                    dataStore.edit {
                        it[PreferencesKeys.CLUB_NIGHT_START_TIME] = action.value.timeInMillis
                    }
                }
            }
            is MainIntent.DrawerIntent.UpdateDefaultMatchTime -> {
                defaultMatchTime = action.value
                viewModelScope.launch {
                    dataStore.edit {
                        it[PreferencesKeys.DEFAULT_MATCH_TIME] = action.value
                    }
                }
            }
            is MainIntent.DrawerIntent.UpdateDefaultTimeToAdd -> {
                defaultTimeToAdd = action.value
                viewModelScope.launch {
                    dataStore.edit {
                        it[PreferencesKeys.DEFAULT_TIME_TO_ADD] = action.value
                    }
                }
            }
            is MainIntent.DrawerIntent.UpdateOverrunIndicatorThreshold -> {
                overrunIndicatorThreshold = action.value
                viewModelScope.launch {
                    dataStore.edit {
                        it[PreferencesKeys.OVERRUN_INDICATOR_THRESHOLD] = action.value
                    }
                }
            }
            MainIntent.DrawerIntent.DeleteAllMatches -> viewModelScope.launch {
                matchRepo.deleteAll()
            }
            is MainIntent.DrawerIntent.DeleteMatch -> viewModelScope.launch {
                matchRepo.delete(action.match.asDatabaseMatch())
            }
            is MainIntent.DrawerIntent.UpdatePlayers -> viewModelScope.launch {
                playerRepo.update(*action.players.map { it.asDatabasePlayer() }.toTypedArray())
            }
            is MainIntent.DrawerIntent.UpdateMatch -> viewModelScope.launch {
                matchRepo.update(action.match.asDatabaseMatch())
            }
        }
    }

    private fun updateClubNightStartTime(value: MainIntent.DrawerIntent.UpdateClubNightStartTime) {
        val newClubNightStartTime = clubNightStartTime.clone() as Calendar
        value.day?.let { newClubNightStartTime.set(Calendar.DATE, value.day) }
        value.month?.let { newClubNightStartTime.set(Calendar.MONTH, value.month) }
        value.year?.let { newClubNightStartTime.set(Calendar.YEAR, value.year) }
        value.hours?.let { newClubNightStartTime.set(Calendar.HOUR_OF_DAY, value.hours) }
        value.minutes?.let { newClubNightStartTime.set(Calendar.MINUTE, value.minutes) }
        handle(MainIntent.DrawerIntent.UpdateClubNightStartTimeCalendar(newClubNightStartTime))
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