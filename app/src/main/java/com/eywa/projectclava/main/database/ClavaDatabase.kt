package com.eywa.projectclava.main.database

import android.content.Context
import android.os.Build
import androidx.room.*
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
        version = 1,
        exportSchema = true
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
        fun fromTimestamp(value: Long?): Calendar? {
            return value?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Calendar.Builder().setInstant(it).build()
                }
                else {
                    val date = Date(it)
                    Calendar.getInstance().apply {
                        set(
                                date.year,
                                date.month,
                                date.date,
                                date.hours,
                                date.minutes,
                                date.seconds,
                        )
                    }
                }
            }
        }

        @TypeConverter
        fun dateToTimestamp(date: Calendar?): Long? {
            return date?.timeInMillis
        }
    }

    companion object {
        private const val DATABASE_NAME = "clava_database"

        private var dbLock = Object()
        private var INSTANCE: ClavaDatabase? = null

        fun getInstance(applicationContext: Context): ClavaDatabase {
            synchronized(dbLock) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            applicationContext,
                            ClavaDatabase::class.java, DATABASE_NAME
                    ).build()
                }
                return INSTANCE!!
            }
        }
    }
}