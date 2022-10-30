package com.eywa.projectclava.main.mainActivity

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.eywa.projectclava.main.mainActivity.screens.ScreenState
import com.eywa.projectclava.main.mainActivity.screens.createMatch.CreateMatchScreen
import com.eywa.projectclava.main.mainActivity.screens.createMatch.CreateMatchState
import com.eywa.projectclava.main.mainActivity.screens.manage.SetupCourtsScreen
import com.eywa.projectclava.main.mainActivity.screens.manage.SetupListState
import com.eywa.projectclava.main.mainActivity.screens.manage.SetupPlayersScreen
import com.eywa.projectclava.main.model.*
import com.eywa.projectclava.main.ui.mainScreens.*
import com.eywa.projectclava.main.ui.sharedUi.AvailableCourtsHeader
import com.eywa.projectclava.main.ui.sharedUi.ClavaScreen
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
                databaseState: DatabaseState,
                preferencesState: DatastoreState,
        ) {
            @Suppress("UNCHECKED_CAST")
            SetupPlayersScreen(
                    state = viewModel.getScreenState(this) as SetupListState<Player>,
                    databaseState = databaseState,
                    getTimeRemaining = getTimeRemaining,
                    listener = { viewModel.handleIntent(it) },
            )
        }
    },

    ARCHIVED_PLAYERS("archived_players") {
        @Composable
        override fun ClavaNavigation(
                navController: NavHostController,
                currentTime: () -> Calendar,
                getTimeRemaining: Match.() -> TimeRemaining?,
                viewModel: MainViewModel,
                databaseState: DatabaseState,
                preferencesState: DatastoreState,
        ) {
            ArchivedPlayersScreen(
                    players = databaseState.players,
                    matches = databaseState.matches,
                    itemNameEditedListener = { player, newName ->
                        viewModel.updatePlayers(player.copy(name = newName))
                    },
                    itemDeletedListener = { viewModel.deletePlayer(it) },
                    itemUnarchivedListener = { viewModel.updatePlayers(it.copy(isArchived = false)) },
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
                databaseState: DatabaseState,
                preferencesState: DatastoreState,
        ) {
            @Suppress("UNCHECKED_CAST")
            SetupCourtsScreen(
                    state = viewModel.getScreenState(this) as SetupListState<Court>,
                    databaseState = databaseState,
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
                databaseState: DatabaseState,
                preferencesState: DatastoreState,
        ) {
            CreateMatchScreen(
                    state = viewModel.getScreenState(this) as CreateMatchState,
                    clubNightStartTime = preferencesState.clubNightStartTime,
                    databaseState = databaseState,
                    getTimeRemaining = getTimeRemaining,
                    listener = { viewModel.handleIntent(it) }
            )
        }
    },

    UPCOMING_MATCHES("upcoming_matches") {
        @Composable
        override fun ClavaNavigation(
                navController: NavHostController,
                currentTime: () -> Calendar,
                getTimeRemaining: Match.() -> TimeRemaining?,
                viewModel: MainViewModel,
                databaseState: DatabaseState,
                preferencesState: DatastoreState,
        ) {
            UpcomingMatchesScreen(
                    courts = databaseState.courts,
                    matches = databaseState.matches,
                    getTimeRemaining = getTimeRemaining,
                    startMatchOkListener = { match, court, totalTimeSeconds ->
                        viewModel.updateMatch(
                                match.startMatch(
                                        currentTime(),
                                        court,
                                        totalTimeSeconds
                                )
                        )
                    },
                    removeMatchListener = { viewModel.deleteMatch(it) },
                    defaultTimeSeconds = preferencesState.defaultMatchTime,
                    navigateListener = { navController.navigate(it.route) },
                    missingContentNextStep = MissingContentNextStep.getMissingContent(databaseState),
            )
        }
    },

    CURRENT_MATCHES("current_matches") {
        @Composable
        override fun ClavaNavigation(
                navController: NavHostController,
                currentTime: () -> Calendar,
                getTimeRemaining: Match.() -> TimeRemaining?,
                viewModel: MainViewModel,
                databaseState: DatabaseState,
                preferencesState: DatastoreState,
        ) {
            CurrentMatchesScreen(
                    courts = databaseState.courts,
                    matches = databaseState.matches,
                    getTimeRemaining = getTimeRemaining,
                    defaultTimeToAddSeconds = preferencesState.defaultTimeToAdd,
                    addTimeListener = { match, timeToAdd ->
                        viewModel.updateMatch(
                                match.addTime(
                                        currentTime(),
                                        timeToAdd
                                )
                        )
                    },
                    setCompletedListener = { viewModel.updateMatch(it.completeMatch(currentTime())) },
                    changeCourtListener = { match, court ->
                        viewModel.updateMatch(match.changeCourt(court))
                    },
                    pauseListener = { viewModel.updateMatch(it.pauseMatch(currentTime())) },
                    resumeListener = { match, court, resumeTime ->
                        viewModel.updateMatch(
                                match.resumeMatch(
                                        currentTime(),
                                        court,
                                        resumeTime
                                )
                        )
                    },
                    navigateListener = { navController.navigate(it.route) },
                    missingContentNextStep = MissingContentNextStep.getMissingContent(databaseState),
            )
        }
    },

    PREVIOUS_MATCHES("previous_matches") {
        @Composable
        override fun ClavaNavigation(
                navController: NavHostController,
                currentTime: () -> Calendar,
                getTimeRemaining: Match.() -> TimeRemaining?,
                viewModel: MainViewModel,
                databaseState: DatabaseState,
                preferencesState: DatastoreState,
        ) {
            PreviousMatchesScreen(
                    matches = databaseState.matches,
                    defaultTimeToAddSeconds = preferencesState.defaultTimeToAdd,
                    addTimeListener = { match, timeToAdd ->
                        viewModel.updateMatch(
                                match.addTime(
                                        currentTime(),
                                        timeToAdd
                                )
                        )
                    },
                    deleteMatchListener = { viewModel.deleteMatch(it) },
                    onTabSelectedListener = { navController.navigate(it.destination.route) },
                    navigateListener = { navController.navigate(it.route) },
                    missingContentNextStep = MissingContentNextStep.getMissingContent(databaseState),
            )
        }
    },

    DAYS_REPORT("days_report") {
        @Composable
        override fun ClavaNavigation(
                navController: NavHostController,
                currentTime: () -> Calendar,
                getTimeRemaining: Match.() -> TimeRemaining?,
                viewModel: MainViewModel,
                databaseState: DatabaseState,
                preferencesState: DatastoreState
        ) {
            DaysReportScreen(
                    matches = databaseState.matches,
                    onTabSelectedListener = { navController.navigate(it.destination.route) },
                    navigateListener = { navController.navigate(it.route) },
                    missingContentNextStep = MissingContentNextStep.getMissingContent(databaseState),
            )
        }
    },

    // TODO_HACKY Create a build variant for this
    TEST_PAGE("test") {
        @Composable
        override fun ClavaNavigation(
                navController: NavHostController,
                currentTime: () -> Calendar,
                getTimeRemaining: Match.() -> TimeRemaining?,
                viewModel: MainViewModel,
                databaseState: DatabaseState,
                preferencesState: DatastoreState
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

    open fun createInitialState(): ScreenState {
        // TODO Make this abstract
        throw NotImplementedError("No initial state")
    }

    @Composable
    abstract fun ClavaNavigation(
            navController: NavHostController,
            currentTime: () -> Calendar,
            getTimeRemaining: Match.() -> TimeRemaining?,
            viewModel: MainViewModel,
            databaseState: DatabaseState,
            preferencesState: DatastoreState,
    )
}