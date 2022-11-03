package com.eywa.projectclava.main.features.ui.editNameDialog

sealed class EditDialogIntent {
    object EditItemSubmitted : EditDialogIntent()
    data class EditItemStarted<T : NamedItem>(val value: T) : EditDialogIntent()
    data class EditItemNameChanged(val value: String) : EditDialogIntent()
    object EditNameCleared : EditDialogIntent()
    object EditItemCancelled : EditDialogIntent()

    @Suppress("UNCHECKED_CAST")
    fun <T : NamedItem, E : EditDialogState<T>> handle(
            currentState: E,
            newStateListener: (E) -> Unit,
            updateName: (editItem: T, newName: String) -> Unit,
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
            EditItemSubmitted -> {
                updateName(currentState.editDialogOpenFor!!, currentState.editItemName)
                EditItemCancelled.handle(currentState, newStateListener, updateName)
            }
        }
    }
}