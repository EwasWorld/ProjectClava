package com.eywa.projectclava.main.database.player

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM ${DatabasePlayer.TABLE_NAME}")
    fun getAll(): Flow<List<DatabasePlayer>>

    @Insert
    suspend fun insertAll(vararg players: DatabasePlayer)

    @Delete
    suspend fun delete(player: DatabasePlayer)

    @Update
    suspend fun update(vararg players: DatabasePlayer)
}