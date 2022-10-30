package com.eywa.projectclava.main.mainActivity.screens.history

import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.ui.sharedUi.TabSwitcherItem

enum class HistoryTabSwitcherItem(
        override val label: String,
        override val destination: NavRoute
) : TabSwitcherItem {
    MATCHES("Matches", NavRoute.MATCH_HISTORY),
    SUMMARY("Summary", NavRoute.HISTORY_SUMMARY),
}