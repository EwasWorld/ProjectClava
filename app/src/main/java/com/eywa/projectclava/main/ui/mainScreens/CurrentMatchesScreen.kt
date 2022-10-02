package com.eywa.projectclava.main.ui.mainScreens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.R
import com.eywa.projectclava.main.common.*
import com.eywa.projectclava.main.model.Court
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.MatchState
import com.eywa.projectclava.ui.theme.ClavaColor
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
        Text(
                text = "Available courts: " +
                        (courts.filterAvailable(currentTime)?.joinToString { it.number.toString() }
                                ?: "none"),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = Typography.h4,
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
        )
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
                val timeLeft = match.state.getTimeLeft(currentTime)
                val isSelected = selectedMatch == match
                val court = courts?.find { it.currentMatch == match }

                Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = match.state.asColor(currentTime) ?: ClavaColor.ItemBackground,
                        border = BorderStroke(
                                width = if (isSelected) 4.dp else 1.dp,
                                color = if (isSelected) ClavaColor.SelectedBorder else ClavaColor.GeneralBorder
                        )
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
                                    text = timeLeft.asString(),
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
        Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Text(
                    text = courts
                            ?.find { it.currentMatch == selectedMatch }?.name
                            ?: "No match selected",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = Typography.h4,
                    modifier = Modifier.weight(1f)
            )
            IconButton(
                    enabled = selectedMatch != null,
                    onClick = { selectedMatch?.let { addTimeListener(it) } }
            ) {
                Icon(
                        painter = painterResource(id = R.drawable.baseline_more_time_24),
                        contentDescription = "Add time"
                )
            }
            IconButton(
                    enabled = selectedMatch != null,
                    onClick = { selectedMatch?.let { changeCourtListener(it) } }
            ) {
                Icon(
                        painter = painterResource(id = R.drawable.baseline_swap_horiz_24),
                        contentDescription = "Change court"
                )
            }
            if (selectedMatch?.isPaused == true) {
                IconButton(
                        enabled = true,
                        onClick = { unPauseListener(selectedMatch) }
                ) {
                    Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Resume match"
                    )
                }
            }
            else {
                IconButton(
                        enabled = selectedMatch != null,
                        onClick = { selectedMatch?.let { pauseListener(it) } }
                ) {
                    Icon(
                            painter = painterResource(id = R.drawable.baseline_pause_24),
                            contentDescription = "Pause match"
                    )
                }
            }
            IconButton(
                    enabled = selectedMatch != null,
                    onClick = { selectedMatch?.let { completeMatchListener(it) } }
            ) {
                Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "End match"
                )
            }
        }
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