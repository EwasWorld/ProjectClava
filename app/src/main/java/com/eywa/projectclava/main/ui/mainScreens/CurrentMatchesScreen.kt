package com.eywa.projectclava.main.ui.mainScreens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.R
import com.eywa.projectclava.main.common.asString
import com.eywa.projectclava.main.common.generateCourts
import com.eywa.projectclava.main.common.generateMatches
import com.eywa.projectclava.main.common.transformForSorting
import com.eywa.projectclava.main.model.Court
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.MatchState
import com.eywa.projectclava.main.ui.sharedUi.*
import com.eywa.projectclava.ui.theme.DividerThickness
import com.eywa.projectclava.ui.theme.Typography
import kotlinx.coroutines.delay
import java.util.*

@Composable
fun CurrentMatchesScreen(
        courts: Iterable<Court>?,
        matches: Iterable<Match>?,
        addTimeListener: (Match) -> Unit,
        setCompletedListener: (Match) -> Unit,
        changeCourtListener: (Match) -> Unit,
        pauseListener: (Match) -> Unit,
        unPauseListener: (Match) -> Unit,
) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = Calendar.getInstance()
        }
    }
    val selectedMatch: MutableState<Match?> =
            rememberSaveable(matches?.map { it.players }) { mutableStateOf(null) }

    // TODO Change court popup
    // TODO Add time popup

    CurrentMatchesScreen(
            currentTime = currentTime,
            courts = courts,
            matches = matches,
            selectedMatch = selectedMatch.value,
            selectedMatchListener = { selectedMatch.value = it },
            addTimeListener = addTimeListener,
            completeMatchListener = setCompletedListener,
            changeCourtListener = changeCourtListener,
            pauseListener = pauseListener,
            unPauseListener = unPauseListener,
    )
}

@Composable
fun CurrentMatchesScreen(
        currentTime: Calendar,
        courts: Iterable<Court>?,
        matches: Iterable<Match>?,
        selectedMatch: Match?,
        selectedMatchListener: (Match) -> Unit,
        addTimeListener: (Match) -> Unit,
        completeMatchListener: (Match) -> Unit,
        changeCourtListener: (Match) -> Unit,
        pauseListener: (Match) -> Unit,
        unPauseListener: (Match) -> Unit,
) {
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
            items(matches?.sortedBy { it.transformForSorting(currentTime).state } ?: listOf()) { match ->
                val isSelected = selectedMatch == match
                val court = courts?.find { it.currentMatch == match }

                SelectableListItem(
                        currentTime = currentTime,
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
                                    text = court?.let { court.name } ?: "No court",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = Typography.h4,
                                    modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                    text = match.state.getTimeLeft(currentTime).asString(),
                                    style = Typography.h4.copy(fontWeight = FontWeight.Normal),
                            )
                            if (match.isPaused) {
                                Icon(
                                        painter = painterResource(id = R.drawable.baseline_pause_24),
                                        contentDescription = "Match paused"
                                )
                            }
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

        Divider(thickness = DividerThickness)
        SelectedItemActions(
                text = courts?.find { it.currentMatch == selectedMatch }?.name ?: "No match selected",
                buttons = listOf(
                        SelectedItemAction(
                                icon = SelectedItemActionIcon.PainterIcon(R.drawable.baseline_more_time_24),
                                contentDescription = "Add time",
                                enabled = selectedMatch != null,
                                onClick = { selectedMatch?.let { addTimeListener(it) } }
                        ),
                        SelectedItemAction(
                                icon = SelectedItemActionIcon.PainterIcon(R.drawable.baseline_swap_horiz_24),
                                contentDescription = "Change court",
                                enabled = selectedMatch != null,
                                onClick = { selectedMatch?.let { changeCourtListener(it) } }
                        ),
                        if (selectedMatch?.isPaused == true) {
                            SelectedItemAction(
                                    icon = SelectedItemActionIcon.VectorIcon(Icons.Default.PlayArrow),
                                    contentDescription = "Resume match",
                                    enabled = true,
                                    onClick = { unPauseListener(selectedMatch) }
                            )
                        }
                        else {
                            SelectedItemAction(
                                    icon = SelectedItemActionIcon.PainterIcon(R.drawable.baseline_pause_24),
                                    contentDescription = "Pause match",
                                    enabled = selectedMatch != null,
                                    onClick = { selectedMatch?.let { pauseListener(it) } }
                            )
                        },
                        SelectedItemAction(
                                icon = SelectedItemActionIcon.VectorIcon(Icons.Default.Check),
                                contentDescription = "End match",
                                enabled = selectedMatch != null,
                                onClick = { selectedMatch?.let { completeMatchListener(it) } }
                        ),
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CurrentMatchesScreen_Preview(
        @PreviewParameter(CurrentMatchesScreenPreviewParamProvider::class) params: CurrentMatchesScreenPreviewParam
) {
    val courts = generateCourts(params.matchCount + (params.availableCourtsCount ?: 0)).toMutableList()
    val matches = generateMatches(params.matchCount)
    matches.forEach {
        courts.add(courts.removeFirst().copy(currentMatch = it))
    }
    val currentTime = Calendar.getInstance()
    CurrentMatchesScreen(
            currentTime = currentTime,
            courts = courts,
            matches = matches,
            selectedMatch = params.selectedIndex
                    ?.let { index ->
                        matches.map {
                            it.takeIf { it.state is MatchState.InProgress && it.state.isFinished(currentTime) }
                                    ?.copy(state = MatchState.NoTime)
                                    ?: it
                        }.sortedBy { it.state }[index]
                    },
            selectedMatchListener = {},
            addTimeListener = {},
            completeMatchListener = {},
            changeCourtListener = {},
            pauseListener = {},
            unPauseListener = {},
    )
}

data class CurrentMatchesScreenPreviewParam(
        val matchCount: Int = 5,
        val availableCourtsCount: Int? = 4,
        val selectedIndex: Int? = 3,
)

private class CurrentMatchesScreenPreviewParamProvider :
        CollectionPreviewParameterProvider<CurrentMatchesScreenPreviewParam>(
                listOf(
                        CurrentMatchesScreenPreviewParam(),
                        CurrentMatchesScreenPreviewParam(
                                matchCount = 20,
                                availableCourtsCount = 0,
                                selectedIndex = null
                        ),
                        CurrentMatchesScreenPreviewParam(selectedIndex = 1),
                )
        )