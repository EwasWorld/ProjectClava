package com.eywa.projectclava.main.model

import com.eywa.projectclava.main.database.player.DatabasePlayer
import com.eywa.projectclava.main.mainActivity.screens.manage.helperClasses.SetupListItem

fun DatabasePlayer.asPlayer() = Player(id, name, isPresent, isArchived)

data class Player(
        val id: Int,
        override val name: String,
        val isPresent: Boolean = true,
        val isArchived: Boolean = false,
) : SetupListItem {
    override val enabled: Boolean
        get() = isPresent && !isArchived

    fun asDatabasePlayer() = DatabasePlayer(id, name, isPresent, isArchived)
}
