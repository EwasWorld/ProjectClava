package com.eywa.projectclava.main.ui.mainScreens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.R
import com.eywa.projectclava.main.common.GeneratableMatchState
import com.eywa.projectclava.main.common.asString
import com.eywa.projectclava.main.common.generateMatches
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.ui.sharedUi.*
import com.eywa.projectclava.ui.theme.Typography
import kotlinx.coroutines.delay
import java.util.*

@Composable
fun PreviousMatchesScreen(
        matches: Iterable<Match>?,
        addTimeListener: (Match, timeToAdd: Int) -> Unit,
        deleteMatchListener: (Match) -> Unit,
) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance(Locale.getDefault())) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = Calendar.getInstance(Locale.getDefault())
        }
    }
    var selectedMatch: Match? by remember(matches) { mutableStateOf(null) }
    var addTimeDialogOpenFor: Match? by remember(matches) { mutableStateOf(null) }

    PreviousMatchesScreen(
            currentTime = currentTime,
            matches = matches,
            selectedMatch = selectedMatch,
            selectedMatchListener = { newSelection ->
                selectedMatch = newSelection.takeIf { selectedMatch?.id != it.id }
            },
            addTimeListener = addTimeListener,
            addTimeDialogOpenFor = addTimeDialogOpenFor,
            openAddTimeDialogListener = { addTimeDialogOpenFor = it },
            closeAddTimeDialogListener = { addTimeDialogOpenFor = null },
            deleteMatchListener = deleteMatchListener,
    )
}

@Composable
fun PreviousMatchesScreen(
        currentTime: Calendar,
        matches: Iterable<Match>?,
        selectedMatch: Match?,
        selectedMatchListener: (Match) -> Unit,
        addTimeDialogOpenFor: Match?,
        openAddTimeDialogListener: (Match) -> Unit,
        closeAddTimeDialogListener: () -> Unit,
        addTimeListener: (Match, timeToAdd: Int) -> Unit,
        deleteMatchListener: (Match) -> Unit,
) {
    PreviousMatchesScreenDialogs(
            addTimeDialogOpenFor = addTimeDialogOpenFor,
            closeAddTimeDialogListener = closeAddTimeDialogListener,
            addTimeListener = addTimeListener,
    )

    val finishedMatches = matches?.filter { it.isFinished(currentTime) }
    ClavaScreen(
            noContentText = "No matches have been completed",
            hasContent = !finishedMatches.isNullOrEmpty(),
            footerContent = {
                PreviousMatchesScreenFooter(
                        selectedMatch = selectedMatch,
                        openAddTimeDialogListener = openAddTimeDialogListener,
                        deleteMatchListener = deleteMatchListener
                )
            }
    ) {
        items(finishedMatches?.sortedBy { it.state } ?: listOf()) { match ->
            val isSelected = selectedMatch?.id == match.id

            SelectableListItem(isSelected = isSelected) {
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
                                text = match.players.sortedBy { it.name }.joinToString(limit = 10) { it.name },
                                style = Typography.h4,
                                modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                                text = match.getFinishTime()?.asString()!!,
                                style = Typography.h4.copy(fontWeight = FontWeight.Normal),
                        )
                        if (match.isPaused) {
                            Icon(
                                    painter = painterResource(id = R.drawable.baseline_pause_24),
                                    contentDescription = "Match paused"
                            )
                        }
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
                            icon = SelectedItemActionIcon.PainterIcon(R.drawable.baseline_more_time_24),
                            contentDescription = "Add time",
                            enabled = selectedMatch != null,
                            onClick = { selectedMatch?.let { openAddTimeDialogListener(it) } }
                    ),
                    SelectedItemAction(
                            icon = SelectedItemActionIcon.VectorIcon(Icons.Default.Close),
                            contentDescription = "Delete match",
                            enabled = selectedMatch != null,
                            onClick = { selectedMatch?.let { deleteMatchListener(it) } }
                    ),
            ),
    )
}

@Composable
private fun PreviousMatchesScreenDialogs(
        addTimeDialogOpenFor: Match?,
        closeAddTimeDialogListener: () -> Unit,
        addTimeListener: (Match, timeToAdd: Int) -> Unit,
) {
    var timeToAdd: Int by remember { mutableStateOf(2 * 60) }

    ClavaDialog(
            isShown = addTimeDialogOpenFor != null,
            title = "Add time",
            okButtonText = "Add",
            onCancelListener = closeAddTimeDialogListener,
            onOkListener = { addTimeListener(addTimeDialogOpenFor!!, timeToAdd) }
    ) {
        TimePicker(
                totalSeconds = timeToAdd,
                timeChangedListener = { timeToAdd = it }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviousMatchesScreen_Preview() {
    val currentTime = Calendar.getInstance(Locale.getDefault())
    PreviousMatchesScreen(
            currentTime = currentTime,
            matches = generateMatches(4, currentTime, GeneratableMatchState.COMPLETE),
            selectedMatch = null,
            selectedMatchListener = { },
            addTimeListener = { _, _ -> },
            addTimeDialogOpenFor = null,
            openAddTimeDialogListener = { },
            closeAddTimeDialogListener = { },
            deleteMatchListener = { },
    )
}