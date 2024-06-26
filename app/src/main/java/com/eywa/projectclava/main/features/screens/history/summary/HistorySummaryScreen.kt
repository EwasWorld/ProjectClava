package com.eywa.projectclava.main.features.screens.history.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eywa.projectclava.main.common.GeneratableMatchState
import com.eywa.projectclava.main.common.MissingContentNextStep
import com.eywa.projectclava.main.common.asDateString
import com.eywa.projectclava.main.common.asTimeString
import com.eywa.projectclava.main.common.generateMatches
import com.eywa.projectclava.main.features.screens.history.HistoryTabSwitcherItem
import com.eywa.projectclava.main.features.ui.ClavaScreen
import com.eywa.projectclava.main.features.ui.WrappingRow
import com.eywa.projectclava.main.features.ui.topTabSwitcher.TabSwitcher
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.ModelState
import com.eywa.projectclava.main.theme.Typography
import java.util.Calendar

@Composable
fun HistorySummaryScreen(
        databaseState: ModelState,
        navigateListener: (NavRoute) -> Unit,
) {
    // Most recent first
    val matchesGroupedByDate = databaseState.matches
            .filter { it.isFinished }
            .groupBy { it.getTime().asDateString() }
            .entries
            .sortedByDescending { it.value.first().getTime() }
    val current = databaseState.matches
            .filter { it.isCurrent && it.getTime().asDateString() == matchesGroupedByDate.first().key }
            .flatMap { m -> m.players.map { it.name } }

    ClavaScreen(
            showNoContentPlaceholder = matchesGroupedByDate.isEmpty(),
            noContentText = "No matches have been completed",
            missingContentNextStep = databaseState.getMissingContent()
                    .takeIf { states -> states.any { it == MissingContentNextStep.COMPLETE_A_MATCH } },
            navigateListener = navigateListener,
            headerContent = {
                TabSwitcher(
                        items = HistoryTabSwitcherItem.values().toList(),
                        selectedItem = HistoryTabSwitcherItem.SUMMARY,
                        navigateListener = { navigateListener(it) },
                )
            },
            listArrangement = Arrangement.spacedBy(25.dp),
            listModifier = Modifier.padding(horizontal = 5.dp)
    ) {
        // TODO_CURRENT Semantics
        items(matchesGroupedByDate) { (dateString, matches) ->
            val isSingleMatch = matches.count() == 1
            val firstMatch = matches.minOfOrNull { it.getTime() }!!.asTimeString()
            val lastMatch = matches.maxOfOrNull { it.getTime() }!!.asTimeString()
            val players = matches
                    .flatMap { it.players }
                    .groupBy { it.name }
                    .mapValues { (_, value) -> value.size }
                    .entries
                    .sortedBy { (name, _) -> name }
            val plural = "es".takeIf { !isSingleMatch } ?: ""
            val matchTimes = if (isSingleMatch) "at $firstMatch" else "from $firstMatch to $lastMatch"

            Column(
                    modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                        text = dateString,
                        style = Typography.h3,
                )
                Text(
                        text = "${matches.size} match$plural $matchTimes",
                        style = Typography.h4,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                        text = "Attendees:",
                        style = Typography.body1,
                )
                WrappingRow(modifier = Modifier.padding(start = 10.dp)) {
                    // If players were deleted but matches remain
                    if (players.none()) {
                        Text(
                                text = "No data",
                                style = Typography.body1,
                        )
                    }

                    players.forEachIndexed { index, (name, matchCount) ->
                        val currentlyPlaying = if (current.contains(name)) "*" else ""
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                    text = name,
                                    style = Typography.body1,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                    text = "$matchCount$currentlyPlaying",
                                    style = Typography.body1.copy(fontSize = 12.sp),
                                    modifier = Modifier
                                            .alpha(0.7f)
                                            .align(Alignment.Bottom)
                                            .padding(bottom = 1.dp)
                            )
                            if (index != players.lastIndex) {
                                Text(
                                        text = ",",
                                        style = Typography.body1,
                                )
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
fun HistorySummaryScreen_Preview() {
    val currentTime = Calendar.getInstance()
    val matches = (0..4).fold(listOf<Match>()) { acc, i ->
        acc + generateMatches(
                count = 5,
                currentTime = (currentTime.clone() as Calendar).apply { add(Calendar.DATE, i) },
                forceState = GeneratableMatchState.COMPLETE,
        )
    }
    HistorySummaryScreen(
            databaseState = ModelState(
                    matches = matches,
            ),
            navigateListener = {},
    )
}
