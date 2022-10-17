package com.eywa.projectclava.main.ui.mainScreens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.R
import com.eywa.projectclava.main.DEFAULT_ADD_TIME
import com.eywa.projectclava.main.common.generateCourts
import com.eywa.projectclava.main.common.generateMatchTimeRemaining
import com.eywa.projectclava.main.common.generateMatches
import com.eywa.projectclava.main.model.*
import com.eywa.projectclava.main.ui.sharedUi.*
import com.eywa.projectclava.ui.theme.DividerThickness
import com.eywa.projectclava.ui.theme.Typography
import java.util.*

@Composable
fun CurrentMatchesScreen(
        courts: Iterable<Court>?,
        matches: Iterable<Match>?,
        matchIdToTimeRem: () -> Map<Int, TimeRemaining?>?,
        addTimeListener: (Match, timeToAdd: Int) -> Unit,
        setCompletedListener: (Match) -> Unit,
        changeCourtListener: (Match, Court) -> Unit,
        pauseListener: (Match) -> Unit,
        resumeListener: (Match, Court, resumeTime: Int) -> Unit,
) {
    var selectedMatch: Match? by remember(matches) { mutableStateOf(null) }
    var addTimeDialogOpenFor: Match? by remember(matches) { mutableStateOf(null) }
    var changeCourtDialogOpenFor: Match? by remember(matches) { mutableStateOf(null) }
    var resumeDialogOpenFor: Match? by remember(matches) { mutableStateOf(null) }

    CurrentMatchesScreen(
            courts = courts,
            matches = matches,
            matchIdToTimeRem = matchIdToTimeRem,
            selectedMatch = selectedMatch,
            selectedMatchListener = { newSelection ->
                selectedMatch = newSelection.takeIf { selectedMatch?.id != it.id }
            },
            completeMatchListener = setCompletedListener,
            pauseListener = pauseListener,
            addTimeListener = addTimeListener,
            changeCourtListener = changeCourtListener,
            addTimeDialogOpenFor = addTimeDialogOpenFor,
            openAddTimeDialogListener = { addTimeDialogOpenFor = it },
            closeAddTimeDialogListener = { addTimeDialogOpenFor = null },
            changeCourtDialogOpenFor = changeCourtDialogOpenFor,
            openChangeCourtDialogListener = { changeCourtDialogOpenFor = it },
            closeChangeCourtDialogListener = { changeCourtDialogOpenFor = null },
            resumeDialogOpenFor = resumeDialogOpenFor,
            openResumeDialogListener = { resumeDialogOpenFor = it },
            closeResumeDialogListener = { resumeDialogOpenFor = null },
            resumeListener = resumeListener,
    )
}

@Composable
fun CurrentMatchesScreen(
        courts: Iterable<Court>?,
        matches: Iterable<Match>?,
        matchIdToTimeRem: () -> Map<Int, TimeRemaining?>?,
        selectedMatch: Match?,
        selectedMatchListener: (Match) -> Unit,
        completeMatchListener: (Match) -> Unit,
        pauseListener: (Match) -> Unit,
        addTimeDialogOpenFor: Match?,
        openAddTimeDialogListener: (Match) -> Unit,
        closeAddTimeDialogListener: () -> Unit,
        addTimeListener: (Match, timeToAdd: Int) -> Unit,
        changeCourtDialogOpenFor: Match?,
        openChangeCourtDialogListener: (Match) -> Unit,
        closeChangeCourtDialogListener: () -> Unit,
        changeCourtListener: (Match, Court) -> Unit,
        resumeDialogOpenFor: Match?,
        openResumeDialogListener: (Match) -> Unit,
        closeResumeDialogListener: () -> Unit,
        resumeListener: (Match, Court, resumeTime: Int) -> Unit,
) {
    CurrentMatchesScreenDialogs(
            availableCourts = courts?.getAvailable(matches),
            addTimeDialogOpenFor = addTimeDialogOpenFor,
            closeAddTimeDialogListener = closeAddTimeDialogListener,
            changeCourtDialogOpenFor = changeCourtDialogOpenFor,
            addTimeListener = addTimeListener,
            closeChangeCourtDialogListener = closeChangeCourtDialogListener,
            changeCourtListener = changeCourtListener,
            resumeDialogOpenFor = resumeDialogOpenFor,
            closeResumeDialogListener = closeResumeDialogListener,
            resumeListener = resumeListener,
    )

    ClavaScreen(
            noContentText = "No matches being played",
            hasContent = !matches?.filter { it.isCurrent }.isNullOrEmpty(),
            headerContent = {
                AvailableCourtsHeader(courts = courts, matches = matches, timeRemaining = { matchIdToTimeRem() })
            },
            footerContent = {
                CurrentMatchesScreenFooter(
                        selectedMatch = selectedMatch,
                        completeMatchListener = completeMatchListener,
                        pauseListener = pauseListener,
                        openAddTimeDialogListener = openAddTimeDialogListener,
                        openChangeCourtDialogListener = openChangeCourtDialogListener,
                        openResumeDialogListener = openResumeDialogListener,
                )
            },
    ) {
        items(matches
                ?.filter { it.isCurrent }
                ?.sortedBy { it.state }
                ?: listOf()
        ) { match ->
            val isSelected = selectedMatch?.id == match.id
            val timeRemaining = { matchIdToTimeRem()?.get(match.id) }

            SelectableListItem(
                    timeRemaining = timeRemaining,
                    matchState = match.state,
                    isSelected = isSelected,
            ) {
                Column(
                        modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                        selected = isSelected,
                                        onClick = { selectedMatchListener(match) }
                                )
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
                        MatchStateIndicator(match = match, timeRemaining = timeRemaining)
                    }
                    Text(
                            text = match.players.sortedBy { it.name }.joinToString(limit = 10) { it.name },
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
        completeMatchListener: (Match) -> Unit,
        pauseListener: (Match) -> Unit,
        openAddTimeDialogListener: (Match) -> Unit,
        openChangeCourtDialogListener: (Match) -> Unit,
        openResumeDialogListener: (Match) -> Unit,
) {
    SelectedItemActions(
            text = when {
                selectedMatch == null -> "No match selected"
                selectedMatch.court != null -> selectedMatch.court!!.name
                selectedMatch.isPaused -> selectedMatch.players.joinToString { it.name }
                else -> "Unidentifiable match selected"
            },
            buttons = listOf(
                    SelectedItemAction(
                            icon = ClavaIconInfo.PainterIcon(
                                    drawable = R.drawable.baseline_more_time_24,
                                    contentDescription = "Add time",
                            ),
                            enabled = selectedMatch != null,
                            onClick = { selectedMatch?.let { openAddTimeDialogListener(it) } }
                    ),
                    SelectedItemAction(
                            icon = ClavaIconInfo.PainterIcon(
                                    drawable = R.drawable.baseline_swap_horiz_24,
                                    contentDescription = "Change court",
                            ),
                            enabled = selectedMatch != null,
                            onClick = { selectedMatch?.let { openChangeCourtDialogListener(it) } }
                    ),
                    if (selectedMatch?.isPaused == true) {
                        SelectedItemAction(
                                icon = ClavaIconInfo.VectorIcon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Resume match",
                                ),
                                enabled = true,
                                onClick = { openResumeDialogListener(selectedMatch) }
                        )
                    }
                    else {
                        SelectedItemAction(
                                icon = ClavaIconInfo.PainterIcon(
                                        drawable = R.drawable.baseline_pause_24,
                                        contentDescription = "Pause match",
                                ),
                                enabled = selectedMatch != null,
                                onClick = { selectedMatch?.let { pauseListener(it) } }
                        )
                    },
                    SelectedItemAction(
                            icon = ClavaIconInfo.VectorIcon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "End match",
                            ),
                            enabled = selectedMatch != null,
                            onClick = { selectedMatch?.let { completeMatchListener(it) } }
                    ),
            ),
    )
}

@Composable
private fun CurrentMatchesScreenDialogs(
        availableCourts: Iterable<Court>?,
        addTimeDialogOpenFor: Match?,
        closeAddTimeDialogListener: () -> Unit,
        changeCourtDialogOpenFor: Match?,
        addTimeListener: (Match, timeToAdd: Int) -> Unit,
        closeChangeCourtDialogListener: () -> Unit,
        changeCourtListener: (Match, Court) -> Unit,
        resumeDialogOpenFor: Match?,
        closeResumeDialogListener: () -> Unit,
        resumeListener: (Match, Court, resumeTime: Int) -> Unit,
) {
    // TODO Caching issue with these. Pause match, resume with random time - A, then pause, resume again will time A
    var timeToAdd by remember(addTimeDialogOpenFor) { mutableStateOf(TimePickerState(DEFAULT_ADD_TIME)) }
    val remainingTime = addTimeDialogOpenFor?.state?.let { it as MatchState.Paused }
            ?.remainingTimeSeconds?.toInt()
            ?.takeIf { it > 0 }
    var resumeTime by remember(addTimeDialogOpenFor) {
        mutableStateOf(TimePickerState(remainingTime ?: DEFAULT_ADD_TIME))
    }
    var selectedCourt by remember(addTimeDialogOpenFor) { mutableStateOf(availableCourts?.minByOrNull { it.name }) }

    ClavaDialog(
            isShown = addTimeDialogOpenFor != null,
            title = "Add time",
            okButtonText = "Add",
            onCancelListener = closeAddTimeDialogListener,
            okButtonEnabled = timeToAdd.isValid,
            onOkListener = { addTimeListener(addTimeDialogOpenFor!!, timeToAdd.totalSeconds) }
    ) {
        TimePicker(
                timePickerState = timeToAdd,
                timeChangedListener = { timeToAdd = it },
                modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
        )
    }
    ClavaDialog(
            isShown = addTimeDialogOpenFor == null && changeCourtDialogOpenFor != null && selectedCourt != null,
            title = "Change court",
            okButtonText = "Change",
            onCancelListener = closeChangeCourtDialogListener,
            onOkListener = { changeCourtListener(changeCourtDialogOpenFor!!, selectedCourt!!) }
    ) {
        SelectCourtRadioButtons(
                availableCourts = availableCourts,
                selectedCourt = selectedCourt,
                onCourtSelected = { selectedCourt = it }
        )
    }
    ClavaDialog(
            isShown = addTimeDialogOpenFor == null && changeCourtDialogOpenFor == null
                    && resumeDialogOpenFor != null && selectedCourt != null,
            title = "Resume match",
            okButtonText = "Resume",
            onCancelListener = closeResumeDialogListener,
            okButtonEnabled = resumeTime.isValid,
            onOkListener = { resumeListener(resumeDialogOpenFor!!, selectedCourt!!, resumeTime.totalSeconds) }
    ) {
        TimePicker(
                timePickerState = resumeTime,
                timeChangedListener = { resumeTime = it },
                modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
        )
        Divider(thickness = DividerThickness)
        SelectCourtRadioButtons(
                availableCourts = availableCourts,
                selectedCourt = selectedCourt,
                onCourtSelected = { selectedCourt = it }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CurrentMatchesScreen_Preview(
        @PreviewParameter(CurrentMatchesScreenPreviewParamProvider::class) params: CurrentMatchesScreenPreviewParam
) {
    val currentTime = Calendar.getInstance(Locale.getDefault())
    val matches = generateMatches(params.matchCount, currentTime)
    CurrentMatchesScreen(
            courts = generateCourts(params.matchCount + params.availableCourtsCount),
            matches = matches,
            matchIdToTimeRem = { generateMatchTimeRemaining(matches, currentTime) },
            selectedMatch = params.selectedIndex?.let { index ->
                matches.filter { it.isCurrent }.sortedBy { it.state }[index]
            },
            selectedMatchListener = {},
            completeMatchListener = {},
            pauseListener = {},
            addTimeDialogOpenFor = null,
            openAddTimeDialogListener = {},
            closeAddTimeDialogListener = {},
            addTimeListener = { _, _ -> },
            changeCourtDialogOpenFor = null,
            openChangeCourtDialogListener = {},
            closeChangeCourtDialogListener = {},
            changeCourtListener = { _, _ -> },
            resumeDialogOpenFor = null,
            openResumeDialogListener = {},
            closeResumeDialogListener = {},
            resumeListener = { _, _, _ -> },
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