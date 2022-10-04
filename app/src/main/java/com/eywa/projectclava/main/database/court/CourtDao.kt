package com.eywa.projectclava.main.database.court

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CourtDao {
    @Query("SELECT * FROM ${DatabaseCourt.TABLE_NAME}")
    fun getAll(): Flow<List<DatabaseCourt>>

    @Insert
    suspend fun insertAll(vararg courts: DatabaseCourt)

    @Delete
    suspend fun delete(court: DatabaseCourt)

    @Update
    suspend fun update(vararg courts: DatabaseCourt)
}