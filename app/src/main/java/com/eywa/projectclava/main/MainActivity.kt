package com.eywa.projectclava.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eywa.projectclava.main.model.Player
import com.eywa.projectclava.main.ui.mainScreens.*
import com.eywa.projectclava.ui.theme.DividerThickness
import com.eywa.projectclava.ui.theme.ProjectClavaTheme
import com.eywa.projectclava.ui.theme.Typography
import kotlinx.coroutines.launch
import java.util.*

/*
 * Time spent: 21 hrs
 */

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectClavaTheme {
                Navigation(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Navigation(viewModel: MainViewModel) {
    val navController = rememberNavController()

    val players by viewModel.players.collectAsState(initial = listOf())
    val matches by viewModel.matches.collectAsState(initial = listOf())
    val courts by viewModel.courts.collectAsState(initial = listOf())
    val defaultTimer = rememberSaveable { mutableStateOf(15 * 60) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalDrawer(
            drawerContent = {
                Drawer(
                        navController = navController,
                        viewModel = viewModel,
                        players = players,
                        closeDrawer = {
                            scope.launch {
                                drawerState.animateTo(
                                        DrawerValue.Closed,
                                        tween(300, easing = FastOutLinearInEasing)
                                )
                            }
                        }
                )
            },
            drawerState = drawerState,
    ) {
        NavHost(navController = navController, startDestination = NavRoute.ADD_PLAYER.route) {
            composable(NavRoute.ADD_PLAYER.route) {
                SetupPlayersScreen(
                        items = players,
                        matches = matches,
                        itemAddedListener = { viewModel.addPlayer(it) },
                        itemNameEditedListener = { player, newName ->
                            viewModel.updatePlayers(player.copy(name = newName))
                        },
                        itemDeletedListener = { viewModel.deletePlayer(it) },
                        toggleIsPresentListener = { viewModel.updatePlayers(it.copy(isPresent = !it.isPresent)) },
                )
            }
            composable(NavRoute.ADD_COURT.route) {
                SetupCourtsScreen(
                        courts = courts,
                        matches = matches,
                        itemAddedListener = { viewModel.addCourt(it) },
                        itemNameEditedListener = { court, newName ->
                            viewModel.updateCourt(court.copy(number = newName))
                        },
                        itemDeletedListener = { viewModel.deleteCourt(it) },
                        toggleIsPresentListener = { viewModel.updateCourt(it.copy(canBeUsed = !it.canBeUsed)) },
                )
            }
            composable(NavRoute.CREATE_MATCH.route) {
                CreateMatchScreen(
                        players = players,
                        matches = matches,
                        courts = courts,
                        createMatchListener = { viewModel.addMatch(it, Calendar.getInstance()) }
                )
            }
            composable(NavRoute.UPCOMING_MATCHES.route) {
                UpcomingMatchesScreen(
                        courts = courts,
                        matches = matches,
                        startMatchOkListener = { match, court, totalTimeSeconds, useAsDefaultTime ->
                            if (useAsDefaultTime) {
                                defaultTimer.value = totalTimeSeconds
                            }
                            viewModel.updateMatch(match.startMatch(Calendar.getInstance(), court, totalTimeSeconds))
                        },
                        removeMatchListener = { viewModel.deleteMatch(it) },
                        defaultTimeSeconds = defaultTimer.value,
                )
            }
            composable(NavRoute.CURRENT_MATCHES.route) {
                CurrentMatchesScreen(
                        courts = courts,
                        matches = matches,
                        addTimeListener = { match, timeToAdd ->
                            viewModel.updateMatch(match.addTime(Calendar.getInstance(), timeToAdd))
                        },
                        setCompletedListener = { viewModel.updateMatch(it.completeMatch(Calendar.getInstance())) },
                        changeCourtListener = { match, court ->
                            viewModel.updateMatch(match.changeCourt(court))
                        },
                        pauseListener = { viewModel.updateMatch(it.pauseMatch(Calendar.getInstance())) },
                        resumeListener = { match, court ->
                            viewModel.updateMatch(match.resumeMatch(Calendar.getInstance(), court))
                        },
                )
            }
            composable(NavRoute.PREVIOUS_MATCHES.route) {
                PreviousMatchesScreen()
            }
        }
    }
}

@Composable
fun Drawer(
        navController: NavController,
        viewModel: MainViewModel,
        players: Iterable<Player>,
        closeDrawer: () -> Unit,
) {
    @Composable
    fun DrawerTextButton(text: String, onClick: () -> Unit) {
        Text(
                text = text,
                style = Typography.h4,
                modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onClick)
                        .padding(horizontal = 25.dp, vertical = 10.dp)
        )
    }

    @Composable
    fun TextNavButton(text: String, destination: String) {
        DrawerTextButton(
                text = text,
        ) {
            navController.navigate(destination)
            closeDrawer()
        }
    }

    @Composable
    fun DrawerDivider() {
        Divider(
                thickness = DividerThickness,
                modifier = Modifier.padding(vertical = 5.dp)
        )
    }

    var isExpanded by remember { mutableStateOf(false) }

    Column(
            modifier = Modifier.padding(vertical = 15.dp)
    ) {
        NavRoute.values().forEach {
            TextNavButton(text = it.drawerText, destination = it.route)
            if (it.dividerAfter) {
                DrawerDivider()
            }
        }

        DrawerDivider()
        DrawerTextButton(text = "Mark all players as not present") {
            viewModel.updatePlayers(*players.map { it.copy(isPresent = false) }.toTypedArray())
        }

        DrawerDivider()
        DrawerTextButton(text = "Extra options") { isExpanded = !isExpanded }
        if (isExpanded) {
            Column(modifier = Modifier.padding(start = 20.dp)) {
                DrawerTextButton(text = "Delete all matches") { viewModel.deleteAllMatches() }
            }
        }
        // TODO Button to complete all matches
    }
}

enum class NavRoute(val route: String, val drawerText: String, val dividerAfter: Boolean = false) {
    ADD_PLAYER("add_player", "Add players"),
    ADD_COURT("add_court", "Add courts", true),

    CREATE_MATCH("create_match", "Setup matches"),
    UPCOMING_MATCHES("upcoming_matches", "Queued matches"),
    CURRENT_MATCHES("current_matches", "Ongoing matches"),
    PREVIOUS_MATCHES("previous_matches", "Match history"),
}