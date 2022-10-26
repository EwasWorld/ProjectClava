package com.eywa.projectclava.main.mainActivity.screens.manage

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.eywa.projectclava.R
import com.eywa.projectclava.main.common.asTimeString
import com.eywa.projectclava.main.common.generateCourts
import com.eywa.projectclava.main.common.generateMatches
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.model.*
import java.util.*

// TODO Sort by number (plus on court-picking dialogs)
// TODO Court picking dialogs: no courts available
@Composable
fun SetupCourtsScreen(
        databaseState: DatabaseState,
        getTimeRemaining: Match.() -> TimeRemaining?,
        prependCourt: Boolean = true,
        itemAddedListener: (String) -> Unit,
        itemNameEditedListener: (Court, String) -> Unit,
        itemDeletedListener: (Court) -> Unit,
        toggleIsPresentListener: (Court) -> Unit,
        onTabSelectedListener: (SetupListTabSwitcherItem) -> Unit,
        navigateListener: (NavRoute) -> Unit,
) {
    val state = remember(prependCourt) { mutableStateOf(SetupListState<Court>(useTextPlaceholderAlt = prependCourt)) }

    SetupCourtsScreen(
            state = state.value,
            databaseState = databaseState,
            getTimeRemaining = getTimeRemaining,
            prependCourt = prependCourt,
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
            itemDeletedListener = { itemDeletedListener(it) },
            toggleIsPresentListener = toggleIsPresentListener,
            onTabSelectedListener = onTabSelectedListener,
            navigateListener = navigateListener,
    )
}

@Composable
fun SetupCourtsScreen(
        state: SetupListState<Court>,
        databaseState: DatabaseState,
        prependCourt: Boolean = true,
        getTimeRemaining: Match.() -> TimeRemaining?,
        addItemNameClearPressedListener: () -> Unit,
        addItemNameChangedListener: (String) -> Unit,
        itemAddedListener: (String) -> Unit,
        itemNameEditedListener: (Court, String) -> Unit,
        itemNameEditCancelledListener: () -> Unit,
        itemNameEditStartedListener: (Court) -> Unit,
        itemDeletedListener: (Court) -> Unit,
        toggleIsPresentListener: (Court) -> Unit,
        onTabSelectedListener: (SetupListTabSwitcherItem) -> Unit,
        navigateListener: (NavRoute) -> Unit,
) {
    SetupListScreen(
            setupListSettings = SetupListSettings.COURTS,
            setupListState = state,
            items = databaseState.courts,
            nameIsDuplicate = { newName, editItemName ->
                if (newName == editItemName) return@SetupListScreen true

                val checkName = if (prependCourt) "Court $newName" else newName
                databaseState.courts.any { it.name == checkName }
            },
            getMatch = { databaseState.matches.getLatestMatchForCourt(it) },
            getTimeRemaining = getTimeRemaining,
            addItemNameClearPressedListener = addItemNameClearPressedListener,
            addItemNameChangedListener = addItemNameChangedListener,
            itemAddedListener = itemAddedListener,
            itemNameEditedListener = itemNameEditedListener,
            itemNameEditCancelledListener = itemNameEditCancelledListener,
            itemNameEditStartedListener = itemNameEditStartedListener,
            itemDeletedListener = { itemDeletedListener(it) },
            itemClickedListener = toggleIsPresentListener,
            hasExtraContent = { databaseState.matches.getLatestMatchForCourt(it) != null },
            extraContent = {
                ExtraContent(
                        match = databaseState.matches.getLatestMatchForCourt(it)!!,
                        getTimeRemaining = getTimeRemaining
                )
            },
            onTabSelectedListener = onTabSelectedListener,
            navigateListener = navigateListener,
    )
}

@Composable
fun RowScope.ExtraContent(match: Match, getTimeRemaining: Match.() -> TimeRemaining?) {
    if (match.court == null) return

    Text(
            text = match.players.joinToString { it.name },
            modifier = Modifier.weight(1f)
    )
    Text(
            text = match.getTimeRemaining().asTimeString()
    )
    if (match.isPaused) {
        Icon(
                painter = painterResource(id = R.drawable.baseline_pause_24),
                contentDescription = "Match paused"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SetupCourtsScreen_Preview() {
    val currentTime = Calendar.getInstance(Locale.getDefault())
    SetupCourtsScreen(
            state = SetupListState(),
            databaseState = DatabaseState(
                    courts = generateCourts(10),
                    matches = generateMatches(5, currentTime),
            ),
            getTimeRemaining = { state.getTimeLeft(currentTime) },
            addItemNameClearPressedListener = {},
            addItemNameChangedListener = {},
            itemAddedListener = {},
            itemNameEditedListener = { _, _ -> },
            itemNameEditCancelledListener = {},
            itemNameEditStartedListener = {},
            itemDeletedListener = {},
            toggleIsPresentListener = {},
            onTabSelectedListener = {},
            navigateListener = {},
    )
}