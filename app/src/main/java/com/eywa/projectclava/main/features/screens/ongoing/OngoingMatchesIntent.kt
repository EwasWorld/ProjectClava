package com.eywa.projectclava.main.features.screens.ongoing

import com.eywa.projectclava.main.database.DatabaseIntent
import com.eywa.projectclava.main.features.screens.ScreenIntent
import com.eywa.projectclava.main.features.ui.addTimeDialog.AddTimeDialogIntent
import com.eywa.projectclava.main.features.ui.timePicker.TimePickerState
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.mainActivity.viewModel.CoreIntent
import com.eywa.projectclava.main.mainActivity.viewModel.MainEffect
import com.eywa.projectclava.main.model.Court
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.MatchState


fun AddTimeDialogIntent.toOngoingMatchesIntent(defaultTimeToAddSeconds: Int) =
        OngoingMatchesIntent.AddTimeIntent(this, defaultTimeToAddSeconds)


sealed class OngoingMatchesIntent : ScreenIntent<OngoingMatchesState> {
    override val screen: NavRoute = NavRoute.ONGOING_MATCHES

    data class MatchClicked(val match: Match) : OngoingMatchesIntent()
    object CompleteMatch : OngoingMatchesIntent()
    object PauseMatch : OngoingMatchesIntent()
    data class AddTimeIntent(
            val value: AddTimeDialogIntent,
            val defaultTimeToAdd: Int,
    ) : OngoingMatchesIntent()

    data class CourtSelected(val court: Court) : OngoingMatchesIntent()
    object CloseCurrentDialog : OngoingMatchesIntent()

    data class OpenChangeCourtDialog(val initialSelectedCourt: Court?) : OngoingMatchesIntent()
    object ChangeCourtDialogSubmit : OngoingMatchesIntent()

    data class OpenResumeDialog(
            val match: Match,
            val initialSelectedCourt: Court?,
            val defaultTimeToAddSeconds: Int,
    ) : OngoingMatchesIntent()

    object ResumeDialogSubmit : OngoingMatchesIntent()
    data class ResumeTimeChanged(val value: TimePickerState) : OngoingMatchesIntent()

    data class Navigate(val destination: NavRoute) : OngoingMatchesIntent()

    override fun handle(
            currentState: OngoingMatchesState,
            handle: (CoreIntent) -> Unit,
            newStateListener: (OngoingMatchesState) -> Unit
    ) {
        when (this) {
            is AddTimeIntent -> value.handle(defaultTimeToAdd, currentState, newStateListener, handle)
            is Navigate -> handle(MainEffect.Navigate(destination))
            is CourtSelected -> newStateListener(currentState.copy(selectedCourt = court))
            is MatchClicked -> newStateListener(
                    currentState.copy(selectedMatchId = match.id.takeIf { currentState.selectedMatchId != match.id })
            )
            is ResumeTimeChanged -> newStateListener(currentState.copy(resumeTime = value))

            /*
             * Database action on selected match
             */
            CompleteMatch -> {
                handle(DatabaseIntent.CompleteMatch(currentState.selectedMatchId!!))
                newStateListener(
                        currentState.copy(
                                selectedMatchId = null,
                                selectedCourt = null,
                                openDialog = null,
                                timeToAdd = null,
                                resumeTime = null,
                        )
                )
            }
            PauseMatch -> handle(DatabaseIntent.PauseMatch(currentState.selectedMatchId!!))
            ChangeCourtDialogSubmit -> {
                handle(DatabaseIntent.ChangeMatchCourt(currentState.selectedMatchId!!, currentState.selectedCourt!!))
                newStateListener(currentState.copy(openDialog = null))
            }
            ResumeDialogSubmit -> {
                handle(
                        DatabaseIntent.ResumeMatch(
                                matchId = currentState.selectedMatchId!!,
                                court = currentState.selectedCourt!!,
                                resumeTimeSeconds = currentState.resumeTime!!.totalSeconds,
                        )
                )
                newStateListener(currentState.copy(openDialog = null))
            }

            /*
             * Open/Close dialog
             */
            CloseCurrentDialog -> newStateListener(currentState.copy(openDialog = null))
            is OpenChangeCourtDialog -> newStateListener(
                    currentState.copy(
                            openDialog = OngoingMatchesDialog.CHANGE_COURT,
                            selectedCourt = initialSelectedCourt,
                    )
            )
            is OpenResumeDialog -> {
                require(match.id == currentState.selectedMatchId) { "Ids don't match" }

                val remainingTime = match
                        .state
                        .let { it as MatchState.Paused }
                        .remainingTimeSeconds
                        .toInt()
                        .takeIf { it > 0 }

                newStateListener(
                        currentState.copy(
                                openDialog = OngoingMatchesDialog.RESUME,
                                selectedCourt = initialSelectedCourt,
                                resumeTime = TimePickerState(remainingTime ?: defaultTimeToAddSeconds)
                        )
                )
            }
        }
    }
}
