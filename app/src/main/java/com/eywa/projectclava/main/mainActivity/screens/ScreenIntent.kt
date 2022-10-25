package com.eywa.projectclava.main.mainActivity.screens

import com.eywa.projectclava.main.mainActivity.CoreIntent
import com.eywa.projectclava.main.mainActivity.MainIntent
import kotlin.reflect.KClass

/**
 * Top level for any state belonging to a single screen. Usually accompanied by a [ScreenIntent]
 */
interface ScreenState

interface ScreenIntent<T : ScreenState> : MainIntent {
    fun getStateClass(): KClass<T>

    fun handle(
            currentState: T,
            handle: (CoreIntent) -> Unit,
            newStateListener: (T) -> Unit,
    )
}
