package com.eywa.projectclava.main.ui.mainScreens

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import com.eywa.projectclava.main.model.Player
import com.eywa.projectclava.main.ui.sharedUi.SetupListScreen

@Composable
fun SetupPlayersScreen(
        listItems: Map<Player, Color?>,
        itemAddedListener: (String) -> Unit,
        itemNameEditedListener: (Player, String) -> Unit,
        itemDeletedListener: (Player) -> Unit,
        itemClickedListener: (Player) -> Unit,
) {
    val newItemName = rememberSaveable { mutableStateOf("") }
    var isEditDialogShown: Player? by remember { mutableStateOf(null) }

    SetupPlayersScreen(
            items = listItems,
            addItemName = newItemName.value,
            addItemNameChangedListener = { newItemName.value = it },
            itemAddedListener = itemAddedListener,
            editDialogOpenFor = isEditDialogShown,
            itemNameEditedListener = { item, newName ->
                isEditDialogShown = null
                itemNameEditedListener(item, newName)
            },
            itemNameEditCancelledListener = { isEditDialogShown = null },
            itemNameEditStartedListener = { isEditDialogShown = it },
            itemDeletedListener = { itemDeletedListener(it) },
            itemClickedListener = itemClickedListener,
    )
}

@Composable
fun SetupPlayersScreen(
        items: Map<Player, Color?>,
        addItemName: String,
        addItemNameChangedListener: (String) -> Unit,
        itemAddedListener: (String) -> Unit,
        editDialogOpenFor: Player?,
        itemNameEditedListener: (Player, String) -> Unit,
        itemNameEditCancelledListener: () -> Unit,
        itemNameEditStartedListener: (Player) -> Unit,
        itemDeletedListener: (Player) -> Unit,
        itemClickedListener: (Player) -> Unit,
) {
    SetupListScreen(
            typeContentDescription = "player",
            items = items,
            addItemName = addItemName,
            addItemNameChangedListener = addItemNameChangedListener,
            itemAddedListener = itemAddedListener,
            editDialogOpenFor = editDialogOpenFor,
            itemNameEditedListener = itemNameEditedListener,
            itemNameEditCancelledListener = itemNameEditCancelledListener,
            itemNameEditStartedListener = itemNameEditStartedListener,
            itemDeletedListener = { itemDeletedListener(it) },
            itemClickedListener = itemClickedListener,
    )
}