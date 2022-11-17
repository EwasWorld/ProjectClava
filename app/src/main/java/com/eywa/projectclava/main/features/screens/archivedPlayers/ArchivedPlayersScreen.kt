package com.eywa.projectclava.main.features.screens.archivedPlayers

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.R
import com.eywa.projectclava.main.features.ui.ClavaIconInfo.PainterIcon
import com.eywa.projectclava.main.features.ui.ClavaIconInfo.VectorIcon
import com.eywa.projectclava.main.features.ui.ClavaScreen
import com.eywa.projectclava.main.features.ui.SelectableListItem
import com.eywa.projectclava.main.features.ui.SelectedItemAction
import com.eywa.projectclava.main.features.ui.confirmDialog.ConfirmDialog
import com.eywa.projectclava.main.features.ui.confirmDialog.ConfirmDialogIntent
import com.eywa.projectclava.main.features.ui.confirmDialog.ConfirmDialogType
import com.eywa.projectclava.main.features.ui.editNameDialog.EditDialogIntent
import com.eywa.projectclava.main.features.ui.editNameDialog.EditNameDialog
import com.eywa.projectclava.main.model.ModelState
import com.eywa.projectclava.main.theme.Typography


@Composable
fun ArchivedPlayersScreen(
        databaseState: ModelState,
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
    ConfirmDialog(
            state = state.deletePlayerDialogState,
            type = ConfirmDialogType.DELETE,
            listener = { listener(it.toArchivedPlayersIntent()) },
    )

    // TODO Add a search?
    ClavaScreen(
            noContentText = "No archived players",
            showNoContentPlaceholder = archivedPlayers.isEmpty(),
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
            val buttons = listOf(
                    SelectedItemAction(
                            VectorIcon(Icons.Default.Edit, "Edit ${player.name}")
                    ) { listener(EditDialogIntent.EditItemStarted(player).toArchivedPlayersIntent()) },
                    SelectedItemAction(
                            PainterIcon(R.drawable.baseline_unarchive_24, "Unarchive ${player.name}")
                    ) { listener(ArchivedPlayersIntent.PlayerUnarchived(player)) },
                    SelectedItemAction(
                            VectorIcon(Icons.Default.Close, "Delete ${player.name}")
                    ) { listener(ConfirmDialogIntent.Open(player).toArchivedPlayersIntent()) },
            )

            SelectableListItem(
                    contentDescription = player.name,
                    onClick = null,
                    actions = buttons.map {
                        CustomAccessibilityAction(
                                label = it.icon.contentDescription!!,
                                action = { it.onClick(); true },
                        )
                    }
            ) {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 15.dp)
                ) {
                    Text(
                            text = player.name,
                            style = Typography.h4,
                            modifier = Modifier.weight(1f)
                    )
                    buttons.forEach {
                        IconButton(onClick = it.onClick) {
                            it.icon.ClavaIcon()
                        }
                    }
                }
            }
        }
    }
}