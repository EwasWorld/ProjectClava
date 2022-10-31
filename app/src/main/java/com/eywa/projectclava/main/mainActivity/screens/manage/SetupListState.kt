package com.eywa.projectclava.main.mainActivity.screens.manage

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import com.eywa.projectclava.R
import com.eywa.projectclava.main.common.sortByName
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.mainActivity.screens.ScreenState
import com.eywa.projectclava.main.model.Court
import com.eywa.projectclava.main.ui.sharedUi.ClavaIconInfo
import com.eywa.projectclava.main.ui.sharedUi.EditDialogState
import com.eywa.projectclava.main.ui.sharedUi.NamedItem
import com.eywa.projectclava.main.ui.sharedUi.TabSwitcherItem


interface SetupListItem : NamedItem {
    val enabled: Boolean
}

enum class SetupListTabSwitcherItem(
        override val label: String,
        override val destination: NavRoute,
) : TabSwitcherItem {
    PLAYERS("Players", NavRoute.ADD_PLAYER),
    COURTS("Courts", NavRoute.ADD_COURT),
}

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

/**
 * Properties that are different between screens but not dynamic like state
 */
enum class SetupListSettings(
        val typeContentDescription: String,
        private val textPlaceholder: String,
        private val textPlaceholderAlt: String?,
        val deleteIconInfo: ClavaIconInfo = ClavaIconInfo.VectorIcon(Icons.Default.Close, "Delete"),
        val selectedTab: SetupListTabSwitcherItem,
        val sortItems: (Iterable<SetupListItem>) -> List<SetupListItem> = { items -> items.sortedBy { it.name } }
) {
    PLAYERS(
            typeContentDescription = "player",
            deleteIconInfo = ClavaIconInfo.PainterIcon(R.drawable.baseline_archive_24, "Archive"),
            selectedTab = SetupListTabSwitcherItem.PLAYERS,
            textPlaceholder = "John Doe",
            textPlaceholderAlt = null,
    ),
    COURTS(
            typeContentDescription = "court",
            selectedTab = SetupListTabSwitcherItem.COURTS,
            textPlaceholder = "Court 1",
            textPlaceholderAlt = "1",
            sortItems = {
                @Suppress("UNCHECKED_CAST")
                (it as Iterable<Court>).sortByName()
            }
    )
    ;

    fun getTextPlaceholder(useAlt: Boolean) =
            if (useAlt && textPlaceholderAlt != null) textPlaceholderAlt else textPlaceholder
}
