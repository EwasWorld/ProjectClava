package com.eywa.projectclava.main.ui.mainScreens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.main.common.*
import com.eywa.projectclava.main.model.*
import com.eywa.projectclava.main.ui.sharedUi.*
import com.eywa.projectclava.ui.theme.ClavaColor
import com.eywa.projectclava.ui.theme.DividerThickness
import kotlinx.coroutines.delay
import java.util.*

@Composable
fun UpcomingMatchesScreen(
        courts: Iterable<Court>?,
        matches: Iterable<Match> = listOf(),
        openStartMatchDialogListener: (Match) -> Unit,
        startMatchDialogOpenFor: Match?,
        startMatchOkListener: (Match, Court, totalTimeSeconds: Int, useAsDefaultTime: Boolean) -> Unit,
        startMatchCancelListener: () -> Unit,
        removeMatchListener: (Match) -> Unit,
        selectedMatch: Match?,
        defaultTimeSeconds: Int,
        selectedMatchListener: (Match) -> Unit,
) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = Calendar.getInstance()
        }
    }

    val availableCourts = courts?.minus(matches.getCourtsInUse(currentTime).toSet())
    val playerMatchStates = matches.getPlayerStates()
    val sortedMatches = matches.filter { it.state is MatchState.NotStarted }.sortedBy { it.state }

    StartMatchDialog(
            availableCourts = availableCourts,
            startMatchDialogOpenFor = startMatchDialogOpenFor,
            startMatchOkListener = startMatchOkListener,
            startMatchCancelListener = startMatchCancelListener,
            defaultTimeSeconds = defaultTimeSeconds,
    )

    Column {
        AvailableCourtsHeader(currentTime = currentTime, courts = courts, matches = matches)
        Divider(thickness = DividerThickness)

        LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 10.dp),
                modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 10.dp)
        ) {
            items(sortedMatches) { match ->
                val matchingPlayersInEarlierUpcoming = sortedMatches
                        .takeWhile { it != match }
                        .flatMap { it.players }
                        .distinct()
                        .filter { match.players.find { player -> it.name == player.name } != null }
                        .takeIf { it.isNotEmpty() }

                SelectableListItem(
                        currentTime = currentTime,
                        isSelected = selectedMatch == match,
                        generalInProgressColor = ClavaColor.DisabledItemBackground,
                        matchState = match.players
                                .mapNotNull { playerMatchStates[it.name]?.state }
                                .maxByOrNull { it }
                                ?.takeIf { !it.isFinished(currentTime) }
                                ?.let { matchState ->
                                    if (matchState !is MatchState.NotStarted) return@let matchState
                                    matchingPlayersInEarlierUpcoming?.let { matchState }
                                }
                ) {
                    LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            contentPadding = PaddingValues(10.dp),
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(onClick = { selectedMatchListener(match) })
                    ) {
                        items(
                                match.players
                                        .map { it to playerMatchStates[it.name] }
                                        .partition {
                                            it.second?.isCurrent(currentTime) != true || it.second?.state?.isFinished(
                                                    currentTime
                                            ) != true
                                        }
                                        .let { (noMatch, match) ->
                                            match.sortedBy { it.second?.state } + noMatch.sortedBy { it.first.name }
                                        }
                        ) { (player, match) ->
                            SelectableListItem(
                                    currentTime = currentTime,
                                    matchState = match?.state?.let { matchState ->
                                        if (matchState !is MatchState.NotStarted) return@let matchState
                                        matchingPlayersInEarlierUpcoming
                                                ?.takeIf { it.find { p -> p.name == player.name } != null }
                                                ?.let { matchState }
                                    },
                                    generalInProgressColor = ClavaColor.DisabledItemBackground,
                            ) {
                                Text(
                                        text = player.name,
                                        modifier = Modifier.padding(vertical = 3.dp, horizontal = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Divider(thickness = DividerThickness)
        val selectedMatchState = selectedMatch?.players
                ?.associateWith { playerMatchStates[it.name]?.state }
                ?.filter { it.value?.isFinished(currentTime) == false }
                ?.maxByOrNull { it.value!! }
        SelectedItemActions(
                text = selectedMatch?.players?.joinToString { it.name } ?: "No match selected",
                extraText = when (selectedMatchState?.value) {
                    null,
                    is MatchState.NotStarted,
                    is MatchState.Paused -> null
                    is MatchState.InProgressOrComplete -> {
                        val playerName = selectedMatchState.key.name
                        val matchState = (selectedMatchState.value!! as MatchState.InProgressOrComplete)
                        "$playerName is on ${matchState.court.name}\nTime remaining: ${
                            matchState.getTimeLeft(
                                    currentTime
                            ).asString()
                        }"
                    }
                },
                color = selectedMatchState?.value?.takeIf { it !is MatchState.NotStarted }?.asColor(
                        currentTime = currentTime,
                        generalInProgressColor = ClavaColor.DisabledItemBackground
                ),
                buttons = listOf(
                        SelectedItemAction(
                                icon = SelectedItemActionIcon.VectorIcon(Icons.Default.Close),
                                contentDescription = "Remove match",
                                enabled = selectedMatch != null,
                                onClick = { selectedMatch?.let { removeMatchListener(it) } },
                        ),
                        SelectedItemAction(
                                icon = SelectedItemActionIcon.VectorIcon(Icons.Default.PlayArrow),
                                contentDescription = "Start match",
                                enabled = selectedMatch != null
                                        && !availableCourts.isNullOrEmpty()
                                        && selectedMatch.players
                                        .all {
                                            playerMatchStates[it.name]?.isInProgress(currentTime)?.not() ?: true
                                        },
                                onClick = { selectedMatch?.let { openStartMatchDialogListener(it) } },
                        ),
                ),
        )
    }
}

@Composable
fun StartMatchDialog(
        availableCourts: Iterable<Court>?,
        startMatchDialogOpenFor: Match?,
        startMatchOkListener: (Match, Court, totalTimeSeconds: Int, useAsDefaultTime: Boolean) -> Unit,
        startMatchCancelListener: () -> Unit,
        defaultTimeSeconds: Int,
) {
    var selectedCourt by remember { mutableStateOf(availableCourts?.minByOrNull { it.name }) }
    var timeSeconds by remember { mutableStateOf(defaultTimeSeconds) }
    var defaultTimeChecked by remember { mutableStateOf(true) }

    ClavaDialog(
            isShown = startMatchDialogOpenFor != null,
            title = "Choose a duration and court",
            okButtonText = "Start",
            okButtonEnabled = selectedCourt != null,
            onCancelListener = startMatchCancelListener,
            onOkListener = {
                startMatchOkListener(startMatchDialogOpenFor!!, selectedCourt!!, timeSeconds, defaultTimeChecked)
            },
    ) {
        Column {
            TimePicker(
                    totalSeconds = timeSeconds,
                    timeChangedListener = { timeSeconds = it },
            )
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { defaultTimeChecked = !defaultTimeChecked }
            ) {
                Checkbox(checked = defaultTimeChecked, onCheckedChange = { defaultTimeChecked = !defaultTimeChecked })
                Text(
                        text = "Use this as the default time",
                        modifier = Modifier.padding(end = 20.dp)
                )
            }
        }
        Divider(thickness = DividerThickness)
        SelectCourtRadioButtons(
                availableCourts = availableCourts,
                selectedCourt = selectedCourt,
                onCourtSelected = { selectedCourt = it },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UpcomingMatchesScreen_Preview(
        @PreviewParameter(UpcomingMatchesScreenPreviewParamProvider::class) params: UpcomingMatchesScreenPreviewParam
) {
    val currentTime = Calendar.getInstance()
    val matches = generateMatches(5, currentTime) + generateMatches(4, currentTime, GeneratableMatchState.NOT_STARTED)

    UpcomingMatchesScreen(
            courts = generateCourts(4),
            matches = matches,
            removeMatchListener = {},
            selectedMatch = params.selectedIndex?.let {
                matches.filter { match -> match.state is MatchState.NotStarted }[it]
            },
            selectedMatchListener = {},
            openStartMatchDialogListener = {},
            startMatchDialogOpenFor = if (params.startMatchDialogOpen) matches[0] else null,
            startMatchOkListener = { _, _, _, _ -> },
            startMatchCancelListener = {},
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