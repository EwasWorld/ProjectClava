package com.eywa.projectclava.main.features.screens.manage.helperClasses

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import com.eywa.projectclava.R
import com.eywa.projectclava.main.common.sortByName
import com.eywa.projectclava.main.features.ui.ClavaIconInfo
import com.eywa.projectclava.main.model.Court

/**
 * Properties that are different between screens but not dynamic like state
 */
enum class SetupListSettings(
        val typeContentDescription: String,
        private val textPlaceholder: String,
        private val textPlaceholderAlt: String?,
        val deleteIconInfo: ClavaIconInfo = ClavaIconInfo.VectorIcon(Icons.Default.Close, "Delete"),
        val selectedTab: SetupListTabSwitcherItem,
        val confirmBeforeDelete: Boolean = true,
        val enabledStateDescription: String,
        val disabledStateDescription: String,
) {
    PLAYERS(
            typeContentDescription = "player",
            deleteIconInfo = ClavaIconInfo.PainterIcon(R.drawable.baseline_archive_24, "Archive"),
            selectedTab = SetupListTabSwitcherItem.PLAYERS,
            textPlaceholder = "John Doe",
            textPlaceholderAlt = null,
            confirmBeforeDelete = false,
            enabledStateDescription = "present",
            disabledStateDescription = "absent",
    ),
    COURTS(
            typeContentDescription = "court",
            selectedTab = SetupListTabSwitcherItem.COURTS,
            textPlaceholder = "Court 1",
            textPlaceholderAlt = "1",
            enabledStateDescription = "usable",
            disabledStateDescription = "unusable",
    ) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : SetupListItem> sortItems(items: Iterable<T>) =
                (items as Iterable<Court>).sortByName() as List<T>
    }
    ;

    fun getTextPlaceholder(useAlt: Boolean) =
            if (useAlt && textPlaceholderAlt != null) textPlaceholderAlt else textPlaceholder

    fun getStateDescription(enabled: Boolean) =
            if (enabled) enabledStateDescription else disabledStateDescription

    open fun <T : SetupListItem> sortItems(items: Iterable<T>) =
            items.sortedBy { it.name }
}