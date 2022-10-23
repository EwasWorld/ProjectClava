package com.eywa.projectclava.main.ui.mainScreens

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.R
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.MissingContentNextStep
import com.eywa.projectclava.main.model.Player
import com.eywa.projectclava.main.ui.sharedUi.ClavaScreen
import com.eywa.projectclava.main.ui.sharedUi.EditNameDialog
import com.eywa.projectclava.main.ui.sharedUi.SelectableListItem
import com.eywa.projectclava.ui.theme.Typography

@Composable
fun ArchivedPlayersScreen(
        players: Iterable<Player>?,
        matches: Iterable<Match>?,
        itemNameEditedListener: (Player, String) -> Unit,
        itemDeletedListener: (Player) -> Unit,
        itemUnarchivedListener: (Player) -> Unit,
) {
    var editDialogOpenFor: Player? by remember { mutableStateOf(null) }

    ArchivedPlayersScreen(
            players = players,
            matches = matches,
            editDialogOpenFor = editDialogOpenFor,
            itemNameEditedListener = { item, newName ->
                editDialogOpenFor = null
                itemNameEditedListener(item, newName)
            },
            itemNameEditCancelledListener = { editDialogOpenFor = null },
            itemNameEditStartedListener = { editDialogOpenFor = it },
            itemDeletedListener = { itemDeletedListener(it) },
            itemUnarchivedListener = { itemUnarchivedListener(it) },
    )
}

@Composable
fun ArchivedPlayersScreen(
        players: Iterable<Player>?,
        matches: Iterable<Match>?,
        editDialogOpenFor: Player?,
        itemNameEditedListener: (Player, String) -> Unit,
        itemNameEditCancelledListener: () -> Unit,
        itemNameEditStartedListener: (Player) -> Unit,
        itemDeletedListener: (Player) -> Unit,
        itemUnarchivedListener: (Player) -> Unit,
) {
    val archivedPlayers = players?.filter { it.isArchived }

    EditNameDialog(
            typeContentDescription = "player",
            textPlaceholder = "John Doe",
            nameIsDuplicate = { newName, editItemName ->
                newName != editItemName && players?.any { it.name == newName } == true
            },
            editDialogOpenFor = editDialogOpenFor,
            itemEditedListener = itemNameEditedListener,
            itemEditCancelledListener = itemNameEditCancelledListener,
    )

    // TODO Add a search?
    ClavaScreen(
            noContentText = "No archived players",
            missingContentNextStep = if (archivedPlayers.isNullOrEmpty()) listOf(MissingContentNextStep.ADD_PLAYERS) else null,
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
        items(archivedPlayers?.sortedBy { it.name } ?: listOf()) { item ->
            SelectableListItem {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 15.dp)
                ) {
                    Text(
                            text = item.name,
                            style = Typography.h4,
                            modifier = Modifier.weight(1f)
                    )
                    IconButton(
                            onClick = { itemNameEditStartedListener(item) }
                    ) {
                        Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit ${item.name}"
                        )
                    }
                    IconButton(
                            onClick = { itemUnarchivedListener(item) }
                    ) {
                        Icon(
                                painter = painterResource(R.drawable.baseline_unarchive_24),
                                contentDescription = "Unarchive ${item.name}"
                        )
                    }
                    IconButton(
                            onClick = { itemDeletedListener(item) }
                    ) {
                        Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete ${item.name}"
                        )
                    }
                }
            }
        }
    }
}