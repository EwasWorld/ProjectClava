package com.eywa.projectclava.main.ui.mainScreens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.R
import com.eywa.projectclava.main.common.*
import com.eywa.projectclava.main.model.*
import com.eywa.projectclava.main.ui.sharedUi.*
import com.eywa.projectclava.ui.theme.ClavaColor
import com.eywa.projectclava.ui.theme.DividerThickness
import com.eywa.projectclava.ui.theme.Typography
import kotlinx.coroutines.delay
import java.util.*

@Composable
fun CreateMatchScreen(
        players: Iterable<Player>,
        matches: Iterable<Match> = listOf(),
        courts: Iterable<Court>? = listOf(),
        createMatchListener: (Iterable<Player>) -> Unit,
) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance(Locale.getDefault())) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = Calendar.getInstance(Locale.getDefault())
        }
    }

    var selectedPlayers: Set<Player> by remember { mutableStateOf(setOf()) }

    CreateMatchScreen(
            currentTime = currentTime,
            players = players,
            matches = matches,
            courts = courts,
            selectedPlayers = selectedPlayers,
            createMatchListener = {
                createMatchListener(selectedPlayers)
                selectedPlayers = setOf()
            },
            removeAllFromMatchListener = { selectedPlayers = setOf() },
            playerClickedListener = {
                selectedPlayers = if (selectedPlayers.contains(it)) {
                    selectedPlayers.minus(it)
                }
                else {
                    selectedPlayers.plus(it)
                }
            }
    )
}

/**
 * @param selectedPlayers the people selected to form the next match
 */
@Composable
fun CreateMatchScreen(
        currentTime: Calendar,
        players: Iterable<Player>,
        matches: Iterable<Match> = listOf(),
        courts: Iterable<Court>? = listOf(),
        selectedPlayers: Iterable<Player>,
        createMatchListener: () -> Unit,
        removeAllFromMatchListener: () -> Unit,
        playerClickedListener: (Player) -> Unit,
) {

    val playerMatchStates = matches.getPlayerStates()
    val previouslyPlayed = matches
            .filter { match ->
                if (match.state is MatchState.NotStarted) return@filter false
                val selected = selectedPlayers.map { it.name }
                match.players.any { selected.contains(it.name) }
            }
            .flatMap { it.players.map { player -> player.name } }

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
            items(
                    players.associateWith { playerMatchStates[it.name] }.entries.sortedBy { it.value?.lastPlayedTime }
            ) { (player, match) ->
                SelectableListItem(
                        currentTime = currentTime,
                        matchState = match?.state,
                        generalInProgressColor = ClavaColor.DisabledItemBackground,
                        isSelected = selectedPlayers.map { it.name }.contains(player.name),
                ) {
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                    .clickable { playerClickedListener(player) }
                                    .padding(10.dp)
                    ) {
                        Text(
                                text = player.name,
                        )
                        if (
                            !selectedPlayers.map { it.name }.contains(player.name)
                            && previouslyPlayed.contains(player.name)
                        ) {
                            Icon(
                                    imageVector = Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Played tonight",
                                    modifier = Modifier.padding(horizontal = 5.dp)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                                text = match?.state?.getTimeLeft(currentTime)?.asString() ?: "Not played"
                        )
                        if (match?.isPaused == true) {
                            Icon(
                                    painter = painterResource(id = R.drawable.baseline_pause_24),
                                    contentDescription = "Match paused"
                            )
                        }
                    }
                }
            }
        }

        Divider(thickness = DividerThickness)
        SelectedItemActions(
                buttons = listOf(
                        SelectedItemAction(
                                icon = SelectedItemActionIcon.VectorIcon(Icons.Default.Close),
                                contentDescription = "Remove all",
                                enabled = selectedPlayers.any(),
                                onClick = removeAllFromMatchListener,
                        ),
                        SelectedItemAction(
                                icon = SelectedItemActionIcon.VectorIcon(Icons.Default.Check),
                                contentDescription = "Create match",
                                enabled = selectedPlayers.any(),
                                onClick = createMatchListener,
                        ),
                ),
        ) {
            if (selectedPlayers.none()) {
                Text(
                        text = "No players selected",
                        style = Typography.h4,
                        modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp)
                )
            }
            else {
                LazyRow(
                        contentPadding = PaddingValues(vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.weight(1f)
                ) {
                    items(
                            selectedPlayers
                                    .map { it to playerMatchStates[it.name]?.state }
                                    // Show players who are already on court first
                                    .sortedByDescending { it.second.transformForSorting(currentTime) }
                    ) { (player, matchState) ->
                        SelectableListItem(
                                currentTime = currentTime,
                                matchState = matchState,
                                generalInProgressColor = ClavaColor.DisabledItemBackground
                        ) {
                            // TODO Mark if anyone has played each other already tonight
                            Text(
                                    text = player.name,
                                    style = Typography.h4,
                                    modifier = Modifier
                                            .padding(vertical = 5.dp, horizontal = 10.dp)
                                            .clickable { playerClickedListener(player) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateMatchScreen_Preview() {
    CreateMatchScreen(
            currentTime = Calendar.getInstance(Locale.getDefault()),
            players = generatePlayers(15),
            matches = generateMatches(5, Calendar.getInstance(Locale.getDefault())),
            courts = generateCourts(5),
            selectedPlayers = generatePlayers(2),
            createMatchListener = {},
            removeAllFromMatchListener = {},
            playerClickedListener = {},
    )
}

@Preview(showBackground = true)
@Composable
fun Individual_CreateMatchScreen_Preview(
        @PreviewParameter(CreateMatchScreenPreviewParamProvider::class) params: CreateMatchScreenPreviewParam
) {
    val players = generatePlayers(2)
    val match = generateMatches(1, Calendar.getInstance(Locale.getDefault()), params.matchType)
    CreateMatchScreen(
            currentTime = Calendar.getInstance(Locale.getDefault()),
            players = players,
            matches = match,
            courts = generateCourts(1),
            selectedPlayers = listOf(),
            createMatchListener = {},
            removeAllFromMatchListener = {},
            playerClickedListener = {},
    )
}

data class CreateMatchScreenPreviewParam(
        val matchType: GeneratableMatchState
)

private class CreateMatchScreenPreviewParamProvider :
        CollectionPreviewParameterProvider<CreateMatchScreenPreviewParam>(
                GeneratableMatchState.values().map { CreateMatchScreenPreviewParam(it) }
        )