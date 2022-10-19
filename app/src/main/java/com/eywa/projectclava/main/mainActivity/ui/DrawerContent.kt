package com.eywa.projectclava.main.mainActivity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eywa.projectclava.main.common.asDateTimeString
import com.eywa.projectclava.main.common.asTimeString
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.MatchState
import com.eywa.projectclava.main.model.Player
import com.eywa.projectclava.main.ui.sharedUi.TimePicker
import com.eywa.projectclava.main.ui.sharedUi.TimePickerState
import com.eywa.projectclava.ui.theme.DividerThickness
import com.eywa.projectclava.ui.theme.Typography
import java.util.*

private val drawerTextStyle = Typography.h4

@Composable
fun DrawerContent(
        currentTime: () -> Calendar,
        navController: NavController,
        viewModel: MainViewModel,
        players: Iterable<Player>,
        matches: Iterable<Match>,
        isDrawerOpen: Boolean,
        closeDrawer: () -> Unit,
) {
    val context = LocalContext.current
    var matchTimePickerState by remember(isDrawerOpen) {
        mutableStateOf(TimePickerState(viewModel.defaultMatchTime))
    }
    var timeToAddPickerState by remember(isDrawerOpen) {
        mutableStateOf(TimePickerState(viewModel.defaultTimeToAdd))
    }

    /**
     * Which expandable section is currently open
     */
    var expandedItemIndex: Int? by remember { mutableStateOf(null) }

    /**
     * Incremented every time a new expandable section is created to force unique indexes
     */
    var expanderUniquenessIndex = 0

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
                label = "Irreversible actions",
                expandedItemIndex = expandedItemIndex,
                updateExpandedItemIndex = { expandedItemIndex = it },
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
                label = "Destructive actions",
                expandedItemIndex = expandedItemIndex,
                updateExpandedItemIndex = { expandedItemIndex = it },
        ) {
            DrawerTextButton(text = "Delete all matches") { viewModel.deleteAllMatches() }
        }
    }
}

@Composable
private fun DrawerTextButton(
        text: String,
        modifier: Modifier = Modifier,
        onClick: () -> Unit
) {
    Text(
            text = text,
            style = drawerTextStyle,
            modifier = modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 25.dp, vertical = 10.dp)
    )
}

@Composable
private fun ExpandableSection(
        label: String,
        index: Int,
        expandedItemIndex: Int?,
        updateExpandedItemIndex: (Int?) -> Unit,
        content: @Composable () -> Unit,
) {
    DrawerTextButton(text = label) {
        updateExpandedItemIndex(index.takeIf { expandedItemIndex != index })
    }
    if (expandedItemIndex == index) {
        Column(modifier = Modifier.padding(start = 20.dp)) {
            content()
        }
    }
}

@Composable
private fun DrawerDivider() {
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
                style = drawerTextStyle,
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

// TODO
//@Preview
//@Composable
//fun DrawerContent_Preview() {
//    val currentTime = Calendar.getInstance()
//    DrawerContent(
//            currentTime = { currentTime },
//            navController = rememberNavController(),
//            viewModel = MainViewModel(),
//            players = listOf(),
//            matches = listOf(),
//            isDrawerOpen = true,
//            closeDrawer = {},
//    )
//}