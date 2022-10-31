package com.eywa.projectclava.main.mainActivity.screens.ongoing

import com.eywa.projectclava.main.mainActivity.CoreIntent
import com.eywa.projectclava.main.mainActivity.DatabaseIntent
import com.eywa.projectclava.main.mainActivity.MainEffect
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.mainActivity.screens.ScreenIntent
import com.eywa.projectclava.main.mainActivity.screens.ScreenState
import com.eywa.projectclava.main.model.Court
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.MatchState
import com.eywa.projectclava.main.ui.sharedUi.AddTimeDialogIntent
import com.eywa.projectclava.main.ui.sharedUi.AddTimeDialogState
import com.eywa.projectclava.main.ui.sharedUi.TimePickerState


enum class OngoingMatchesDialog {
    ADD_TIME, CHANGE_COURT, RESUME
}

data class OngoingMatchesState(
        val openDialog: OngoingMatchesDialog? = null,
        override val selectedMatchId: Int? = null,
        override val timeToAdd: TimePickerState? = null,
        val selectedCourt: Court? = null,
        val resumeTime: TimePickerState? = null,
) : AddTimeDialogState, ScreenState {
    override val addTimeDialogIsOpen: Boolean = openDialog == OngoingMatchesDialog.ADD_TIME
    override fun addTimeCopy(
            addTimeDialogIsOpen: Boolean,
            timeToAdd: TimePickerState?,
    ) = copy(
            openDialog = when {
                // If a different dialog is open, keep it
                openDialog != null && openDialog != OngoingMatchesDialog.ADD_TIME -> openDialog
                addTimeDialogIsOpen -> OngoingMatchesDialog.ADD_TIME
                else -> null
            },
            timeToAdd = timeToAdd,
    )
}

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

fun AddTimeDialogIntent.toOngoingMatchesIntent(defaultTimeToAddSeconds: Int) =
        OngoingMatchesIntent.AddTimeIntent(this, defaultTimeToAddSeconds)
