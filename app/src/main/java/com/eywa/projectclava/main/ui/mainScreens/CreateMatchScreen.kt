package com.eywa.projectclava.main.ui.mainScreens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.main.common.*
import com.eywa.projectclava.main.model.Court
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.Player
import com.eywa.projectclava.main.model.getPlayerStates
import com.eywa.projectclava.main.ui.sharedUi.*
import com.eywa.projectclava.ui.theme.ClavaColor
import com.eywa.projectclava.ui.theme.DividerThickness
import kotlinx.coroutines.delay
import java.util.*

/**
 * @param selectedPlayers the people selected to form the next match
 */
@Composable
fun CreateMatchScreen(
        players: Iterable<Player>,
        matches: Iterable<Match> = listOf(),
        courts: Iterable<Court>? = listOf(),
        selectedPlayers: Iterable<Player>,
        createMatchListener: () -> Unit,
        removeAllFromMatchListener: () -> Unit,
        playerClickedListener: (Player) -> Unit,
) {
    // TODO Change to time left
    // TODO Add pause icon
    // TODO Icon/color if they've already played tonight?
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = Calendar.getInstance()
        }
    }

    val playerMatchStates = matches.getPlayerStates()

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
                        isSelected = selectedPlayers.contains(player),
                ) {
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                    .padding(10.dp)
                                    .clickable { playerClickedListener(player) }
                    ) {
                        Text(
                                text = player.name,
                                modifier = Modifier.weight(1f)
                        )
                        Text(
                                text = match?.lastPlayedTime?.asString() ?: "Not played"
                        )
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
                        modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp)
                )
            }
            else {
                LazyRow(
                        contentPadding = PaddingValues(horizontal = 10.dp),
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
                            Text(
                                    text = player.name,
                                    modifier = Modifier
                                            .padding(vertical = 3.dp, horizontal = 5.dp)
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
            players = generatePlayers(15),
            matches = generateMatches(5, Calendar.getInstance()),
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
    val match = generateMatches(1, Calendar.getInstance(), params.matchType)
    CreateMatchScreen(
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