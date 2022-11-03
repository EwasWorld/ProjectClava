package com.eywa.projectclava.main.features.ui.topTabSwitcher

import com.eywa.projectclava.main.mainActivity.NavRoute

interface TabSwitcherItem {
    val label: String
    val destination: NavRoute
}