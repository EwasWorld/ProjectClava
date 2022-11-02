package com.eywa.projectclava.main.mainActivity.screens.history.matches

import com.eywa.projectclava.main.database.DatabaseIntent
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.mainActivity.screens.ScreenIntent
import com.eywa.projectclava.main.mainActivity.viewModel.CoreIntent
import com.eywa.projectclava.main.mainActivity.viewModel.MainEffect
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.ui.sharedUi.AddTimeDialogIntent


fun AddTimeDialogIntent.toMatchHistoryIntent(defaultTimeToAdd: Int) =
        MatchHistoryIntent.AddTimeIntent(this, defaultTimeToAdd)


sealed class MatchHistoryIntent : ScreenIntent<MatchHistoryState> {
    override val screen: NavRoute = NavRoute.MATCH_HISTORY

    data class MatchClicked(val match: Match) : MatchHistoryIntent()
    data class MatchDeleted(val match: Match) : MatchHistoryIntent()

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
        }
    }
}