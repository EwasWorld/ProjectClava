package com.eywa.projectclava.main.features.screens

import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.mainActivity.viewModel.CoreIntent
import com.eywa.projectclava.main.mainActivity.viewModel.MainIntent


interface ScreenIntent<T : ScreenState> : MainIntent {
    // TODO_HACKY This is kind of breaking abstraction and it's not my favourite...
    val screen: NavRoute

    fun handle(
            currentState: T,
            handle: (CoreIntent) -> Unit,
            newStateListener: (T) -> Unit,
    )
}
