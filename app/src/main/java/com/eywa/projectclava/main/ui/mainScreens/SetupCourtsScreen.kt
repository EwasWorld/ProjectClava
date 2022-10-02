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
import com.eywa.projectclava.main.model.Court
import com.eywa.projectclava.main.ui.sharedUi.SetupListScreen
import kotlinx.coroutines.delay
import java.util.*


@Composable
fun SetupCourtsScreen(
        listItems: Iterable<Court>?,
        itemAddedListener: (String) -> Unit,
        itemNameEditedListener: (Court, String) -> Unit,
        itemDeletedListener: (Court) -> Unit,
        itemClickedListener: (Court) -> Unit,
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
fun SetupCourtsScreen(
        currentTime: Calendar,
        items: Iterable<Court>?,
        addItemName: String,
        addItemNameChangedListener: (String) -> Unit,
        itemAddedListener: (String) -> Unit,
        editDialogOpenFor: Court?,
        itemNameEditedListener: (Court, String) -> Unit,
        itemNameEditCancelledListener: () -> Unit,
        itemNameEditStartedListener: (Court) -> Unit,
        itemDeletedListener: (Court) -> Unit,
        itemClickedListener: (Court) -> Unit,
) {
    SetupListScreen(
            currentTime = currentTime,
            typeContentDescription = "court",
            items = items,
            getMatchState = { it.currentMatch?.state },
            addItemName = addItemName,
            addItemNameChangedListener = addItemNameChangedListener,
            itemAddedListener = itemAddedListener,
            editDialogOpenFor = editDialogOpenFor,
            itemNameEditedListener = itemNameEditedListener,
            itemNameEditCancelledListener = itemNameEditCancelledListener,
            itemNameEditStartedListener = itemNameEditStartedListener,
            itemDeletedListener = { itemDeletedListener(it) },
            itemClickedListener = itemClickedListener,
            hasExtraContent = { it.canBeUsed && it.currentMatch != null },
            extraContent = {
                ExtraContent(currentTime = currentTime, court = it)
            }
    )
}

@Composable
fun RowScope.ExtraContent(currentTime: Calendar, court: Court) {
    if (!court.canBeUsed || court.currentMatch == null) return

    Text(
            text = court.currentMatch.players.joinToString { it.name },
            modifier = Modifier.weight(1f)
    )
    Text(
            text = court.currentMatch.state.getTimeLeft(currentTime).asString()
    )
    if (court.currentMatch.isPaused) {
        Icon(
                painter = painterResource(id = R.drawable.baseline_pause_24),
                contentDescription = "Match paused"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SetupCourtsScreen_Preview() {
    SetupCourtsScreen(
            currentTime = Calendar.getInstance(),
            items = generateCourts(3, 7),
            addItemName = "",
            addItemNameChangedListener = {},
            itemAddedListener = {},
            editDialogOpenFor = null,
            itemNameEditedListener = { _, _ -> },
            itemNameEditCancelledListener = {},
            itemNameEditStartedListener = {},
            itemDeletedListener = {},
            itemClickedListener = {},
    )
}