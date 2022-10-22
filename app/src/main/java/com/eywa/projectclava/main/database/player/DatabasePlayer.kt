package com.eywa.projectclava.main.database.player

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = DatabasePlayer.TABLE_NAME)
data class DatabasePlayer(
        @PrimaryKey(autoGenerate = true) val id: Int,
        val name: String,
        val isPresent: Boolean = true,
        @ColumnInfo(defaultValue = "0") // Required for auto migration 1 -> 2
        val isArchived: Boolean = false,
) {
    companion object {
        const val TABLE_NAME = "players"
    }
}