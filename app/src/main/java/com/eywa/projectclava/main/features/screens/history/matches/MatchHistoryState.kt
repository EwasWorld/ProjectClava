package com.eywa.projectclava.main.features.screens.history.matches

import com.eywa.projectclava.main.features.screens.ScreenState
import com.eywa.projectclava.main.features.ui.addTimeDialog.AddTimeDialogState
import com.eywa.projectclava.main.features.ui.confirmDialog.ConfirmDialogState
import com.eywa.projectclava.main.features.ui.timePicker.TimePickerState
import com.eywa.projectclava.main.model.Match


data class MatchHistoryState(
        override val selectedMatchId: Int? = null,
        override val addTimeDialogIsOpen: Boolean = false,
        override val timeToAdd: TimePickerState? = null,
        val deleteMatchDialogState: ConfirmDialogState<Match>? = null,
) : ScreenState, AddTimeDialogState {
    override fun addTimeCopy(
            addTimeDialogIsOpen: Boolean,
            timeToAdd: TimePickerState?,
    ) = copy(
            addTimeDialogIsOpen = addTimeDialogIsOpen,
            timeToAdd = timeToAdd,
    )
}