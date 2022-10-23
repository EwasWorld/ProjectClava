package com.eywa.projectclava.main.mainActivity

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.eywa.projectclava.main.model.*
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
                players: Iterable<Player>,
                matches: Iterable<Match>,
                getTimeRemaining: Match.() -> TimeRemaining?,
                courts: Iterable<Court>,
                viewModel: MainViewModel
        ) {
            SetupPlayersScreen(
                    items = players,
                    matches = matches,
                    getTimeRemaining = getTimeRemaining,
                    itemAddedListener = { viewModel.addPlayer(it) },
                    itemNameEditedListener = { player, newName ->
                        viewModel.updatePlayers(player.copy(name = newName))
                    },
                    itemArchivedListener = { viewModel.updatePlayers(it.copy(isArchived = true)) },
                    toggleIsPresentListener = { viewModel.updatePlayers(it.copy(isPresent = !it.isPresent)) },
                    onTabSelectedListener = { navController.navigate(it.destination.route) },
                    navigateListener = { navController.navigate(it.route) },
                    missingContentNextStep = MissingContentNextStep.getMissingContent(players, courts, matches),
            )
        }
    },

    ARCHIVED_PLAYERS("archived_players") {
        @Composable
        override fun ClavaNavigation(
                navController: NavHostController,
                currentTime: () -> Calendar,
                players: Iterable<Player>,
                matches: Iterable<Match>,
                getTimeRemaining: Match.() -> TimeRemaining?,
                courts: Iterable<Court>,
                viewModel: MainViewModel
        ) {
            ArchivedPlayersScreen(
                    players = players,
                    matches = matches,
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
                players: Iterable<Player>,
                matches: Iterable<Match>,
                getTimeRemaining: Match.() -> TimeRemaining?,
                courts: Iterable<Court>,
                viewModel: MainViewModel
        ) {
            SetupCourtsScreen(
                    courts = courts,
                    matches = matches,
                    getTimeRemaining = getTimeRemaining,
                    itemAddedListener = { viewModel.addCourt(it) },
                    itemNameEditedListener = { court, newName ->
                        viewModel.updateCourt(court.copy(name = newName))
                    },
                    prependCourt = viewModel.prependCourt,
                    itemDeletedListener = { viewModel.deleteCourt(it) },
                    toggleIsPresentListener = { viewModel.updateCourt(it.copy(canBeUsed = !it.canBeUsed)) },
                    onTabSelectedListener = { navController.navigate(it.destination.route) },
                    navigateListener = { navController.navigate(it.route) },
                    missingContentNextStep = MissingContentNextStep.getMissingContent(players, courts, matches),
            )
        }
    },

    CREATE_MATCH("create_match") {
        @Composable
        override fun ClavaNavigation(
                navController: NavHostController,
                currentTime: () -> Calendar,
                players: Iterable<Player>,
                matches: Iterable<Match>,
                getTimeRemaining: Match.() -> TimeRemaining?,
                courts: Iterable<Court>,
                viewModel: MainViewModel
        ) {
            CreateMatchScreen(
                    players = players,
                    matches = matches.filterToAfterCutoff(viewModel.clubNightStartTime),
                    getTimeRemaining = getTimeRemaining,
                    courts = courts,
                    createMatchListener = { viewModel.addMatch(it, currentTime()) },
                    navigateListener = { navController.navigate(it.route) },
                    missingContentNextStep = MissingContentNextStep.getMissingContent(players, courts, matches),
            )
        }
    },

    UPCOMING_MATCHES("upcoming_matches") {
        @Composable
        override fun ClavaNavigation(
                navController: NavHostController,
                currentTime: () -> Calendar,
                players: Iterable<Player>,
                matches: Iterable<Match>,
                getTimeRemaining: Match.() -> TimeRemaining?,
                courts: Iterable<Court>,
                viewModel: MainViewModel
        ) {
            UpcomingMatchesScreen(
                    courts = courts,
                    matches = matches,
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
                    defaultTimeSeconds = viewModel.defaultMatchTime,
                    navigateListener = { navController.navigate(it.route) },
                    missingContentNextStep = MissingContentNextStep.getMissingContent(players, courts, matches),
            )
        }
    },

    CURRENT_MATCHES("current_matches") {
        @Composable
        override fun ClavaNavigation(
                navController: NavHostController,
                currentTime: () -> Calendar,
                players: Iterable<Player>,
                matches: Iterable<Match>,
                getTimeRemaining: Match.() -> TimeRemaining?,
                courts: Iterable<Court>,
                viewModel: MainViewModel
        ) {
            CurrentMatchesScreen(
                    courts = courts,
                    matches = matches,
                    getTimeRemaining = getTimeRemaining,
                    defaultTimeToAddSeconds = viewModel.defaultTimeToAdd,
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
                    missingContentNextStep = MissingContentNextStep.getMissingContent(players, courts, matches),
            )
        }
    },

    PREVIOUS_MATCHES("previous_matches") {
        @Composable
        override fun ClavaNavigation(
                navController: NavHostController,
                currentTime: () -> Calendar,
                players: Iterable<Player>,
                matches: Iterable<Match>,
                getTimeRemaining: Match.() -> TimeRemaining?,
                courts: Iterable<Court>,
                viewModel: MainViewModel
        ) {
            PreviousMatchesScreen(
                    matches = matches,
                    defaultTimeToAddSeconds = viewModel.defaultTimeToAdd,
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
                    missingContentNextStep = MissingContentNextStep.getMissingContent(players, courts, matches),
            )
        }
    },

    DAYS_REPORT("days_report") {
        @Composable
        override fun ClavaNavigation(
                navController: NavHostController,
                currentTime: () -> Calendar,
                players: Iterable<Player>,
                matches: Iterable<Match>,
                getTimeRemaining: Match.() -> TimeRemaining?,
                courts: Iterable<Court>,
                viewModel: MainViewModel
        ) {
            DaysReportScreen(
                    matches = matches,
                    onTabSelectedListener = { navController.navigate(it.destination.route) },
                    navigateListener = { navController.navigate(it.route) },
                    missingContentNextStep = MissingContentNextStep.getMissingContent(players, courts, matches),
            )
        }
    },

    TEST_PAGE("test") {
        @Composable
        override fun ClavaNavigation(
                navController: NavHostController,
                currentTime: () -> Calendar,
                players: Iterable<Player>,
                matches: Iterable<Match>,
                getTimeRemaining: Match.() -> TimeRemaining?,
                courts: Iterable<Court>,
                viewModel: MainViewModel
        ) {
//            val filtered = matches.filterKeys { it.isCurrent }.entries

            ClavaScreen(
                    noContentText = "No content",
                    navigateListener = { },
                    missingContentNextStep = null,
                    headerContent = {
                        AvailableCourtsHeader(
                                courts = courts,
                                matches = matches,
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
            players: Iterable<Player>,
            matches: Iterable<Match>,
            getTimeRemaining: Match.() -> TimeRemaining?,
            courts: Iterable<Court>,
            viewModel: MainViewModel,
    )

    fun Iterable<Match>.filterToAfterCutoff(cutoff: Calendar) =
            filter { !it.isFinished && (it.getFinishTime()?.after(cutoff) ?: true) }
}