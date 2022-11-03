package com.eywa.projectclava.main.features.ui.addTimeDialog

import com.eywa.projectclava.main.database.DatabaseIntent
import com.eywa.projectclava.main.features.ui.timePicker.TimePickerState
import com.eywa.projectclava.main.mainActivity.viewModel.CoreIntent

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