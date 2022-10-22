package com.eywa.projectclava.main.database.match

import androidx.room.Entity
import androidx.room.ForeignKey
import com.eywa.projectclava.main.database.player.DatabasePlayer

@Entity(
        tableName = DatabaseMatchPlayer.TABLE_NAME,
        primaryKeys = ["matchId", "playerId"],
        foreignKeys = [
            ForeignKey(
                    entity = DatabaseMatch::class,
                    parentColumns = ["id"],
                    childColumns = ["matchId"],
                    onDelete = ForeignKey.CASCADE,
            ),
            ForeignKey(
                    entity = DatabasePlayer::class,
                    parentColumns = ["id"],
                    childColumns = ["playerId"],
                    onDelete = ForeignKey.CASCADE,
            )
        ]
)
data class DatabaseMatchPlayer(
        val matchId: Int,
        val playerId: Int,
) {
    companion object {
        const val TABLE_NAME = "match_players"
    }
}