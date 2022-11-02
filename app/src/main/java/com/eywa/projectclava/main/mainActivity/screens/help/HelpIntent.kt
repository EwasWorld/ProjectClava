package com.eywa.projectclava.main.mainActivity.screens.help

import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.mainActivity.screens.ScreenIntent
import com.eywa.projectclava.main.mainActivity.viewModel.CoreIntent
import com.eywa.projectclava.main.mainActivity.viewModel.MainEffect


sealed class HelpIntent : ScreenIntent<HelpState> {
    override val screen: NavRoute = NavRoute.HELP_SCREEN

    data class Navigate(val destination: NavRoute) : HelpIntent()
    data class GoToHelpScreen(val destination: NavRoute?) : HelpIntent()
    object OpenNavDialog : HelpIntent()
    object CloseNavDialog : HelpIntent()

    override fun handle(
            currentState: HelpState,
            handle: (CoreIntent) -> Unit,
            newStateListener: (HelpState) -> Unit
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
