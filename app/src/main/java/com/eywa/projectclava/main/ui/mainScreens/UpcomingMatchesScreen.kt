package com.eywa.projectclava.main.ui.mainScreens

import androidx.compose.foundation.BorderStroke
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
import com.eywa.projectclava.main.model.Court
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.MatchState
import com.eywa.projectclava.main.model.getPlayerStates
import com.eywa.projectclava.main.ui.sharedUi.AvailableCourtsHeader
import com.eywa.projectclava.main.ui.sharedUi.SelectedItemAction
import com.eywa.projectclava.main.ui.sharedUi.SelectedItemActionIcon
import com.eywa.projectclava.main.ui.sharedUi.SelectedItemActions
import com.eywa.projectclava.ui.theme.ClavaColor
import com.eywa.projectclava.ui.theme.DividerThickness
import com.eywa.projectclava.ui.theme.Typography
import kotlinx.coroutines.delay
import java.util.*

@Composable
fun UpcomingMatchesScreen(
        courts: Iterable<Court>?,
        upcomingMatches: List<Match> = listOf(),
        openStartMatchDialogListener: (Match) -> Unit,
        startMatchDialogOpenFor: Match?,
        startMatchOkListener: (Match, Court) -> Unit,
        startMatchCancelListener: () -> Unit,
        removeMatchListener: (Match) -> Unit,
        selectedMatch: Match?,
        selectedMatchListener: (Match) -> Unit,
) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = Calendar.getInstance()
        }
    }

    val availableCourts = courts.filterAvailable(currentTime)
    val playerMatchStates = courts?.getPlayerStates() ?: mapOf()

    StartMatchDialog(
            availableCourts = availableCourts,
            startMatchDialogOpenFor = startMatchDialogOpenFor,
            startMatchOkListener = startMatchOkListener,
            startMatchCancelListener = startMatchCancelListener,
    )

    Column {
        AvailableCourtsHeader(currentTime = currentTime, courts = courts)
        Divider(thickness = DividerThickness)

        LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 10.dp),
                modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 10.dp)
        ) {
            items(upcomingMatches) { match ->
                val isSelected = selectedMatch == match

                Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = match.state.asColor(currentTime) ?: ClavaColor.ItemBackground,
                        border = BorderStroke(
                                width = if (isSelected) 4.dp else 1.dp,
                                color = if (isSelected) ClavaColor.SelectedBorder else ClavaColor.GeneralBorder
                        )
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
                                        // Show players who are already on court first
                                        .sortedByDescending {
                                            it.second?.transformForSorting(currentTime) ?: MatchState.NoTime
                                        }
                        ) { (player, matchState) ->
                            Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    color = matchState?.asColor(currentTime, ClavaColor.DisabledItemBackground)
                                            ?: ClavaColor.ItemBackground,
                                    border = BorderStroke(1.dp, ClavaColor.GeneralBorder)
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
        // TODO text to say that a player is busy
        SelectedItemActions(
                text = selectedMatch?.players?.joinToString { it.name } ?: "No match selected",
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
                                        && selectedMatch.players.all {
                                    playerMatchStates[it.name]?.isFinished(currentTime) != false
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
    var courts: MutableList<Court>? = null
    if (params.matchCount + (params.availableCourtsCount ?: 0) > 0) {
        courts = generateCourts(params.matchCount + (params.availableCourtsCount ?: 0)).toMutableList()
    }
    if (params.matchCount > 0) {
        val matches = generateMatches(params.matchCount)
        matches.forEach {
            courts!!.add(courts.removeFirst().copy(currentMatch = it))
        }
    }
    val upcoming = generatePlayers(params.totalRows * params.playersPerRow)
            .chunked(params.playersPerRow)
            .map { Match(it) }

    UpcomingMatchesScreen(
            courts = courts,
            upcomingMatches = upcoming,
            removeMatchListener = {},
            selectedMatch = params.selectedIndex?.let { upcoming[it] },
            selectedMatchListener = {},
            openStartMatchDialogListener = {},
            startMatchDialogOpenFor = if (params.startMatchDialogOpen) upcoming[0] else null,
            startMatchOkListener = { _, _ -> },
            startMatchCancelListener = {},
    )
}

data class UpcomingMatchesScreenPreviewParam(
        val totalRows: Int = 10,
        val playersPerRow: Int = 2,
        val matchCount: Int = 5,
        val availableCourtsCount: Int? = 4,
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