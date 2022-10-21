package com.eywa.projectclava.main.ui.mainScreens

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.model.*
import com.eywa.projectclava.main.ui.sharedUi.SetupListScreen
import com.eywa.projectclava.main.ui.sharedUi.SetupListTabSwitcherItem

@Composable
fun SetupPlayersScreen(
        items: Iterable<Player>?,
        matches: Iterable<Match>?,
        getTimeRemaining: Match.() -> TimeRemaining?,
        itemAddedListener: (String) -> Unit,
        itemNameEditedListener: (Player, String) -> Unit,
        itemDeletedListener: (Player) -> Unit,
        toggleIsPresentListener: (Player) -> Unit,
        onTabSelectedListener: (SetupListTabSwitcherItem) -> Unit,
        missingContentNextStep: Iterable<MissingContentNextStep>?,
        navigateListener: (NavRoute) -> Unit,
) {
    val newItemName = rememberSaveable { mutableStateOf("") }
    var editDialogOpenFor: Player? by remember { mutableStateOf(null) }
    val addFieldTouched = rememberSaveable { mutableStateOf(false) }

    SetupPlayersScreen(
            items = items,
            matches = matches,
            getTimeRemaining = getTimeRemaining,
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
            onTabSelectedListener = onTabSelectedListener,
            missingContentNextStep = missingContentNextStep,
            navigateListener = navigateListener,
    )
}

@Composable
fun SetupPlayersScreen(
        items: Iterable<Player>?,
        matches: Iterable<Match>?,
        getTimeRemaining: Match.() -> TimeRemaining?,
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
        onTabSelectedListener: (SetupListTabSwitcherItem) -> Unit,
        missingContentNextStep: Iterable<MissingContentNextStep>?,
        navigateListener: (NavRoute) -> Unit,
) {
    SetupListScreen(
            typeContentDescription = "player",
            textPlaceholder = "John Doe",
            items = items,
            getMatch = { player ->
                matches
                        ?.filter { match -> match.players.any { player.name == it.name } }
                        ?.getPlayerColouringMatch()
            },
            getTimeRemaining = getTimeRemaining,
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
            selectedTab = SetupListTabSwitcherItem.PLAYERS,
            onTabSelectedListener = onTabSelectedListener,
            missingContentNextStep = missingContentNextStep?.find { it == MissingContentNextStep.ADD_PLAYERS },
            navigateListener = navigateListener,
    )
}