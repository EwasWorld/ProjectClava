package com.eywa.projectclava.main.mainActivity.viewModel

import com.eywa.projectclava.main.mainActivity.NavRoute

sealed class MainEffect : CoreIntent {
    data class Navigate(val destination: NavRoute, val currentRoute: NavRoute? = null) : MainEffect()
    object BackPressed : MainEffect()
    object OpenDrawer : MainEffect()
    object CloseDrawer : MainEffect()
}