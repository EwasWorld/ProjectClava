package com.eywa.projectclava.main.mainActivity

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.eywa.projectclava.main.model.DatabaseState
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.MissingContentNextStep
import com.eywa.projectclava.main.model.TimeRemaining
import com.eywa.projectclava.main.ui.mainScreens.*
import com.eywa.projectclava.main.ui.sharedUi.AvailableCourtsHeader
import com.eywa.projectclava.main.ui.sharedUi.ClavaScreen
import java.util.*

enum class NavRoute(val route: String) {
    ADD_PLAYER("add_player") {
        @Composable
        override fun ClavaNavigation(
                navController: NavHostController,
                currentTime: () -> Calendar,
                getTimeRemaining: Match.() -> TimeRemaining?,
                viewModel: MainViewModel,
                databaseState: DatabaseState,
                preferencesState: DatastoreState,
        ) {
            SetupPlayersScreen(
                    items = databaseState.players,
                    matches = databaseState.matches,
                    getTimeRemaining = getTimeRemaining,
                    itemAddedListener = { viewModel.addPlayer(it) },
                    itemNameEditedListener = { player, newName ->
                        viewModel.updatePlayers(player.copy(name = newName))
                    },
                    itemArchivedListener = { viewModel.updatePlayers(it.copy(isArchived = true)) },
                    toggleIsPresentListener = { viewModel.updatePlayers(it.copy(isPresent = !it.isPresent)) },
                    onTabSelectedListener = { navController.navigate(it.destination.route) },
                    navigateListener = { navController.navigate(it.route) },
                    missingContentNextStep = MissingContentNextStep.getMissingContent(databaseState),
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
        @Composable
        override fun ClavaNavigation(
                navController: NavHostController,
                currentTime: () -> Calendar,
                getTimeRemaining: Match.() -> TimeRemaining?,
                viewModel: MainViewModel,
                databaseState: DatabaseState,
                preferencesState: DatastoreState,
        ) {
            SetupCourtsScreen(
                    courts = databaseState.courts,
                    matches = databaseState.matches,
                    getTimeRemaining = getTimeRemaining,
                    itemAddedListener = { viewModel.addCourt(it) },
                    itemNameEditedListener = { court, newName ->
                        viewModel.updateCourt(court.copy(name = newName))
                    },
                    prependCourt = preferencesState.prependCourt,
                    itemDeletedListener = { viewModel.deleteCourt(it) },
                    toggleIsPresentListener = { viewModel.updateCourt(it.copy(canBeUsed = !it.canBeUsed)) },
                    onTabSelectedListener = { navController.navigate(it.destination.route) },
                    navigateListener = { navController.navigate(it.route) },
                    missingContentNextStep = MissingContentNextStep.getMissingContent(databaseState),
            )
        }
    },

    CREATE_MATCH("create_match") {
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
                    players = databaseState.players,
                    matches = databaseState.matches.filterToAfterCutoff(preferencesState.clubNightStartTime),
                    getTimeRemaining = getTimeRemaining,
                    courts = databaseState.courts,
                    createMatchListener = { viewModel.addMatch(it, currentTime()) },
                    navigateListener = { navController.navigate(it.route) },
                    missingContentNextStep = MissingContentNextStep.getMissingContent(databaseState),
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

    @Composable
    abstract fun ClavaNavigation(
            navController: NavHostController,
            currentTime: () -> Calendar,
            getTimeRemaining: Match.() -> TimeRemaining?,
            viewModel: MainViewModel,
            databaseState: DatabaseState,
            preferencesState: DatastoreState,
    )

    fun Iterable<Match>.filterToAfterCutoff(cutoff: Calendar) =
            filter { !it.isFinished && (it.getFinishTime()?.after(cutoff) ?: true) }
}