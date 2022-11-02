package com.eywa.projectclava.main.mainActivity.screens.archivedPlayers

import com.eywa.projectclava.main.mainActivity.screens.ScreenState
import com.eywa.projectclava.main.model.Player
import com.eywa.projectclava.main.ui.sharedUi.EditDialogState


data class ArchivedPlayersState(
        override val editItemName: String = "",
        override val editNameIsDirty: Boolean = false,
        override val editDialogOpenFor: Player? = null,
) : ScreenState, EditDialogState<Player> {
    override fun editItemCopy(
            editItemName: String,
            editNameIsDirty: Boolean,
            editDialogOpenFor: Player?
    ) = copy(
            editItemName = editItemName,
            editNameIsDirty = editNameIsDirty,
            editDialogOpenFor = editDialogOpenFor
    )
}