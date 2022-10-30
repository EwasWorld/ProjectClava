package com.eywa.projectclava.main.ui.sharedUi

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.eywa.projectclava.main.mainActivity.CoreIntent
import com.eywa.projectclava.main.mainActivity.DatabaseIntent
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.ui.sharedUi.AddTimeDialogIntent.*

interface AddTimeDialogState {
    val addTimeDialogOpenFor: Match?
    val timeToAdd: TimePickerState?

    fun addTimeCopy(
            addTimeDialogOpenFor: Match? = this.addTimeDialogOpenFor,
            timeToAdd: TimePickerState? = this.timeToAdd,
    ): AddTimeDialogState
}

sealed class AddTimeDialogIntent {
    object Submitted : AddTimeDialogIntent()
    object CloseDialog : AddTimeDialogIntent()
    data class TimeToAddChanged(val newTimePickerState: TimePickerState) : AddTimeDialogIntent()
    data class AddTimeOpened(val match: Match) : AddTimeDialogIntent()

    @Suppress("UNCHECKED_CAST")
    fun <S : AddTimeDialogState> handle(
            defaultTimeToAddSeconds: Int,
            currentState: S,
            newStateListener: (S) -> Unit,
            handle: (CoreIntent) -> Unit,
    ) {
        when (this) {
            CloseDialog -> newStateListener(currentState.addTimeCopy(null, null) as S)
            is TimeToAddChanged -> newStateListener(currentState.addTimeCopy(timeToAdd = newTimePickerState) as S)
            is AddTimeOpened -> newStateListener(
                    currentState.addTimeCopy(match, TimePickerState(defaultTimeToAddSeconds)) as S
            )
            Submitted -> {
                handle(
                        DatabaseIntent.AddTimeToMatch(
                                match = currentState.addTimeDialogOpenFor!!,
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
            (state.addTimeDialogOpenFor == null && state.timeToAdd == null)
                    || (state.addTimeDialogOpenFor != null && state.timeToAdd != null)
    ) { "Invalid AddTimeDialogState" }

    ClavaDialog(
            isShown = state.addTimeDialogOpenFor != null,
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