package com.eywa.projectclava.main.ui.mainScreens

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.Player
import com.eywa.projectclava.main.model.getLatestMatchForPlayer
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
    var currentTime by remember { mutableStateOf(Calendar.getInstance(Locale.getDefault())) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = Calendar.getInstance(Locale.getDefault())
        }
    }

    val newItemName = rememberSaveable { mutableStateOf("") }
    var editDialogOpenFor: Player? by remember { mutableStateOf(null) }
    val addFieldTouched = rememberSaveable { mutableStateOf(false) }

    SetupPlayersScreen(
            currentTime = currentTime,
            items = items,
            matches = matches,
            addItemName = newItemName.value,
            showAddItemBlankError = addFieldTouched.value,
            addItemNameClearPressedListener = {
                newItemName.value = ""
                addFieldTouched.value = false
            },
            addItemNameChangedListener = {
                newItemName.value = it
                addFieldTouched.value = true
            },
            itemAddedListener = {
                itemAddedListener(it)
                newItemName.value = ""
                addFieldTouched.value = false
            },
            editDialogOpenFor = editDialogOpenFor,
            itemNameEditedListener = { item, newName ->
                editDialogOpenFor = null
                itemNameEditedListener(item, newName)
            },
            itemNameEditCancelledListener = { editDialogOpenFor = null },
            itemNameEditStartedListener = { editDialogOpenFor = it },
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
        showAddItemBlankError: Boolean,
        addItemNameClearPressedListener: () -> Unit,
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
            getMatchState = { matches?.getLatestMatchForPlayer(it)?.state },
            addItemName = addItemName,
            showAddItemBlankError = showAddItemBlankError,
            addItemNameClearPressedListener = addItemNameClearPressedListener,
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