package com.eywa.projectclava.main.features.screens.history.matches

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.R
import com.eywa.projectclava.main.common.GeneratableMatchState
import com.eywa.projectclava.main.common.MissingContentNextStep
import com.eywa.projectclava.main.common.asDateString
import com.eywa.projectclava.main.common.generateMatches
import com.eywa.projectclava.main.features.screens.history.HistoryTabSwitcherItem
import com.eywa.projectclava.main.features.screens.history.matches.MatchHistoryIntent.MatchClicked
import com.eywa.projectclava.main.features.screens.history.matches.MatchHistoryIntent.Navigate
import com.eywa.projectclava.main.features.ui.*
import com.eywa.projectclava.main.features.ui.addTimeDialog.AddTimeDialog
import com.eywa.projectclava.main.features.ui.addTimeDialog.AddTimeDialogIntent
import com.eywa.projectclava.main.features.ui.confirmDialog.ConfirmDialog
import com.eywa.projectclava.main.features.ui.confirmDialog.ConfirmDialogIntent
import com.eywa.projectclava.main.features.ui.confirmDialog.ConfirmDialogType
import com.eywa.projectclava.main.features.ui.topTabSwitcher.TabSwitcher
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.ModelState
import com.eywa.projectclava.main.theme.Typography
import java.util.*


@Composable
fun MatchHistoryScreen(
        state: MatchHistoryState,
        databaseState: ModelState,
        defaultTimeToAdd: Int,
        listener: (MatchHistoryIntent) -> Unit,
) {
    AddTimeDialog(
            state = state,
            listener = { listener(it.toMatchHistoryIntent(defaultTimeToAdd)) },
    )
    ConfirmDialog(
            state = state.deleteMatchDialogState,
            type = ConfirmDialogType.DELETE,
            listener = { listener(it.toMatchHistoryIntent()) },
    )

    val finishedMatches = databaseState.matches.filter { it.isFinished }.sortedByDescending { it.state }
    ClavaScreen(
            noContentText = "No matches have been completed",
            missingContentNextStep = databaseState.getMissingContent()
                    .takeIf { states -> states.any { it == MissingContentNextStep.COMPLETE_A_MATCH } },
            navigateListener = { listener(Navigate(it)) },
            footerContent = {
                PreviousMatchesScreenFooter(
                        selectedMatch = state.selectedMatchId?.let { selected ->
                            databaseState.matches.find { it.id == selected }
                        },
                        openAddTimeDialogListener = {
                            listener(AddTimeDialogIntent.AddTimeOpened.toMatchHistoryIntent(defaultTimeToAdd))
                        },
                        deleteMatchListener = { listener(ConfirmDialogIntent.Open(it).toMatchHistoryIntent()) }
                )
            },
            headerContent = {
                TabSwitcher(
                        items = HistoryTabSwitcherItem.values().toList(),
                        selectedItem = HistoryTabSwitcherItem.MATCHES,
                        navigateListener = { listener(Navigate(it)) },
                )
            }
    ) {
        items(finishedMatches.withIndex().toList()) { (index, match) ->
            val isSelected = state.selectedMatchId == match.id

            val date = match.getTime().asDateString()
            val matchesPreviousDate = index
                    .takeIf { it > 0 }
                    ?.let { finishedMatches[it - 1].getTime().asDateString() == date }
                    ?: false

            Column {
                if (!matchesPreviousDate) {
                    Text(
                            text = match.getTime().asDateString(),
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
                                            onClick = { listener(MatchClicked(match)) }
                                    )
                                    .padding(10.dp)
                    ) {
                        Text(
                                text = match.playerNameString(),
                                style = if (match.players.any()) Typography.h4 else Typography.body1,
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
            text = selectedMatch?.playerNameString() ?: "No match selected",
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

@Preview(showBackground = true)
@Composable
fun MatchHistoryScreen_Preview() {
    val currentTime = Calendar.getInstance(Locale.getDefault())
    MatchHistoryScreen(
            databaseState = ModelState(
                    matches = generateMatches(4, currentTime, GeneratableMatchState.COMPLETE),
            ),
            defaultTimeToAdd = 2 * 60,
            state = MatchHistoryState(),
            listener = {},
    )
}