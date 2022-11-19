package com.eywa.projectclava.main.database

import com.eywa.projectclava.main.database.DatabaseIntent.*
import com.eywa.projectclava.main.database.court.DatabaseCourt
import com.eywa.projectclava.main.database.match.DatabaseMatchPlayer
import com.eywa.projectclava.main.database.player.DatabasePlayer
import com.eywa.projectclava.main.mainActivity.viewModel.CoreIntent
import com.eywa.projectclava.main.model.*
import java.util.*


sealed interface DatabaseIntent : CoreIntent {
    object DeleteAllData : GlobalIntent

    /*
     * Matches
     */
    data class DeleteMatch(val match: Match) : MatchIntent
    data class DeleteMatchById(val matchId: Int) : MatchIntent
    object DeleteAllMatches : MatchIntent
    data class AddMatch(val players: Iterable<Player>) : MatchIntent

    /*
     * Update match
     */
    data class UpdateMatch(val match: Match) : MatchIntent
    data class AddTimeToMatch(val matchId: Int, val secondsToAdd: Int) : MatchIntent
    data class CompleteMatch(val matchId: Int) : MatchIntent
    data class PauseMatch(val matchId: Int) : MatchIntent
    data class ChangeMatchCourt(val matchId: Int, val court: Court) : MatchIntent
    data class ResumeMatch(val matchId: Int, val court: Court, val resumeTimeSeconds: Int) : MatchIntent
    data class StartMatch(val matchId: Int, val court: Court, val timeSeconds: Int) : MatchIntent

    /*
     * Players
     */
    data class UpdatePlayer(val player: Player) : PlayerIntent
    data class UpdatePlayers(val players: Iterable<Player>) : PlayerIntent
    data class AddPlayer(val name: String) : PlayerIntent
    data class DeletePlayer(val player: Player) : PlayerIntent

    /*
     * Courts
     */
    /**
     * Intent will prepend "Court " if the corresponding preference is set
     */
    data class AddCourt(val name: String) : CourtIntent
    data class UpdateCourt(val court: Court) : CourtIntent
    data class DeleteCourt(val court: Court) : CourtIntent

    suspend fun handle(
            currentTime: Calendar,
            currentState: ModelState,
            db: ClavaDatabase,
            prependCourt: Boolean,
    )
}

private interface GlobalIntent : DatabaseIntent {
    override suspend fun handle(
            currentTime: Calendar,
            currentState: ModelState,
            db: ClavaDatabase,
            prependCourt: Boolean
    ) {
        when (this) {
            DeleteAllData -> db.clearAllTables()
            else -> throw NotImplementedError()
        }
    }
}

private interface MatchIntent : DatabaseIntent {
    override suspend fun handle(
            currentTime: Calendar,
            currentState: ModelState,
            db: ClavaDatabase,
            prependCourt: Boolean,
    ) {
        val matchRepo = db.matchRepo()

        fun matchFromId(id: Int) = currentState.matches.find { it.id == id }!!
        suspend fun update(match: Match) = matchRepo.update(match.asDatabaseMatch())

        when (this) {
            DeleteAllMatches -> matchRepo.deleteAll()
            is DeleteMatch -> matchRepo.delete(match.asDatabaseMatch())
            is AddMatch -> {
                check(players.any()) { "No players in match" }
                val databaseMatch =
                        Match(0, players, MatchState.NotStarted(currentTime)).asDatabaseMatch()
                val matchId = matchRepo.insert(databaseMatch)
                matchRepo.insert(*players.map { DatabaseMatchPlayer(matchId.toInt(), it.id) }.toTypedArray())
            }
            is DeleteMatchById -> matchRepo.delete(matchFromId(matchId).asDatabaseMatch())

            /*
             * Update
             */
            is UpdateMatch -> update(match)
            is AddTimeToMatch -> update(matchFromId(matchId).addTime(currentTime, secondsToAdd))
            is CompleteMatch -> update(matchFromId(matchId).completeMatch(currentTime))
            is PauseMatch -> update(matchFromId(matchId).pauseMatch(currentTime))
            is ResumeMatch -> update(matchFromId(matchId).resumeMatch(currentTime, court, resumeTimeSeconds))
            is ChangeMatchCourt -> update(matchFromId(matchId).changeCourt(court))
            is StartMatch -> update(matchFromId(matchId).startMatch(currentTime, court, timeSeconds))

            else -> throw NotImplementedError()
        }
    }
}

private interface PlayerIntent : DatabaseIntent {
    override suspend fun handle(
            currentTime: Calendar,
            currentState: ModelState,
            db: ClavaDatabase,
            prependCourt: Boolean,
    ) {
        val playerRepo = db.playerRepo()

        when (this) {
            is AddPlayer -> playerRepo.insertAll(DatabasePlayer(0, name))
            is DeletePlayer -> playerRepo.delete(player.asDatabasePlayer())
            is UpdatePlayer -> playerRepo.update(player.asDatabasePlayer())
            is UpdatePlayers -> playerRepo.update(
                    *players.map { it.asDatabasePlayer() }.toTypedArray()
            )
            else -> throw NotImplementedError()
        }
    }
}

private interface CourtIntent : DatabaseIntent {
    override suspend fun handle(
            currentTime: Calendar,
            currentState: ModelState,
            db: ClavaDatabase,
            prependCourt: Boolean,
    ) {
        val courtRepo = db.courtRepo()

        when (this) {
            is AddCourt -> courtRepo.insertAll(DatabaseCourt(0, Court.formatName(name, prependCourt)))
            is DeleteCourt -> courtRepo.delete(court.asDatabaseCourt())
            is UpdateCourt -> courtRepo.update(court.asDatabaseCourt())

            else -> throw NotImplementedError()
        }
    }
}