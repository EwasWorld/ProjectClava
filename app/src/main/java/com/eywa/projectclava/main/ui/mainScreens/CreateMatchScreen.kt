package com.eywa.projectclava.main.ui.mainScreens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
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
    val playerMatches = matches.getPlayerMatches()
    val selectedPlayerNames = selectedPlayers.map { it.name }
    // Everyone the selected players have previously played
    val previouslyPlayed = playerMatches
            .filter { (player, _) -> selectedPlayerNames.contains(player) }
            .values
            .flatten()
            .flatMap { it.players }
            .map { it.name }
            .toSet()
            .minus(selectedPlayerNames.toSet())

    // Players to show
    val availablePlayers = players.filter { it.isPresent }

    ClavaScreen(
            noContentText = "No players to match up",
            hasContent = availablePlayers.isNotEmpty(),
            headerContent = { AvailableCourtsHeader(currentTime = currentTime, courts = courts, matches = matches) },
            footerContent = {
                CreateMatchScreenFooter(
                        currentTime = currentTime,
                        selectedPlayers = selectedPlayers,
                        playerMatches = playerMatches,
                        createMatchListener = createMatchListener,
                        removeAllFromMatchListener = removeAllFromMatchListener,
                        playerClickedListener = playerClickedListener,
                )
            },
    ) {
        items(
                availablePlayers
                        .associateWith { playerMatches[it.name] }
                        .entries
                        .sortedWith(Comparator { (player0, match0), (player1, match1) ->
                            if (match0.isNullOrEmpty() && match1.isNullOrEmpty())
                                return@Comparator player0.name.compareTo(player1.name)
                            if (match0.isNullOrEmpty()) return@Comparator -1
                            if (match1.isNullOrEmpty()) return@Comparator 1

                            fun Match.asTimeInt() =
                                    (getLastPlayedTime(currentTime)?.timeInMillis ?: 0) - currentTime.timeInMillis
                            match0.maxOf { it.asTimeInt() }.compareTo(match1.maxOf { it.asTimeInt() })
                        })
        ) { (player, matches) ->
            SelectableListItem(
                    currentTime = currentTime,
                    matchState = matches?.getPlayerColouringMatch()?.state,
                    isSelected = selectedPlayerNames.contains(player.name),
            ) {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                                .clickable { playerClickedListener(player) }
                                .padding(10.dp)
                ) {
                    Text(
                            text = player.name,
                            style = Typography.body1,
                    )
                    if (
                        !selectedPlayerNames.contains(player.name)
                        && previouslyPlayed.contains(player.name)
                    ) {
                        Icon(
                                imageVector = Icons.Outlined.FavoriteBorder,
                                contentDescription = "Played tonight",
                                modifier = Modifier.padding(horizontal = 5.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    MatchStateIndicator(matches?.maxByOrNull { it.state }, currentTime)
                }
            }
        }
    }
}

@Composable
private fun CreateMatchScreenFooter(
        currentTime: Calendar,
        selectedPlayers: Iterable<Player>,
        playerMatches: Map<String, List<Match>?>,
        createMatchListener: () -> Unit,
        removeAllFromMatchListener: () -> Unit,
        playerClickedListener: (Player) -> Unit,
) {
    SelectedItemActions(
            buttons = listOf(
                    SelectedItemAction(
                            icon = ClavaIconInfo.VectorIcon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove all",
                            ),
                            enabled = selectedPlayers.any(),
                            onClick = removeAllFromMatchListener,
                    ),
                    SelectedItemAction(
                            icon = ClavaIconInfo.VectorIcon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Create match",
                            ),
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
            val sortedPlayers = selectedPlayers
                    .map { it to playerMatches[it.name] }
                    // Show players who are already on court first
                    .sortedByDescending { (_, matches) ->
                        matches?.maxOfOrNull { it.state }.transformForSorting(currentTime)
                    }

            // Map playerName to hasPlayedAnotherSelectedPlayerBefore
            val playedBefore = sortedPlayers.map { it.first.name }.toSortedSet().let { sortedPlayerNames ->
                selectedPlayers.associate { player ->
                    player.name to (
                            playerMatches[player.name]
                                    ?.filter { it.state !is MatchState.NotStarted }
                                    ?.any { match ->
                                        match.players.any { sortedPlayerNames.minus(player.name).contains(it.name) }
                                    }
                                    ?: false
                            )
                }
            }

            LazyRow(
                    contentPadding = PaddingValues(vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f)
            ) {
                items(sortedPlayers) { (player, matches) ->
                    // TODO Remove general in progress colour
                    SelectableListItem(
                            currentTime = currentTime,
                            matchState = matches?.getPlayerColouringMatch()?.state,
                            generalInProgressColor = ClavaColor.DisabledItemBackground
                    ) {
                        Text(
                                text = player.name + if (playedBefore[player.name] == true) "*" else "",
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