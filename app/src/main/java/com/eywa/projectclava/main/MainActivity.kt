package com.eywa.projectclava.main

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eywa.projectclava.main.model.Court
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.MatchState
import com.eywa.projectclava.main.model.Player
import com.eywa.projectclava.main.ui.mainScreens.*
import com.eywa.projectclava.main.ui.sharedUi.ClavaBottomNav
import com.eywa.projectclava.main.ui.sharedUi.SetupListTabSwitcherItem
import com.eywa.projectclava.main.ui.sharedUi.TimePicker
import com.eywa.projectclava.ui.theme.ClavaColor
import com.eywa.projectclava.ui.theme.DividerThickness
import com.eywa.projectclava.ui.theme.ProjectClavaTheme
import com.eywa.projectclava.ui.theme.Typography
import kotlinx.coroutines.launch
import java.util.*

/*
 * Time spent: 29 hrs
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

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Navigation(viewModel: MainViewModel) {
    val navController = rememberNavController()

    val players by viewModel.players.collectAsState(initial = listOf())
    val matches by viewModel.matches.collectAsState(initial = listOf())
    val courts by viewModel.courts.collectAsState(initial = listOf())

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    Scaffold(
            backgroundColor = ClavaColor.Background,
            scaffoldState = rememberScaffoldState(drawerState = drawerState),
            bottomBar = { ClavaBottomNav(navController = navController) },
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
    ) {
        ClavaNavigation(
                navController = navController,
                players = players,
                matches = matches,
                courts = courts,
                viewModel = viewModel,
                bottomPadding = it.calculateBottomPadding(),
        )
    }
}

@Composable
fun ClavaNavigation(
        navController: NavHostController,
        players: Iterable<Player>,
        matches: Iterable<Match>,
        courts: Iterable<Court>,
        viewModel: MainViewModel,
        bottomPadding: Dp = 0.dp
) {
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

    NavHost(
            navController = navController,
            startDestination = NavRoute.ADD_PLAYER.route,
            modifier = Modifier.padding(bottom = bottomPadding),
    ) {
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

@Composable
fun Drawer(
        navController: NavController,
        viewModel: MainViewModel,
        players: Iterable<Player>,
        matches: Iterable<Match>,
        closeDrawer: () -> Unit,
) {
    val textStyle = Typography.h4

    /**
     * Which expandable section is currently open
     */
    var expandedItem: Int? by remember { mutableStateOf(null) }

    /**
     * Incremented every time a new expandable section is created to force unique indexes
     */
    var expanderUniquenessIndex = 0

    val expanderOnClick = { index: Int -> expandedItem = index.takeIf { expandedItem != index } }

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
    fun ExpandableSection(
            index: Int,
            label: String,
            content: @Composable () -> Unit,
    ) {
        DrawerTextButton(text = label) { expanderOnClick(index) }
        if (expandedItem == index) {
            Column(modifier = Modifier.padding(start = 20.dp)) {
                content()
            }
        }
    }

    @Composable
    fun DrawerDivider() {
        Divider(
                thickness = DividerThickness,
                modifier = Modifier.padding(vertical = 5.dp)
        )
    }

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
        ExpandableSection(
                index = expanderUniquenessIndex++,
                label = "Irreversible actions"
        ) {
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
        }

        ExpandableSection(
                index = expanderUniquenessIndex++,
                label = "Destructive actions"
        ) {
            DrawerTextButton(text = "Delete all matches") { viewModel.deleteAllMatches() }
        }
    }
}

enum class NavRoute(val route: String) {
    ADD_PLAYER("add_player"),
    ADD_COURT("add_court"),

    CREATE_MATCH("create_match"),
    UPCOMING_MATCHES("upcoming_matches"),
    CURRENT_MATCHES("current_matches"),
    PREVIOUS_MATCHES("previous_matches"),
}