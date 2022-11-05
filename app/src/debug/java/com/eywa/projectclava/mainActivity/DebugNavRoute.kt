package com.eywa.projectclava.mainActivity

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.eywa.projectclava.main.datastore.DatastoreState
import com.eywa.projectclava.main.features.ui.AvailableCourtsHeader
import com.eywa.projectclava.main.features.ui.ClavaScreen
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.mainActivity.viewModel.MainViewModel
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.ModelState
import com.eywa.projectclava.main.model.TimeRemaining
import java.util.*

enum class DebugNavRoute(override val route: String) : NavRoute {
    TEST_PAGE("test") {
        override fun createInitialState() = throw NotImplementedError("No initial state")

        @Composable
        override fun ClavaNavigation(
                navController: NavHostController,
                currentTime: () -> Calendar,
                getTimeRemaining: Match.() -> TimeRemaining?,
                viewModel: MainViewModel,
                databaseState: ModelState,
                preferencesState: DatastoreState,
                isSoftKeyboardOpen: Boolean
        ) {
//            val filtered = matches.filterKeys { it.isCurrent }.entries

            ClavaScreen(
                    noContentText = "No content",
                    navigateListener = { },
                    missingContentNextStep = null,
                    headerContent = {
                        AvailableCourtsHeader(
                                courts = databaseState.courts,
                                matches = databaseState.matches,
                                getTimeRemaining = getTimeRemaining
                        )
                    },
                    footerContent = {
//                        Text(
//                                text = filtered.firstOrNull()?.key?.players?.joinToString { it.name } ?: "No players"
//                        )
                    }
            ) {
//                item {
////                    SelectableListItem() {
////
////                    }
//                    Text(
//                            text = filtered.firstOrNull()?.value?.asTimeString() ?: "No Time"
//                    )
//                }
            }
        }
    },
}