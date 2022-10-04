package com.eywa.projectclava.main.model

import com.eywa.projectclava.main.database.player.DatabasePlayer
import com.eywa.projectclava.main.ui.sharedUi.SetupListItem

fun DatabasePlayer.asPlayer() = Player(id, name, isPresent)

data class Player(
        val id: Int,
        override val name: String,
        val isPresent: Boolean = true,
) : SetupListItem {
    override val enabled: Boolean
        get() = isPresent

    fun asDatabasePlayer() = DatabasePlayer(id, name, isPresent)
}
