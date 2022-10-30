package com.eywa.projectclava.main.mainActivity.screens.manage

import androidx.compose.runtime.Composable
import com.eywa.projectclava.main.mainActivity.CoreIntent
import com.eywa.projectclava.main.mainActivity.DatabaseIntent
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.mainActivity.screens.ScreenIntent
import com.eywa.projectclava.main.model.*
import com.eywa.projectclava.main.ui.sharedUi.EditDialogIntent

sealed class AddPlayerIntent : ScreenIntent<SetupListState<Player>> {
    override val screen: NavRoute = NavRoute.ADD_PLAYER

    object AddPlayerSubmitted : AddPlayerIntent()
    object EditPlayerSubmitted : AddPlayerIntent()
    data class PlayerArchived(val player: Player) : AddPlayerIntent()
    data class PlayerClicked(val player: Player) : AddPlayerIntent()

    data class ScreenIntent(val value: SetupListIntent.SetupListStateIntent) : AddPlayerIntent()

    override fun handle(
            currentState: SetupListState<Player>,
            handle: (CoreIntent) -> Unit,
            newStateListener: (SetupListState<Player>) -> Unit
    ) {
        when (this) {
            AddPlayerSubmitted -> {
                handle(DatabaseIntent.AddPlayer(currentState.addItemName.trim()))
                SetupListIntent.SetupListStateIntent.AddNameCleared.handle(currentState, handle, newStateListener)
            }
            is EditPlayerSubmitted -> {
                EditDialogIntent.EditItemSubmitted.handle(
                        currentState,
                        newStateListener
                ) { editItem, newName ->
                    handle(DatabaseIntent.UpdatePlayer(editItem.copy(name = newName.trim())))
                }
            }
            is PlayerClicked -> {
                handle(DatabaseIntent.UpdatePlayer(player.copy(isPresent = !player.isPresent)))
            }
            is PlayerArchived -> handle(DatabaseIntent.UpdatePlayer(player.copy(isArchived = true)))
            is ScreenIntent -> value.handle(currentState, handle, newStateListener)
        }
    }
}

private fun SetupListIntent.toAddPlayerIntent() = when (this) {
    is SetupListIntent.SetupListStateIntent -> AddPlayerIntent.ScreenIntent(this)
    SetupListIntent.SetupListItemIntent.AddItemSubmitted -> AddPlayerIntent.AddPlayerSubmitted
    SetupListIntent.SetupListItemIntent.EditItemSubmitted -> AddPlayerIntent.EditPlayerSubmitted
    is SetupListIntent.SetupListItemIntent.ItemClicked<*> -> AddPlayerIntent.PlayerClicked(value as Player)
    is SetupListIntent.SetupListItemIntent.ItemDeleted<*> -> AddPlayerIntent.PlayerArchived(value as Player)
}

@Composable
fun SetupPlayersScreen(
        state: SetupListState<Player>,
        databaseState: DatabaseState,
        isSoftKeyboardOpen: Boolean,
        getTimeRemaining: Match.() -> TimeRemaining?,
        listener: (AddPlayerIntent) -> Unit,
) {
    SetupListScreen(
            setupListSettings = SetupListSettings.PLAYERS,
            state = state,
            isSoftKeyboardOpen = isSoftKeyboardOpen,
            items = databaseState.players.filter { !it.isArchived },
            getMatch = { player ->
                databaseState.matches
                        .filter { match -> match.players.any { player.name == it.name } }
                        .getPlayerColouringMatch()
            },
            nameIsDuplicate = { newName, nameOfItemBeingEdited ->
                newName != nameOfItemBeingEdited && databaseState.players.any { it.name == newName.trim() }
            },
            getTimeRemaining = getTimeRemaining,
            listener = { listener(it.toAddPlayerIntent()) },
    )
}
