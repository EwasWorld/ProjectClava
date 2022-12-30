package com.eywa.projectclava.main.features.screens.queue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.main.common.*
import com.eywa.projectclava.main.features.screens.queue.MatchQueueIntent.*
import com.eywa.projectclava.main.features.ui.*
import com.eywa.projectclava.main.features.ui.confirmDialog.ConfirmDialog
import com.eywa.projectclava.main.features.ui.confirmDialog.ConfirmDialogIntent
import com.eywa.projectclava.main.features.ui.confirmDialog.ConfirmDialogType
import com.eywa.projectclava.main.features.ui.timePicker.TimePicker
import com.eywa.projectclava.main.features.ui.timePicker.TimePickerState
import com.eywa.projectclava.main.model.*
import com.eywa.projectclava.main.theme.ClavaColor
import com.eywa.projectclava.main.theme.DividerThickness
import com.eywa.projectclava.main.theme.Typography
import java.util.*


@Composable
fun MatchQueueScreen(
        state: MatchQueueState,
        databaseState: ModelState,
        getTimeRemaining: Match.() -> TimeRemaining?,
        defaultTimeSeconds: Int,
        listener: (MatchQueueIntent) -> Unit,
) {
    val availableCourts = databaseState.courts.getAvailable(databaseState.matches)
    val playerMatchStates = databaseState.matches.getPlayerStatus()
    val sortedMatches = databaseState.matches.filter { it.state is MatchState.NotStarted }.sortedBy { it.state }

    StartMatchDialog(
            availableCourts = availableCourts,
            state = state,
            listener = listener,
    )
    ConfirmDialog(
            state.deleteMatchDialogState,
            type = ConfirmDialogType.DELETE,
            listener = { listener(it.toMatchQueueIntent()) },
    )

    ClavaScreen(
            showNoContentPlaceholder = sortedMatches.isEmpty() || databaseState.courts.none { it.canBeUsed },
            noContentText = if (sortedMatches.isEmpty()) "No matches queued" else "No courts to put the matches on!",
            missingContentNextStep = databaseState.getMissingContent(),
            navigateListener = { listener(Navigate(it)) },
            headerContent = {
                AvailableCourtsHeader(
                        courts = databaseState.courts,
                        matches = databaseState.matches,
                        getTimeRemaining = getTimeRemaining
                )
            },
            footerContent = {
                UpcomingMatchesScreenFooter(
                        getTimeRemaining = getTimeRemaining,
                        openStartMatchDialogListener = {
                            listener(
                                    OpenStartMatchDialog(
                                            initialSelectedCourt = availableCourts?.minByOrNull { it.name },
                                            defaultTimeToAddSeconds = defaultTimeSeconds,
                                    )
                            )
                        },
                        deleteMatchListener = {
                            val match = databaseState.matches.find { it.id == state.selectedMatchId }!!
                            listener(ConfirmDialogIntent.Open(match).toMatchQueueIntent())
                        },
                        selectedMatch = state.selectedMatchId?.let { selectedId ->
                            databaseState.matches.find { it.id == selectedId }
                        },
                        playerMatchStates = playerMatchStates,
                        hasAvailableCourts = !availableCourts.isNullOrEmpty(),
                )
            },
    ) {
        items(sortedMatches) { match ->
            val matchingPlayersInEarlierUpcoming = sortedMatches
                    .takeWhile { it != match }
                    .flatMap { it.players }
                    .distinct()
                    .filter { match.players.find { player -> it.name == player.name } != null }
                    .takeIf { it.isNotEmpty() }

            SelectableListItem(
                    isSelected = state.selectedMatchId == match.id,
                    enabled = match.players.all { it.isPresent },
                    match = match.players
                            .mapNotNull { playerMatchStates[it.name] }
                            .maxByOrNull { it.state }
                            ?.let { foundMatch ->
                                if (foundMatch.state !is MatchState.NotStarted) return@let foundMatch
                                // If anyone is in an earlier upcoming mach, use the NotStarted colour
                                matchingPlayersInEarlierUpcoming?.let { foundMatch }
                            },
                    getTimeRemaining = getTimeRemaining,
                    onClick = { listener(MatchClicked(match)) },
                    contentDescription = "", // TODO_CURRENT
            ) {
                LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        contentPadding = PaddingValues(10.dp),
                        modifier = Modifier
                                .fillMaxWidth()
                ) {
                    if (match.players.none()) {
                        item {
                            Text(
                                    text = "No player data",
                                    style = Typography.body1,
                            )
                        }
                    }

                    items(
                            match.players
                                    .map { it to playerMatchStates[it.name] }
                                    .partition {
                                        it.second?.isCurrent != true || it.second?.isFinished != true
                                    }
                                    .let { (noMatch, match) ->
                                        match.sortedBy { it.second?.state } + noMatch.sortedBy { it.first.name }
                                    }
                    ) { (player, playerMatch) ->
                        SelectableListItem(
                                enabled = player.enabled,
                                match = playerMatch?.let { match ->
                                    if (!match.isNotStarted) return@let match
                                    matchingPlayersInEarlierUpcoming
                                            ?.takeIf { it.find { p -> p.name == player.name } != null }
                                            ?.let { match }
                                },
                                getTimeRemaining = getTimeRemaining,
                                contentDescription = "",
                        ) {
                            Text(
                                    text = player.name,
                                    style = Typography.body1,
                                    modifier = Modifier.padding(vertical = 3.dp, horizontal = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingMatchesScreenFooter(
        getTimeRemaining: Match.() -> TimeRemaining?,
        openStartMatchDialogListener: () -> Unit,
        deleteMatchListener: () -> Unit,
        selectedMatch: Match?,
        playerMatchStates: Map<String, Match?>,
        hasAvailableCourts: Boolean,
) {
    // TODO Add a button to swap one player in this match with someone in another match?
    val extraText: String?
    val color: Color?
    selectedMatch?.players?.find { !it.isPresent }.let { disabledPlayer ->
        if (disabledPlayer != null) {
            extraText = "${disabledPlayer.name} is not present"
            color = ClavaColor.DisabledItemBackground
        }
        else {
            // The latest-finishing match and the player in selectedMatch who is also in latestMatch
            val (latestPlayer, latestMatch) = selectedMatch?.players
                    ?.associateWith { playerMatchStates[it.name] }
                    ?.filter { it.value?.isFinished == false }
                    ?.maxByOrNull { it.value!!.state }
                    ?: mapOf(null to null).asIterable().first()

            color = latestMatch?.takeIf { !it.isNotStarted }?.asColor(getTimeRemaining)
            extraText = when (latestMatch?.state) {
                null -> null
                is MatchState.NotStarted,
                is MatchState.Completed -> "No courts available".takeIf { !hasAvailableCourts }
                is MatchState.Paused -> "${latestPlayer?.name}'s match is paused"
                is MatchState.OnCourt -> {
                    "${latestPlayer?.name} is on ${latestMatch.court!!.name}" +
                            "\nTime remaining: " + latestMatch.getTimeRemaining().asTimeString()
                }
            }
        }
    }

    SelectedItemActions(
            text = selectedMatch?.playerNameString() ?: "No match selected",
            extraText = extraText,
            color = color,
            buttons = listOf(
                    SelectedItemAction(
                            icon = ClavaIconInfo.VectorIcon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete match",
                            ),
                            enabled = selectedMatch != null,
                            onClick = { selectedMatch?.let { deleteMatchListener() } },
                    ),
                    SelectedItemAction(
                            icon = ClavaIconInfo.VectorIcon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Start match",
                            ),
                            enabled = selectedMatch != null
                                    && hasAvailableCourts
                                    && selectedMatch.players
                                    .all {
                                        (playerMatchStates[it.name]?.isOnCourt?.not() ?: true)
                                                && it.isPresent
                                    },
                            onClick = { selectedMatch?.let { openStartMatchDialogListener() } },
                    ),
            ),
    )
}

@Composable
private fun StartMatchDialog(
        state: MatchQueueState,
        availableCourts: Iterable<Court>?,
        listener: (MatchQueueIntent) -> Unit,
) {
    ClavaDialog(
            isShown = state.startMatchDialogIsOpen,
            title = "Choose a duration and court",
            okButtonText = "Start",
            okButtonEnabled = state.selectedCourt != null && state.startMatchTimePickerState?.isValid == true,
            onCancelListener = { listener(CloseStartMatchDialog) },
            onOkListener = { listener(StartMatchSubmitted) },
    ) {
        TimePicker(
                timePickerState = state.startMatchTimePickerState ?: TimePickerState(0),
                timeChangedListener = { listener(UpdateTimePicker(it)) },
                modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
        )
        Divider(thickness = DividerThickness)
        SelectCourtRadioButtons(
                availableCourts = availableCourts,
                selectedCourt = state.selectedCourt,
                onCourtSelected = { listener(UpdateSelectedCourt(it)) },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MatchQueueScreen_Preview(
        @PreviewParameter(UpcomingMatchesScreenPreviewParamProvider::class) params: UpcomingMatchesScreenPreviewParam
) {
    val currentTime = Calendar.getInstance(Locale.getDefault())
    val matches = generateMatches(5, currentTime) + generateMatches(4, currentTime, GeneratableMatchState.NOT_STARTED)

    MatchQueueScreen(
            databaseState = ModelState(
                    courts = generateCourts(4),
                    matches = matches,
            ),
            getTimeRemaining = { state.getTimeLeft(currentTime) },
            state = MatchQueueState(
                    selectedMatchId = params.selectedIndex?.let {
                        matches.filter { match -> match.state is MatchState.NotStarted }[it]
                    }?.id,
            ),
            listener = {},
            defaultTimeSeconds = 15 * 60,
    )
}

data class UpcomingMatchesScreenPreviewParam(
        val totalRows: Int = 10,
        val playersPerRow: Int = 2,
        val matchCount: Int = 5,
        val availableCourtsCount: Int = 4,
        val selectedIndex: Int? = 3,
        val startMatchDialogOpen: Boolean = false,
)

private class UpcomingMatchesScreenPreviewParamProvider :
        CollectionPreviewParameterProvider<UpcomingMatchesScreenPreviewParam>(
                listOf(
                        UpcomingMatchesScreenPreviewParam(),
                        UpcomingMatchesScreenPreviewParam(
                                matchCount = 1,
                                availableCourtsCount = 0,
                                selectedIndex = null
                        ),
                        UpcomingMatchesScreenPreviewParam(
                                matchCount = 0,
                                availableCourtsCount = 0,
                                selectedIndex = 2
                        ),
                        UpcomingMatchesScreenPreviewParam(selectedIndex = 1),
                        UpcomingMatchesScreenPreviewParam(
                                playersPerRow = 6,
                                selectedIndex = 1
                        ),
                        UpcomingMatchesScreenPreviewParam(startMatchDialogOpen = true),
                )
        )