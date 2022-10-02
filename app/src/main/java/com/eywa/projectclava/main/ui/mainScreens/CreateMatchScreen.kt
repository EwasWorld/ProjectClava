package com.eywa.projectclava.main.ui.mainScreens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.main.common.*
import com.eywa.projectclava.main.model.*
import com.eywa.projectclava.main.ui.sharedUi.AvailableCourtsHeader
import com.eywa.projectclava.main.ui.sharedUi.SelectedItemAction
import com.eywa.projectclava.main.ui.sharedUi.SelectedItemActionIcon
import com.eywa.projectclava.main.ui.sharedUi.SelectedItemActions
import com.eywa.projectclava.ui.theme.ClavaColor
import com.eywa.projectclava.ui.theme.DividerThickness
import kotlinx.coroutines.delay
import java.util.*

/**
 * @param partiallyCreatedMatch the people added to the match currently being created
 */
@Composable
fun CreateMatchScreen(
        players: Iterable<Player>,
        previousMatches: Iterable<Match> = listOf(),
        courts: Iterable<Court>? = listOf(),
        upcomingMatches: List<Match> = listOf(),
        partiallyCreatedMatch: Iterable<Player>,
        createMatchListener: () -> Unit,
        removeAllFromMatchListener: () -> Unit,
        addPlayerToPartialListener: (Player) -> Unit,
        removePlayerFromPartialListener: (Player) -> Unit,
) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = Calendar.getInstance()
        }
    }

    val playerMatchStates = courts?.getPlayerStates() ?: mapOf()


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
            items(
                    players.associateWith { player ->
                        courts?.asSequence()
                                ?.mapNotNull { it.currentMatch }
                                ?.plus(previousMatches)
                                ?.filter { it.players.contains(player) && it.lastPlayedTime != null }
                                ?.minByOrNull { it.lastPlayedTime!! }
                    }.entries.sortedBy { it.value?.lastPlayedTime }
            ) { (player, match) ->
                Surface(
                        shape = RoundedCornerShape(5.dp),
                        // TODO Colours not showing correctly
                        // TODO Color if already in an upcoming match
                        color = match?.state?.asColor(currentTime, ClavaColor.DisabledItemBackground)
                                ?: ClavaColor.ItemBackground,
                        border = BorderStroke(1.dp, ClavaColor.GeneralBorder)
                ) {
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                    .padding(10.dp)
                                    .clickable { addPlayerToPartialListener(player) }
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
                                enabled = partiallyCreatedMatch.any(),
                                onClick = removeAllFromMatchListener,
                        ),
                        SelectedItemAction(
                                icon = SelectedItemActionIcon.VectorIcon(Icons.Default.Check),
                                contentDescription = "Create match",
                                enabled = partiallyCreatedMatch.any(),
                                onClick = createMatchListener,
                        ),
                ),
        ) {
            if (partiallyCreatedMatch.none()) {
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
                            partiallyCreatedMatch
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
                                    modifier = Modifier
                                            .padding(vertical = 3.dp, horizontal = 5.dp)
                                            .clickable { removePlayerFromPartialListener(player) }
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
    val courts = generateCourts(5).toMutableList()
    val matches = generateMatches(3)
    matches.forEach {
        courts.add(courts.removeFirst().copy(currentMatch = it))
    }
    CreateMatchScreen(
            players = generatePlayers(10),
            previousMatches = listOf(),
            courts = courts,
            upcomingMatches = listOf(),
            partiallyCreatedMatch = generatePlayers(2),
            createMatchListener = {},
            removeAllFromMatchListener = {},
            addPlayerToPartialListener = {},
            removePlayerFromPartialListener = {},
    )
}