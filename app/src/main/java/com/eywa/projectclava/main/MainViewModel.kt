package com.eywa.projectclava.main

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
import com.eywa.projectclava.main.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val playerRepo: PlayerRepo
    private val courtRepo: CourtRepo
    private val matchRepo: MatchRepo

    val currentTime = MutableSharedFlow<Calendar>(1)

    // TODO Store these in shared preferences
    var defaultMatchTime by mutableStateOf(15 * 60)
        private set
    var defaultTimeToAdd by mutableStateOf(2 * 60)
        private set
    var clubNightStartTime: Calendar by mutableStateOf(Calendar.getInstance(Locale.getDefault()))
        private set

    init {
        val db = ClavaDatabase.getInstance(application)
        playerRepo = db.playerRepo()
        courtRepo = db.courtRepo()
        matchRepo = db.matchRepo()

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

    fun updateDefaultMatchTime(timeInSeconds: Int) {
        defaultMatchTime = timeInSeconds
    }

    fun updateDefaultTimeTimeToAdd(timeInSeconds: Int) {
        defaultTimeToAdd = timeInSeconds
    }

    fun updateClubNightStartTime(calendar: Calendar) {
        clubNightStartTime = calendar
    }

    fun updateClubNightStartTime(
            day: Int? = null,
            month: Int? = null,
            year: Int? = null,
            hours: Int? = null,
            minutes: Int? = null,
    ) {
        require(
                day != null
                        || month != null
                        || year != null
                        || hours != null
                        || minutes != null
        ) { "No new values set" }

        val newClubNightStartTime = clubNightStartTime.clone() as Calendar
        day?.let { newClubNightStartTime.set(Calendar.DATE, day) }
        month?.let { newClubNightStartTime.set(Calendar.MONTH, month) }
        year?.let { newClubNightStartTime.set(Calendar.YEAR, year) }
        hours?.let { newClubNightStartTime.set(Calendar.HOUR_OF_DAY, hours) }
        minutes?.let { newClubNightStartTime.set(Calendar.MINUTE, minutes) }
        clubNightStartTime = newClubNightStartTime
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

    fun deleteAllMatches() = viewModelScope.launch { matchRepo.deleteAll() }
}