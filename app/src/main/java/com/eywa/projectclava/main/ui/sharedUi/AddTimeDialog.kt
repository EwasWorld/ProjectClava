package com.eywa.projectclava.main.ui.sharedUi

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.eywa.projectclava.main.mainActivity.CoreIntent
import com.eywa.projectclava.main.mainActivity.DatabaseIntent
import com.eywa.projectclava.main.ui.sharedUi.AddTimeDialogIntent.*

interface SelectableMatch {
    val selectedMatchId: Int?
}

interface AddTimeDialogState : SelectableMatch {
    val addTimeDialogIsOpen: Boolean
    val timeToAdd: TimePickerState?

    fun addTimeCopy(
            addTimeDialogIsOpen: Boolean = this.addTimeDialogIsOpen,
            timeToAdd: TimePickerState? = this.timeToAdd,
    ): AddTimeDialogState
}

sealed class AddTimeDialogIntent {
    object Submitted : AddTimeDialogIntent()
    object CloseDialog : AddTimeDialogIntent()
    data class TimeToAddChanged(val newTimePickerState: TimePickerState) : AddTimeDialogIntent()
    object AddTimeOpened : AddTimeDialogIntent()

    @Suppress("UNCHECKED_CAST")
    fun <S : AddTimeDialogState> handle(
            defaultTimeToAddSeconds: Int,
            currentState: S,
            newStateListener: (S) -> Unit,
            handle: (CoreIntent) -> Unit,
    ) {
        when (this) {
            CloseDialog -> newStateListener(currentState.addTimeCopy(false, null) as S)
            is TimeToAddChanged -> newStateListener(currentState.addTimeCopy(timeToAdd = newTimePickerState) as S)
            is AddTimeOpened -> newStateListener(
                    currentState.addTimeCopy(
                            addTimeDialogIsOpen = true,
                            timeToAdd = TimePickerState(defaultTimeToAddSeconds),
                    ) as S
            )
            Submitted -> {
                handle(
                        DatabaseIntent.AddTimeToMatch(
                                matchId = currentState.selectedMatchId!!,
                                secondsToAdd = currentState.timeToAdd!!.totalSeconds,
                        )
                )
                CloseDialog.handle(defaultTimeToAddSeconds, currentState, newStateListener, handle)
            }
        }
    }
}

@Composable
fun AddTimeDialog(
        state: AddTimeDialogState,
        listener: (AddTimeDialogIntent) -> Unit,
) {
    check(
            (!state.addTimeDialogIsOpen && state.timeToAdd == null)
                    || (state.addTimeDialogIsOpen && state.timeToAdd != null)
    ) { "Invalid AddTimeDialogState" }

    ClavaDialog(
            isShown = state.addTimeDialogIsOpen,
            title = "Add time",
            okButtonText = "Add",
            onCancelListener = { listener(CloseDialog) },
            okButtonEnabled = state.timeToAdd?.isValid == true,
            onOkListener = { listener(Submitted) },
    ) {
        TimePicker(
                timePickerState = state.timeToAdd ?: TimePickerState(0),
                timeChangedListener = { listener(TimeToAddChanged(it)) },
                modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
        )
    }
}