package com.eywa.projectclava.main.features.screens.matchUp

import com.eywa.projectclava.main.database.DatabaseIntent
import com.eywa.projectclava.main.features.screens.ScreenIntent
import com.eywa.projectclava.main.mainActivity.MainNavRoute
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.mainActivity.viewModel.CoreIntent
import com.eywa.projectclava.main.mainActivity.viewModel.MainEffect
import com.eywa.projectclava.main.model.Player


sealed class CreateMatchIntent : ScreenIntent<CreateMatchState> {
    override val screen = MainNavRoute.CREATE_MATCH

    object CreateMatch : CreateMatchIntent()
    data class Navigate(val value: NavRoute) : CreateMatchIntent()
    data class PlayerClicked(val value: Player) : CreateMatchIntent()
    object ClearSelectedPlayers : CreateMatchIntent()

    override fun handle(
            currentState: CreateMatchState,
            handle: (CoreIntent) -> Unit,
            newStateListener: (CreateMatchState) -> Unit,
    ) {
        when (this) {
            ClearSelectedPlayers -> newStateListener(currentState.copy(selectedPlayers = setOf()))
            CreateMatch -> {
                ClearSelectedPlayers.handle(currentState, handle, newStateListener)
                handle(DatabaseIntent.AddMatch(currentState.selectedPlayers))
            }
            is Navigate -> handle(MainEffect.Navigate(value))
            is PlayerClicked -> newStateListener(
                    currentState.copy(
                            selectedPlayers =
                            if (currentState.selectedPlayers.contains(value.id)) {
                                currentState.selectedPlayers.minus(value.id)
                            }
                            else {
                                currentState.selectedPlayers.plus(value.id)
                            }
                    )
            )
        }
    }
}