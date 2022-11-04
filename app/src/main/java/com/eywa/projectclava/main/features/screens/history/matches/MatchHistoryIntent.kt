package com.eywa.projectclava.main.features.screens.history.matches

import com.eywa.projectclava.main.database.DatabaseIntent
import com.eywa.projectclava.main.features.ui.addTimeDialog.AddTimeDialogIntent
import com.eywa.projectclava.main.features.ui.confirmDialog.ConfirmDialogIntent
import com.eywa.projectclava.main.features.ui.confirmDialog.ConfirmDialogType
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.mainActivity.viewModel.CoreIntent
import com.eywa.projectclava.main.mainActivity.viewModel.MainEffect
import com.eywa.projectclava.main.model.Match


fun AddTimeDialogIntent.toMatchHistoryIntent(defaultTimeToAdd: Int) =
        MatchHistoryIntent.AddTimeIntent(this, defaultTimeToAdd)

fun ConfirmDialogIntent.toMatchHistoryIntent() = MatchHistoryIntent.ConfirmIntent(this)


sealed class MatchHistoryIntent : com.eywa.projectclava.main.features.screens.ScreenIntent<MatchHistoryState> {
    override val screen: NavRoute = NavRoute.MATCH_HISTORY

    data class MatchClicked(val match: Match) : MatchHistoryIntent()
    data class MatchDeleted(val match: Match) : MatchHistoryIntent()

    data class ConfirmIntent(val value: ConfirmDialogIntent) : MatchHistoryIntent()

    data class AddTimeIntent(
            val value: AddTimeDialogIntent,
            val defaultTimeToAdd: Int,
    ) : MatchHistoryIntent()

    data class Navigate(val destination: NavRoute) : MatchHistoryIntent()

    override fun handle(
            currentState: MatchHistoryState,
            handle: (CoreIntent) -> Unit,
            newStateListener: (MatchHistoryState) -> Unit
    ) {
        when (this) {
            is AddTimeIntent -> value.handle(defaultTimeToAdd, currentState, newStateListener, handle)
            is MatchClicked -> newStateListener(
                    currentState.copy(selectedMatchId = match.id.takeIf { currentState.selectedMatchId != match.id })
            )
            is MatchDeleted -> handle(DatabaseIntent.DeleteMatch(match))
            is Navigate -> handle(MainEffect.Navigate(destination))
            is ConfirmIntent -> value.handle(
                    currentState = currentState.deleteMatchDialogState,
                    newStateListener = { newStateListener(currentState.copy(deleteMatchDialogState = it)) },
                    confirmHandler = { item, actionType ->
                        when (actionType) {
                            ConfirmDialogType.DELETE ->
                                MatchDeleted(item).handle(currentState, handle, newStateListener)
                        }
                    }
            )
        }
    }
}