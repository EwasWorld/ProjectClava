package com.eywa.projectclava.main

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.toWindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eywa.projectclava.main.common.asDateTimeString
import com.eywa.projectclava.main.common.asTimeString
import com.eywa.projectclava.main.model.*
import com.eywa.projectclava.main.ui.mainScreens.*
import com.eywa.projectclava.main.ui.sharedUi.*
import com.eywa.projectclava.ui.theme.ClavaColor
import com.eywa.projectclava.ui.theme.DividerThickness
import com.eywa.projectclava.ui.theme.ProjectClavaTheme
import com.eywa.projectclava.ui.theme.Typography
import kotlinx.coroutines.launch
import java.util.*

/*
 * Time spent: 36 hrs
 */

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var isBottomNavVisible by mutableStateOf(true)

        // Hide the nav bar when the keyboard is showing
        setOnApplyWindowInsetsListener(window.decorView) { view, insets ->
            isBottomNavVisible = !insets.isVisible(WindowInsetsCompat.Type.ime())
            toWindowInsetsCompat(view.onApplyWindowInsets(insets.toWindowInsets()!!))
        }

        setContent {
            ProjectClavaTheme {
                Navigation(viewModel, isBottomNavVisible)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Navigation(
        viewModel: MainViewModel,
        isBottomNavVisible: Boolean = true,
) {
    val currentTime by viewModel.currentTime.collectAsState(initial = Calendar.getInstance())

    val players by viewModel.players.collectAsState(initial = listOf())
    val matches by viewModel.matches.collectAsState(initial = listOf())
    val courts by viewModel.courts.collectAsState(initial = listOf())

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val focusManager = LocalFocusManager.current

    LaunchedEffect(drawerState.isOpen) {
        if (!drawerState.isOpen) {
            focusManager.clearFocus()
        }
    }

    Scaffold(
            backgroundColor = ClavaColor.Background,
            scaffoldState = rememberScaffoldState(drawerState = drawerState),
            bottomBar = {
                if (isBottomNavVisible) {
                    ClavaBottomNav(navController = navController)
                }
            },
            drawerContent = {
                Drawer(
                        currentTime = { currentTime },
                        navController = navController,
                        viewModel = viewModel,
                        players = players,
                        matches = matches,
                        isDrawerOpen = drawerState.isOpen,
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
    ) { padding ->
        ClavaNavigation(
                navController = navController,
                currentTime = { currentTime },
                players = players,
                matches = matches,
                getTimeRemaining = { state.getTimeLeft(currentTime) },
                courts = courts,
                viewModel = viewModel,
                bottomPadding = padding.calculateBottomPadding(),
        )
    }
}

@Composable
fun ClavaNavigation(
        navController: NavHostController,
        currentTime: () -> Calendar,
        players: Iterable<Player>,
        matches: Iterable<Match>,
        getTimeRemaining: Match.() -> TimeRemaining?,
        courts: Iterable<Court>,
        viewModel: MainViewModel,
        bottomPadding: Dp = 0.dp
) {
    val filterToMatchesAfterCutoff = {
        matches.filter { !it.isFinished && (it.getFinishTime()?.after(viewModel.clubNightStartTime) ?: true) }
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
                    getTimeRemaining = getTimeRemaining,
                    itemAddedListener = { viewModel.addPlayer(it) },
                    itemNameEditedListener = { player, newName ->
                        viewModel.updatePlayers(player.copy(name = newName))
                    },
                    itemDeletedListener = { viewModel.deletePlayer(it) },
                    toggleIsPresentListener = { viewModel.updatePlayers(it.copy(isPresent = !it.isPresent)) },
                    onTabSelectedListener = { navController.navigate(it.destination.route) },
            )
        }
        composable(NavRoute.ADD_COURT.route) {
            SetupCourtsScreen(
                    courts = courts,
                    matches = matches,
                    getTimeRemaining = getTimeRemaining,
                    itemAddedListener = { viewModel.addCourt(it) },
                    itemNameEditedListener = { court, newName ->
                        viewModel.updateCourt(court.copy(number = newName))
                    },
                    itemDeletedListener = { viewModel.deleteCourt(it) },
                    toggleIsPresentListener = { viewModel.updateCourt(it.copy(canBeUsed = !it.canBeUsed)) },
                    onTabSelectedListener = { navController.navigate(it.destination.route) },
            )
        }
        composable(NavRoute.CREATE_MATCH.route) {
            CreateMatchScreen(
                    players = players,
                    matches = filterToMatchesAfterCutoff(),
                    getTimeRemaining = getTimeRemaining,
                    courts = courts,
                    createMatchListener = { viewModel.addMatch(it, currentTime()) }
            )
        }
        composable(NavRoute.UPCOMING_MATCHES.route) {
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
            )
        }
        composable(NavRoute.CURRENT_MATCHES.route) {
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
            )
        }
        composable(NavRoute.PREVIOUS_MATCHES.route) {
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
            )
        }
        composable(NavRoute.DAYS_REPORT.route) {
            DaysReportScreen(
                    matches = matches,
                    onTabSelectedListener = { navController.navigate(it.destination.route) },
            )
        }
        composable(NavRoute.TEST_PAGE.route) {
//            val filtered = matches.filterKeys { it.isCurrent }.entries

            ClavaScreen(
                    noContentText = "No content",
                    hasContent = true,
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
    }
}

@Composable
fun Drawer(
        currentTime: () -> Calendar,
        navController: NavController,
        viewModel: MainViewModel,
        players: Iterable<Player>,
        matches: Iterable<Match>,
        isDrawerOpen: Boolean,
        closeDrawer: () -> Unit,
) {
    val context = LocalContext.current
    val textStyle = Typography.h4
    var matchTimePickerState by remember(isDrawerOpen) {
        mutableStateOf(TimePickerState(viewModel.defaultMatchTime))
    }
    var timeToAddPickerState by remember(isDrawerOpen) {
        mutableStateOf(TimePickerState(viewModel.defaultTimeToAdd))
    }

    /**
     * Which expandable section is currently open
     */
    var expandedItem: Int? by remember { mutableStateOf(null) }

    /**
     * Incremented every time a new expandable section is created to force unique indexes
     */
    var expanderUniquenessIndex = 0

    val expanderOnClick = { index: Int -> expandedItem = index.takeIf { expandedItem != index } }

    val timePicker by lazy {
        TimePickerDialog(
                context,
                { _, hours, minutes ->
                    viewModel.updateClubNightStartTime(
                            hours = hours,
                            minutes = minutes,
                    )
                },
                viewModel.clubNightStartTime.get(Calendar.HOUR_OF_DAY),
                viewModel.clubNightStartTime.get(Calendar.MINUTE),
                true,
        )
    }
    val datePicker by lazy {
        DatePickerDialog(
                context,
                { _, year, month, day ->
                    viewModel.updateClubNightStartTime(
                            day = day,
                            month = month,
                            year = year,
                    )
                    timePicker.show()
                },
                viewModel.clubNightStartTime.get(Calendar.YEAR),
                viewModel.clubNightStartTime.get(Calendar.MONTH),
                viewModel.clubNightStartTime.get(Calendar.DATE),
        )
    }

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

    @Composable
    fun DefaultTimePicker(
            title: String,
            errorSuffix: String,
            state: TimePickerState,
            updateState: (TimePickerState) -> Unit
    ) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 25.dp),
        ) {
            Text(
                    text = title,
                    style = textStyle,
            )
            TimePicker(
                    timePickerState = state,
                    timeChangedListener = updateState,
                    showError = false,
            )
        }
        if (state.error != null) {
            Text(
                    text = state.error!! + "\n" + errorSuffix,
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.padding(horizontal = 30.dp),
            )
        }
    }

    Column(
            modifier = Modifier.padding(vertical = 15.dp)
    ) {
        DefaultTimePicker(
                title = "Default match time:",
                errorSuffix = "Default match time is still " + viewModel.defaultMatchTime.asTimeString(),
                state = matchTimePickerState,
                updateState = {
                    matchTimePickerState = it
                    if (matchTimePickerState.isValid) {
                        viewModel.updateDefaultMatchTime(it.totalSeconds)
                    }
                }
        )
        Spacer(modifier = Modifier.height(10.dp))
        DefaultTimePicker(
                title = "Default add time:",
                errorSuffix = "Default add time is still " + viewModel.defaultTimeToAdd.asTimeString(),
                state = timeToAddPickerState,
                updateState = {
                    timeToAddPickerState = it
                    if (timeToAddPickerState.isValid) {
                        viewModel.updateDefaultTimeTimeToAdd(it.totalSeconds)
                    }
                }
        )
        DrawerTextButton(text = "Cut-off: " + viewModel.clubNightStartTime.asDateTimeString()) {
            datePicker.show()
        }

//        DrawerDivider()
//        // TODO Create a build variant for this
//        DrawerTextButton(text = "Go to test page") {
//            navController.navigate(NavRoute.TEST_PAGE.route)
//        }

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
            DrawerTextButton(text = "Clear matches and set cut off to now") {
                matches.forEach {
                    when (it.state) {
                        is MatchState.NotStarted -> viewModel.deleteMatch(it)
                        is MatchState.Completed -> {}
                        is MatchState.OnCourt -> viewModel.updateMatch(it.completeMatch(currentTime()))
                        is MatchState.Paused -> viewModel.updateMatch(it.completeMatch(currentTime()))
                    }
                    viewModel.updateClubNightStartTime(currentTime())
                }
            }
            DrawerTextButton(text = "Mark all ongoing matches as complete") {
                matches.forEach {
                    if (it.state is MatchState.OnCourt) {
                        viewModel.updateMatch(it.completeMatch(currentTime()))
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
    DAYS_REPORT("days_report"),

    TEST_PAGE("test"),
}