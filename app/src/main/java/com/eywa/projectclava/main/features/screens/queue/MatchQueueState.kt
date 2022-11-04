package com.eywa.projectclava.main.features.screens.queue

import com.eywa.projectclava.main.features.screens.ScreenState
import com.eywa.projectclava.main.features.ui.confirmDialog.ConfirmDialogState
import com.eywa.projectclava.main.features.ui.timePicker.TimePickerState
import com.eywa.projectclava.main.model.Court
import com.eywa.projectclava.main.model.Match

data class MatchQueueState(
        val selectedMatchId: Int? = null,
        val startMatchDialogIsOpen: Boolean = false,
        val selectedCourt: Court? = null,
        val startMatchTimePickerState: TimePickerState? = null,
        val deleteMatchDialogState: ConfirmDialogState<Match>? = null,
) : ScreenState