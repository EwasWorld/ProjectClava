package com.eywa.projectclava.main.database.match

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update

@Dao
interface MatchPlayerDao {
    @Insert
    fun insertAll(vararg matchPlayers: DatabaseMatchPlayer)

    @Delete
    suspend fun delete(matchPlayer: DatabaseMatchPlayer)

    @Update
    suspend fun update(vararg matchPlayers: DatabaseMatchPlayer)
}