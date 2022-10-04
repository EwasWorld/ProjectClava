package com.eywa.projectclava.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.eywa.projectclava.main.database.ClavaDatabase
import com.eywa.projectclava.main.database.court.CourtRepo
import com.eywa.projectclava.main.database.match.MatchRepo
import com.eywa.projectclava.main.database.player.PlayerRepo
import com.eywa.projectclava.main.model.Player
import com.eywa.projectclava.main.model.asCourt
import com.eywa.projectclava.main.model.asMatch
import com.eywa.projectclava.main.model.asPlayer
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val playerRepo: PlayerRepo
    private val courtRepo: CourtRepo
    private val matchRepo: MatchRepo

    init {
        val db = ClavaDatabase.getInstance(application)
        playerRepo = db.playerRepo()
        courtRepo = db.courtRepo()
        matchRepo = db.matchRepo()
    }

    var courts = courtRepo.getAll().map { it.map { dbMatch -> dbMatch.asCourt() } }
    var players = playerRepo.getAll().map { it.map { dbMatch -> dbMatch.asPlayer() } }
    val matches = matchRepo.getAll().map { it.map { dbMatch -> dbMatch.asMatch() } }

    fun addPlayer(playerName: String) = viewModelScope.launch {
        playerRepo.insertAll(Player(0, playerName).asDatabasePlayer())
    }

    fun updatePlayer(player: Player) = viewModelScope.launch {
        playerRepo.update(player.asDatabasePlayer())
    }

    fun deletePlayer(player: Player) = viewModelScope.launch {
        playerRepo.delete(player.asDatabasePlayer())
    }
}