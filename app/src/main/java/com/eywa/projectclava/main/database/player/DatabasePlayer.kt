package com.eywa.projectclava.main.database.player

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = DatabasePlayer.TABLE_NAME)
data class DatabasePlayer(
        @PrimaryKey(autoGenerate = true) val id: Int,
        val name: String,
        val isPresent: Boolean = true,
) {
    companion object {
        const val TABLE_NAME = "players"
    }
}