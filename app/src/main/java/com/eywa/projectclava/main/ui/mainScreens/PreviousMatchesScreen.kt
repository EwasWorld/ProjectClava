package com.eywa.projectclava.main.ui.mainScreens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.R
import com.eywa.projectclava.main.common.GeneratableMatchState
import com.eywa.projectclava.main.common.asDateString
import com.eywa.projectclava.main.common.generateMatches
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.MissingContentNextStep
import com.eywa.projectclava.main.ui.sharedUi.*
import com.eywa.projectclava.ui.theme.Typography
import java.util.*

enum class HistoryTabSwitcherItem(
        override val label: String,
        override val destination: NavRoute
) : TabSwitcherItem {
    MATCHES("Matches", NavRoute.PREVIOUS_MATCHES),
    SUMMARY("Summary", NavRoute.HISTORY_SUMMARY),
}

@Composable
fun PreviousMatchesScreen(
        matches: Iterable<Match>?,
        defaultTimeToAddSeconds: Int,
        addTimeListener: (Match, timeToAdd: Int) -> Unit,
        deleteMatchListener: (Match) -> Unit,
        onTabSelectedListener: (HistoryTabSwitcherItem) -> Unit,
        missingContentNextStep: Iterable<MissingContentNextStep>?,
        navigateListener: (NavRoute) -> Unit,
) {
    var selectedMatch: Match? by remember(matches) { mutableStateOf(null) }
    var addTimeDialogOpenFor: Match? by remember(matches) { mutableStateOf(null) }

    PreviousMatchesScreen(
            matches = matches,
            defaultTimeToAddSeconds = defaultTimeToAddSeconds,
            selectedMatch = selectedMatch,
            selectedMatchListener = { newSelection ->
                selectedMatch = newSelection.takeIf { selectedMatch?.id != it.id }
            },
            addTimeListener = addTimeListener,
            addTimeDialogOpenFor = addTimeDialogOpenFor,
            openAddTimeDialogListener = { addTimeDialogOpenFor = it },
            closeAddTimeDialogListener = { addTimeDialogOpenFor = null },
            deleteMatchListener = deleteMatchListener,
            onTabSelectedListener = onTabSelectedListener,
            missingContentNextStep = missingContentNextStep,
            navigateListener = navigateListener,
    )
}

@Composable
fun PreviousMatchesScreen(
        matches: Iterable<Match>?,
        defaultTimeToAddSeconds: Int,
        selectedMatch: Match?,
        selectedMatchListener: (Match) -> Unit,
        addTimeDialogOpenFor: Match?,
        openAddTimeDialogListener: (Match) -> Unit,
        closeAddTimeDialogListener: () -> Unit,
        addTimeListener: (Match, timeToAdd: Int) -> Unit,
        deleteMatchListener: (Match) -> Unit,
        onTabSelectedListener: (HistoryTabSwitcherItem) -> Unit,
        missingContentNextStep: Iterable<MissingContentNextStep>?,
        navigateListener: (NavRoute) -> Unit,
) {
    PreviousMatchesScreenDialogs(
            defaultTimeToAddSeconds = defaultTimeToAddSeconds,
            addTimeDialogOpenFor = addTimeDialogOpenFor,
            closeAddTimeDialogListener = closeAddTimeDialogListener,
            addTimeListener = addTimeListener,
    )

    val finishedMatches = matches?.filter { it.isFinished }?.sortedByDescending { it.state }
    ClavaScreen(
            noContentText = "No matches have been completed",
            missingContentNextStep = missingContentNextStep
                    ?.takeIf { states -> states.any { it == MissingContentNextStep.COMPLETE_A_MATCH } },
            navigateListener = navigateListener,
            footerContent = {
                PreviousMatchesScreenFooter(
                        selectedMatch = selectedMatch,
                        openAddTimeDialogListener = openAddTimeDialogListener,
                        deleteMatchListener = deleteMatchListener
                )
            },
            headerContent = {
                TabSwitcher(
                        items = HistoryTabSwitcherItem.values().toList(),
                        selectedItem = HistoryTabSwitcherItem.MATCHES,
                        navigateListener = { navigateListener(it) },
                )
            }
    ) {
        items((finishedMatches ?: listOf()).withIndex().toList()) { (index, match) ->
            val isSelected = selectedMatch?.id == match.id

            val date = match.getFinishTime()!!.asDateString()
            val matchesPreviousDate = index
                    .takeIf { it > 0 }
                    ?.let { finishedMatches!![it - 1].getFinishTime()!!.asDateString() == date }
                    ?: false

            Column {
                if (!matchesPreviousDate) {
                    Text(
                            text = match.getFinishTime()!!.asDateString(),
                            style = Typography.h3,
                            modifier = Modifier
                                    .padding(
                                            bottom = 10.dp,
                                            top = if (index == 0) 0.dp else 10.dp
                                    )
                                    .padding(horizontal = 5.dp)
                    )
                }
                SelectableListItem(isSelected = isSelected) {
                    Row(
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                            selected = isSelected,
                                            onClick = { selectedMatchListener(match) }
                                    )
                                    .padding(10.dp)
                    ) {
                        Text(
                                text = match.players.sortedBy { it.name }.joinToString(limit = 10) { it.name },
                                style = Typography.h4,
                                modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        MatchStateIndicator(match = match)
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviousMatchesScreenFooter(
        selectedMatch: Match?,
        openAddTimeDialogListener: (Match) -> Unit,
        deleteMatchListener: (Match) -> Unit,
) {
    SelectedItemActions(
            text = selectedMatch?.players?.joinToString { it.name } ?: "No match selected",
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
                            icon = ClavaIconInfo.VectorIcon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete match",
                            ),
                            enabled = selectedMatch != null,
                            onClick = { selectedMatch?.let { deleteMatchListener(it) } }
                    ),
            ),
    )
}

@Composable
private fun PreviousMatchesScreenDialogs(
        defaultTimeToAddSeconds: Int,
        addTimeDialogOpenFor: Match?,
        closeAddTimeDialogListener: () -> Unit,
        addTimeListener: (Match, timeToAdd: Int) -> Unit,
) {
    var timeToAdd by remember(addTimeDialogOpenFor) { mutableStateOf(TimePickerState(defaultTimeToAddSeconds)) }

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
                timeChangedListener = { timeToAdd = it }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviousMatchesScreen_Preview() {
    val currentTime = Calendar.getInstance(Locale.getDefault())
    PreviousMatchesScreen(
            matches = generateMatches(4, currentTime, GeneratableMatchState.COMPLETE),
            defaultTimeToAddSeconds = 2 * 60,
            selectedMatch = null,
            selectedMatchListener = { },
            addTimeListener = { _, _ -> },
            addTimeDialogOpenFor = null,
            openAddTimeDialogListener = { },
            closeAddTimeDialogListener = { },
            deleteMatchListener = { },
            onTabSelectedListener = {},
            missingContentNextStep = null,
            navigateListener = {},
    )
}