package com.eywa.projectclava.main.mainActivity.screens.archivedPlayers

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.R
import com.eywa.projectclava.main.model.DatabaseState
import com.eywa.projectclava.main.model.MissingContentNextStep
import com.eywa.projectclava.main.ui.sharedUi.ClavaScreen
import com.eywa.projectclava.main.ui.sharedUi.EditDialogIntent
import com.eywa.projectclava.main.ui.sharedUi.EditNameDialog
import com.eywa.projectclava.main.ui.sharedUi.SelectableListItem
import com.eywa.projectclava.ui.theme.Typography


@Composable
fun ArchivedPlayersScreen(
        databaseState: DatabaseState,
        state: ArchivedPlayersState,
        listener: (ArchivedPlayersIntent) -> Unit,
) {
    val archivedPlayers = databaseState.players.filter { it.isArchived }

    EditNameDialog(
            typeContentDescription = "player",
            textPlaceholder = "John Doe",
            nameIsDuplicate = { newName, nameOfItemBeingEdited ->
                newName != nameOfItemBeingEdited && databaseState.players.any { it.name == newName }
            },
            state = state,
            listener = { listener(it.toArchivedPlayersIntent()) },
    )

    // TODO Add a search?
    ClavaScreen(
            noContentText = "No archived players",
            missingContentNextStep = if (archivedPlayers.isEmpty()) listOf(MissingContentNextStep.ADD_PLAYERS) else null,
            showMissingContentNextStep = false,
            navigateListener = {},
            headerContent = {
                Text(
                        text = "Archived players",
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = Typography.h4,
                        modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                )
            }
    ) {
        items(archivedPlayers.sortedBy { it.name }) { player ->
            SelectableListItem {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 15.dp)
                ) {
                    Text(
                            text = player.name,
                            style = Typography.h4,
                            modifier = Modifier.weight(1f)
                    )
                    IconButton(
                            onClick = {
                                listener(
                                        EditDialogIntent.EditItemStateIntent.EditItemStarted(player)
                                                .toArchivedPlayersIntent()
                                )
                            }
                    ) {
                        Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit ${player.name}"
                        )
                    }
                    IconButton(
                            onClick = { listener(ArchivedPlayersIntent.PlayerUnarchived(player)) }
                    ) {
                        Icon(
                                painter = painterResource(R.drawable.baseline_unarchive_24),
                                contentDescription = "Unarchive ${player.name}"
                        )
                    }
                    IconButton(
                            onClick = { listener(ArchivedPlayersIntent.ItemDeleted(player)) }
                    ) {
                        Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete ${player.name}"
                        )
                    }
                }
            }
        }
    }
}