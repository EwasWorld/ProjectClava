package com.eywa.projectclava.main.mainActivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.toWindowInsetsCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eywa.projectclava.main.mainActivity.ui.ClavaBottomNav
import com.eywa.projectclava.main.mainActivity.ui.DrawerContent
import com.eywa.projectclava.ui.theme.ClavaColor
import com.eywa.projectclava.ui.theme.ProjectClavaTheme
import kotlinx.coroutines.launch
import java.util.*

/*
 * Time spent: 38 hrs
 */
// TODO What happens if a player is deleted - their matches aren't cleared but then the match doesn't have any players
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterialApi::class)
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
                val currentTime by viewModel.currentTime.collectAsState(initial = Calendar.getInstance())

                val players by viewModel.players.collectAsState(initial = listOf())
                val matches by viewModel.matches.collectAsState(initial = listOf())
                val courts by viewModel.courts.collectAsState(initial = listOf())

                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                val focusManager = LocalFocusManager.current

                // Hide the soft keyboard on closing the drawer
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
                                ClavaBottomNav(
                                        hasOverrunningMatch = matches.any {
                                            if (!it.isOnCourt) return@any false
                                            val remaining = it.state.getTimeLeft(currentTime) ?: return@any false
                                            remaining.isEndingSoon(viewModel.overrunIndicatorThreshold)
                                        },
                                        navController = navController,
                                )
                            }
                        },
                        drawerContent = {
                            DrawerContent(
                                    currentTime = { currentTime },
                                    defaultMatchTime = viewModel.defaultMatchTime,
                                    defaultTimeToAdd = viewModel.defaultTimeToAdd,
                                    clubNightStartTime = viewModel.clubNightStartTime,
                                    prependCourt = viewModel.prependCourt,
                                    overrunIndicatorThreshold = viewModel.overrunIndicatorThreshold,
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
                                    },
                                    listener = {
                                        if (it is MainIntent.DrawerIntent.Navigate) {
                                            navController.navigate(it.route.route)
                                        }
                                        else {
                                            viewModel.mainHandle(it)
                                        }
                                    }
                            )
                        },
                ) { padding ->
                    NavHost(
                            navController = navController,
                            startDestination = NavRoute.ADD_PLAYER.route,
                            modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
                    ) {
                        NavRoute.values().forEach { route ->
                            composable(route.route) {
                                route.ClavaNavigation(
                                        navController = navController,
                                        currentTime = { currentTime },
                                        players = players,
                                        matches = matches,
                                        getTimeRemaining = { state.getTimeLeft(currentTime) },
                                        courts = courts,
                                        viewModel = viewModel,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
