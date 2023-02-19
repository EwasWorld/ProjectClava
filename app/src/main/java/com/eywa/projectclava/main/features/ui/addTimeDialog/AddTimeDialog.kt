package com.eywa.projectclava.main.features.ui.addTimeDialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.eywa.projectclava.main.features.ui.ClavaDialog
import com.eywa.projectclava.main.features.ui.addTimeDialog.AddTimeDialogIntent.*
import com.eywa.projectclava.main.features.ui.timePicker.TimePicker
import com.eywa.projectclava.main.features.ui.timePicker.TimePickerState


@Composable
fun AddTimeDialog(
        state: AddTimeDialogState,
        listener: (AddTimeDialogIntent) -> Unit,
) {
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
