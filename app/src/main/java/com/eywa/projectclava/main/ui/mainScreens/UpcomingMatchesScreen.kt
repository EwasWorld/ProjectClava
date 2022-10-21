package com.eywa.projectclava.main.ui.mainScreens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.main.common.*
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.model.*
import com.eywa.projectclava.main.ui.sharedUi.*
import com.eywa.projectclava.ui.theme.ClavaColor
import com.eywa.projectclava.ui.theme.DividerThickness
import java.util.*

@Composable
fun UpcomingMatchesScreen(
        courts: Iterable<Court>?,
        matches: Iterable<Match> = listOf(),
        getTimeRemaining: Match.() -> TimeRemaining?,
        startMatchOkListener: (Match, Court, totalTimeSeconds: Int) -> Unit,
        removeMatchListener: (Match) -> Unit,
        defaultTimeSeconds: Int,
        missingContentNextStep: Iterable<MissingContentNextStep>?,
        navigateListener: (NavRoute) -> Unit,
) {
    var startMatchDialogOpenFor: Match? by remember { mutableStateOf(null) }
    var selectedMatch: Match? by remember { mutableStateOf(null) }
    UpcomingMatchesScreen(
            courts = courts,
            matches = matches,
            getTimeRemaining = getTimeRemaining,
            openStartMatchDialogListener = { startMatchDialogOpenFor = it },
            startMatchDialogOpenFor = startMatchDialogOpenFor,
            startMatchOkListener = { match, court, totalTimeSeconds ->
                selectedMatch = null
                startMatchOkListener(match, court, totalTimeSeconds)
            },
            startMatchCancelListener = { startMatchDialogOpenFor = null },
            removeMatchListener = removeMatchListener,
            selectedMatch = selectedMatch,
            defaultTimeSeconds = defaultTimeSeconds,
            selectMatchListener = { newSelection ->
                selectedMatch = newSelection.takeIf { selectedMatch?.id != newSelection.id }
            },
            missingContentNextStep = missingContentNextStep,
            navigateListener = navigateListener,
    )
}

@Composable
fun UpcomingMatchesScreen(
        courts: Iterable<Court>?,
        matches: Iterable<Match> = listOf(),
        getTimeRemaining: Match.() -> TimeRemaining?,
        openStartMatchDialogListener: (Match) -> Unit,
        startMatchDialogOpenFor: Match?,
        startMatchOkListener: (Match, Court, totalTimeSeconds: Int) -> Unit,
        startMatchCancelListener: () -> Unit,
        removeMatchListener: (Match) -> Unit,
        selectedMatch: Match?,
        defaultTimeSeconds: Int,
        selectMatchListener: (Match) -> Unit,
        missingContentNextStep: Iterable<MissingContentNextStep>?,
        navigateListener: (NavRoute) -> Unit,
) {
    val availableCourts = courts?.getAvailable(matches)
    val playerMatchStates = matches.getPlayerStates()
    val sortedMatches = matches.filter { it.state is MatchState.NotStarted }.sortedBy { it.state }

    val missingCourts = missingContentNextStep?.filter {
        it == MissingContentNextStep.ADD_COURTS || it == MissingContentNextStep.ENABLE_COURTS
    }
    val missingContentMain = setOf(
            MissingContentNextStep.ADD_PLAYERS, MissingContentNextStep.ENABLE_PLAYERS,
            MissingContentNextStep.ADD_COURTS, MissingContentNextStep.ENABLE_COURTS,
            MissingContentNextStep.SETUP_A_MATCH
    ).let { allowed ->
        missingContentNextStep
                ?.takeIf { states -> states.any { it == MissingContentNextStep.SETUP_A_MATCH } }
                ?.filter { allowed.contains(it) }
    }

    StartMatchDialog(
            availableCourts = availableCourts,
            startMatchDialogOpenFor = startMatchDialogOpenFor,
            startMatchOkListener = startMatchOkListener,
            startMatchCancelListener = startMatchCancelListener,
            defaultTimeSeconds = defaultTimeSeconds,
    )

    ClavaScreen(
            noContentText = if (missingContentMain != null) "No matches planned" else "No courts to put the matches on!",
            missingContentNextStep = missingContentMain ?: missingCourts,
            navigateListener = navigateListener,
            headerContent = {
                AvailableCourtsHeader(courts = courts, matches = matches, getTimeRemaining = getTimeRemaining)
            },
            footerContent = {
                UpcomingMatchesScreenFooter(
                        getTimeRemaining = getTimeRemaining,
                        openStartMatchDialogListener = openStartMatchDialogListener,
                        removeMatchListener = removeMatchListener,
                        selectedMatch = selectedMatch,
                        playerMatchStates = playerMatchStates,
                        hasAvailableCourts = !availableCourts.isNullOrEmpty(),
                )
            },
    ) {
        items(sortedMatches) { match ->
            val matchingPlayersInEarlierUpcoming = sortedMatches
                    .takeWhile { it != match }
                    .flatMap { it.players }
                    .distinct()
                    .filter { match.players.find { player -> it.name == player.name } != null }
                    .takeIf { it.isNotEmpty() }

            SelectableListItem(
                    isSelected = selectedMatch == match,
                    enabled = match.players.all { it.isPresent },
                    matchState = match.players
                            .mapNotNull { playerMatchStates[it.name] }
                            .maxByOrNull { it.state }
                            ?.takeIf { !it.isFinished }
                            ?.let { foundMatch ->
                                if (foundMatch.state !is MatchState.NotStarted) return@let foundMatch.state
                                // If anyone is in an earlier upcoming mach, use the NotStarted colour
                                matchingPlayersInEarlierUpcoming?.let { foundMatch.state }
                            },
                    timeRemaining = { match.getTimeRemaining() },
            ) {
                LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        contentPadding = PaddingValues(10.dp),
                        modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = { selectMatchListener(match) })
                ) {
                    items(
                            match.players
                                    .map { it to playerMatchStates[it.name] }
                                    .partition {
                                        it.second?.isCurrent != true || it.second?.isFinished != true
                                    }
                                    .let { (noMatch, match) ->
                                        match.sortedBy { it.second?.state } + noMatch.sortedBy { it.first.name }
                                    }
                    ) { (player, playerMatch) ->
                        SelectableListItem(
                                enabled = player.enabled,
                                matchState = playerMatch?.state?.let { matchState ->
                                    if (matchState !is MatchState.NotStarted) return@let matchState
                                    matchingPlayersInEarlierUpcoming
                                            ?.takeIf { it.find { p -> p.name == player.name } != null }
                                            ?.let { matchState }
                                },
                                timeRemaining = { playerMatch?.getTimeRemaining() },
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
}

@Composable
private fun UpcomingMatchesScreenFooter(
        getTimeRemaining: Match.() -> TimeRemaining?,
        openStartMatchDialogListener: (Match) -> Unit,
        removeMatchListener: (Match) -> Unit,
        selectedMatch: Match?,
        playerMatchStates: Map<String, Match?>,
        hasAvailableCourts: Boolean,
) {
    val extraText: String?
    val color: Color?
    selectedMatch?.players?.find { !it.isPresent }.let { disabledPlayer ->
        if (disabledPlayer != null) {
            extraText = "${disabledPlayer.name} is not present"
            color = ClavaColor.DisabledItemBackground
        }
        else {
            // The latest-finishing match and the player in selectedMatch who is also in latestMatch
            val (latestPlayer, latestMatch) = selectedMatch?.players
                    ?.associateWith { playerMatchStates[it.name] }
                    ?.filter { it.value?.isFinished == false }
                    ?.maxByOrNull { it.value!!.state }
                    ?: mapOf(null to null).asIterable().first()

            color = latestMatch?.state?.takeIf { it !is MatchState.NotStarted }
                    ?.asColor(selectedMatch?.getTimeRemaining())
            extraText = when (latestMatch?.state) {
                null,
                is MatchState.NotStarted,
                is MatchState.Completed -> null
                is MatchState.Paused -> "${latestPlayer?.name}'s match is paused"
                is MatchState.OnCourt -> {
                    "${latestPlayer?.name} is on ${latestMatch.court!!.name}" +
                            "\nTime remaining: " + selectedMatch?.getTimeRemaining().asTimeString()
                }
            }
        }
    }

    SelectedItemActions(
            text = selectedMatch?.players?.sortedBy { it.name }?.joinToString { it.name } ?: "No match selected",
            extraText = extraText,
            color = color,
            buttons = listOf(
                    SelectedItemAction(
                            icon = ClavaIconInfo.VectorIcon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove match",
                            ),
                            enabled = selectedMatch != null,
                            onClick = { selectedMatch?.let { removeMatchListener(it) } },
                    ),
                    SelectedItemAction(
                            icon = ClavaIconInfo.VectorIcon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Start match",
                            ),
                            enabled = selectedMatch != null
                                    && hasAvailableCourts
                                    && selectedMatch.players
                                    .all {
                                        (playerMatchStates[it.name]?.isOnCourt?.not() ?: true)
                                                && it.isPresent
                                    },
                            onClick = { selectedMatch?.let { openStartMatchDialogListener(it) } },
                    ),
            ),
    )
}

@Composable
private fun StartMatchDialog(
        availableCourts: Iterable<Court>?,
        startMatchDialogOpenFor: Match?,
        startMatchOkListener: (Match, Court, totalTimeSeconds: Int) -> Unit,
        startMatchCancelListener: () -> Unit,
        defaultTimeSeconds: Int,
) {
    var selectedCourt by remember(startMatchDialogOpenFor) { mutableStateOf(availableCourts?.minByOrNull { it.name }) }
    var timeSeconds by remember(startMatchDialogOpenFor) { mutableStateOf(TimePickerState(defaultTimeSeconds)) }

    ClavaDialog(
            isShown = startMatchDialogOpenFor != null,
            title = "Choose a duration and court",
            okButtonText = "Start",
            okButtonEnabled = selectedCourt != null && timeSeconds.isValid,
            onCancelListener = startMatchCancelListener,
            onOkListener = {
                startMatchOkListener(startMatchDialogOpenFor!!, selectedCourt!!, timeSeconds.totalSeconds)
            },
    ) {
        TimePicker(
                timePickerState = timeSeconds,
                timeChangedListener = { timeSeconds = it },
                modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
        )
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
    val currentTime = Calendar.getInstance(Locale.getDefault())
    val matches = generateMatches(5, currentTime) + generateMatches(4, currentTime, GeneratableMatchState.NOT_STARTED)

    UpcomingMatchesScreen(
            courts = generateCourts(4),
            matches = matches,
            getTimeRemaining = { state.getTimeLeft(currentTime) },
            removeMatchListener = {},
            selectedMatch = params.selectedIndex?.let {
                matches.filter { match -> match.state is MatchState.NotStarted }[it]
            },
            selectMatchListener = {},
            openStartMatchDialogListener = {},
            startMatchDialogOpenFor = if (params.startMatchDialogOpen) matches[0] else null,
            startMatchOkListener = { _, _, _ -> },
            startMatchCancelListener = {},
            defaultTimeSeconds = 15 * 60,
            missingContentNextStep = null,
            navigateListener = {},
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