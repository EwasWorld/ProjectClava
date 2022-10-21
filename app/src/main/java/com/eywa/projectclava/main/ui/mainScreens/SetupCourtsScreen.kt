package com.eywa.projectclava.main.ui.mainScreens

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.eywa.projectclava.R
import com.eywa.projectclava.main.common.asTimeString
import com.eywa.projectclava.main.common.generateCourts
import com.eywa.projectclava.main.common.generateMatches
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.model.*
import com.eywa.projectclava.main.ui.sharedUi.SetupListScreen
import com.eywa.projectclava.main.ui.sharedUi.SetupListTabSwitcherItem
import java.util.*


@Composable
fun SetupCourtsScreen(
        courts: Iterable<Court>?,
        matches: Iterable<Match>?,
        getTimeRemaining: Match.() -> TimeRemaining?,
        itemAddedListener: (String) -> Unit,
        itemNameEditedListener: (Court, String) -> Unit,
        itemDeletedListener: (Court) -> Unit,
        toggleIsPresentListener: (Court) -> Unit,
        onTabSelectedListener: (SetupListTabSwitcherItem) -> Unit,
        missingContentNextStep: Iterable<MissingContentNextStep>?,
        navigateListener: (NavRoute) -> Unit,
) {
    val newItemName = rememberSaveable { mutableStateOf("") }
    var editDialogOpenFor: Court? by remember { mutableStateOf(null) }
    val addFieldTouched = rememberSaveable { mutableStateOf(false) }

    SetupCourtsScreen(
            matches = matches,
            getTimeRemaining = getTimeRemaining,
            courts = courts,
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
fun SetupCourtsScreen(
        matches: Iterable<Match>?,
        getTimeRemaining: Match.() -> TimeRemaining?,
        courts: Iterable<Court>?,
        addItemName: String,
        showAddItemBlankError: Boolean,
        addItemNameClearPressedListener: () -> Unit,
        addItemNameChangedListener: (String) -> Unit,
        itemAddedListener: (String) -> Unit,
        editDialogOpenFor: Court?,
        itemNameEditedListener: (Court, String) -> Unit,
        itemNameEditCancelledListener: () -> Unit,
        itemNameEditStartedListener: (Court) -> Unit,
        itemDeletedListener: (Court) -> Unit,
        toggleIsPresentListener: (Court) -> Unit,
        onTabSelectedListener: (SetupListTabSwitcherItem) -> Unit,
        missingContentNextStep: Iterable<MissingContentNextStep>?,
        navigateListener: (NavRoute) -> Unit,
) {
    // TODO Name already exists doesn't work properly due to court prefix
    SetupListScreen(
            typeContentDescription = "court",
            textPlaceholder = "1",
            items = courts,
            getMatch = { matches?.getLatestMatchForCourt(it) },
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
            hasExtraContent = { matches?.getLatestMatchForCourt(it) != null },
            extraContent = {
                ExtraContent(match = matches?.getLatestMatchForCourt(it)!!, getTimeRemaining = getTimeRemaining)
            },
            selectedTab = SetupListTabSwitcherItem.COURTS,
            onTabSelectedListener = onTabSelectedListener,
            missingContentNextStep = missingContentNextStep?.find { it == MissingContentNextStep.ADD_COURTS },
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
            courts = generateCourts(10),
            matches = generateMatches(5, currentTime),
            getTimeRemaining = { state.getTimeLeft(currentTime) },
            addItemName = "",
            addItemNameClearPressedListener = {},
            showAddItemBlankError = false,
            addItemNameChangedListener = {},
            itemAddedListener = {},
            editDialogOpenFor = null,
            itemNameEditedListener = { _, _ -> },
            itemNameEditCancelledListener = {},
            itemNameEditStartedListener = {},
            itemDeletedListener = {},
            toggleIsPresentListener = {},
            onTabSelectedListener = {},
            missingContentNextStep = null,
            navigateListener = {},
    )
}