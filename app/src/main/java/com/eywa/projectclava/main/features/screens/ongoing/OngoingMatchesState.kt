package com.eywa.projectclava.main.features.screens.ongoing

import com.eywa.projectclava.main.features.screens.ScreenState
import com.eywa.projectclava.main.features.ui.addTimeDialog.AddTimeDialogState
import com.eywa.projectclava.main.features.ui.timePicker.TimePickerState
import com.eywa.projectclava.main.model.Court

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