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
import com.eywa.projectclava.main.mainActivity.drawer.DrawerContent
import com.eywa.projectclava.main.mainActivity.drawer.DrawerIntent
import com.eywa.projectclava.main.mainActivity.ui.ClavaBottomNav
import com.eywa.projectclava.main.model.DatabaseState
import com.eywa.projectclava.ui.theme.ClavaColor
import com.eywa.projectclava.ui.theme.ProjectClavaTheme
import kotlinx.coroutines.launch
import java.util.*

/*
 * Time spent: 38 hrs
 */
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        check(NavRoute.values().map { it.route }.distinct().size == NavRoute.values().size) {
            "Duplicate NavRoute found"
        }

        var isBottomNavVisible by mutableStateOf(true)

        // Hide the nav bar when the keyboard is showing
        setOnApplyWindowInsetsListener(window.decorView) { view, insets ->
            isBottomNavVisible = !insets.isVisible(WindowInsetsCompat.Type.ime())
            toWindowInsetsCompat(view.onApplyWindowInsets(insets.toWindowInsets()!!))
        }

        setContent {
            ProjectClavaTheme {
                val currentTime by viewModel.currentTime.collectAsState(initial = Calendar.getInstance())

                val databaseState by viewModel.databaseState.collectAsState(initial = DatabaseState())
                val preferences by viewModel.preferences.collectAsState(initial = DatastoreState())

                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                val focusManager = LocalFocusManager.current

                val closeDrawer = {
                    scope.launch {
                        drawerState.animateTo(
                                DrawerValue.Closed,
                                tween(300, easing = FastOutLinearInEasing)
                        )
                    }
                }

                LaunchedEffect(Unit) {
                    scope.launch {
                        viewModel.effects.collect { effect ->
                            when (effect) {
                                is MainEffect.Navigate -> navController.navigate(effect.value.route)
                                null -> {}
                            }
                        }
                    }
                }

                // Hide the soft keyboard on opening/closing the drawer
                LaunchedEffect(drawerState.isOpen) {
                    focusManager.clearFocus()
                }

                Scaffold(
                        backgroundColor = ClavaColor.Background,
                        scaffoldState = rememberScaffoldState(drawerState = drawerState),
                        bottomBar = {
                            if (isBottomNavVisible) {
                                ClavaBottomNav(
                                        hasOverrunningMatch = databaseState.matches.any {
                                            if (!it.isOnCourt) return@any false
                                            val remaining = it.state.getTimeLeft(currentTime) ?: return@any false
                                            remaining.isEndingSoon(preferences.overrunIndicatorThreshold)
                                        },
                                        navController = navController,
                                )
                            }
                        },
                        drawerContent = {
                            DrawerContent(
                                    currentTime = { currentTime },
                                    preferencesState = preferences,
                                    databaseState = databaseState,
                                    isDrawerOpen = drawerState.isOpen,
                                    closeDrawer = { closeDrawer() },
                                    listener = {
                                        if (it is DrawerIntent.Navigate) {
                                            navController.navigate(it.value.route)
                                            closeDrawer()
                                        }
                                        else {
                                            viewModel.handleIntent(it)
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
                                        getTimeRemaining = { state.getTimeLeft(currentTime) },
                                        databaseState = databaseState,
                                        preferencesState = preferences,
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
