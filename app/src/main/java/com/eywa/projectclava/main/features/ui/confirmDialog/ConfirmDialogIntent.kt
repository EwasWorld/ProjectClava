package com.eywa.projectclava.main.features.ui.confirmDialog

import com.eywa.projectclava.main.features.ui.editNameDialog.NamedItem

sealed class ConfirmDialogIntent {
    data class Open(val item: NamedItem) : ConfirmDialogIntent()
    object Cancel : ConfirmDialogIntent()
    data class Ok(val type: ConfirmDialogType) : ConfirmDialogIntent()


    @Suppress("UNCHECKED_CAST")
    fun <T : NamedItem> handle(
            currentState: ConfirmDialogState<T>?,
            newStateListener: (ConfirmDialogState<T>?) -> Unit,
            confirmHandler: (T, ConfirmDialogType) -> Unit,
    ) {
        when (this) {
            is Open -> newStateListener(ConfirmDialogState(item) as ConfirmDialogState<T>)
            Cancel -> newStateListener(null)
            is Ok -> {
                confirmHandler(currentState!!.item, type)
                Cancel.handle(currentState, newStateListener, confirmHandler)
            }
        }
    }
}