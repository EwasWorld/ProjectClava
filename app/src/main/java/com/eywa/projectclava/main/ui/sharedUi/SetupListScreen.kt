package com.eywa.projectclava.main.ui.sharedUi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.main.common.generateCourts
import com.eywa.projectclava.main.common.generateMatches
import com.eywa.projectclava.main.common.generatePlayers
import com.eywa.projectclava.main.model.MatchState
import com.eywa.projectclava.main.model.Player
import com.eywa.projectclava.main.ui.mainScreens.SetupCourtsScreen
import com.eywa.projectclava.ui.theme.DividerThickness
import com.eywa.projectclava.ui.theme.Typography
import java.util.*

interface SetupListItem {
    val name: String
    val enabled: Boolean
}

@Composable
fun <T : SetupListItem> SetupListScreen(
        typeContentDescription: String,
        currentTime: Calendar,
        items: Iterable<T>?,
        getMatchState: (T) -> MatchState?,
        itemAddedListener: (String) -> Unit,
        itemNameEditedListener: (T, String) -> Unit,
        itemDeletedListener: (T) -> Unit,
        itemClickedListener: (T) -> Unit,
        hasExtraContent: (T) -> Boolean = { false },
        extraContent: @Composable RowScope.(T) -> Unit = {},
) {
    val newItemName = rememberSaveable { mutableStateOf("") }
    var isEditDialogShown: T? by remember { mutableStateOf(null) }

    SetupListScreen(
            typeContentDescription = typeContentDescription,
            currentTime = currentTime,
            items = items,
            getMatchState = getMatchState,
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
            itemDeletedListener = itemDeletedListener,
            itemClickedListener = itemClickedListener,
            hasExtraContent = hasExtraContent,
            extraContent = extraContent,
    )
}

@Composable
fun <T : SetupListItem> SetupListScreen(
        typeContentDescription: String,
        currentTime: Calendar,
        items: Iterable<T>?,
        getMatchState: (T) -> MatchState?,
        addItemName: String,
        addItemNameChangedListener: (String) -> Unit,
        itemAddedListener: (String) -> Unit,
        editDialogOpenFor: T?,
        itemNameEditedListener: (T, String) -> Unit,
        itemNameEditCancelledListener: () -> Unit,
        itemNameEditStartedListener: (T) -> Unit,
        itemDeletedListener: (T) -> Unit,
        itemClickedListener: (T) -> Unit,
        hasExtraContent: (T) -> Boolean = { false },
        extraContent: @Composable RowScope.(T) -> Unit = {},
) {
    EditDialog(
            typeContentDescription = typeContentDescription,
            items = items,
            editDialogOpenFor = editDialogOpenFor,
            itemEditedListener = itemNameEditedListener,
            itemEditCancelledListener = itemNameEditCancelledListener,
    )

    Column(modifier = Modifier.fillMaxSize()) {
        val isAddNameDuplicate = items?.any { it.name == addItemName } ?: false

        LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 20.dp),
                modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp)
        ) {
            items(items?.sortedBy { it.name } ?: listOf()) { item ->
                SelectableListItem(
                        currentTime = currentTime,
                        enabled = item.enabled,
                        matchState = getMatchState(item),
                ) {
                    Column(
                            modifier = Modifier.clickable { itemClickedListener(item) }
                    ) {
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 15.dp)
                        ) {
                            val decoration = if (item.enabled) TextDecoration.None else TextDecoration.LineThrough
                            Text(
                                    text = item.name,
                                    style = Typography.h4.copy(textDecoration = decoration),
                                    modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                    enabled = item.enabled,
                                    onClick = { itemNameEditStartedListener(item) }
                            ) {
                                Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit ${item.name}"
                                )
                            }
                            IconButton(
                                    enabled = item.enabled,
                                    onClick = { itemDeletedListener(item) }
                            ) {
                                Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete ${item.name}"
                                )
                            }
                        }
                        if (hasExtraContent(item)) {
                            Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                            .padding(horizontal = 15.dp)
                                            .padding(bottom = 10.dp)
                            ) {
                                extraContent(item)
                            }
                        }
                    }
                }
            }
        }

        Divider(thickness = DividerThickness)
        Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(20.dp)
        ) {
            ListItemNameTextField(
                    typeContentDescription = typeContentDescription,
                    existingItems = items,
                    proposedItemName = addItemName,
                    onValueChangedListener = addItemNameChangedListener,
                    modifier = Modifier.weight(1f),
            )
            Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colors.primary,
            ) {
                IconButton(
                        enabled = !isAddNameDuplicate && addItemName.isNotBlank(),
                        onClick = {
                            itemAddedListener(addItemName)
                            addItemNameChangedListener("")
                        },
                ) {
                    Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add"
                    )
                }
            }
        }
    }
}

@Composable
fun <T : SetupListItem> ListItemNameTextField(
        typeContentDescription: String,
        existingItems: Iterable<T>?,
        proposedItemName: String,
        onValueChangedListener: (String) -> Unit,
        modifier: Modifier = Modifier
) {
    val isDuplicate = existingItems?.any { it.name == proposedItemName } ?: false

    Column(
            verticalArrangement = Arrangement.spacedBy(3.dp),
            modifier = modifier
    ) {
        OutlinedTextField(
                value = proposedItemName,
                onValueChange = onValueChangedListener,
                label = {
                    Text("Add $typeContentDescription")
                },
                trailingIcon = {
                    IconButton(onClick = { onValueChangedListener("") }) {
                        Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear"
                        )
                    }
                },
                isError = isDuplicate,
        )
        if (isDuplicate) {
            Text(
                    text = "Name has already been used",
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.padding(start = 5.dp)
            )
        }
    }
}

@Composable
fun <T : SetupListItem> EditDialog(
        typeContentDescription: String,
        items: Iterable<T>?,
        editDialogOpenFor: T?,
        itemEditedListener: (T, String) -> Unit,
        itemEditCancelledListener: () -> Unit,
) {
    val editName = rememberSaveable { mutableStateOf(editDialogOpenFor?.name ?: "") }
    val isDuplicate = items?.any { it.name == editName.value } ?: false

    ClavaDialog(
            isShown = editDialogOpenFor != null,
            title = "Edit ${editDialogOpenFor?.name}",
            okButtonText = "Edit",
            okButtonEnabled = !isDuplicate,
            onCancelListener = itemEditCancelledListener,
            onOkListener = { itemEditedListener(editDialogOpenFor!!, editName.value) }
    ) {
        ListItemNameTextField(
                typeContentDescription = typeContentDescription,
                existingItems = items,
                proposedItemName = editName.value,
                onValueChangedListener = { editName.value = it },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SetupListScreen_Preview() {
    val playersToGenerate = 15
    val currentTime = Calendar.getInstance()
    val players = generatePlayers(playersToGenerate)
    val courts = generateCourts(6)
    val states = listOf(
            MatchState.InProgressOrComplete(currentTime.apply { add(Calendar.HOUR_OF_DAY, -1) }, courts[0]), // Disabled
            MatchState.InProgressOrComplete(currentTime.apply { add(Calendar.HOUR_OF_DAY, -1) }, courts[1]),
            null,
            null, // Disabled
            MatchState.InProgressOrComplete(currentTime.apply { add(Calendar.MINUTE, 1) }, courts[2]),
            MatchState.Paused(5, currentTime)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        SetupListScreen(
                typeContentDescription = "player",
                currentTime = currentTime,
                addItemName = "",
                addItemNameChangedListener = {},
                items = players.sortedBy { it.name },
                getMatchState = { player: Player ->
                    states[players.sortedBy { it.name }.indexOf(player) % states.size]
                },
                editDialogOpenFor = null,
                itemNameEditedListener = { _, _ -> },
                itemNameEditCancelledListener = {},
                itemClickedListener = {},
                itemAddedListener = {},
                itemNameEditStartedListener = {},
                itemDeletedListener = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExtraInfo_SetupListScreen_Preview() {
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
            itemClickedListener = {},
    )
}

@Preview(showBackground = true)
@Composable
fun Dialog_SetupListScreen_Preview() {
    val players = generatePlayers(20)
    Box(modifier = Modifier.fillMaxSize()) {
        SetupListScreen(
                currentTime = Calendar.getInstance(),
                typeContentDescription = "player",
                addItemName = "",
                addItemNameChangedListener = {},
                items = players,
                getMatchState = { null },
                editDialogOpenFor = players[2],
                itemNameEditedListener = { _, _ -> },
                itemNameEditCancelledListener = {},
                itemClickedListener = {},
                itemAddedListener = {},
                itemNameEditStartedListener = {},
                itemDeletedListener = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Error_SetupListScreen_Preview() {
    val generatePlayers = generatePlayers(5)
    Box(modifier = Modifier.fillMaxSize()) {
        SetupListScreen(
                currentTime = Calendar.getInstance(),
                typeContentDescription = "player",
                addItemName = generatePlayers.first().name,
                addItemNameChangedListener = {},
                items = generatePlayers,
                getMatchState = { null },
                editDialogOpenFor = null,
                itemNameEditedListener = { _, _ -> },
                itemNameEditCancelledListener = {},
                itemClickedListener = {},
                itemAddedListener = {},
                itemNameEditStartedListener = {},
                itemDeletedListener = {},
        )
    }
}