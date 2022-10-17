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
import com.eywa.projectclava.main.model.Court
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.getLatestMatchForCourt
import com.eywa.projectclava.main.ui.sharedUi.SetupListScreen
import com.eywa.projectclava.main.ui.sharedUi.SetupListTabSwitcherItem
import kotlinx.coroutines.delay
import java.util.*


@Composable
fun SetupCourtsScreen(
        courts: Iterable<Court>?,
        matches: Iterable<Match>?,
        itemAddedListener: (String) -> Unit,
        itemNameEditedListener: (Court, String) -> Unit,
        itemDeletedListener: (Court) -> Unit,
        toggleIsPresentListener: (Court) -> Unit,
        onTabSelectedListener: (SetupListTabSwitcherItem) -> Unit,
) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance(Locale.getDefault())) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = Calendar.getInstance(Locale.getDefault())
        }
    }

    val newItemName = rememberSaveable { mutableStateOf("") }
    var editDialogOpenFor: Court? by remember { mutableStateOf(null) }
    val addFieldTouched = rememberSaveable { mutableStateOf(false) }

    SetupCourtsScreen(
            currentTime = currentTime,
            matches = matches,
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
    )
}

@Composable
fun SetupCourtsScreen(
        currentTime: Calendar,
        matches: Iterable<Match>?,
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
) {
    SetupListScreen(
            currentTime = currentTime,
            typeContentDescription = "court",
            items = courts,
            getMatchState = { matches?.getLatestMatchForCourt(it)?.state },
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
                ExtraContent(currentTime = currentTime, match = matches?.getLatestMatchForCourt(it)!!)
            },
            selectedTab = SetupListTabSwitcherItem.COURTS,
            onTabSelectedListener = onTabSelectedListener,
    )
}

@Composable
fun RowScope.ExtraContent(currentTime: Calendar, match: Match) {
    if (match.court == null) return

    Text(
            text = match.players.joinToString { it.name },
            modifier = Modifier.weight(1f)
    )
    Text(
            text = match.state.getTimeLeft(currentTime).asTimeString()
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
            currentTime = currentTime,
            courts = generateCourts(10),
            matches = generateMatches(5, currentTime),
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
    )
}