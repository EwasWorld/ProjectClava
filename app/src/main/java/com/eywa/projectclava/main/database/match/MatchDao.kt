package com.eywa.projectclava.main.database.match

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Transaction
    @Query("SELECT * FROM ${DatabaseMatch.TABLE_NAME}")
    fun getAll(): Flow<List<DatabaseMatchFull>>

    @Insert
    suspend fun insertAll(vararg matches: DatabaseMatch)

    @Delete
    suspend fun delete(match: DatabaseMatch)

    @Update
    suspend fun update(vararg matches: DatabaseMatch)
}