package com.eywa.projectclava.main.features.screens.help

import com.eywa.projectclava.main.mainActivity.NavRoute


data class HelpState(
        val screen: NavRoute? = null,
        val isHelpNavigationDialogShown: Boolean = false,
) : com.eywa.projectclava.main.features.screens.ScreenState