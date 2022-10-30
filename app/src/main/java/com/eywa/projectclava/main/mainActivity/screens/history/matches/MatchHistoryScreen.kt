package com.eywa.projectclava.main.mainActivity.screens.history.matches

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
import com.eywa.projectclava.main.common.asDateString
import com.eywa.projectclava.main.common.generateMatches
import com.eywa.projectclava.main.mainActivity.CoreIntent
import com.eywa.projectclava.main.mainActivity.DatabaseIntent
import com.eywa.projectclava.main.mainActivity.MainEffect
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.mainActivity.screens.ScreenIntent
import com.eywa.projectclava.main.mainActivity.screens.ScreenState
import com.eywa.projectclava.main.mainActivity.screens.history.HistoryTabSwitcherItem
import com.eywa.projectclava.main.mainActivity.screens.history.matches.MatchHistoryIntent.*
import com.eywa.projectclava.main.model.DatabaseState
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.MissingContentNextStep
import com.eywa.projectclava.main.ui.sharedUi.*
import com.eywa.projectclava.ui.theme.Typography
import java.util.*

data class MatchHistoryState(
        override val selectedMatchId: Int? = null,
        override val addTimeDialogIsOpen: Boolean = false,
        override val timeToAdd: TimePickerState? = null,
) : ScreenState, AddTimeDialogState {
    override fun addTimeCopy(
            addTimeDialogIsOpen: Boolean,
            timeToAdd: TimePickerState?,
    ) = copy(
            addTimeDialogIsOpen = addTimeDialogIsOpen,
            timeToAdd = timeToAdd,
    )
}

sealed class MatchHistoryIntent : ScreenIntent<MatchHistoryState> {
    override val screen: NavRoute = NavRoute.MATCH_HISTORY

    data class MatchClicked(val match: Match) : MatchHistoryIntent()
    data class MatchDeleted(val match: Match) : MatchHistoryIntent()

    data class AddTimeIntent(
            val value: AddTimeDialogIntent,
            val defaultTimeToAdd: Int,
    ) : MatchHistoryIntent()

    data class Navigate(val destination: NavRoute) : MatchHistoryIntent()

    override fun handle(
            currentState: MatchHistoryState,
            handle: (CoreIntent) -> Unit,
            newStateListener: (MatchHistoryState) -> Unit
    ) {
        when (this) {
            is AddTimeIntent -> value.handle(defaultTimeToAdd, currentState, newStateListener, handle)
            is MatchClicked -> newStateListener(
                    currentState.copy(selectedMatchId = match.id.takeIf { currentState.selectedMatchId != match.id })
            )
            is MatchDeleted -> handle(DatabaseIntent.DeleteMatch(match))
            is Navigate -> handle(MainEffect.Navigate(destination))
        }
    }
}

fun AddTimeDialogIntent.toMatchHistoryIntent(defaultTimeToAdd: Int) =
        AddTimeIntent(this, defaultTimeToAdd)


@Composable
fun MatchHistoryScreen(
        state: MatchHistoryState,
        databaseState: DatabaseState,
        defaultTimeToAdd: Int,
        listener: (MatchHistoryIntent) -> Unit,
) {
    AddTimeDialog(
            state = state,
            listener = { listener(it.toMatchHistoryIntent(defaultTimeToAdd)) },
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
                        deleteMatchListener = { listener(MatchDeleted(it)) }
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

            val date = match.getFinishTime()!!.asDateString()
            val matchesPreviousDate = index
                    .takeIf { it > 0 }
                    ?.let { finishedMatches[it - 1].getFinishTime()!!.asDateString() == date }
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
                                            onClick = { listener(MatchClicked(match)) }
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

@Preview(showBackground = true)
@Composable
fun MatchHistoryScreen_Preview() {
    val currentTime = Calendar.getInstance(Locale.getDefault())
    MatchHistoryScreen(
            databaseState = DatabaseState(
                    matches = generateMatches(4, currentTime, GeneratableMatchState.COMPLETE),
            ),
            defaultTimeToAdd = 2 * 60,
            state = MatchHistoryState(),
            listener = {},
    )
}