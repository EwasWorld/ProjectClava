package com.eywa.projectclava.main.features.ui.confirmDialog

enum class ConfirmDialogType(
        val title: String,
        /**
         * Are you sure you want to [action] 'itemName'?
         */
        val action: String,
        val okButtonText: String,
) {
    DELETE(
            title = "Delete",
            action = "delete",
            okButtonText = "Delete",
    )
}