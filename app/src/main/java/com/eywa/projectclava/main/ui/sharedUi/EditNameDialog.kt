package com.eywa.projectclava.main.ui.sharedUi

import androidx.compose.runtime.Composable
import com.eywa.projectclava.main.ui.sharedUi.EditDialogIntent.EditItemStateIntent

interface NamedItem {
    val name: String
}

interface EditDialogState<T : NamedItem> {
    val editItemName: String
    val editNameIsDirty: Boolean
    val editDialogOpenFor: T?

    fun editItemCopy(
            editItemName: String = this.editItemName,
            editNameIsDirty: Boolean = this.editNameIsDirty,
            editDialogOpenFor: T? = this.editDialogOpenFor,
    ): EditDialogState<T>
}

sealed class EditDialogIntent {
    object EditItemSubmitted : EditDialogIntent() {
        fun <T : NamedItem, E : EditDialogState<T>> handle(
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
        fun <T : NamedItem, E : EditDialogState<T>> handle(
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
        state: EditDialogState<T>,
        listener: (EditDialogIntent) -> Unit,
) {
    val okListener = { listener(EditDialogIntent.EditItemSubmitted) }

    ClavaDialog(
            isShown = state.editDialogOpenFor != null,
            title = "Edit ${state.editDialogOpenFor?.name}",
            okButtonText = "Edit",
            okButtonEnabled = state.editItemName.isNotBlank() &&
                    !nameIsDuplicate(state.editItemName, state.editDialogOpenFor?.name),
            onCancelListener = { listener(EditItemStateIntent.EditItemCancelled) },
            onOkListener = okListener
    ) {
        NamedItemTextField(
                typeContentDescription = typeContentDescription,
                textPlaceholder = textPlaceholder,
                nameIsDuplicate = nameIsDuplicate,
                proposedItemName = state.editItemName,
                onValueChangedListener = { listener(EditItemStateIntent.EditItemNameChanged(it)) },
                onClearPressedListener = { listener(EditItemStateIntent.EditNameCleared) },
                fieldIsDirty = state.editNameIsDirty,
                onDoneListener = okListener,
                itemBeingEdited = state.editDialogOpenFor,
        )
    }
}
