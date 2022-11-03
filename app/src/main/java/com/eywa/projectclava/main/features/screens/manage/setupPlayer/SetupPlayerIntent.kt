package com.eywa.projectclava.main.features.screens.manage.setupPlayer

import com.eywa.projectclava.main.database.DatabaseIntent
import com.eywa.projectclava.main.features.screens.ScreenIntent
import com.eywa.projectclava.main.features.screens.manage.SetupListIntent
import com.eywa.projectclava.main.features.screens.manage.SetupListState
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.mainActivity.viewModel.CoreIntent
import com.eywa.projectclava.main.model.Player

fun SetupListIntent.toSetupPlayerIntent() = when (this) {
    is SetupListIntent.SetupListStateIntent -> SetupPlayerIntent.ScreenIntent(this)
    SetupListIntent.SetupListItemIntent.AddItemSubmitted -> SetupPlayerIntent.AddPlayerSubmitted
    is SetupListIntent.SetupListItemIntent.ItemClicked<*> -> SetupPlayerIntent.PlayerClicked(value as Player)
    is SetupListIntent.SetupListItemIntent.ItemDeleted<*> -> SetupPlayerIntent.PlayerArchived(value as Player)
}

sealed class SetupPlayerIntent : com.eywa.projectclava.main.features.screens.ScreenIntent<SetupListState<Player>> {
    override val screen: NavRoute = NavRoute.ADD_PLAYER

    object AddPlayerSubmitted : SetupPlayerIntent()
    data class PlayerArchived(val player: Player) : SetupPlayerIntent()
    data class PlayerClicked(val player: Player) : SetupPlayerIntent()

    data class ScreenIntent(val value: SetupListIntent.SetupListStateIntent) : SetupPlayerIntent()

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
            is PlayerClicked -> {
                handle(DatabaseIntent.UpdatePlayer(player.copy(isPresent = !player.isPresent)))
            }
            is PlayerArchived -> handle(DatabaseIntent.UpdatePlayer(player.copy(isArchived = true)))
            is ScreenIntent -> value.handle(currentState, handle, newStateListener) { editPlayer, newName ->
                handle(DatabaseIntent.UpdatePlayer(editPlayer.copy(name = newName.trim())))
            }
        }
    }
}
