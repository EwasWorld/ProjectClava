package com.eywa.projectclava.main.model

import com.eywa.projectclava.main.database.court.DatabaseCourt
import com.eywa.projectclava.main.features.screens.manage.helperClasses.SetupListItem

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

    companion object {
        fun formatName(name: String, prependCourt: Boolean): String {
            val usePrefix = prependCourt && !name.startsWith("court", ignoreCase = true)
            return (if (usePrefix) "Court " else "") + name.trim()
        }
    }
}
