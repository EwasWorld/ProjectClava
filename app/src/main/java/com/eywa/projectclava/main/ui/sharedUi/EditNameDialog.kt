package com.eywa.projectclava.main.ui.sharedUi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable

interface NamedItem {
    val name: String
}

interface EditItemState<T : NamedItem> {
    val editItemName: String
    val editNameIsDirty: Boolean
    val editDialogOpenFor: T?
}

@Deprecated("Temp to support non-state EditNameDialog")
data class EditItemStateOld<T : NamedItem>(
        override val editDialogOpenFor: T?,
        override val editNameIsDirty: Boolean,
        override val editItemName: String,
) : EditItemState<T>

sealed class EditDialogListener {
    data class EditItemStarted<T>(val value: T) : EditDialogListener()
    data class EditItemNameChanged(val value: String) : EditDialogListener()
    object EditNameCleared : EditDialogListener()
    object EditItemCancelled : EditDialogListener()
    object EditItemSubmit : EditDialogListener()
}

@Deprecated("Use state instead")
@Composable
fun <T : NamedItem> EditNameDialog(
        typeContentDescription: String,
        textPlaceholder: String,
        nameIsDuplicate: (newName: String, editItemName: String?) -> Boolean,
        editDialogOpenFor: T?,
        itemEditedListener: (T, String) -> Unit,
        itemEditCancelledListener: () -> Unit,
) {
    val editName = rememberSaveable(editDialogOpenFor) { mutableStateOf(editDialogOpenFor?.name ?: "") }
    val fieldTouched = rememberSaveable(editDialogOpenFor) { mutableStateOf(false) }
    val okListener = { itemEditedListener(editDialogOpenFor!!, editName.value.trim()) }

    val state = EditItemStateOld(editDialogOpenFor, fieldTouched.value, editName.value)

    EditNameDialog(
            typeContentDescription = typeContentDescription,
            textPlaceholder = textPlaceholder,
            nameIsDuplicate = nameIsDuplicate,
            editItemState = state,
            listener = {
                when (it) {
                    EditDialogListener.EditItemCancelled -> itemEditCancelledListener()
                    is EditDialogListener.EditItemNameChanged -> {
                        fieldTouched.value = true
                        editName.value = it.value
                    }
                    is EditDialogListener.EditItemStarted<*> -> {
                        fieldTouched.value = false
                        editName.value = ""
                    }
                    EditDialogListener.EditItemSubmit -> {
                        okListener()
                    }
                    EditDialogListener.EditNameCleared -> {
                        fieldTouched.value = false
                        editName.value = ""
                    }
                }
            }
    )
}

@Composable
fun <T : NamedItem> EditNameDialog(
        typeContentDescription: String,
        textPlaceholder: String,
        nameIsDuplicate: (newName: String, editItemName: String?) -> Boolean,
        editItemState: EditItemState<T>,
        listener: (EditDialogListener) -> Unit,
) {
    val okListener = { listener(EditDialogListener.EditItemSubmit) }

    ClavaDialog(
            isShown = editItemState.editDialogOpenFor != null,
            title = "Edit ${editItemState.editDialogOpenFor?.name}",
            okButtonText = "Edit",
            okButtonEnabled = editItemState.editItemName.isNotBlank() &&
                    !nameIsDuplicate(editItemState.editItemName, editItemState.editDialogOpenFor?.name),
            onCancelListener = { listener(EditDialogListener.EditItemCancelled) },
            onOkListener = okListener
    ) {
        NamedItemTextField(
                typeContentDescription = typeContentDescription,
                textPlaceholder = textPlaceholder,
                nameIsDuplicate = nameIsDuplicate,
                proposedItemName = editItemState.editItemName,
                onValueChangedListener = { listener(EditDialogListener.EditItemNameChanged(it)) },
                onClearPressedListener = { listener(EditDialogListener.EditNameCleared) },
                fieldIsDirty = editItemState.editNameIsDirty,
                onDoneListener = okListener,
                itemBeingEdited = editItemState.editDialogOpenFor,
        )
    }
}
