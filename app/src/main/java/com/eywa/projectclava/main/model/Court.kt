package com.eywa.projectclava.main.model

import com.eywa.projectclava.main.database.court.DatabaseCourt
import com.eywa.projectclava.main.mainActivity.screens.manage.helperClasses.SetupListItem

fun DatabaseCourt.asCourt() = Court(id, name, canBeUsed)

@Suppress("ConvertArgumentToSet") // It already is a set... silly compiler
fun Iterable<Court>.getAvailable(matches: Iterable<Match>?) =
        filter { it.canBeUsed }
                .minus(matches?.getCourtsInUse()?.toSet() ?: setOf())
                .takeIf { it.isNotEmpty() }

data class Court(
        val id: Int,
        override val name: String,
        val canBeUsed: Boolean = true,
) : SetupListItem {
    override val enabled: Boolean
        get() = canBeUsed

    fun asDatabaseCourt() = DatabaseCourt(id, name, canBeUsed)
}
