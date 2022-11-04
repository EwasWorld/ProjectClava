package com.eywa.projectclava.main.features.screens.archivedPlayers

import com.eywa.projectclava.main.features.screens.ScreenState
import com.eywa.projectclava.main.features.ui.confirmDialog.ConfirmDialogState
import com.eywa.projectclava.main.features.ui.editNameDialog.EditDialogState
import com.eywa.projectclava.main.model.Player


data class ArchivedPlayersState(
        override val editItemName: String = "",
        override val editNameIsDirty: Boolean = false,
        override val editDialogOpenFor: Player? = null,
        val deletePlayerDialogState: ConfirmDialogState<Player>? = null,
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