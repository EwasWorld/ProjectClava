package com.eywa.projectclava.main.features.ui.confirmDialog

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.eywa.projectclava.main.features.ui.ClavaDialog
import com.eywa.projectclava.main.features.ui.editNameDialog.NamedItem
import com.eywa.projectclava.main.theme.Typography

@Composable
fun <T : NamedItem> ConfirmDialog(
        state: ConfirmDialogState<T>?,
        type: ConfirmDialogType,
        listener: (ConfirmDialogIntent) -> Unit,
) {
    ClavaDialog(
            isShown = state != null,
            title = type.title,
            okButtonText = type.okButtonText,
            onCancelListener = { listener(ConfirmDialogIntent.Cancel) },
            onOkListener = { listener(ConfirmDialogIntent.Ok(type)) }
    ) {
        Text(
                text = "Are you sure you want to ${type.action} ${state?.item?.name}",
                style = Typography.body1
        )
    }
}
