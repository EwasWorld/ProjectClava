package com.eywa.projectclava.main.features.screens.manage.setupPlayer

import com.eywa.projectclava.main.database.DatabaseIntent
import com.eywa.projectclava.main.features.screens.ScreenIntent
import com.eywa.projectclava.main.features.screens.manage.SetupListIntent
import com.eywa.projectclava.main.features.screens.manage.SetupListIntent.SetupListItemIntent.*
import com.eywa.projectclava.main.features.screens.manage.SetupListIntent.SetupListStateIntent.AddNameCleared
import com.eywa.projectclava.main.features.screens.manage.SetupListState
import com.eywa.projectclava.main.mainActivity.MainNavRoute
import com.eywa.projectclava.main.mainActivity.viewModel.CoreIntent
import com.eywa.projectclava.main.model.Player

fun SetupListIntent.toSetupPlayerIntent() = when (this) {
    is SetupListIntent.SetupListStateIntent -> SetupPlayerIntent.ScreenIntent(this)
    AddItemSubmitted -> SetupPlayerIntent.AddPlayerSubmitted
    is UnarchiveItemSubmitted<*> -> SetupPlayerIntent.UnarchivePlayerSubmitted(item as Player)
    is ItemClicked<*> -> SetupPlayerIntent.PlayerClicked(item as Player)
    is ItemDeleted<*> -> SetupPlayerIntent.PlayerArchived(item as Player)
    is ItemNameUpdated<*> -> SetupPlayerIntent.PlayerNameUpdated(item as Player, newName)
}

sealed class SetupPlayerIntent : ScreenIntent<SetupListState<Player>> {
    override val screen = MainNavRoute.ADD_PLAYER

    object AddPlayerSubmitted : SetupPlayerIntent()
    data class UnarchivePlayerSubmitted(val player: Player) : SetupPlayerIntent()
    data class PlayerArchived(val player: Player) : SetupPlayerIntent()
    data class PlayerClicked(val player: Player) : SetupPlayerIntent()
    data class PlayerNameUpdated(val player: Player, val newName: String) : SetupPlayerIntent()

    data class ScreenIntent(val value: SetupListIntent.SetupListStateIntent) : SetupPlayerIntent()

    override fun handle(
            currentState: SetupListState<Player>,
            handle: (CoreIntent) -> Unit,
            newStateListener: (SetupListState<Player>) -> Unit
    ) {
        when (this) {
            AddPlayerSubmitted -> {
                handle(DatabaseIntent.AddPlayer(currentState.addItemName.trim()))
                ScreenIntent(AddNameCleared).handle(currentState, handle, newStateListener)
            }
            is UnarchivePlayerSubmitted -> {
                handle(DatabaseIntent.UpdatePlayer(player.copy(isArchived = false, isPresent = true)))
            }
            is PlayerClicked -> {
                handle(DatabaseIntent.UpdatePlayer(player.copy(isPresent = !player.isPresent)))
            }
            is PlayerArchived -> handle(DatabaseIntent.UpdatePlayer(player.copy(isArchived = true)))
            is ScreenIntent -> value.handle(
                    currentState = currentState,
                    handleCore = handle,
                    handleFollowOn = { it.toSetupPlayerIntent().handle(currentState, handle, newStateListener) },
                    newStateListener = newStateListener,
            )
            is PlayerNameUpdated -> handle(DatabaseIntent.UpdatePlayer(player.copy(name = newName.trim())))
        }
    }
}
