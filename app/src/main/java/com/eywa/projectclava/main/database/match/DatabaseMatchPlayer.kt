package com.eywa.projectclava.main.database.match

import androidx.room.Entity

@Entity(tableName = DatabaseMatchPlayer.TABLE_NAME, primaryKeys = ["matchId", "playerId"])
data class DatabaseMatchPlayer(
        val matchId: Int,
        val playerId: Int,
) {
    companion object {
        const val TABLE_NAME = "match_players"
    }
}