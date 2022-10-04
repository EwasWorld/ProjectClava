package com.eywa.projectclava.main.database.court

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = DatabaseCourt.TABLE_NAME)
data class DatabaseCourt(
        @PrimaryKey(autoGenerate = true) val id: Int,
        val name: String,
        val canBeUsed: Boolean = true,
) {
    companion object {
        const val TABLE_NAME = "courts"
    }
}