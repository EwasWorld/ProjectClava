package com.eywa.projectclava.main.mainActivity.screens.createMatch

import com.eywa.projectclava.main.mainActivity.CoreIntent
import com.eywa.projectclava.main.mainActivity.DatabaseIntent
import com.eywa.projectclava.main.mainActivity.MainEffect
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.mainActivity.screens.ScreenIntent
import com.eywa.projectclava.main.mainActivity.screens.ScreenState
import com.eywa.projectclava.main.model.Player

data class CreateMatchState(
        /**
         * the players who will form the next match
         */
        val selectedPlayers: Iterable<Player> = setOf(),
) : ScreenState


sealed class CreateMatchIntent : ScreenIntent<CreateMatchState> {
    override val screen = NavRoute.CREATE_MATCH

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
                            if (currentState.selectedPlayers.contains(value)) {
                                currentState.selectedPlayers.minus(value)
                            }
                            else {
                                currentState.selectedPlayers.plus(value)
                            }
                    )
            )
        }
    }
}