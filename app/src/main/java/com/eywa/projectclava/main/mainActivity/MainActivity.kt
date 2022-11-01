package com.eywa.projectclava.main.mainActivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.toWindowInsetsCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eywa.projectclava.main.mainActivity.drawer.DrawerContent
import com.eywa.projectclava.main.mainActivity.drawer.DrawerIntent
import com.eywa.projectclava.main.mainActivity.ui.ClavaBottomNav
import com.eywa.projectclava.main.ui.sharedUi.ClavaDialog
import com.eywa.projectclava.ui.theme.ClavaColor
import com.eywa.projectclava.ui.theme.ProjectClavaTheme
import com.eywa.projectclava.ui.theme.Typography
import kotlinx.coroutines.launch
import java.util.*

/*
 * Time spent: 50 hrs
 */
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        check(NavRoute.values().map { it.route }.distinct().size == NavRoute.values().size) {
            "Duplicate NavRoute found"
        }

        var isSoftKeyboardOpen by mutableStateOf(false)

        // Hide the nav bar when the keyboard is showing
        setOnApplyWindowInsetsListener(window.decorView) { view, insets ->
            isSoftKeyboardOpen = insets.isVisible(WindowInsetsCompat.Type.ime())
            toWindowInsetsCompat(view.onApplyWindowInsets(insets.toWindowInsets()!!))
        }

        setContent {
            ProjectClavaTheme {
                val currentTime by viewModel.currentTime.collectAsState(initial = Calendar.getInstance())

                val databaseState by viewModel.databaseState.collectAsState(initial = null)
                val preferences by viewModel.preferences.collectAsState(initial = null)

                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                val focusManager = LocalFocusManager.current

                val changeDrawerState = { openDrawer: Boolean ->
                    scope.launch {
                        drawerState.animateTo(
                                if (openDrawer) DrawerValue.Open else DrawerValue.Closed,
                                tween(300, easing = FastOutLinearInEasing)
                        )
                    }
                }
                val closeDrawer = { changeDrawerState(false) }
                var showUpdateClubNightStartTimeDialog by remember { mutableStateOf(false) }

                if (preferences == null || databaseState == null) {
                    Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator()
                    }
                    return@ProjectClavaTheme
                }

                LaunchedEffect(Unit) {
                    // If no match has happened in the last 8 hours, offer to update the cut-off
                    if (!preferences!!.isDefaultClubNightStartTime) {
                        val matchTimes = databaseState!!.matches.map { it.getTime() }
                        val eightHrsAgo = (currentTime.clone() as Calendar).apply { add(Calendar.HOUR_OF_DAY, -8) }

                        if (
                        // If there's been no match activity in the past X hours
                            matchTimes.none { it.after(eightHrsAgo) }
                            // But there has been activity since club night started
                            && matchTimes.any { it.after(preferences!!.clubNightStartTime) }
                        ) {
                            showUpdateClubNightStartTimeDialog = true
                        }
                    }

                    scope.launch {
                        viewModel.effects.collect { effect ->
                            when (effect) {
                                is MainEffect.Navigate -> navController.navigate(effect.destination.route)
                                null -> {}
                            }
                        }
                    }
                }

                // Hide the soft keyboard on opening/closing the drawer
                LaunchedEffect(drawerState.isOpen) {
                    focusManager.clearFocus()
                }

                UpdateClubNightStartTimeDialog(
                        isShown = showUpdateClubNightStartTimeDialog,
                        onCancel = { showUpdateClubNightStartTimeDialog = false },
                        onOk = {
                            viewModel.handleIntent(DrawerIntent.UpdateClubNightStartTimeCalendar(currentTime))
                            showUpdateClubNightStartTimeDialog = false
                        }
                )

                Scaffold(
                        backgroundColor = ClavaColor.Background,
                        scaffoldState = rememberScaffoldState(drawerState = drawerState),
                        bottomBar = {
                            if (!isSoftKeyboardOpen) {
                                ClavaBottomNav(
                                        hasOverrunningMatch = databaseState!!.matches.any {
                                            if (!it.isOnCourt) return@any false
                                            val remaining = it.state.getTimeLeft(currentTime) ?: return@any false
                                            remaining.isEndingSoon(preferences!!.overrunIndicatorThreshold)
                                        },
                                        navController = navController,
                                )
                            }
                        },
                        drawerContent = {
                            DrawerContent(
                                    currentTime = { currentTime },
                                    preferencesState = preferences!!,
                                    databaseState = databaseState!!,
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
                                        viewModel = viewModel,
                                        databaseState = databaseState!!,
                                        preferencesState = preferences!!,
                                        isSoftKeyboardOpen = isSoftKeyboardOpen,
                                )
                            }
                        }
                    }

                    Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier.fillMaxSize()
                    ) {
                        val rounding = 40
                        Surface(
                                shape = RoundedCornerShape(0, rounding, rounding, 0),
                                color = ClavaColor.FabBackground,
                                contentColor = ClavaColor.FabIcon,
                                onClick = { changeDrawerState(true) },
                                modifier = Modifier.size(width = 25.dp, height = 100.dp)
                        ) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Open menu")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpdateClubNightStartTimeDialog(
        isShown: Boolean,
        onCancel: () -> Unit,
        onOk: () -> Unit,
) {
    ClavaDialog(
            isShown = isShown,
            title = "Update start time",
            okButtonText = "Ok",
            onCancelListener = onCancel,
            onOkListener = onOk,
    ) {
        Text(
                text = "I noticed no matches have been completed for a while." +
                        " Would you like to reset indicators for who's played who?",
                style = Typography.body1,
        )
    }
}