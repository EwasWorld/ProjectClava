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
import com.eywa.projectclava.main.common.asString
import com.eywa.projectclava.main.common.generateCourts
import com.eywa.projectclava.main.common.generateMatches
import com.eywa.projectclava.main.model.Court
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.ui.sharedUi.SetupListScreen
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
) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = Calendar.getInstance()
        }
    }

    val newItemName = rememberSaveable { mutableStateOf("") }
    var isEditDialogShown: Court? by remember { mutableStateOf(null) }

    SetupCourtsScreen(
            currentTime = currentTime,
            matches = matches,
            courts = courts,
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
fun SetupCourtsScreen(
        currentTime: Calendar,
        matches: Iterable<Match>?,
        courts: Iterable<Court>?,
        addItemName: String,
        addItemNameChangedListener: (String) -> Unit,
        itemAddedListener: (String) -> Unit,
        editDialogOpenFor: Court?,
        itemNameEditedListener: (Court, String) -> Unit,
        itemNameEditCancelledListener: () -> Unit,
        itemNameEditStartedListener: (Court) -> Unit,
        itemDeletedListener: (Court) -> Unit,
        toggleIsPresentListener: (Court) -> Unit,
) {
    SetupListScreen(
            currentTime = currentTime,
            typeContentDescription = "court",
            items = courts,
            getMatchState = { matches?.findCourt(it)?.state },
            addItemName = addItemName,
            addItemNameChangedListener = addItemNameChangedListener,
            itemAddedListener = itemAddedListener,
            editDialogOpenFor = editDialogOpenFor,
            itemNameEditedListener = itemNameEditedListener,
            itemNameEditCancelledListener = itemNameEditCancelledListener,
            itemNameEditStartedListener = itemNameEditStartedListener,
            itemDeletedListener = { itemDeletedListener(it) },
            itemClickedListener = toggleIsPresentListener,
            hasExtraContent = { it.canBeUsed && matches?.findCourt(it)?.isCurrent(currentTime) == true },
            extraContent = {
                ExtraContent(currentTime = currentTime, match = matches?.findCourt(it)!!)
            }
    )
}

fun Iterable<Match>.findCourt(court: Court) = find { it.court?.name == court.name }

@Composable
fun RowScope.ExtraContent(currentTime: Calendar, match: Match) {
    if (match.court == null || match.court?.canBeUsed == false) return

    Text(
            text = match.players.joinToString { it.name },
            modifier = Modifier.weight(1f)
    )
    Text(
            text = match.state.getTimeLeft(currentTime).asString()
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
    val currentTime = Calendar.getInstance()
    SetupCourtsScreen(
            currentTime = currentTime,
            courts = generateCourts(10),
            matches = generateMatches(5, currentTime),
            addItemName = "",
            addItemNameChangedListener = {},
            itemAddedListener = {},
            editDialogOpenFor = null,
            itemNameEditedListener = { _, _ -> },
            itemNameEditCancelledListener = {},
            itemNameEditStartedListener = {},
            itemDeletedListener = {},
            toggleIsPresentListener = {},
    )
}