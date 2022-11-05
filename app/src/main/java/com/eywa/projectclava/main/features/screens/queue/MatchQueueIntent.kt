package com.eywa.projectclava.main.features.screens.queue

import com.eywa.projectclava.main.database.DatabaseIntent
import com.eywa.projectclava.main.features.screens.ScreenIntent
import com.eywa.projectclava.main.features.ui.confirmDialog.ConfirmDialogIntent
import com.eywa.projectclava.main.features.ui.confirmDialog.ConfirmDialogType
import com.eywa.projectclava.main.features.ui.timePicker.TimePickerState
import com.eywa.projectclava.main.mainActivity.MainNavRoute
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.mainActivity.viewModel.CoreIntent
import com.eywa.projectclava.main.mainActivity.viewModel.MainEffect
import com.eywa.projectclava.main.model.Court
import com.eywa.projectclava.main.model.Match

fun ConfirmDialogIntent.toMatchQueueIntent() = MatchQueueIntent.ConfirmIntent(this)

sealed class MatchQueueIntent : ScreenIntent<MatchQueueState> {
    override val screen = MainNavRoute.MATCH_QUEUE

    object StartMatchSubmitted : MatchQueueIntent()
    data class OpenStartMatchDialog(
            val initialSelectedCourt: Court?,
            val defaultTimeToAddSeconds: Int,
    ) : MatchQueueIntent()

    object CloseStartMatchDialog : MatchQueueIntent()
    data class MatchClicked(val match: Match) : MatchQueueIntent()
    object MatchDeleted : MatchQueueIntent()
    data class UpdateTimePicker(val value: TimePickerState) : MatchQueueIntent()
    data class UpdateSelectedCourt(val court: Court) : MatchQueueIntent()

    data class ConfirmIntent(val value: ConfirmDialogIntent) : MatchQueueIntent()
    data class Navigate(val destination: NavRoute) : MatchQueueIntent()

    override fun handle(
            currentState: MatchQueueState,
            handle: (CoreIntent) -> Unit,
            newStateListener: (MatchQueueState) -> Unit
    ) {
        fun clearDialogsAndSelection() {
            newStateListener(
                    currentState.copy(
                            selectedMatchId = null,
                            startMatchDialogIsOpen = false,
                            selectedCourt = null,
                            startMatchTimePickerState = null,
                    )
            )
        }

        when (this) {
            is Navigate -> handle(MainEffect.Navigate(destination))
            is MatchClicked -> newStateListener(
                    currentState.copy(selectedMatchId = match.id.takeIf { currentState.selectedMatchId != match.id })
            )
            is OpenStartMatchDialog -> newStateListener(
                    currentState.copy(
                            startMatchDialogIsOpen = true,
                            startMatchTimePickerState = TimePickerState(defaultTimeToAddSeconds),
                            selectedCourt = initialSelectedCourt,
                    )
            )
            CloseStartMatchDialog -> newStateListener(
                    currentState.copy(
                            startMatchDialogIsOpen = false,
                            selectedCourt = null,
                            startMatchTimePickerState = null,
                    )
            )
            StartMatchSubmitted -> {
                handle(
                        DatabaseIntent.StartMatch(
                                matchId = currentState.selectedMatchId!!,
                                court = currentState.selectedCourt!!,
                                timeSeconds = currentState.startMatchTimePickerState!!.totalSeconds,
                        )
                )
                clearDialogsAndSelection()
            }
            MatchDeleted -> {
                handle(DatabaseIntent.DeleteMatchById(currentState.selectedMatchId!!))
                clearDialogsAndSelection()
            }
            is UpdateTimePicker -> newStateListener(currentState.copy(startMatchTimePickerState = value))
            is UpdateSelectedCourt -> newStateListener(currentState.copy(selectedCourt = court))
            is ConfirmIntent -> value.handle(
                    currentState = currentState.deleteMatchDialogState,
                    newStateListener = { newStateListener(currentState.copy(deleteMatchDialogState = it)) },
                    confirmHandler = { _, actionType ->
                        when (actionType) {
                            ConfirmDialogType.DELETE -> MatchDeleted.handle(currentState, handle, newStateListener)
                        }
                    }
            )
        }
    }
}
