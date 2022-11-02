package com.eywa.projectclava.main.mainActivity.screens.archivedPlayers

import com.eywa.projectclava.main.database.DatabaseIntent
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.mainActivity.screens.ScreenIntent
import com.eywa.projectclava.main.mainActivity.viewModel.CoreIntent
import com.eywa.projectclava.main.model.Player
import com.eywa.projectclava.main.ui.sharedUi.EditDialogIntent


fun EditDialogIntent.toArchivedPlayersIntent() =
        ArchivedPlayersIntent.EditItemStateIntent(this)


sealed class ArchivedPlayersIntent : ScreenIntent<ArchivedPlayersState> {
    override val screen: NavRoute = NavRoute.ARCHIVED_PLAYERS

    data class EditItemStateIntent(val value: EditDialogIntent) : ArchivedPlayersIntent()

    data class ItemDeleted(val value: Player) : ArchivedPlayersIntent()
    data class PlayerUnarchived(val value: Player) : ArchivedPlayersIntent()

    override fun handle(
            currentState: ArchivedPlayersState,
            handle: (CoreIntent) -> Unit,
            newStateListener: (ArchivedPlayersState) -> Unit
    ) {
        when (this) {
            is EditItemStateIntent -> value.handle(currentState, newStateListener) { editItem, newName ->
                handle(DatabaseIntent.UpdatePlayer(editItem.copy(name = newName.trim())))
            }
            is ItemDeleted -> handle(DatabaseIntent.DeletePlayer(value))
            is PlayerUnarchived -> handle(DatabaseIntent.UpdatePlayer(value.copy(isArchived = false)))
        }
    }
}