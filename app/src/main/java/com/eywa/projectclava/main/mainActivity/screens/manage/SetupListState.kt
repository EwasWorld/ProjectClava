package com.eywa.projectclava.main.mainActivity.screens.manage

import com.eywa.projectclava.main.mainActivity.screens.ScreenState
import com.eywa.projectclava.main.mainActivity.screens.manage.helperClasses.SetupListItem
import com.eywa.projectclava.main.ui.sharedUi.EditDialogState


data class SetupListState<T : SetupListItem>(
        val addItemName: String = "",
        val addItemIsDirty: Boolean = false,
        override val editItemName: String = "",
        override val editNameIsDirty: Boolean = false,
        override val editDialogOpenFor: T? = null,
        val useTextPlaceholderAlt: Boolean = false,
        val searchText: String? = null,
) : ScreenState, EditDialogState<T> {
    val isSearchExpanded = searchText != null

    override fun editItemCopy(
            editItemName: String,
            editNameIsDirty: Boolean,
            editDialogOpenFor: T?
    ) = copy(
            editItemName = editItemName,
            editNameIsDirty = editNameIsDirty,
            editDialogOpenFor = editDialogOpenFor
    )
}

