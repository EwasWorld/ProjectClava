package com.eywa.projectclava.main.features.screens.manage

import com.eywa.projectclava.main.features.screens.manage.helperClasses.SetupListItem
import com.eywa.projectclava.main.features.ui.editNameDialog.EditDialogState


data class SetupListState<T : SetupListItem>(
        val addItemName: String = "",
        val addItemIsDirty: Boolean = false,
        override val editItemName: String = "",
        override val editNameIsDirty: Boolean = false,
        override val editDialogOpenFor: T? = null,
        val useTextPlaceholderAlt: Boolean = false,
        val searchText: String? = null,
) : com.eywa.projectclava.main.features.screens.ScreenState, EditDialogState<T> {
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

