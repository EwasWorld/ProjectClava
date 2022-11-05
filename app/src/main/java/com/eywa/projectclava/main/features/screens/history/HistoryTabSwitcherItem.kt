package com.eywa.projectclava.main.features.screens.history

import com.eywa.projectclava.main.features.ui.topTabSwitcher.TabSwitcherItem
import com.eywa.projectclava.main.mainActivity.MainNavRoute
import com.eywa.projectclava.main.mainActivity.NavRoute

enum class HistoryTabSwitcherItem(
        override val label: String,
        override val destination: NavRoute
) : TabSwitcherItem {
    MATCHES("Matches", MainNavRoute.MATCH_HISTORY),
    SUMMARY("Summary", MainNavRoute.HISTORY_SUMMARY),
}