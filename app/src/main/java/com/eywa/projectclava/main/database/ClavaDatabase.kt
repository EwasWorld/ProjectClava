package com.eywa.projectclava.main.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.eywa.projectclava.main.common.asCalendar
import com.eywa.projectclava.main.database.court.CourtDao
import com.eywa.projectclava.main.database.court.CourtRepo
import com.eywa.projectclava.main.database.court.DatabaseCourt
import com.eywa.projectclava.main.database.match.*
import com.eywa.projectclava.main.database.player.DatabasePlayer
import com.eywa.projectclava.main.database.player.PlayerDao
import com.eywa.projectclava.main.database.player.PlayerRepo
import java.util.*

@Database(
        entities = [
            DatabaseCourt::class,
            DatabaseMatch::class,
            DatabaseMatchPlayer::class,
            DatabasePlayer::class,
        ],
        version = 3,
        exportSchema = true, // Needs a schema location in the build.gradle too to export!
        autoMigrations = [
            AutoMigration(from = 1, to = 2),
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
        fun fromTimestamp(value: Long?): Calendar? = value.asCalendar()

        @TypeConverter
        fun dateToTimestamp(date: Calendar?): Long? {
            return date?.timeInMillis
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
        private const val DATABASE_NAME = "clava_database"

        private var dbLock = Object()
        private var INSTANCE: ClavaDatabase? = null

        // TODO Dependency injection
        fun getInstance(applicationContext: Context): ClavaDatabase {
            synchronized(dbLock) {
                if (INSTANCE == null) {
                    INSTANCE = Room
                            .databaseBuilder(
                                    applicationContext,
                                    ClavaDatabase::class.java, DATABASE_NAME
                            )
                            .addMigrations(Migrations.MIGRATION_2_3)
                            .build()
                }
                return INSTANCE!!
            }
        }
    }
}