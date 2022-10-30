package com.eywa.projectclava.main.mainActivity.screens.manage

import com.eywa.projectclava.main.mainActivity.CoreIntent
import com.eywa.projectclava.main.mainActivity.MainEffect
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.mainActivity.screens.manage.SetupListIntent.SetupListItemIntent
import com.eywa.projectclava.main.mainActivity.screens.manage.SetupListIntent.SetupListStateIntent
import com.eywa.projectclava.main.ui.sharedUi.EditDialogListener


sealed class SetupListIntent {
    /**
     * Actions that behave differently based on the type of T (usually database actions)
     */
    sealed class SetupListItemIntent<T : SetupListItem> : SetupListIntent() {
        object AddItemSubmitted : SetupListItemIntent<SetupListItem>()
        object EditItemSubmitted : SetupListItemIntent<SetupListItem>()
        data class ItemDeleted<T : SetupListItem>(val value: T) : SetupListItemIntent<T>()
        data class ItemClicked<T : SetupListItem>(val value: T) : SetupListItemIntent<T>()
    }

    /**
     * Actions that will behave the same no matter the type of T
     * (usually due to affecting only the [SetupListState] or things like navigation)
     */
    sealed class SetupListStateIntent : SetupListIntent() {
        object ToggleUseAltPlaceholderText : SetupListStateIntent()

        object AddNameCleared : SetupListStateIntent()
        data class AddNameChanged(val value: String) : SetupListStateIntent()

        data class EditItemStarted(val value: SetupListItem) : SetupListStateIntent()
        data class EditItemNameChanged(val value: String) : SetupListStateIntent()
        object EditItemCancelled : SetupListStateIntent()
        object EditNameCleared : SetupListStateIntent()

        object ToggleSearch : SetupListStateIntent()
        data class SearchTextChanged(val value: String) : SetupListStateIntent()

        data class Navigate(val value: NavRoute) : SetupListStateIntent()

        fun <T : SetupListItem> handle(
                currentState: SetupListState<T>,
                handle: (CoreIntent) -> Unit,
                newStateListener: (SetupListState<T>) -> Unit
        ) {
            @Suppress("UNCHECKED_CAST")
            when (this) {
                AddNameCleared -> newStateListener(currentState.copy(addItemName = "", addItemIsDirty = false))
                is AddNameChanged -> newStateListener(currentState.copy(addItemName = value, addItemIsDirty = true))
                EditItemCancelled -> newStateListener(currentState.copy(editDialogOpenFor = null))
                EditNameCleared -> newStateListener(currentState.copy(editItemName = "", editNameIsDirty = false))
                is EditItemNameChanged -> newStateListener(
                        currentState.copy(
                                editItemName = value,
                                editNameIsDirty = true,
                        )
                )
                is EditItemStarted -> newStateListener(
                        currentState.copy(
                                editItemName = value.name,
                                editNameIsDirty = false,
                                editDialogOpenFor = value as T,
                        )
                )
                is Navigate -> handle(MainEffect.Navigate(value))
                is SearchTextChanged -> newStateListener(currentState.copy(searchText = value))
                ToggleSearch -> {
                    val newSearchText = if (currentState.searchText == null) "" else null
                    newStateListener(currentState.copy(searchText = newSearchText))
                }
                ToggleUseAltPlaceholderText -> newStateListener(
                        currentState.copy(useTextPlaceholderAlt = !currentState.useTextPlaceholderAlt)
                )
            }
        }
    }
}

fun EditDialogListener.toSetupListIntent() = when (this) {
    EditDialogListener.EditItemCancelled -> SetupListStateIntent.EditItemCancelled
    EditDialogListener.EditNameCleared -> SetupListStateIntent.EditNameCleared
    is EditDialogListener.EditItemNameChanged -> SetupListStateIntent.EditItemNameChanged(value)
    is EditDialogListener.EditItemStarted<*> -> SetupListStateIntent.EditItemStarted(value as SetupListItem)
    EditDialogListener.EditItemSubmitted -> SetupListItemIntent.EditItemSubmitted
}
