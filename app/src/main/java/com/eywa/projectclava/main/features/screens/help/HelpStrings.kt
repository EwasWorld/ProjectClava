package com.eywa.projectclava.main.features.screens.help

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import com.eywa.projectclava.R
import com.eywa.projectclava.main.features.ui.ClavaBottomNav
import com.eywa.projectclava.main.mainActivity.MainNavRoute
import com.eywa.projectclava.main.mainActivity.NavRoute

fun NavRoute.getHelpInfo() = when (this) {
    MainNavRoute.ADD_PLAYER -> HelpStrings.ADD_PLAYER
    MainNavRoute.ADD_COURT -> HelpStrings.ADD_COURT
    MainNavRoute.CREATE_MATCH -> HelpStrings.CREATE_MATCH
    MainNavRoute.MATCH_QUEUE -> HelpStrings.MATCH_QUEUE
    MainNavRoute.ONGOING_MATCHES -> HelpStrings.ONGOING_MATCHES
    MainNavRoute.MATCH_HISTORY -> HelpStrings.MATCH_HISTORY
    MainNavRoute.HISTORY_SUMMARY -> HelpStrings.HISTORY_SUMMARY
    MainNavRoute.ARCHIVED_PLAYERS -> null
    MainNavRoute.HELP_SCREEN -> null
    else -> throw NotImplementedError()
}

private fun boldUnderlined(start: Int, end: Int, style: SpanStyle = SpanStyle()) = AnnotatedString.Range(
        style.copy(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline),
        start,
        end,
)

val NAVIGATION_HELP_INFO = HelpData(
        title = "Navigation Icons",
        body = "Press an icon to get to where you want to go!",
        helpContent = HelpContent(
                content = { ClavaBottomNav(currentRoute = null, hasOverrunningMatch = true, onClick = it) },
                contentDescription = listOf(
                        AnnotatedString(
                                """
                            |Manage:
                            |   - Add players or mark existing players as present/absent
                            |   - Add courts or mark existing courts as in use/not in use
                               """.trimMargin(), listOf(boldUnderlined(0, 6))
                        ),
                        AnnotatedString("Match up: set up a new game between players", listOf(boldUnderlined(0, 8))),
                        AnnotatedString(
                                "Queue: view games that have been set up and start them",
                                listOf(boldUnderlined(0, 5))
                        ),
                        AnnotatedString(
                                "Ongoing: view games that are being played and mark them as completed. Red dot indicates an overrunning match",
                                listOf(boldUnderlined(0, 8))
                        ),
                        AnnotatedString("History: view completed games", listOf(boldUnderlined(0, 7)))
                ),
        )
)

private object HelpStrings {
    val ADD_PLAYER = HelpData(
            title = "Manage Players",
            body = "Add, archive, enable, and disable players.",
            showColourHelp = true,
            helpContent = HelpImage(
                    imageId = R.drawable.help_add_players,
                    imageDescription = listOf(
                            AnnotatedString(
                                    "Get here by pressing 'Manage' in the bottom bar (1), then pressing 'Players' (2) in the top bar.",
                                    listOf(boldUnderlined(0, 8))
                            ),
                            AnnotatedString(
                                    "Add a player by typing their name in the 'Add player' box (3) then pressing enter.",
                                    listOf(boldUnderlined(0, 12))
                            ),
                            AnnotatedString(
                                    "Each row shows a player's name (4). The row may be coloured based on the player's status, see below for colour descriptions."
                            ),
                            AnnotatedString(
                                    "Mark a player absent by pressing their row, which will turn grey (5). Press again to mark them as present. When a player is absent, they will not show up on the 'Match Up' screen.",
                                    listOf(boldUnderlined(0, 4), boldUnderlined(14, 20))
                            ),
                            AnnotatedString(
                                    "Edit a player's name by pressing the pencil on their row (6).",
                                    listOf(boldUnderlined(0, 4))
                            ),
                            AnnotatedString(
                                    "Archive a player by pressing the archive icon (7) on their row. This will remove them from this screen and they will not show up on the 'Match Up' screen. To access the list of archived players, open the pullout menu on the left (9)",
                                    listOf(boldUnderlined(0, 7))
                            ),
                            AnnotatedString(
                                    "Search for a player by pressing the search icon (8).",
                                    listOf(boldUnderlined(0, 6))
                            ),
                    )
            ),
    )

    val ADD_COURT = HelpData(
            title = "Manage Courts",
            body = "Add, remove, enable, and disable courts.",
            showColourHelp = true,
            helpContent = HelpImage(
                    imageId = R.drawable.help_add_courts,
                    imageDescription = listOf(
                            AnnotatedString(
                                    "Get here by pressing 'Manage' in the bottom bar (1), then pressing 'Courts' (2) in the top bar.",
                                    listOf(boldUnderlined(0, 8))
                            ),
                            AnnotatedString(
                                    "Add a court by typing their name in the 'Add court' box (3) then pressing enter. By default this will automatically prepend 'Court' to the front of the name. To turn this off, open the pullout menu on the left (9) and disable 'Prepend 'Court' to new courts'.",
                                    listOf(boldUnderlined(0, 11))
                            ),
                            AnnotatedString(
                                    "Each row shows a court's name (4). The row may be coloured based on the status of the game being played on the court, see below for colour descriptions."
                            ),
                            AnnotatedString(
                                    "Mark a court as unusable by pressing its row, which will turn grey (5). Press again to mark it as usable. When a court is unusable, it will not show up on any court selection dialogs or in the 'Available Courts' section of other screens.",
                                    listOf(boldUnderlined(0, 4), boldUnderlined(16, 24))
                            ),
                            AnnotatedString(
                                    "Edit a court's name by pressing the pencil on its row (6).",
                                    listOf(boldUnderlined(0, 4))
                            ),
                            AnnotatedString(
                                    "Delete a court by pressing the X icon (7) on its row. This button will be disabled if a game is being played on the court.",
                                    listOf(boldUnderlined(0, 7))
                            ),
                            AnnotatedString(
                                    "Search for a court by pressing the search icon (8).",
                                    listOf(boldUnderlined(0, 6))
                            ),
                    ),
            ),
    )

    val CREATE_MATCH = HelpData(
            title = "Match Up",
            body = "Select players and press the tick to queue up a game.",
            showColourHelp = true,
            helpContent = HelpImage(
                    imageId = R.drawable.help_match_up,
                    imageDescription = listOf(
                            AnnotatedString(
                                    "Get here by pressing 'Match Up' in the bottom bar (1).",
                                    listOf(boldUnderlined(0, 8))
                            ),
                            AnnotatedString(
                                    "Each row (5) shows a player's name and when they last played (or when they will finish if they're currently playing). The row may be coloured based on the player's status, see below for colour descriptions.",
                            ),
                            AnnotatedString(
                                    "Select players to play by pressing their row. Selected players have a blue border (2) and appear in the footer (7).",
                                    listOf(boldUnderlined(0, 14))
                            ),
                            AnnotatedString(
                                    "Heart icons (4) show that one of the selected players (7) has played this player. For example, while Amy is selected, the heart icon shows that she's already played Claire F.",
                                    listOf(boldUnderlined(0, 11))
                            ),
                            AnnotatedString(
                                    "Note heart icons are only shown for games played after the cut-off. This is usually set to 4am but can be changed in the pullout menu on the left (10)",
                            ),
                            AnnotatedString(
                                    "Create a game by selecting one or more players and pressing the tick in the footer (9). Note this doesn't start the game yet, it only adds it to the queue! Press 'Queue' in the bottom bar to start the game.",
                                    listOf(boldUnderlined(0, 13))
                            ),
                            AnnotatedString(
                                    "Clear the selected players by pressing the X in the footer (8).",
                                    listOf(boldUnderlined(0, 5))
                            ),
                            AnnotatedString(
                                    "Info at the top (6) shows which courts are currently free or when the next court will become available.",
                            ),
                    ),
            ),
    )

    val MATCH_QUEUE = HelpData(
            title = "Game Queue",
            body = "Start games by assigning them a court and a duration.",
            showColourHelp = true,
            helpContent = HelpImage(
                    imageId = R.drawable.help_queue,
                    imageDescription = listOf(
                            AnnotatedString(
                                    "Get here by pressing 'Queue' in the bottom bar (1).",
                                    listOf(boldUnderlined(0, 8))
                            ),
                            AnnotatedString(
                                    "Each row (3) shows the players in the game. Players may be coloured (4) based on their status and the row may be coloured based on player statuses, see below for colour descriptions.",
                            ),
                            AnnotatedString(
                                    "Start a match by pressing its row to select it (3) then pressing the play button in the footer (7). A popup will ask you to choose a court and a game duration. Default game duration can be changed in the pullout menu on the left (8).",
                                    listOf(boldUnderlined(0, 14))
                            ),
                            AnnotatedString(
                                    "Start button may be disabled if one of the players is currently playing or if there are no courts available. Press 'Ongoing' in the bottom bar and complete a game to enable this button.",
                                    listOf(boldUnderlined(20, 28))
                            ),
                            AnnotatedString(
                                    "Delete a match by pressing its row to select it (3) then pressing the X button in the footer (6).",
                                    listOf(boldUnderlined(0, 14))
                            ),
                            AnnotatedString(
                                    "Info at the top (2) shows which courts are currently free or when the next court will become available.",
                            ),
                    ),
            ),
    )

    val ONGOING_MATCHES = HelpData(
            title = "Ongoing Games",
            body = "View in-progress and paused games. You can mark games as completed, pause them, add time, and change courts from here.",
            showColourHelp = true,
            helpContent = HelpImage(
                    imageId = R.drawable.help_ongoing,
                    imageDescription = listOf(
                            AnnotatedString(
                                    "Get here by pressing 'Ongoing' in the bottom bar (1). A red dot will appear next to this icon if there is a game that is overrunning.",
                                    listOf(boldUnderlined(0, 8))
                            ),
                            AnnotatedString(
                                    "Each row (2) shows a game, with information on the court, players and how much time is left (minutes:seconds). Rows are coloured based on the game's status, see below for colour descriptions.",
                            ),
                            AnnotatedString(
                                    "Select a game by pressing it (2), it will then appear in the footer (4). From here you can add time (5), change court (6), pause/resume the game (7), or mark it as complete (8).",
                                    listOf(boldUnderlined(0, 13))
                            ),
                            AnnotatedString(
                                    "Info at the top (3) shows which courts are currently free or when the next court will become available.",
                            ),
                    ),
            ),
    )

    val MATCH_HISTORY = HelpData(
            title = "Match History",
            body = "View all finished games. You can restart or delete games from here.",
            helpContent = HelpImage(
                    imageId = R.drawable.help_history_matches,
                    imageDescription = listOf(
                            AnnotatedString(
                                    "Get here by pressing 'History' in the bottom bar (1), then pressing 'Matches' (2) in the top bar.",
                                    listOf(boldUnderlined(0, 8))
                            ),
                            AnnotatedString(
                                    "Games are sorted by date (3) with the most recent first. Each row (4) shows a game and what time it finished.",
                            ),
                            AnnotatedString(
                                    "Select a game by pressing it (4), it will then appear in the footer (5). From here you can add time (6), or delete the game (7).",
                                    listOf(boldUnderlined(0, 13))
                            ),
                    ),
            ),
    )

    val HISTORY_SUMMARY = HelpData(
            title = "Summary",
            body = "View each day's attendees.",
            showColourHelp = true,
            helpContent = HelpImage(
                    imageId = R.drawable.help_history_summary,
                    imageDescription = listOf(
                            AnnotatedString(
                                    "Get here by pressing 'History' in the bottom bar (1), then pressing 'Summary' (2) in the top bar.",
                                    listOf(boldUnderlined(0, 8))
                            ),
                            AnnotatedString(
                                    "Entries are sorted by date (3) with the most recent first. Each day shows list of players (4) who played at least one game. The number next to the player is the number of games they played that day.",
                            ),
                    ),
            ),
    )
}

@Preview(
        showBackground = true,
        heightDp = 2000
)
@Composable
fun Parameterised_HelpScreen_Preview(
        @PreviewParameter(HelpScreenPreviewParamProvider::class) params: NavRoute?
) {
    HelpScreen(
            state = HelpState(screen = params),
            listener = {},
    )
}

private class HelpScreenPreviewParamProvider : CollectionPreviewParameterProvider<NavRoute?>(
        setOf(null).plus(MainNavRoute.values()).minus(
                setOf(
                        MainNavRoute.ARCHIVED_PLAYERS,
                        MainNavRoute.HELP_SCREEN,
                )
        )
)