package com.eywa.projectclava.main.mainActivity

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.eywa.projectclava.main.datastore.DatastoreState
import com.eywa.projectclava.main.features.screens.archivedPlayers.ArchivedPlayersScreen
import com.eywa.projectclava.main.features.screens.archivedPlayers.ArchivedPlayersState
import com.eywa.projectclava.main.features.screens.help.HelpScreen
import com.eywa.projectclava.main.features.screens.help.HelpState
import com.eywa.projectclava.main.features.screens.history.matches.MatchHistoryScreen
import com.eywa.projectclava.main.features.screens.history.matches.MatchHistoryState
import com.eywa.projectclava.main.features.screens.history.summary.HistorySummaryScreen
import com.eywa.projectclava.main.features.screens.manage.SetupListState
import com.eywa.projectclava.main.features.screens.manage.setupPlayer.SetupPlayersScreen
import com.eywa.projectclava.main.features.screens.manage.ui.SetupCourtsScreen
import com.eywa.projectclava.main.features.screens.matchUp.CreateMatchScreen
import com.eywa.projectclava.main.features.screens.matchUp.CreateMatchState
import com.eywa.projectclava.main.features.screens.ongoing.OngoingMatchesScreen
import com.eywa.projectclava.main.features.screens.ongoing.OngoingMatchesState
import com.eywa.projectclava.main.features.screens.queue.MatchQueueScreen
import com.eywa.projectclava.main.features.screens.queue.MatchQueueState
import com.eywa.projectclava.main.features.ui.AvailableCourtsHeader
import com.eywa.projectclava.main.features.ui.ClavaScreen
import com.eywa.projectclava.main.mainActivity.viewModel.MainViewModel
import com.eywa.projectclava.main.model.*
import java.util.*

enum class NavRoute(val route: String) {
    ADD_PLAYER("add_player") {
        override fun createInitialState() = SetupListState<Player>()

        @Composable
        override fun ClavaNavigation(
                navController: NavHostController,
                currentTime: () -> Calendar,
                getTimeRemaining: Match.() -> TimeRemaining?,
                viewModel: MainViewModel,
                databaseState: ModelState,
                preferencesState: DatastoreState,
                isSoftKeyboardOpen: Boolean,
        ) {
            @Suppress("UNCHECKED_CAST")
            SetupPlayersScreen(
                    state = viewModel.getScreenState(this) as SetupListState<Player>,
                    databaseState = databaseState,
                    isSoftKeyboardOpen = isSoftKeyboardOpen,
                    getTimeRemaining = getTimeRemaining,
                    listener = { viewModel.handleIntent(it) },
            )
        }
    },

    ARCHIVED_PLAYERS("archived_players") {
        override fun createInitialState() = ArchivedPlayersState()

        @Composable
        override fun ClavaNavigation(
                navController: NavHostController,
                currentTime: () -> Calendar,
                getTimeRemaining: Match.() -> TimeRemaining?,
                viewModel: MainViewModel,
                databaseState: ModelState,
                preferencesState: DatastoreState,
                isSoftKeyboardOpen: Boolean,
        ) {
            ArchivedPlayersScreen(
                    databaseState = databaseState,
                    state = viewModel.getScreenState(this) as ArchivedPlayersState,
                    listener = { viewModel.handleIntent(it) },
            )
        }
    },

    ADD_COURT("add_court") {
        override fun createInitialState() = SetupListState<Court>()

        @Composable
        override fun ClavaNavigation(
                navController: NavHostController,
                currentTime: () -> Calendar,
                getTimeRemaining: Match.() -> TimeRemaining?,
                viewModel: MainViewModel,
                databaseState: ModelState,
                preferencesState: DatastoreState,
                isSoftKeyboardOpen: Boolean,
        ) {
            @Suppress("UNCHECKED_CAST")
            SetupCourtsScreen(
                    state = viewModel.getScreenState(this) as SetupListState<Court>,
                    databaseState = databaseState,
                    isSoftKeyboardOpen = isSoftKeyboardOpen,
                    getTimeRemaining = getTimeRemaining,
                    prependCourt = preferencesState.prependCourt,
                    listener = { viewModel.handleIntent(it) },
            )
        }
    },

    CREATE_MATCH("create_match") {
        override fun createInitialState() = CreateMatchState()

        @Composable
        override fun ClavaNavigation(
                navController: NavHostController,
                currentTime: () -> Calendar,
                getTimeRemaining: Match.() -> TimeRemaining?,
                viewModel: MainViewModel,
                databaseState: ModelState,
                preferencesState: DatastoreState,
                isSoftKeyboardOpen: Boolean,
        ) {
            CreateMatchScreen(
                    state = viewModel.getScreenState(this) as CreateMatchState,
                    clubNightStartTime = preferencesState.clubNightStartTime,
                    databaseState = databaseState,
                    getTimeRemaining = getTimeRemaining,
                    listener = { viewModel.handleIntent(it) },
            )
        }
    },

    MATCH_QUEUE("match_queue") {
        override fun createInitialState() = MatchQueueState()

        @Composable
        override fun ClavaNavigation(
                navController: NavHostController,
                currentTime: () -> Calendar,
                getTimeRemaining: Match.() -> TimeRemaining?,
                viewModel: MainViewModel,
                databaseState: ModelState,
                preferencesState: DatastoreState,
                isSoftKeyboardOpen: Boolean,
        ) {
            MatchQueueScreen(
                    databaseState = databaseState,
                    state = viewModel.getScreenState(this) as MatchQueueState,
                    getTimeRemaining = getTimeRemaining,
                    defaultTimeSeconds = preferencesState.defaultMatchTime,
                    listener = { viewModel.handleIntent(it) },
            )
        }
    },

    ONGOING_MATCHES("ongoing_matches") {
        override fun createInitialState() = OngoingMatchesState()

        @Composable
        override fun ClavaNavigation(
                navController: NavHostController,
                currentTime: () -> Calendar,
                getTimeRemaining: Match.() -> TimeRemaining?,
                viewModel: MainViewModel,
                databaseState: ModelState,
                preferencesState: DatastoreState,
                isSoftKeyboardOpen: Boolean,
        ) {
            OngoingMatchesScreen(
                    databaseState = databaseState,
                    state = viewModel.getScreenState(this) as OngoingMatchesState,
                    getTimeRemaining = getTimeRemaining,
                    defaultTimeToAddSeconds = preferencesState.defaultTimeToAdd,
                    listener = { viewModel.handleIntent(it) },
            )
        }
    },

    MATCH_HISTORY("match_history") {
        override fun createInitialState() = MatchHistoryState()

        @Composable
        override fun ClavaNavigation(
                navController: NavHostController,
                currentTime: () -> Calendar,
                getTimeRemaining: Match.() -> TimeRemaining?,
                viewModel: MainViewModel,
                databaseState: ModelState,
                preferencesState: DatastoreState,
                isSoftKeyboardOpen: Boolean,
        ) {
            MatchHistoryScreen(
                    state = viewModel.getScreenState(this) as MatchHistoryState,
                    databaseState = databaseState,
                    defaultTimeToAdd = preferencesState.defaultTimeToAdd,
                    listener = { viewModel.handleIntent(it) },
            )
        }
    },

    HISTORY_SUMMARY("history_summary") {
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
            HistorySummaryScreen(
                    databaseState = databaseState,
                    navigateListener = { navController.navigate(it.route) },
            )
        }
    },

    HELP_SCREEN("help") {
        override fun createInitialState() = HelpState()

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
            HelpScreen(
                    state = viewModel.getScreenState(this) as HelpState,
                    listener = { viewModel.handleIntent(it) },
            )
        }
    },

    // TODO_HACKY Create a build variant for this
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
    ;

    abstract fun createInitialState(): com.eywa.projectclava.main.features.screens.ScreenState

    @Composable
    abstract fun ClavaNavigation(
            navController: NavHostController,
            currentTime: () -> Calendar,
            getTimeRemaining: Match.() -> TimeRemaining?,
            viewModel: MainViewModel,
            databaseState: ModelState,
            preferencesState: DatastoreState,
            isSoftKeyboardOpen: Boolean,
    )

    companion object {
        fun get(route: String?) = values().find { it.route == route }
    }
}