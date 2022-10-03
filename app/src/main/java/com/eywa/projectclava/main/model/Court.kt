package com.eywa.projectclava.main.model

import com.eywa.projectclava.main.ui.sharedUi.SetupListItem

data class Court(
        val number: Int,
        val canBeUsed: Boolean = true,
) : SetupListItem {
    override val name: String
        get() = "Court $number"
    override val enabled: Boolean
        get() = canBeUsed
}
