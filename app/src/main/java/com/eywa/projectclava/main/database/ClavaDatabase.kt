package com.eywa.projectclava.main.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.eywa.projectclava.main.common.asInstant
import com.eywa.projectclava.main.database.court.CourtDao
import com.eywa.projectclava.main.database.court.CourtRepo
import com.eywa.projectclava.main.database.court.DatabaseCourt
import com.eywa.projectclava.main.database.match.DatabaseMatch
import com.eywa.projectclava.main.database.match.DatabaseMatchPlayer
import com.eywa.projectclava.main.database.match.MatchDao
import com.eywa.projectclava.main.database.match.MatchPlayerDao
import com.eywa.projectclava.main.database.match.MatchRepo
import com.eywa.projectclava.main.database.player.DatabasePlayer
import com.eywa.projectclava.main.database.player.PlayerDao
import com.eywa.projectclava.main.database.player.PlayerRepo
import kotlin.time.Instant

@Database(
    entities = [
        DatabaseCourt::class,
        DatabaseMatch::class,
        DatabaseMatchPlayer::class,
        DatabasePlayer::class,
    ],
    version = 4,
    exportSchema = true, // Needs a schema location in the build.gradle too to export!
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 3, to = 4),
    ]
)
@TypeConverters(ClavaDatabase.Converters::class)
abstract class ClavaDatabase : RoomDatabase() {

    abstract fun courtDao(): CourtDao
    abstract fun matchDao(): MatchDao
    abstract fun matchPlayerDao(): MatchPlayerDao
    abstract fun playerDao(): PlayerDao

    fun courtRepo() = CourtRepo(courtDao())
    fun playerRepo() = PlayerRepo(playerDao())
    fun matchRepo() = MatchRepo(matchDao(), matchPlayerDao())

    class Converters {
        @TypeConverter
        fun fromTimestamp(value: Long?): Instant? = value.asInstant()

        @TypeConverter
        fun dateToTimestamp(date: Instant?): Long? {
            return date?.toEpochMilliseconds()
        }
    }

    object Migrations {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val tableName = "match_players"
                val tempName = "match_players_new"
                val matchCol = "matchId"
                val playerCol = "playerId"

                database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `$tempName` (
                                `$matchCol` INTEGER NOT NULL, 
                                `$playerCol` INTEGER NOT NULL, 
                                PRIMARY KEY(`$matchCol`, `$playerCol`),
                                FOREIGN KEY(`$matchCol`) 
                                        REFERENCES `matches`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                                FOREIGN KEY(`$playerCol`) 
                                        REFERENCES `players`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                        )
                        """.trimIndent()
                )
                database.execSQL(
                        """
                        INSERT INTO `$tempName` ($matchCol, $playerCol) 
                                SELECT $matchCol, $playerCol 
                                FROM $tableName
                        """
                )
                database.execSQL("DROP TABLE $tableName")
                database.execSQL("ALTER TABLE $tempName RENAME TO $tableName")
            }
        }
    }

    companion object {
        const val DATABASE_NAME = "clava_database"
    }
}
