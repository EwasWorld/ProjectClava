package com.eywa.projectclava.main.features.ui.editNameDialog

import androidx.compose.runtime.Composable
import com.eywa.projectclava.main.features.ui.ClavaDialog
import com.eywa.projectclava.main.features.ui.NamedItemTextField
import com.eywa.projectclava.main.features.ui.editNameDialog.EditDialogIntent.*

@Composable
fun <T : NamedItem> EditNameDialog(
        typeContentDescription: String,
        textPlaceholder: String,
        nameIsDuplicate: (newName: String, nameOfItemBeingEdited: String?) -> Boolean,
        state: EditDialogState<T>,
        listener: (EditDialogIntent) -> Unit,
) {
    val okListener = { listener(EditItemSubmitted) }

    ClavaDialog(
            isShown = state.editDialogOpenFor != null,
            title = "Edit ${state.editDialogOpenFor?.name}",
            okButtonText = "Edit",
            okButtonEnabled = state.editItemName.isNotBlank() &&
                    !nameIsDuplicate(state.editItemName, state.editDialogOpenFor?.name),
            onCancelListener = { listener(EditItemCancelled) },
            onOkListener = okListener
    ) {
        NamedItemTextField(
                typeContentDescription = typeContentDescription,
                textPlaceholder = textPlaceholder,
                nameIsDuplicate = nameIsDuplicate,
                nameIsArchived = { _, _ -> null },
                proposedItemName = state.editItemName,
                onValueChangedListener = { listener(EditItemNameChanged(it)) },
                onClearPressedListener = { listener(EditNameCleared) },
                fieldIsDirty = state.editNameIsDirty,
                onDoneListener = okListener,
                onUnarchiveListener = {},
                itemBeingEdited = state.editDialogOpenFor,
        )
    }
}
