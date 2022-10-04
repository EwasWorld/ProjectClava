package com.eywa.projectclava.main.ui.mainScreens

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.Player
import com.eywa.projectclava.main.ui.sharedUi.SetupListScreen
import kotlinx.coroutines.delay
import java.util.*

@Composable
fun SetupPlayersScreen(
        items: Iterable<Player>?,
        matches: Iterable<Match>?,
        itemAddedListener: (String) -> Unit,
        itemNameEditedListener: (Player, String) -> Unit,
        itemDeletedListener: (Player) -> Unit,
        toggleIsPresentListener: (Player) -> Unit,
) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = Calendar.getInstance()
        }
    }

    val newItemName = rememberSaveable { mutableStateOf("") }
    var isEditDialogShown: Player? by remember { mutableStateOf(null) }

    SetupPlayersScreen(
            currentTime = currentTime,
            items = items,
            matches = matches,
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
            toggleIsPresentListener = toggleIsPresentListener,
    )
}

@Composable
fun SetupPlayersScreen(
        currentTime: Calendar,
        items: Iterable<Player>?,
        matches: Iterable<Match>?,
        addItemName: String,
        addItemNameChangedListener: (String) -> Unit,
        itemAddedListener: (String) -> Unit,
        editDialogOpenFor: Player?,
        itemNameEditedListener: (Player, String) -> Unit,
        itemNameEditCancelledListener: () -> Unit,
        itemNameEditStartedListener: (Player) -> Unit,
        itemDeletedListener: (Player) -> Unit,
        toggleIsPresentListener: (Player) -> Unit,
) {
    SetupListScreen(
            currentTime = currentTime,
            typeContentDescription = "player",
            items = items,
            getMatchState = { player ->
                matches
                        ?.filter { it.isCurrent(currentTime) && it.players.contains(player) }
                        ?.maxOfOrNull { it.state }
            },
            addItemName = addItemName,
            addItemNameChangedListener = addItemNameChangedListener,
            itemAddedListener = itemAddedListener,
            editDialogOpenFor = editDialogOpenFor,
            itemNameEditedListener = itemNameEditedListener,
            itemNameEditCancelledListener = itemNameEditCancelledListener,
            itemNameEditStartedListener = itemNameEditStartedListener,
            itemDeletedListener = { itemDeletedListener(it) },
            itemClickedListener = toggleIsPresentListener,
    )
}