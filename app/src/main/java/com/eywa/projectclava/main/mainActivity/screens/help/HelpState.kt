package com.eywa.projectclava.main.mainActivity.screens.help

import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.mainActivity.screens.ScreenState


data class HelpState(
        val screen: NavRoute? = null,
        val isHelpNavigationDialogShown: Boolean = false,
) : ScreenState