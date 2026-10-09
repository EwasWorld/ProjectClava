package com.eywa.projectclava.main.features.drawer

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.main.common.UpdateCalendarInfo
import com.eywa.projectclava.main.common.asDateString
import com.eywa.projectclava.main.common.asLocalDateTime
import com.eywa.projectclava.main.common.asTimeString
import com.eywa.projectclava.main.datastore.DatastoreState
import com.eywa.projectclava.main.features.ui.timePicker.TimePicker
import com.eywa.projectclava.main.features.ui.timePicker.TimePickerState
import com.eywa.projectclava.main.mainActivity.MainNavRoute
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.model.MatchState
import com.eywa.projectclava.main.model.ModelState
import com.eywa.projectclava.main.theme.ClavaTheme
import com.eywa.projectclava.main.theme.DividerThickness
import com.eywa.projectclava.main.theme.Typography
import com.eywa.projectclava.main.theme.asClickableStyle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant
import androidx.compose.material3.Text as M3Text
import androidx.compose.material3.TimePicker as M3TimePicker

private const val PRIVACY_POLICY_URL = "https://github.com/EwasWorld/ProjectClava/blob/master/PRIVACY_POLICY.md"
private val drawerTextStyle = Typography.h4

// TODO Add a help page
@Composable
fun DrawerContent(
        currentTime: () -> Instant,
        preferencesState: DatastoreState,
        databaseState: ModelState,
        isDrawerOpen: Boolean,
        extraNavLocations: List<NavRoute>?,
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

    var isTimePickerOpen by remember(isDrawerOpen) { mutableStateOf(false) }
    var isDatePickerOpen by remember(isDrawerOpen) { mutableStateOf(false) }

    if (isTimePickerOpen) {
        ClubNightStartTimePicker(
                initial = preferencesState.clubNightStartTime.asLocalDateTime(),
                onDismiss = { isTimePickerOpen = false },
                onConfirm = { hours, minutes ->
                    isTimePickerOpen = false
                    listener(
                            DrawerIntent.UpdateClubNightStartTime(
                                    UpdateCalendarInfo(
                                            hours = hours,
                                            minutes = minutes,
                                    )
                            )
                    )
                },
        )
    }
    if (isDatePickerOpen) {
        ClubNightStartDatePicker(
                initial = preferencesState.clubNightStartTime.asLocalDateTime().date,
                onDismiss = { isDatePickerOpen = false },
                onConfirm = { date ->
                    isDatePickerOpen = false
                    listener(
                            DrawerIntent.UpdateClubNightStartTime(
                                    UpdateCalendarInfo(
                                            day = date.day,
                                            month = date.month.number,
                                            year = date.year,
                                    )
                            )
                    )
                },
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
                    modifier = Modifier.clickable { isTimePickerOpen = true }
            )
            Text(
                    text = preferencesState.clubNightStartTime.asDateString(),
                    style = drawerTextStyle.asClickableStyle(),
                    modifier = Modifier.clickable { isDatePickerOpen = true }
            )
            Text(
                    text = "Reset",
                    style = drawerTextStyle.copy(fontSize = drawerTextStyle.fontSize.times(0.8)).asClickableStyle(),
                    modifier = Modifier
                            .clickable { listener(DrawerIntent.ResetClubNightStartTime) }
                            .padding(start = 10.dp)
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
        DrawerTextButton(text = "View archived players") { listener(DrawerIntent.Navigate(MainNavRoute.ARCHIVED_PLAYERS)) }
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
        Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                        .clickable { listener(DrawerIntent.ToggleMuteSounds) }
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp, vertical = 5.dp)
        ) {
        Text(
                text = "Play match ended sounds",
                style = drawerTextStyle,
                modifier = Modifier.weight(1f)
        )
        Switch(
                checked = !preferencesState.muteSounds,
                onCheckedChange = { listener(DrawerIntent.ToggleMuteSounds) }
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


        if (!extraNavLocations.isNullOrEmpty()) {
            DrawerDivider()
            extraNavLocations.forEach { route ->
                DrawerTextButton(text = "Go to ${route.route}") { listener(DrawerIntent.Navigate(route)) }
            }
        }

        DrawerDivider()
        ExpandableSection(
                index = expanderUniquenessIndex++,
                label = "Irreversible actions",
                expandedItemIndex = expandedItemIndex,
                updateExpandedItemIndex = { expandedItemIndex = it },
        ) {
            DrawerTextButton(text = "Mark all players as not present") {
                listener(DrawerIntent.UpdatePlayers(databaseState.players.map { it.copy(isPresent = false) }))
                listener(DrawerIntent.Navigate(MainNavRoute.ADD_PLAYER))
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
                listener(DrawerIntent.UpdateClubNightStartTimeInstant(currentTime()))
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
            DrawerTextButton(text = "Delete everything") { listener(DrawerIntent.DeleteAllData) }
        }

        DrawerDivider()
        DrawerTextButton(text = "Open privacy policy in a browser") {
            val webpage = Uri.parse(PRIVACY_POLICY_URL)
            val intent = Intent(Intent.ACTION_VIEW, webpage)

            // TODO Swap try/catch with `if (intent.resolveActivity(packageManager) != null)`
            try {
                context.startActivity(intent)
            }
            catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "No web browser found", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClubNightStartTimePicker(
        initial: LocalDateTime,
        onDismiss: () -> Unit,
        onConfirm: (hours: Int, minutes: Int) -> Unit,
) {
    val state = rememberTimePickerState(
            initialHour = initial.hour,
            initialMinute = initial.minute,
            is24Hour = true,
    )
    ClavaTheme {
        AlertDialog(
                onDismissRequest = onDismiss,
                confirmButton = {
                    TextButton(onClick = { onConfirm(state.hour, state.minute) }) { M3Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) { M3Text("Cancel") }
                },
                text = { M3TimePicker(state = state) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClubNightStartDatePicker(
        initial: LocalDate,
        onDismiss: () -> Unit,
        onConfirm: (LocalDate) -> Unit,
) {
    // Material3 DatePicker works in UTC millis at the start of the day
    val state = rememberDatePickerState(
            initialSelectedDateMillis = initial.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
    )
    ClavaTheme {
        DatePickerDialog(
                onDismissRequest = onDismiss,
                confirmButton = {
                    TextButton(
                            onClick = {
                                val millis = state.selectedDateMillis
                                if (millis == null) {
                                    onDismiss()
                                }
                                else {
                                    onConfirm(Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date)
                                }
                            }
                    ) { M3Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) { M3Text("Cancel") }
                },
        ) {
            DatePicker(state = state)
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
    val currentTime = Clock.System.now()
    DrawerContent(
            currentTime = { currentTime },
            preferencesState = DatastoreState(),
            databaseState = ModelState(),
            isDrawerOpen = true,
            listener = {},
            extraNavLocations = listOf(MainNavRoute.ADD_COURT),
    )
}
