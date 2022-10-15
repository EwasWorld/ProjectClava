package com.eywa.projectclava.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.MatchState
import com.eywa.projectclava.main.model.Player
import com.eywa.projectclava.main.ui.mainScreens.*
import com.eywa.projectclava.main.ui.sharedUi.SetupListTabSwitcherItem
import com.eywa.projectclava.main.ui.sharedUi.TimePicker
import com.eywa.projectclava.ui.theme.ClavaColor
import com.eywa.projectclava.ui.theme.DividerThickness
import com.eywa.projectclava.ui.theme.ProjectClavaTheme
import com.eywa.projectclava.ui.theme.Typography
import kotlinx.coroutines.launch
import java.util.*

/*
 * Time spent: 26 hrs
 */

// TODO Do something with previous day's matches. Maybe add a divider between days? Add a report for day's attendees?
// TODO Store like default match time
const val DEFAULT_ADD_TIME = 60 * 2

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

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedSetupTab by remember { mutableStateOf(SetupListTabSwitcherItem.PLAYERS) }
    val onTabSelectedListener = { item: SetupListTabSwitcherItem ->
        selectedSetupTab = item
        navController.navigate(
                when (item) {
                    SetupListTabSwitcherItem.PLAYERS -> NavRoute.ADD_PLAYER.route
                    SetupListTabSwitcherItem.COURTS -> NavRoute.ADD_COURT.route
                }
        )
    }

    ModalDrawer(
            drawerContent = {
                Drawer(
                        navController = navController,
                        viewModel = viewModel,
                        players = players,
                        matches = matches,
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
        Box(
                modifier = Modifier.background(ClavaColor.Background)
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
                            selectedTab = selectedSetupTab,
                            onTabSelectedListener = onTabSelectedListener,
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
                            selectedTab = selectedSetupTab,
                            onTabSelectedListener = onTabSelectedListener,
                    )
                }
                composable(NavRoute.CREATE_MATCH.route) {
                    CreateMatchScreen(
                            players = players,
                            matches = matches,
                            courts = courts,
                            createMatchListener = { viewModel.addMatch(it, Calendar.getInstance(Locale.getDefault())) }
                    )
                }
                composable(NavRoute.UPCOMING_MATCHES.route) {
                    UpcomingMatchesScreen(
                            courts = courts,
                            matches = matches,
                            startMatchOkListener = { match, court, totalTimeSeconds ->
                                viewModel.updateMatch(
                                        match.startMatch(
                                                Calendar.getInstance(Locale.getDefault()),
                                                court,
                                                totalTimeSeconds
                                        )
                                )
                            },
                            removeMatchListener = { viewModel.deleteMatch(it) },
                            defaultTimeSeconds = viewModel.defaultMatchTime,
                    )
                }
                composable(NavRoute.CURRENT_MATCHES.route) {
                    CurrentMatchesScreen(
                            courts = courts,
                            matches = matches,
                            addTimeListener = { match, timeToAdd ->
                                viewModel.updateMatch(
                                        match.addTime(
                                                Calendar.getInstance(Locale.getDefault()),
                                                timeToAdd
                                        )
                                )
                            },
                            setCompletedListener = { viewModel.updateMatch(it.completeMatch(Calendar.getInstance(Locale.getDefault()))) },
                            changeCourtListener = { match, court ->
                                viewModel.updateMatch(match.changeCourt(court))
                            },
                            pauseListener = { viewModel.updateMatch(it.pauseMatch(Calendar.getInstance(Locale.getDefault()))) },
                            resumeListener = { match, court, resumeTime ->
                                viewModel.updateMatch(
                                        match.resumeMatch(
                                                Calendar.getInstance(Locale.getDefault()),
                                                court,
                                                resumeTime
                                        )
                                )
                            },
                    )
                }
                composable(NavRoute.PREVIOUS_MATCHES.route) {
                    PreviousMatchesScreen(
                            matches = matches,
                            addTimeListener = { match, timeToAdd ->
                                viewModel.updateMatch(
                                        match.addTime(
                                                Calendar.getInstance(Locale.getDefault()),
                                                timeToAdd
                                        )
                                )
                            },
                            deleteMatchListener = { viewModel.deleteMatch(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun Drawer(
        navController: NavController,
        viewModel: MainViewModel,
        players: Iterable<Player>,
        matches: Iterable<Match>,
        closeDrawer: () -> Unit,
) {
    val current by navController.currentBackStackEntryAsState()
    val textStyle = Typography.h4

    @Composable
    fun DrawerTextButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
        Text(
                text = text,
                style = textStyle,
                modifier = modifier
                        .fillMaxWidth()
                        .clickable(onClick = onClick)
                        .padding(horizontal = 25.dp, vertical = 10.dp)
        )
    }

    @Composable
    fun TextNavButton(text: String, destination: String) {
        DrawerTextButton(
                text = text,
                modifier = if (current?.destination?.route == destination) {
                    Modifier.background(ClavaColor.DrawerCurrentDestination)
                }
                else {
                    Modifier
                }
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
        Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 25.dp),
        ) {
            Text(
                    text = "Default match time:",
                    style = textStyle,
            )
            TimePicker(
                    totalSeconds = viewModel.defaultMatchTime,
                    timeChangedListener = { viewModel.updateDefaultMatchTime(it) }
            )
        }
        DrawerDivider()

        NavRoute.values().forEach {
            TextNavButton(text = it.drawerText, destination = it.route)
            if (it.dividerAfter) {
                DrawerDivider()
            }
        }

        DrawerDivider()
        DrawerTextButton(text = "Mark all players as not present") {
            viewModel.updatePlayers(*players.map { it.copy(isPresent = false) }.toTypedArray())
            navController.navigate(NavRoute.ADD_PLAYER.route)
            closeDrawer()
        }
        DrawerTextButton(text = "Mark all in progress matches as complete") {
            matches.forEach {
                if (it.state is MatchState.OnCourt) {
                    viewModel.updateMatch(
                            it.copy(
                                    state = MatchState.Completed(it.state.matchEndTime)
                            )
                    )
                }
            }
        }

        DrawerDivider()
        DrawerTextButton(text = "Extra options") { isExpanded = !isExpanded }
        if (isExpanded) {
            Column(modifier = Modifier.padding(start = 20.dp)) {
                DrawerTextButton(text = "Delete all matches") { viewModel.deleteAllMatches() }
            }
        }
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