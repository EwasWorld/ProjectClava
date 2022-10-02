package com.eywa.projectclava.main.model

import com.eywa.projectclava.main.ui.sharedUi.SetupListItem
import java.util.*

data class Player(
        override val name: String,
        val lastPlayed: Calendar? = null,
        val isPresent: Boolean = true,
) : SetupListItem {
    override val enabled: Boolean
        get() = isPresent
}