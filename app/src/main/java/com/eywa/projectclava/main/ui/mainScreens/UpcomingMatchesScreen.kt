package com.eywa.projectclava.main.ui.mainScreens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.eywa.projectclava.main.common.*
import com.eywa.projectclava.main.model.*
import com.eywa.projectclava.main.ui.sharedUi.*
import com.eywa.projectclava.ui.theme.ClavaColor
import com.eywa.projectclava.ui.theme.DividerThickness
import com.eywa.projectclava.ui.theme.Typography
import kotlinx.coroutines.delay
import java.util.*

@Composable
fun UpcomingMatchesScreen(
        courts: Iterable<Court>?,
        matches: Iterable<Match> = listOf(),
        openStartMatchDialogListener: (Match) -> Unit,
        startMatchDialogOpenFor: Match?,
        startMatchOkListener: (Match, Court) -> Unit,
        startMatchCancelListener: () -> Unit,
        removeMatchListener: (Match) -> Unit,
        selectedMatch: Match?,
        selectedMatchListener: (Match) -> Unit,
) {
    // TODO Set time popup
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
        startMatchOkListener: (Match, Court) -> Unit,
        startMatchCancelListener: () -> Unit,
) {
    if (startMatchDialogOpenFor == null || availableCourts?.takeIf { it.any() } == null) return
    var selectedCourt: Court by remember { mutableStateOf(availableCourts.minByOrNull { it.name }!!) }

    Dialog(onDismissRequest = startMatchCancelListener) {
        Surface(
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                        text = "Choose a court",
                        style = Typography.h4,
                )

                LazyColumn {
                    items(availableCourts.sortedBy { it.name }) { court ->
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { selectedCourt = court }
                        ) {
                            RadioButton(
                                    selected = selectedCourt == court,
                                    onClick = { selectedCourt = court }
                            )
                            Text(
                                    text = court.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                        modifier = Modifier.align(Alignment.End),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                            onClick = startMatchCancelListener,
                    ) {
                        Text("Cancel")
                    }
                    Button(
                            onClick = {
                                startMatchCancelListener()
                                startMatchOkListener(startMatchDialogOpenFor, selectedCourt)
                            },
                    ) {
                        Text("Start")
                    }
                }
            }
        }
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
            startMatchOkListener = { _, _ -> },
            startMatchCancelListener = {},
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