package com.eywa.projectclava.main.ui.sharedUi

import androidx.compose.runtime.Composable
import com.eywa.projectclava.main.ui.sharedUi.EditDialogIntent.EditItemStateIntent

interface NamedItem {
    val name: String
}

interface EditItemState<T : NamedItem> {
    val editItemName: String
    val editNameIsDirty: Boolean
    val editDialogOpenFor: T?

    fun editItemCopy(
            editItemName: String = this.editItemName,
            editNameIsDirty: Boolean = this.editNameIsDirty,
            editDialogOpenFor: T? = this.editDialogOpenFor,
    ): EditItemState<T>
}

sealed class EditDialogIntent {
    object EditItemSubmitted : EditDialogIntent() {
        fun <T : NamedItem, E : EditItemState<T>> handle(
                currentState: E,
                newStateListener: (E) -> Unit,
                updateName: (editItem: T, newName: String) -> Unit,
        ) {
            updateName(currentState.editDialogOpenFor!!, currentState.editItemName)
            EditItemStateIntent.EditItemCancelled.handle(currentState, newStateListener)
        }
    }

    sealed class EditItemStateIntent : EditDialogIntent() {
        data class EditItemStarted<T : NamedItem>(val value: T) : EditItemStateIntent()
        data class EditItemNameChanged(val value: String) : EditItemStateIntent()
        object EditNameCleared : EditItemStateIntent()
        object EditItemCancelled : EditItemStateIntent()

        @Suppress("UNCHECKED_CAST")
        fun <T : NamedItem, E : EditItemState<T>> handle(
                currentState: E,
                newStateListener: (E) -> Unit,
        ) {
            when (this) {
                EditItemCancelled -> newStateListener(
                        currentState.editItemCopy(editDialogOpenFor = null) as E
                )
                is EditItemNameChanged -> newStateListener(
                        currentState.editItemCopy(editItemName = value, editNameIsDirty = true) as E
                )
                is EditItemStarted<*> -> newStateListener(
                        currentState.editItemCopy(
                                editItemName = value.name,
                                editNameIsDirty = false,
                                editDialogOpenFor = value as T,
                        ) as E
                )
                EditNameCleared -> newStateListener(
                        currentState.editItemCopy(editItemName = "", editNameIsDirty = false) as E
                )
            }
        }
    }
}

@Composable
fun <T : NamedItem> EditNameDialog(
        typeContentDescription: String,
        textPlaceholder: String,
        nameIsDuplicate: (newName: String, nameOfItemBeingEdited: String?) -> Boolean,
        editItemState: EditItemState<T>,
        listener: (EditDialogIntent) -> Unit,
) {
    val okListener = { listener(EditDialogIntent.EditItemSubmitted) }

    ClavaDialog(
            isShown = editItemState.editDialogOpenFor != null,
            title = "Edit ${editItemState.editDialogOpenFor?.name}",
            okButtonText = "Edit",
            okButtonEnabled = editItemState.editItemName.isNotBlank() &&
                    !nameIsDuplicate(editItemState.editItemName, editItemState.editDialogOpenFor?.name),
            onCancelListener = { listener(EditItemStateIntent.EditItemCancelled) },
            onOkListener = okListener
    ) {
        NamedItemTextField(
                typeContentDescription = typeContentDescription,
                textPlaceholder = textPlaceholder,
                nameIsDuplicate = nameIsDuplicate,
                proposedItemName = editItemState.editItemName,
                onValueChangedListener = { listener(EditItemStateIntent.EditItemNameChanged(it)) },
                onClearPressedListener = { listener(EditItemStateIntent.EditNameCleared) },
                fieldIsDirty = editItemState.editNameIsDirty,
                onDoneListener = okListener,
                itemBeingEdited = editItemState.editDialogOpenFor,
        )
    }
}
