package com.eywa.projectclava.main.features.screens.manage

import com.eywa.projectclava.main.features.screens.manage.SetupListIntent.SetupListStateIntent.EditItemStateIntent
import com.eywa.projectclava.main.features.screens.manage.helperClasses.SetupListItem
import com.eywa.projectclava.main.features.ui.editNameDialog.EditDialogIntent
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.mainActivity.viewModel.CoreIntent
import com.eywa.projectclava.main.mainActivity.viewModel.MainEffect


fun EditDialogIntent.toSetupListIntent() =
        EditItemStateIntent(this)


sealed class SetupListIntent {
    /**
     * Actions that behave differently based on the type of T (usually database actions)
     */
    sealed class SetupListItemIntent<T : SetupListItem> : SetupListIntent() {
        object AddItemSubmitted : SetupListItemIntent<SetupListItem>()
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

        data class EditItemStateIntent(val value: EditDialogIntent) : SetupListStateIntent()

        object ToggleSearch : SetupListStateIntent()
        data class SearchTextChanged(val value: String) : SetupListStateIntent()

        data class Navigate(val value: NavRoute) : SetupListStateIntent()

        fun <T : SetupListItem> handle(
                currentState: SetupListState<T>,
                handle: (CoreIntent) -> Unit,
                newStateListener: (SetupListState<T>) -> Unit,
                updateName: ((editItem: T, newName: String) -> Unit)? = null,
        ) {
            when (this) {
                is EditItemStateIntent -> value.handle(currentState, newStateListener, updateName!!)
                AddNameCleared -> newStateListener(currentState.copy(addItemName = "", addItemIsDirty = false))
                is AddNameChanged -> newStateListener(currentState.copy(addItemName = value, addItemIsDirty = true))
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
