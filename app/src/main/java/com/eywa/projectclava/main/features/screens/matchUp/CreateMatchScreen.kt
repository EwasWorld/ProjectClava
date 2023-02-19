package com.eywa.projectclava.main.features.screens.matchUp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.main.common.GeneratableMatchState
import com.eywa.projectclava.main.common.generateMatches
import com.eywa.projectclava.main.common.generatePlayers
import com.eywa.projectclava.main.common.stateSemanticsText
import com.eywa.projectclava.main.features.ui.*
import com.eywa.projectclava.main.model.*
import com.eywa.projectclava.main.theme.Typography
import java.util.*

// TODO Add button to randomly match up all players
/**
 * @param clubNightStartTime When club night began.
 * Matches that ended before this time will not count towards the 'played before' markers
 */
@Composable
fun CreateMatchScreen(
    overrunThreshold: Int,
    state: CreateMatchState,
    databaseState: ModelState,
    clubNightStartTime: Calendar,
    getTimeRemaining: Match.() -> TimeRemaining?,
    listener: (CreateMatchIntent) -> Unit,
) {
    val playerMatches = databaseState.matches
            // Ignore matches from before club night started
            .filter { !it.isFinished || it.getTime().after(clubNightStartTime) }
            .getPlayerMatches()
    val selectedPlayers = state.selectedPlayers.mapNotNull { id -> databaseState.players.find { it.id == id } }
    val selectedPlayerNames = selectedPlayers.map { it.name }
    // Everyone the selected players have previously played
    val previouslyPlayed = playerMatches
            .filterKeys { player -> selectedPlayerNames.contains(player) }
            .values
            .flatten()
            .flatMap { it.players }
            .map { it.name }
            .toSet()
            .minus(selectedPlayerNames.toSet())

    // Players to show
    val availablePlayers = databaseState.players.filter { it.enabled }

    ClavaScreen(
            showNoContentPlaceholder = availablePlayers.isEmpty(),
            noContentText = "No players to match up",
            missingContentNextStep = databaseState.getMissingContent(),
            navigateListener = { listener(CreateMatchIntent.Navigate(it)) },
            headerContent = {
                AvailableCourtsHeader(
                        courts = databaseState.courts,
                        matches = databaseState.matches,
                        getTimeRemaining = getTimeRemaining
                )
            },
            footerContent = {
                CreateMatchScreenFooter(
                    overrunThreshold = overrunThreshold,
                    getTimeRemaining = getTimeRemaining,
                    selectedPlayers = selectedPlayers,
                    playerMatches = playerMatches,
                    listener = listener,
                )
            },
    ) {
        items(
                availablePlayers
                        .associateWith { playerMatches[it.name] }
                        .entries
                        .sortedWith(Comparator { (player0, matches0), (player1, matches1) ->
                            fun comparePredicate(
                                    predicate: List<Match>?.() -> Boolean,
                                    truePredToEnd: Boolean = false,
                            ): Int? {
                                val multiplier = if (truePredToEnd) -1 else 1
                                if (matches0.predicate() && matches1.predicate())
                                    return player0.name.compareTo(player1.name)
                                if (matches0.predicate()) return -1 * multiplier
                                if (matches1.predicate()) return 1 * multiplier
                                return null
                            }

                            // Players with no queued matches to the start
                            var comparison = comparePredicate({ isNullOrEmpty() })
                            if (comparison != null) return@Comparator comparison

                            matches0!!
                            matches1!!

                            // Players with only queued matches to the start
                            comparison = comparePredicate({ this!!.all { it.isNotStarted } })
                            if (comparison != null) return@Comparator comparison

                            // Players with any in progress matches to the end
                            comparison = comparePredicate({ this!!.any { it.isOnCourt } }, true)
                            if (comparison != null) return@Comparator comparison

                            // Get the latest finish time of the set
                            fun List<Match>.latestFinishTime() =
                                    filter { !it.isNotStarted }.maxOf { it.getTime() }
                            matches0.latestFinishTime().compareTo(matches1.latestFinishTime())
                        })
        ) { (player, matches) ->
            val match = matches?.getPlayerColouringMatch()

            SelectableListItem(
                overrunThreshold = overrunThreshold,
                match = match,
                isSelected = selectedPlayerNames.contains(player.name),
                getTimeRemaining = getTimeRemaining,
                contentDescription = player.name + " " + match.stateSemanticsText { match?.getTimeRemaining() },
                onClick = { listener(CreateMatchIntent.PlayerClicked(player)) },
            ) {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp)
                ) {
                    Text(
                            text = player.name,
                            style = Typography.body1,
                    )
                    if (!selectedPlayerNames.contains(player.name) && previouslyPlayed.contains(player.name)) {
                        PlayedBeforeIcon(Modifier.padding(horizontal = 5.dp))
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    MatchTimeRemainingText(matches?.maxByOrNull { it.state }, getTimeRemaining)
                }
            }
        }
    }
}

@Composable
private fun PlayedBeforeIcon(modifier: Modifier = Modifier) =
        Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = "Played tonight",
                modifier = modifier
        )

@Composable
private fun CreateMatchScreenFooter(
    overrunThreshold: Int,
    getTimeRemaining: Match.() -> TimeRemaining?,
    selectedPlayers: Iterable<Player>,
    playerMatches: Map<String, List<Match>?>,
    listener: (CreateMatchIntent) -> Unit,
) {
    SelectedItemActions(
            buttons = listOf(
                    SelectedItemAction(
                            icon = ClavaIconInfo.VectorIcon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear selection",
                            ),
                            enabled = selectedPlayers.any(),
                            onClick = { listener(CreateMatchIntent.ClearSelectedPlayers) },
                    ),
                    SelectedItemAction(
                            icon = ClavaIconInfo.VectorIcon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Create match",
                            ),
                            enabled = selectedPlayers.any(),
                            onClick = { listener(CreateMatchIntent.CreateMatch) },
                    ),
            ),
    ) {
        if (selectedPlayers.none()) {
            Text(
                    text = "No players selected",
                    style = Typography.h4,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp, vertical = 15.dp)
            )
        }
        else {
            val sortedPlayers = Calendar.getInstance().let { currentTime ->
                selectedPlayers
                        .map { it to playerMatches[it.name] }
                        // Show players who are already on court first
                        .sortedByDescending { (_, matches) ->
                            matches?.maxOfOrNull { it.state } ?: MatchState.NotStarted(currentTime)
                        }
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
                    val match = matches?.getPlayerColouringMatch()

                    SelectableListItem(
                        overrunThreshold = overrunThreshold,
                        enabled = player.enabled,
                        match = match,
                        getTimeRemaining = { match?.getTimeRemaining() },
                        contentDescription = player.name + " " + match.stateSemanticsText { match?.getTimeRemaining() },
                        onClickActionLabel = "Deselect",
                        onClick = { listener(CreateMatchIntent.PlayerClicked(player)) },
                    ) {
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(vertical = 5.dp, horizontal = 10.dp)
                        ) {
                            Text(
                                    text = player.name,
                                    style = Typography.h4,
                            )
                            if (playedBefore[player.name] == true) {
                                PlayedBeforeIcon()
                            }
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
    val currentTime = Calendar.getInstance(Locale.getDefault())
    CreateMatchScreen(
        overrunThreshold = 10,
        state = CreateMatchState(
            selectedPlayers = generatePlayers(2).map { it.id },
        ),
        databaseState = ModelState(
            players = generatePlayers(15),
            matches = generateMatches(5, Calendar.getInstance(Locale.getDefault())),
        ),
        clubNightStartTime = currentTime,
        getTimeRemaining = { state.getTimeLeft(currentTime) },
        listener = {},
    )
}

@Preview(showBackground = true)
@Composable
fun Individual_CreateMatchScreen_Preview(
        @PreviewParameter(CreateMatchScreenPreviewParamProvider::class) params: CreateMatchScreenPreviewParam
) {
    val currentTime = Calendar.getInstance(Locale.getDefault())
    val players = generatePlayers(2)
    val match = generateMatches(1, Calendar.getInstance(Locale.getDefault()), params.matchType)
    CreateMatchScreen(
        overrunThreshold = 10,
        state = CreateMatchState(
        ),
        databaseState = ModelState(
            players = players,
            matches = match,
        ),
        clubNightStartTime = currentTime,
        getTimeRemaining = { state.getTimeLeft(currentTime) },
        listener = {},
    )
}

data class CreateMatchScreenPreviewParam(
        val matchType: GeneratableMatchState
)

private class CreateMatchScreenPreviewParamProvider :
        CollectionPreviewParameterProvider<CreateMatchScreenPreviewParam>(
                GeneratableMatchState.values().map { CreateMatchScreenPreviewParam(it) }
        )
