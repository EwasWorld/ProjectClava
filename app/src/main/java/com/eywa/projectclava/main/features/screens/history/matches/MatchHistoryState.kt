package com.eywa.projectclava.main.features.screens.history.matches

import com.eywa.projectclava.main.features.screens.ScreenState
import com.eywa.projectclava.main.features.ui.addTimeDialog.AddTimeDialogState
import com.eywa.projectclava.main.features.ui.timePicker.TimePickerState


data class MatchHistoryState(
        override val selectedMatchId: Int? = null,
        override val addTimeDialogIsOpen: Boolean = false,
        override val timeToAdd: TimePickerState? = null,
) : ScreenState, AddTimeDialogState {
    override fun addTimeCopy(
            addTimeDialogIsOpen: Boolean,
            timeToAdd: TimePickerState?,
    ) = copy(
            addTimeDialogIsOpen = addTimeDialogIsOpen,
            timeToAdd = timeToAdd,
    )
}