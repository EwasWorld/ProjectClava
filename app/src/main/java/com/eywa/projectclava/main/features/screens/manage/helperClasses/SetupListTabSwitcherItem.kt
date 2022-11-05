package com.eywa.projectclava.main.features.screens.manage.helperClasses

import com.eywa.projectclava.main.features.ui.topTabSwitcher.TabSwitcherItem
import com.eywa.projectclava.main.mainActivity.MainNavRoute
import com.eywa.projectclava.main.mainActivity.NavRoute

enum class SetupListTabSwitcherItem(
        override val label: String,
        override val destination: NavRoute,
) : TabSwitcherItem {
    PLAYERS("Players", MainNavRoute.ADD_PLAYER),
    COURTS("Courts", MainNavRoute.ADD_COURT),
}