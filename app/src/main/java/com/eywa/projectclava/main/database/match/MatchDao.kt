package com.eywa.projectclava.main.database.match

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Transaction
    @Query("SELECT * FROM ${DatabaseMatch.TABLE_NAME}")
    fun getAll(): Flow<List<DatabaseMatchFull>>

    @Insert
    suspend fun insert(match: DatabaseMatch): Long

    @Delete
    suspend fun delete(match: DatabaseMatch)

    @Query("DELETE FROM ${DatabaseMatch.TABLE_NAME}")
    suspend fun deleteAll()

    @Update
    suspend fun update(vararg matches: DatabaseMatch)

    @Query("UPDATE ${DatabaseMatch.TABLE_NAME} SET soundPlayed = 1 WHERE id IN (:matchIds)")
    suspend fun updateSoundHappened(matchIds: List<Int>)
}
