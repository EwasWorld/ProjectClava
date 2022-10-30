package com.eywa.projectclava.main.mainActivity.screens

import com.eywa.projectclava.main.mainActivity.CoreIntent
import com.eywa.projectclava.main.mainActivity.MainIntent
import com.eywa.projectclava.main.mainActivity.NavRoute

/**
 * Top level for any state belonging to a single screen. Usually accompanied by a [ScreenIntent]
 */
interface ScreenState

interface ScreenIntent<T : ScreenState> : MainIntent {
    // TODO_HACKY This is kind of breaking abstraction and it's not my favourite...
    val screen: NavRoute

    fun handle(
            currentState: T,
            handle: (CoreIntent) -> Unit,
            newStateListener: (T) -> Unit,
    )
}
