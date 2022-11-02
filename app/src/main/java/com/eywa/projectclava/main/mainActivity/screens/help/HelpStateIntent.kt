package com.eywa.projectclava.main.mainActivity.screens.help

import com.eywa.projectclava.main.mainActivity.CoreIntent
import com.eywa.projectclava.main.mainActivity.MainEffect
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.mainActivity.screens.ScreenIntent
import com.eywa.projectclava.main.mainActivity.screens.ScreenState


data class HelpScreenState(
        val screen: NavRoute? = null,
        val isHelpNavigationDialogShown: Boolean = false,
) : ScreenState

sealed class HelpScreenIntent : ScreenIntent<HelpScreenState> {
    override val screen: NavRoute = NavRoute.HELP_SCREEN

    data class Navigate(val destination: NavRoute) : HelpScreenIntent()
    data class GoToHelpScreen(val destination: NavRoute?) : HelpScreenIntent()
    object OpenNavDialog : HelpScreenIntent()
    object CloseNavDialog : HelpScreenIntent()

    override fun handle(
            currentState: HelpScreenState,
            handle: (CoreIntent) -> Unit,
            newStateListener: (HelpScreenState) -> Unit
    ) {
        when (this) {
            is Navigate -> handle(MainEffect.Navigate(destination))
            OpenNavDialog -> newStateListener(currentState.copy(isHelpNavigationDialogShown = true))
            CloseNavDialog -> newStateListener(currentState.copy(isHelpNavigationDialogShown = false))
            is GoToHelpScreen -> newStateListener(
                    currentState.copy(screen = destination, isHelpNavigationDialogShown = false)
            )
        }
    }
}
