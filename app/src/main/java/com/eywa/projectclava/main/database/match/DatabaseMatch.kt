package com.eywa.projectclava.main.database.match

import androidx.room.*
import com.eywa.projectclava.main.database.court.DatabaseCourt
import com.eywa.projectclava.main.database.player.DatabasePlayer
import com.eywa.projectclava.main.model.MatchState
import java.util.*

@Entity(
        tableName = DatabaseMatch.TABLE_NAME,
        foreignKeys = [
            ForeignKey(
                    entity = DatabaseCourt::class,
                    parentColumns = ["id"],
                    childColumns = ["courtId"],
                    onDelete = ForeignKey.SET_NULL,
            )
        ],
)
data class DatabaseMatch(
        @PrimaryKey(autoGenerate = true) val id: Int,

        /**
         * Identifies the type of [MatchState]
         */
        val stateType: String,

        /**
         * All [MatchState]s have a date used for something
         */
        val stateDate: Calendar,

        /**
         * Used only by [MatchState.Paused]
         */
        val stateSecondsLeft: Long?,

        /**
         * Used only by [MatchState.InProgressOrComplete]
         */
        @ColumnInfo(index = true)
        val courtId: Int?,
) {

    companion object {
        const val TABLE_NAME = "matches"
    }
}

data class DatabaseMatchFull(
        @Embedded val match: DatabaseMatch,
        @Relation(
                parentColumn = "id",
                entityColumn = "name",
                associateBy = Junction(
                        value = DatabaseMatchPlayer::class,
                        parentColumn = "matchId",
                        entityColumn = "playerId",
                )
        )
        val players: List<DatabasePlayer>,
        @Relation(
                parentColumn = "courtId",
                entityColumn = "id",
        )
        val court: DatabaseCourt?
)