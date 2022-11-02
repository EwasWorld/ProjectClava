package com.eywa.projectclava.main.mainActivity.screens.history.matches

import com.eywa.projectclava.main.mainActivity.screens.ScreenState
import com.eywa.projectclava.main.ui.sharedUi.AddTimeDialogState
import com.eywa.projectclava.main.ui.sharedUi.TimePickerState


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