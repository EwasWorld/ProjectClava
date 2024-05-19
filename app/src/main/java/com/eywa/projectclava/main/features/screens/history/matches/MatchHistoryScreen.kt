package com.eywa.projectclava.main.features.screens.history.matches

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.R
import com.eywa.projectclava.main.common.GeneratableMatchState
import com.eywa.projectclava.main.common.IMissingContentNextStep
import com.eywa.projectclava.main.common.asDateString
import com.eywa.projectclava.main.common.generateMatches
import com.eywa.projectclava.main.common.stateSemanticsText
import com.eywa.projectclava.main.features.screens.history.HistoryTabSwitcherItem
import com.eywa.projectclava.main.features.screens.history.matches.MatchHistoryIntent.MatchClicked
import com.eywa.projectclava.main.features.screens.history.matches.MatchHistoryIntent.Navigate
import com.eywa.projectclava.main.features.screens.manage.SearchFab
import com.eywa.projectclava.main.features.ui.ClavaIconInfo
import com.eywa.projectclava.main.features.ui.ClavaScreen
import com.eywa.projectclava.main.features.ui.MatchTimeRemainingText
import com.eywa.projectclava.main.features.ui.SelectableListItem
import com.eywa.projectclava.main.features.ui.SelectedItemAction
import com.eywa.projectclava.main.features.ui.SelectedItemActions
import com.eywa.projectclava.main.features.ui.addTimeDialog.AddTimeDialog
import com.eywa.projectclava.main.features.ui.addTimeDialog.AddTimeDialogIntent
import com.eywa.projectclava.main.features.ui.confirmDialog.ConfirmDialog
import com.eywa.projectclava.main.features.ui.confirmDialog.ConfirmDialogIntent
import com.eywa.projectclava.main.features.ui.confirmDialog.ConfirmDialogType
import com.eywa.projectclava.main.features.ui.topTabSwitcher.TabSwitcher
import com.eywa.projectclava.main.mainActivity.MainNavRoute
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.ModelState
import com.eywa.projectclava.main.theme.Typography
import java.util.Calendar
import java.util.Locale


@Composable
fun MatchHistoryScreen(
    state: MatchHistoryState,
    databaseState: ModelState,
    defaultTimeToAdd: Int,
    overrunThreshold: Int,
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

    val finishedMatches = databaseState.matches
            .filter { match ->
                when {
                    !match.isFinished -> false
                    state.searchText.isNullOrBlank() -> true
                    else -> {
                        match.players.any()
                                && match.playerNameString().contains(state.searchText, ignoreCase = true)
                                && match.players.all { it.isPresent }
                    }
                }
            }
            .sortedByDescending { it.state }

    val noContentMessage = when (state.searchText) {
        null -> "No matches have been completed"
        else -> "No matches found for player '${state.searchText}' against present players"
    }
    val nextStep = when (state.searchText) {
        null -> databaseState.getMissingContent()
        else -> listOf(
                object : IMissingContentNextStep {
                    override val nextStepsText = "Present players show up as non-grey on the manage screen"
                    override val buttonRoute = MainNavRoute.ADD_PLAYER
                    override val buttonText = "Manage Players"
                }
        )
    }

    ClavaScreen(
            showNoContentPlaceholder = finishedMatches.isEmpty(),
            noContentText = noContentMessage,
            missingContentNextStep = nextStep,
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
            },
            fabs = { modifier ->
                SearchFab(
                        isExpanded = state.isSearchExpanded,
                        textPlaceholder = "John Doe",
                        typeContentDescription = "player",
                        toggleExpanded = { listener(MatchHistoryIntent.ToggleSearch) },
                        searchText = state.searchText ?: "",
                        onValueChangedListener = { listener(MatchHistoryIntent.SearchTextChanged(it)) },
                        modifier = modifier
                )
            },
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
                SelectableListItem(
                    overrunThreshold = overrunThreshold,
                    isSelected = isSelected,
                    contentDescription = match.playerNameString() + " " +
                            match.stateSemanticsText() + " " +
                            match.getTime().asDateString(),
                    onClick = { listener(MatchClicked(match)) },
                ) {
                    Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                    ) {
                        Text(
                                text = match.playerNameString(),
                                style = if (match.players.any()) Typography.h4 else Typography.body1,
                                modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        MatchTimeRemainingText(match = match)
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
        overrunThreshold = 10,
        databaseState = ModelState(
            matches = generateMatches(4, currentTime, GeneratableMatchState.COMPLETE),
        ),
        defaultTimeToAdd = 2 * 60,
        state = MatchHistoryState(),
        listener = {},
    )
}


@Preview(showBackground = true)
@Composable
fun Empty_MatchHistoryScreen_Preview() {
    val currentTime = Calendar.getInstance(Locale.getDefault())
    MatchHistoryScreen(
        overrunThreshold = 10,
        databaseState = ModelState(
            matches = generateMatches(4, currentTime, GeneratableMatchState.COMPLETE),
        ),
        defaultTimeToAdd = 2 * 60,
        state = MatchHistoryState(searchText = "xxx"),
        listener = {},
    )
}
