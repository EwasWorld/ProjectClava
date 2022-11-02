package com.eywa.projectclava.main.mainActivity.screens.queue

import com.eywa.projectclava.main.mainActivity.screens.ScreenState
import com.eywa.projectclava.main.model.Court
import com.eywa.projectclava.main.ui.sharedUi.TimePickerState

data class MatchQueueState(
        val selectedMatchId: Int? = null,
        val startMatchDialogIsOpen: Boolean = false,
        val selectedCourt: Court? = null,
        val startMatchTimePickerState: TimePickerState? = null,
) : ScreenState