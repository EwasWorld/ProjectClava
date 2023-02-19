package com.eywa.projectclava.main.features.screens.ongoing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.R
import com.eywa.projectclava.main.common.MissingContentNextStep
import com.eywa.projectclava.main.common.generateCourts
import com.eywa.projectclava.main.common.generateMatches
import com.eywa.projectclava.main.features.screens.ongoing.OngoingMatchesIntent.*
import com.eywa.projectclava.main.features.ui.*
import com.eywa.projectclava.main.features.ui.addTimeDialog.AddTimeDialog
import com.eywa.projectclava.main.features.ui.addTimeDialog.AddTimeDialogIntent
import com.eywa.projectclava.main.features.ui.timePicker.TimePicker
import com.eywa.projectclava.main.features.ui.timePicker.TimePickerState
import com.eywa.projectclava.main.model.*
import com.eywa.projectclava.main.theme.DividerThickness
import com.eywa.projectclava.main.theme.Typography
import java.util.*

@Composable
fun OngoingMatchesScreen(
    state: OngoingMatchesState,
    databaseState: ModelState,
    getTimeRemaining: Match.() -> TimeRemaining?,
    defaultTimeToAddSeconds: Int,
    overrunThreshold: Int,
    listener: (OngoingMatchesIntent) -> Unit,
) {
    val availableCourts = databaseState.courts.getAvailable(databaseState.matches)
    val selectedMatch = state.selectedMatchId?.let { selected ->
        databaseState.matches.find { it.id == selected }
    }
    val filteredMatches = databaseState.matches.filter { it.isCurrent }

    CurrentMatchesScreenDialogs(
            availableCourts = availableCourts,
            state = state,
            listener = listener,
            defaultTimeToAddSeconds = defaultTimeToAddSeconds,
    )

    ClavaScreen(
            showNoContentPlaceholder = filteredMatches.isEmpty(),
            noContentText = "No matches being played",
            missingContentNextStep = databaseState.getMissingContent(MissingContentNextStep.START_A_MATCH),
            navigateListener = { listener(Navigate(it)) },
            headerContent = {
                AvailableCourtsHeader(
                        courts = databaseState.courts,
                        matches = databaseState.matches,
                        getTimeRemaining = getTimeRemaining
                )
            },
            footerContent = {
                CurrentMatchesScreenFooter(
                        selectedMatch = selectedMatch,
                        defaultTimeToAddSeconds = defaultTimeToAddSeconds,
                        listener = listener,
                        firstAvailableCourt = { availableCourts?.minByOrNull { it.name } }
                )
            },
    ) {
        items(filteredMatches.sortedBy { it.state }) { match ->
            val isSelected = state.selectedMatchId == match.id

            SelectableListItem(
                overrunThreshold = overrunThreshold,
                getTimeRemaining = getTimeRemaining,
                match = match,
                isSelected = isSelected,
                contentDescription = "", // TODO_CURRENT
                onClick = { listener(MatchClicked(match)) },
            ) {
                Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                ) {
                    Row {
                        Text(
                                text = match.court?.name ?: if (match.isPaused) "Paused" else "No court",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = Typography.h4,
                                modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        MatchTimeRemainingText(match = match, getTimeRemaining = getTimeRemaining)
                    }
                    Text(
                            text = match.playerNameString(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentMatchesScreenFooter(
        selectedMatch: Match?,
        defaultTimeToAddSeconds: Int,
        listener: (OngoingMatchesIntent) -> Unit,
        firstAvailableCourt: () -> Court?,
) {
    SelectedItemActions(
            text = when {
                selectedMatch == null -> "No match selected"
                selectedMatch.court != null -> selectedMatch.court!!.name
                selectedMatch.isPaused -> selectedMatch.playerNameString()
                else -> "Unidentifiable match selected"
            },
            buttons = listOf(
                    SelectedItemAction(
                            icon = ClavaIconInfo.PainterIcon(
                                    drawable = R.drawable.baseline_more_time_24,
                                    contentDescription = "Add time",
                            ),
                            enabled = selectedMatch != null,
                            onClick = {
                                listener(
                                        AddTimeDialogIntent.AddTimeOpened
                                                .toOngoingMatchesIntent(defaultTimeToAddSeconds)
                                )
                            }
                    ),
                    SelectedItemAction(
                            icon = ClavaIconInfo.PainterIcon(
                                    drawable = R.drawable.baseline_swap_horiz_24,
                                    contentDescription = "Change court",
                            ),
                            enabled = selectedMatch != null && selectedMatch.isOnCourt,
                            onClick = { listener(OpenChangeCourtDialog(firstAvailableCourt())) }
                    ),
                    if (selectedMatch?.isPaused == true) {
                        SelectedItemAction(
                                icon = ClavaIconInfo.VectorIcon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Resume match",
                                ),
                                enabled = true,
                                onClick = {
                                    listener(
                                            OpenResumeDialog(
                                                    selectedMatch,
                                                    firstAvailableCourt(),
                                                    defaultTimeToAddSeconds,
                                            )
                                    )
                                }
                        )
                    }
                    else {
                        SelectedItemAction(
                                icon = ClavaIconInfo.PainterIcon(
                                        drawable = R.drawable.baseline_pause_24,
                                        contentDescription = "Pause match",
                                ),
                                enabled = selectedMatch != null,
                                onClick = { listener(PauseMatch) }
                        )
                    },
                    SelectedItemAction(
                            icon = ClavaIconInfo.VectorIcon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "End match",
                            ),
                            enabled = selectedMatch != null,
                            onClick = { listener(CompleteMatch) }
                    ),
            ),
    )
}

@Composable
private fun CurrentMatchesScreenDialogs(
        availableCourts: Iterable<Court>?,
        state: OngoingMatchesState,
        listener: (OngoingMatchesIntent) -> Unit,
        defaultTimeToAddSeconds: Int,
) {
    AddTimeDialog(state = state, listener = { listener(it.toOngoingMatchesIntent(defaultTimeToAddSeconds)) })

    ClavaDialog(
            isShown = state.openDialog == OngoingMatchesDialog.CHANGE_COURT,
            title = "Change court",
            okButtonText = "Change",
            okButtonEnabled = state.selectedCourt != null,
            onCancelListener = { listener(CloseCurrentDialog) },
            onOkListener = { listener(ChangeCourtDialogSubmit) }
    ) {
        SelectCourtRadioButtons(
                availableCourts = availableCourts,
                selectedCourt = state.selectedCourt,
                onCourtSelected = { listener(CourtSelected(it)) }
        )
    }

    ClavaDialog(
            isShown = state.openDialog == OngoingMatchesDialog.RESUME,
            title = "Resume match",
            okButtonText = "Resume",
            onCancelListener = { listener(CloseCurrentDialog) },
            okButtonEnabled = state.resumeTime?.isValid == true && state.selectedCourt != null,
            onOkListener = { listener(ResumeDialogSubmit) }
    ) {
        TimePicker(
                timePickerState = state.resumeTime ?: TimePickerState(0),
                timeChangedListener = { listener(ResumeTimeChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
        )
        Divider(thickness = DividerThickness)
        SelectCourtRadioButtons(
                availableCourts = availableCourts,
                selectedCourt = state.selectedCourt,
                onCourtSelected = { listener(CourtSelected(it)) }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OngoingMatchesScreen_Preview(
        @PreviewParameter(CurrentMatchesScreenPreviewParamProvider::class) params: CurrentMatchesScreenPreviewParam
) {
    val currentTime = Calendar.getInstance(Locale.getDefault())
    val matches = generateMatches(params.matchCount, currentTime)
    OngoingMatchesScreen(
        overrunThreshold = 10,
        databaseState = ModelState(
            courts = generateCourts(params.matchCount + params.availableCourtsCount),
            matches = matches,
        ),
        getTimeRemaining = { state.getTimeLeft(currentTime) },
        defaultTimeToAddSeconds = 2 * 60,
        state = OngoingMatchesState(
            selectedMatchId = params.selectedIndex?.let { index ->
                matches.filter { it.isCurrent }.sortedBy { it.state }[index]
            }?.id,
            ),
            listener = {},
    )
}

data class CurrentMatchesScreenPreviewParam(
        val matchCount: Int = 5,
        val availableCourtsCount: Int = 4,
        val selectedIndex: Int? = 1,
)

private class CurrentMatchesScreenPreviewParamProvider :
        CollectionPreviewParameterProvider<CurrentMatchesScreenPreviewParam>(
                listOf(
                        CurrentMatchesScreenPreviewParam(),
                        CurrentMatchesScreenPreviewParam(
                                selectedIndex = 0
                        ),
                        CurrentMatchesScreenPreviewParam(
                                matchCount = 20,
                                availableCourtsCount = 0,
                                selectedIndex = null
                        ),
                )
        )
