package com.eywa.projectclava.main.mainActivity.screens.manage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.model.*

@Composable
fun SetupPlayersScreen(
        databaseState: DatabaseState,
        getTimeRemaining: Match.() -> TimeRemaining?,
        itemAddedListener: (String) -> Unit,
        itemNameEditedListener: (Player, String) -> Unit,
        itemArchivedListener: (Player) -> Unit,
        toggleIsPresentListener: (Player) -> Unit,
        onTabSelectedListener: (SetupListTabSwitcherItem) -> Unit,
        navigateListener: (NavRoute) -> Unit,
) {
    val state = remember { mutableStateOf(SetupListState<Player>()) }

    SetupPlayersScreen(
            state = state.value,
            databaseState = databaseState,
            getTimeRemaining = getTimeRemaining,
            addItemNameClearPressedListener = {
                state.value = state.value.copy(
                        addItemName = "",
                        addItemIsDirty = false,
                )
            },
            addItemNameChangedListener = {
                state.value = state.value.copy(
                        addItemName = it,
                        addItemIsDirty = true,
                )
            },
            itemAddedListener = {
                itemAddedListener(it)
                state.value = state.value.copy(
                        addItemName = "",
                        addItemIsDirty = false,
                )
            },
            itemNameEditedListener = { item, newName ->
                state.value = state.value.copy(editDialogOpenFor = null)
                itemNameEditedListener(item, newName)
            },
            itemNameEditCancelledListener = {
                state.value = state.value.copy(editDialogOpenFor = null)
            },
            itemNameEditStartedListener = {
                state.value = state.value.copy(editDialogOpenFor = it)
            },
            itemArchivedListener = { itemArchivedListener(it) },
            toggleIsPresentListener = toggleIsPresentListener,
            onTabSelectedListener = onTabSelectedListener,
            navigateListener = navigateListener,
    )
}

@Composable
fun SetupPlayersScreen(
        state: SetupListState<Player>,
        databaseState: DatabaseState,
        getTimeRemaining: Match.() -> TimeRemaining?,
        addItemNameClearPressedListener: () -> Unit,
        addItemNameChangedListener: (String) -> Unit,
        itemAddedListener: (String) -> Unit,
        itemNameEditedListener: (Player, String) -> Unit,
        itemNameEditCancelledListener: () -> Unit,
        itemNameEditStartedListener: (Player) -> Unit,
        itemArchivedListener: (Player) -> Unit,
        toggleIsPresentListener: (Player) -> Unit,
        onTabSelectedListener: (SetupListTabSwitcherItem) -> Unit,
        navigateListener: (NavRoute) -> Unit,
) {
    SetupListScreen(
            setupListSettings = SetupListSettings.PLAYERS,
            setupListState = state,
            items = databaseState.players.filter { !it.isArchived },
            getMatch = { player ->
                databaseState.matches
                        .filter { match -> match.players.any { player.name == it.name } }
                        .getPlayerColouringMatch()
            },
            nameIsDuplicate = { newName, editItemName ->
                newName != editItemName && databaseState.players.any { it.name == newName }
            },
            getTimeRemaining = getTimeRemaining,
            addItemNameClearPressedListener = addItemNameClearPressedListener,
            addItemNameChangedListener = addItemNameChangedListener,
            itemAddedListener = itemAddedListener,
            itemNameEditedListener = itemNameEditedListener,
            itemNameEditCancelledListener = itemNameEditCancelledListener,
            itemNameEditStartedListener = itemNameEditStartedListener,
            itemDeletedListener = { itemArchivedListener(it) },
            itemClickedListener = toggleIsPresentListener,
            onTabSelectedListener = onTabSelectedListener,
            navigateListener = navigateListener,
    )
}