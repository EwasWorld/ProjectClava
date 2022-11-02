package com.eywa.projectclava.main.mainActivity.drawer

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.main.common.UpdateCalendarInfo
import com.eywa.projectclava.main.common.asDateString
import com.eywa.projectclava.main.common.asTimeString
import com.eywa.projectclava.main.datastore.DatastoreState
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.model.MatchState
import com.eywa.projectclava.main.model.ModelState
import com.eywa.projectclava.main.ui.sharedUi.TimePicker
import com.eywa.projectclava.main.ui.sharedUi.TimePickerState
import com.eywa.projectclava.ui.theme.DividerThickness
import com.eywa.projectclava.ui.theme.Typography
import com.eywa.projectclava.ui.theme.asClickableStyle
import java.util.*

private val drawerTextStyle = Typography.h4

@Composable
fun DrawerContent(
        currentTime: () -> Calendar,
        preferencesState: DatastoreState,
        databaseState: ModelState,
        isDrawerOpen: Boolean,
        listener: (DrawerIntent) -> Unit,
) {
    val context = LocalContext.current
    var matchTimePickerState by remember(isDrawerOpen) {
        mutableStateOf(TimePickerState(preferencesState.defaultMatchTime))
    }
    var timeToAddPickerState by remember(isDrawerOpen) {
        mutableStateOf(TimePickerState(preferencesState.defaultTimeToAdd))
    }
    var overrunThresholdPickerState by remember(isDrawerOpen) {
        mutableStateOf(TimePickerState(preferencesState.overrunIndicatorThreshold))
    }

    /**
     * Which expandable section is currently open
     */
    var expandedItemIndex: Int? by remember(isDrawerOpen) { mutableStateOf(null) }

    /**
     * Incremented every time a new expandable section is created to force unique indexes
     */
    var expanderUniquenessIndex = 0

    val timePicker by lazy {
        TimePickerDialog(
                context,
                { _, hours, minutes ->
                    listener(
                            DrawerIntent.UpdateClubNightStartTime(
                                    UpdateCalendarInfo(
                                            hours = hours,
                                            minutes = minutes,
                                    )
                            )
                    )
                },
                preferencesState.clubNightStartTime.get(Calendar.HOUR_OF_DAY),
                preferencesState.clubNightStartTime.get(Calendar.MINUTE),
                true,
        )
    }
    val datePicker by lazy {
        DatePickerDialog(
                context,
                { _, year, month, day ->
                    listener(
                            DrawerIntent.UpdateClubNightStartTime(
                                    UpdateCalendarInfo(
                                            day = day,
                                            month = month,
                                            year = year,
                                    )
                            )
                    )
                },
                preferencesState.clubNightStartTime.get(Calendar.YEAR),
                preferencesState.clubNightStartTime.get(Calendar.MONTH),
                preferencesState.clubNightStartTime.get(Calendar.DATE),
        )
    }

    Column(
            modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 15.dp)
    ) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                        .padding(top = 10.dp)
                        .padding(horizontal = 25.dp)
        ) {
            Text(
                    text = "Cut-off:",
                    style = drawerTextStyle,
            )
            Text(
                    text = preferencesState.clubNightStartTime.asTimeString(),
                    style = drawerTextStyle.asClickableStyle(),
                    modifier = Modifier.clickable { timePicker.show() }
            )
            Text(
                    text = preferencesState.clubNightStartTime.asDateString(),
                    style = drawerTextStyle.asClickableStyle(),
                    modifier = Modifier.clickable { datePicker.show() }
            )
        }
        // TODO Make this collapsable?
        Text(
                text = "On the Match Up screen, an icon will appear next to people who the selected players have already played." +
                        " Matches played before this cut-off will not be counted." +
                        " It's good to set it to just before your club night or event starts!",
                style = Typography.body1,
                modifier = Modifier
                        .padding(
                                start = 35.dp,
                                end = 25.dp,
                                bottom = 10.dp,
                                top = 5.dp,
                        )
        )
        DrawerTextButton(text = "View archived players") { listener(DrawerIntent.Navigate(NavRoute.ARCHIVED_PLAYERS)) }
        Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                        .clickable { listener(DrawerIntent.TogglePrependCourt) }
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp, vertical = 5.dp)
        ) {
            Text(
                    text = "Prepend 'Court' to new courts",
                    style = drawerTextStyle,
                    modifier = Modifier.weight(1f)
            )
            Switch(
                    checked = preferencesState.prependCourt,
                    onCheckedChange = { listener(DrawerIntent.TogglePrependCourt) }
            )
        }

        DrawerDivider()
        Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(vertical = 10.dp)
        ) {
            DefaultTimePicker(
                    title = "Default match duration:",
                    errorSuffix = "Default match time is still " + preferencesState.defaultMatchTime.asTimeString(),
                    state = matchTimePickerState,
                    updateState = {
                        matchTimePickerState = it
                        if (matchTimePickerState.isValid) {
                            listener(DrawerIntent.UpdateDefaultMatchTime(it.totalSeconds))
                        }
                    }
            )
            DefaultTimePicker(
                    title = "Default additional time:",
                    errorSuffix = "Default add time is still " + preferencesState.defaultTimeToAdd.asTimeString(),
                    state = timeToAddPickerState,
                    updateState = {
                        timeToAddPickerState = it
                        if (timeToAddPickerState.isValid) {
                            listener(DrawerIntent.UpdateDefaultTimeToAdd(it.totalSeconds))
                        }
                    }
            )
            // TODO Add an amber threshold
            DefaultTimePicker(
                    title = "Overrun indicator threshold:",
                    errorSuffix = "Threshold is still " + preferencesState.overrunIndicatorThreshold.asTimeString(),
                    state = overrunThresholdPickerState,
                    updateState = {
                        overrunThresholdPickerState = it
                        if (overrunThresholdPickerState.isValid) {
                            listener(DrawerIntent.UpdateOverrunIndicatorThreshold(it.totalSeconds))
                        }
                    }
            )
        }

//        DrawerDivider()
//        // TODO_HACKY Create a build variant for this
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
                listener(DrawerIntent.UpdatePlayers(databaseState.players.map { it.copy(isPresent = false) }))
                listener(DrawerIntent.Navigate(NavRoute.ADD_PLAYER))
            }
            DrawerTextButton(text = "Clear matches and set cut off to now") {
                databaseState.matches.forEach {
                    when (it.state) {
                        is MatchState.NotStarted -> listener(DrawerIntent.DeleteMatch(it))
                        is MatchState.Completed -> {}
                        is MatchState.OnCourt -> listener(DrawerIntent.UpdateMatch(it.completeMatch(currentTime())))
                        is MatchState.Paused -> listener(DrawerIntent.UpdateMatch(it.completeMatch(currentTime())))
                    }
                }
                listener(DrawerIntent.UpdateClubNightStartTimeCalendar(currentTime()))
            }
            DrawerTextButton(text = "Mark all ongoing matches as complete") {
                databaseState.matches.forEach {
                    if (it.state is MatchState.OnCourt) {
                        listener(DrawerIntent.UpdateMatch(it.completeMatch(currentTime())))
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
            DrawerTextButton(text = "Delete all matches") { listener(DrawerIntent.DeleteAllMatches) }
            DrawerTextButton(text = "Clear settings") { listener(DrawerIntent.ClearDatastore) }
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
    Column(
            modifier = Modifier.padding(horizontal = 25.dp),
    ) {
        Text(
                text = title,
                style = drawerTextStyle,
        )
        Spacer(modifier = Modifier.height(10.dp))
        TimePicker(
                timePickerState = state,
                timeChangedListener = updateState,
                showError = false,
                modifier = Modifier.fillMaxWidth()
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

@Preview(showBackground = true)
@Composable
fun DrawerContent_Preview() {
    val currentTime = Calendar.getInstance()
    DrawerContent(
            currentTime = { currentTime },
            preferencesState = DatastoreState(),
            databaseState = ModelState(),
            isDrawerOpen = true,
            listener = {},
    )
}