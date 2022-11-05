package com.eywa.projectclava.main.features.screens.archivedPlayers

import com.eywa.projectclava.main.database.DatabaseIntent
import com.eywa.projectclava.main.features.screens.ScreenIntent
import com.eywa.projectclava.main.features.ui.confirmDialog.ConfirmDialogIntent
import com.eywa.projectclava.main.features.ui.confirmDialog.ConfirmDialogType
import com.eywa.projectclava.main.features.ui.editNameDialog.EditDialogIntent
import com.eywa.projectclava.main.mainActivity.MainNavRoute
import com.eywa.projectclava.main.mainActivity.viewModel.CoreIntent
import com.eywa.projectclava.main.model.Player


fun EditDialogIntent.toArchivedPlayersIntent() =
        ArchivedPlayersIntent.EditItemIntent(this)

fun ConfirmDialogIntent.toArchivedPlayersIntent() =
        ArchivedPlayersIntent.ConfirmIntent(this)


sealed class ArchivedPlayersIntent : ScreenIntent<ArchivedPlayersState> {
    override val screen = MainNavRoute.ARCHIVED_PLAYERS

    data class EditItemIntent(val value: EditDialogIntent) : ArchivedPlayersIntent()
    data class ConfirmIntent(val value: ConfirmDialogIntent) : ArchivedPlayersIntent()

    data class ItemDeleted(val value: Player) : ArchivedPlayersIntent()
    data class PlayerUnarchived(val value: Player) : ArchivedPlayersIntent()

    override fun handle(
            currentState: ArchivedPlayersState,
            handle: (CoreIntent) -> Unit,
            newStateListener: (ArchivedPlayersState) -> Unit
    ) {
        when (this) {
            is EditItemIntent -> value.handle(currentState, newStateListener) { editItem, newName ->
                handle(DatabaseIntent.UpdatePlayer(editItem.copy(name = newName.trim())))
            }
            is ItemDeleted -> handle(DatabaseIntent.DeletePlayer(value))
            is PlayerUnarchived -> handle(DatabaseIntent.UpdatePlayer(value.copy(isArchived = false)))
            is ConfirmIntent -> value.handle(
                    currentState = currentState.deletePlayerDialogState,
                    newStateListener = { newStateListener(currentState.copy(deletePlayerDialogState = it)) },
                    confirmHandler = { item, actionType ->
                        when (actionType) {
                            ConfirmDialogType.DELETE ->
                                ItemDeleted(item).handle(currentState, handle, newStateListener)
                        }
                    }
            )
        }
    }
}