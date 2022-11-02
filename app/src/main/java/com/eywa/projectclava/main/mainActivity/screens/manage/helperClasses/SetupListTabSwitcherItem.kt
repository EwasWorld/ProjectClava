package com.eywa.projectclava.main.mainActivity.screens.manage.helperClasses

import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.ui.sharedUi.TabSwitcherItem

enum class SetupListTabSwitcherItem(
        override val label: String,
        override val destination: NavRoute,
) : TabSwitcherItem {
    PLAYERS("Players", NavRoute.ADD_PLAYER),
    COURTS("Courts", NavRoute.ADD_COURT),
}