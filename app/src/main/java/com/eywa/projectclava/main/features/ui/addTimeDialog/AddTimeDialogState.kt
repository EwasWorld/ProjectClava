package com.eywa.projectclava.main.features.ui.addTimeDialog

import com.eywa.projectclava.main.features.ui.timePicker.TimePickerState


interface AddTimeDialogState : SelectableMatch {
    val addTimeDialogIsOpen: Boolean
    val timeToAdd: TimePickerState?

    fun addTimeCopy(
            addTimeDialogIsOpen: Boolean = this.addTimeDialogIsOpen,
            timeToAdd: TimePickerState? = this.timeToAdd,
    ): AddTimeDialogState
}