package com.eywa.projectclava.main.features.screens.manage

import com.eywa.projectclava.main.features.screens.manage.SetupListIntent.SetupListStateIntent.ConfirmIntent
import com.eywa.projectclava.main.features.screens.manage.SetupListIntent.SetupListStateIntent.EditItemStateIntent
import com.eywa.projectclava.main.features.screens.manage.helperClasses.SetupListItem
import com.eywa.projectclava.main.features.ui.confirmDialog.ConfirmDialogIntent
import com.eywa.projectclava.main.features.ui.confirmDialog.ConfirmDialogType
import com.eywa.projectclava.main.features.ui.editNameDialog.EditDialogIntent
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.mainActivity.viewModel.CoreIntent
import com.eywa.projectclava.main.mainActivity.viewModel.MainEffect


fun EditDialogIntent.toSetupListIntent() = EditItemStateIntent(this)
fun ConfirmDialogIntent.toSetupListIntent() = ConfirmIntent(this)

sealed class SetupListIntent {
    /**
     * Actions that behave differently based on the type of T (usually database actions)
     */
    sealed class SetupListItemIntent<T : SetupListItem> : SetupListIntent() {
        object AddItemSubmitted : SetupListItemIntent<SetupListItem>()
        data class UnarchiveItemSubmitted<T : SetupListItem>(val item: T) : SetupListItemIntent<T>()
        data class ItemDeleted<T : SetupListItem>(val item: T) : SetupListItemIntent<T>()
        data class ItemClicked<T : SetupListItem>(val item: T) : SetupListItemIntent<T>()
        data class ItemNameUpdated<T : SetupListItem>(val item: T, val newName: String) : SetupListItemIntent<T>()
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
        data class ConfirmIntent(val value: ConfirmDialogIntent) : SetupListStateIntent()

        object ToggleSearch : SetupListStateIntent()
        data class SearchTextChanged(val value: String) : SetupListStateIntent()

        data class Navigate(val value: NavRoute) : SetupListStateIntent()

        fun <T : SetupListItem> handle(
                currentState: SetupListState<T>,
                handleCore: (CoreIntent) -> Unit,
                handleFollowOn: (SetupListItemIntent<T>) -> Unit,
                newStateListener: (SetupListState<T>) -> Unit,
        ) {
            when (this) {
                is EditItemStateIntent -> value.handle(currentState, newStateListener) { editItem, newName ->
                    handleFollowOn(SetupListItemIntent.ItemNameUpdated(editItem, newName))
                }
                AddNameCleared -> newStateListener(currentState.copy(addItemName = "", addItemIsDirty = false))
                is AddNameChanged -> newStateListener(currentState.copy(addItemName = value, addItemIsDirty = true))
                is Navigate -> handleCore(MainEffect.Navigate(value))
                is SearchTextChanged -> newStateListener(currentState.copy(searchText = value))
                ToggleSearch -> {
                    val newSearchText = if (currentState.searchText == null) "" else null
                    newStateListener(currentState.copy(searchText = newSearchText))
                }
                ToggleUseAltPlaceholderText -> newStateListener(
                        currentState.copy(useTextPlaceholderAlt = !currentState.useTextPlaceholderAlt)
                )
                is ConfirmIntent -> value.handle(
                        currentState = currentState.deleteItemDialogState,
                        newStateListener = { newStateListener(currentState.copy(deleteItemDialogState = it)) },
                        confirmHandler = { item, actionType ->
                            when (actionType) {
                                ConfirmDialogType.DELETE -> handleFollowOn(SetupListItemIntent.ItemDeleted(item))
                            }
                        }
                )
            }
        }
    }
}
