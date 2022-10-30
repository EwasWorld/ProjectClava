package com.eywa.projectclava.main.mainActivity.screens.archivedPlayers

import com.eywa.projectclava.main.mainActivity.CoreIntent
import com.eywa.projectclava.main.mainActivity.DatabaseIntent
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.mainActivity.screens.ScreenIntent
import com.eywa.projectclava.main.mainActivity.screens.ScreenState
import com.eywa.projectclava.main.model.Player
import com.eywa.projectclava.main.ui.sharedUi.EditDialogIntent
import com.eywa.projectclava.main.ui.sharedUi.EditItemState


sealed class ArchivedPlayersIntent : ScreenIntent<ArchivedPlayersState> {
    override val screen: NavRoute = NavRoute.ARCHIVED_PLAYERS

    data class EditItemStateIntent(val value: EditDialogIntent.EditItemStateIntent) : ArchivedPlayersIntent()
    object EditItemSubmitted : ArchivedPlayersIntent()

    data class ItemDeleted(val value: Player) : ArchivedPlayersIntent()
    data class PlayerUnarchived(val value: Player) : ArchivedPlayersIntent()

    override fun handle(
            currentState: ArchivedPlayersState,
            handle: (CoreIntent) -> Unit,
            newStateListener: (ArchivedPlayersState) -> Unit
    ) {
        when (this) {
            is EditItemStateIntent -> value.handle(currentState, newStateListener)
            EditItemSubmitted -> {
                EditDialogIntent.EditItemSubmitted.handle(
                        currentState,
                        newStateListener
                ) { editItem, newName ->
                    handle(DatabaseIntent.UpdatePlayer(editItem.copy(name = newName.trim())))
                }
            }
            is ItemDeleted -> handle(DatabaseIntent.DeletePlayer(value))
            is PlayerUnarchived -> handle(DatabaseIntent.UpdatePlayer(value.copy(isArchived = false)))
        }
    }
}

fun EditDialogIntent.toArchivedPlayersIntent() = when (this) {
    is EditDialogIntent.EditItemStateIntent -> ArchivedPlayersIntent.EditItemStateIntent(this)
    EditDialogIntent.EditItemSubmitted -> ArchivedPlayersIntent.EditItemSubmitted
}

data class ArchivedPlayersState(
        override val editItemName: String = "",
        override val editNameIsDirty: Boolean = false,
        override val editDialogOpenFor: Player? = null,
) : ScreenState, EditItemState<Player> {
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