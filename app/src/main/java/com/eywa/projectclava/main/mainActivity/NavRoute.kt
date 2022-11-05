package com.eywa.projectclava.main.mainActivity

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.eywa.projectclava.main.datastore.DatastoreState
import com.eywa.projectclava.main.features.screens.ScreenState
import com.eywa.projectclava.main.mainActivity.viewModel.MainViewModel
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.ModelState
import com.eywa.projectclava.main.model.TimeRemaining
import java.util.*

interface NavRoute {
    val route: String
    fun createInitialState(): ScreenState

    @Composable
    fun ClavaNavigation(
            navController: NavHostController,
            currentTime: () -> Calendar,
            getTimeRemaining: Match.() -> TimeRemaining?,
            viewModel: MainViewModel,
            databaseState: ModelState,
            preferencesState: DatastoreState,
            isSoftKeyboardOpen: Boolean,
    )
}