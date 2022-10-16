package com.eywa.projectclava.main.ui.sharedUi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.main.NavRoute
import com.eywa.projectclava.main.common.generateCourts
import com.eywa.projectclava.main.common.generateMatches
import com.eywa.projectclava.main.common.generatePlayers
import com.eywa.projectclava.main.model.MatchState
import com.eywa.projectclava.main.model.Player
import com.eywa.projectclava.main.ui.mainScreens.SetupCourtsScreen
import com.eywa.projectclava.ui.theme.ClavaColor
import com.eywa.projectclava.ui.theme.Typography
import java.util.*

interface SetupListItem {
    val name: String
    val enabled: Boolean
}

enum class SetupListTabSwitcherItem(
        override val label: String,
        override val destination: NavRoute,
) : TabSwitcherItem {
    PLAYERS("Players", NavRoute.ADD_PLAYER),
    COURTS("Courts", NavRoute.ADD_COURT),
}

fun <T : SetupListItem> String.isDuplicate(
        existingItems: Iterable<T>?,
        itemBeingEdited: T? = null,
) = this != itemBeingEdited?.name && existingItems?.any { it.name == this } == true

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
        selectedTab: SetupListTabSwitcherItem,
        onTabSelectedListener: (SetupListTabSwitcherItem) -> Unit,
        extraContent: @Composable RowScope.(T) -> Unit = {},
) {
    val newItemName = rememberSaveable { mutableStateOf("") }
    var editDialogOpenFor: T? by remember { mutableStateOf(null) }
    val addFieldTouched = rememberSaveable { mutableStateOf(false) }

    SetupListScreen(
            typeContentDescription = typeContentDescription,
            currentTime = currentTime,
            items = items,
            getMatchState = getMatchState,
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
            itemDeletedListener = itemDeletedListener,
            itemClickedListener = itemClickedListener,
            hasExtraContent = hasExtraContent,
            selectedTab = selectedTab,
            onTabSelectedListener = onTabSelectedListener,
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
        showAddItemBlankError: Boolean,
        addItemNameClearPressedListener: () -> Unit,
        addItemNameChangedListener: (String) -> Unit,
        itemAddedListener: (String) -> Unit,
        editDialogOpenFor: T?,
        itemNameEditedListener: (T, String) -> Unit,
        itemNameEditCancelledListener: () -> Unit,
        itemNameEditStartedListener: (T) -> Unit,
        itemDeletedListener: (T) -> Unit,
        itemClickedListener: (T) -> Unit,
        hasExtraContent: (T) -> Boolean = { false },
        selectedTab: SetupListTabSwitcherItem,
        onTabSelectedListener: (SetupListTabSwitcherItem) -> Unit,
        extraContent: @Composable RowScope.(T) -> Unit = {},
) {
    // TODO Add a search FAB
    // TODO Add an are you sure to deletion
    val focusManager = LocalFocusManager.current

    EditDialog(
            typeContentDescription = typeContentDescription,
            items = items,
            editDialogOpenFor = editDialogOpenFor,
            itemEditedListener = itemNameEditedListener,
            itemEditCancelledListener = itemNameEditCancelledListener,
    )

    ClavaScreen(
            noContentText = "No ${typeContentDescription}s to show",
            hasContent = items?.any() ?: false,
            footerContent = {
                SetupListScreenFooter(
                        typeContentDescription = typeContentDescription,
                        items = items,
                        addItemName = addItemName,
                        showAddItemBlankError = showAddItemBlankError,
                        addItemNameClearPressedListener = addItemNameClearPressedListener,
                        addItemNameChangedListener = addItemNameChangedListener,
                        itemAddedListener = itemAddedListener
                )
            },
            aboveListContent = {
                TabSwitcher(
                        items = SetupListTabSwitcherItem.values().toList(),
                        selectedItem = selectedTab,
                        onItemClicked = onTabSelectedListener,
                        modifier = Modifier.padding(20.dp)
                )
            }
    ) {
        items(items!!.sortedBy { it.name }) { item ->
            SelectableListItem(
                    currentTime = currentTime,
                    enabled = item.enabled,
                    matchState = getMatchState(item),
            ) {
                Column(
                        modifier = Modifier.clickable {
                            itemClickedListener(item)
                            focusManager.clearFocus()
                        }
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
                                onClick = {
                                    itemNameEditStartedListener(item)
                                    focusManager.clearFocus()
                                }
                        ) {
                            Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit ${item.name}"
                            )
                        }
                        IconButton(
                                onClick = {
                                    itemDeletedListener(item)
                                    focusManager.clearFocus()
                                }
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
}

@Composable
private fun <T : SetupListItem> SetupListScreenFooter(
        typeContentDescription: String,
        items: Iterable<T>?,
        addItemName: String,
        showAddItemBlankError: Boolean,
        addItemNameClearPressedListener: () -> Unit,
        addItemNameChangedListener: (String) -> Unit,
        itemAddedListener: (String) -> Unit,
) {
    Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                    .background(ClavaColor.HeaderFooterBackground)
                    .padding(20.dp)
    ) {
        val onAddPressed = { itemAddedListener(addItemName) }

        ListItemNameTextField(
                typeContentDescription = typeContentDescription,
                existingItems = items,
                proposedItemName = addItemName,
                showBlankError = showAddItemBlankError,
                onValueChangedListener = addItemNameChangedListener,
                onClearPressedListener = addItemNameClearPressedListener,
                onDoneListener = onAddPressed,
                modifier = Modifier.weight(1f),
        )
        Surface(
                shape = RoundedCornerShape(100),
                color = MaterialTheme.colors.primary,
                modifier = Modifier.padding(start = 10.dp)
        ) {
            IconButton(
                    enabled = !addItemName.isDuplicate(items) && addItemName.isNotBlank(),
                    onClick = onAddPressed,
            ) {
                Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add"
                )
            }
        }
    }
}

@Composable
fun <T : SetupListItem> ListItemNameTextField(
        typeContentDescription: String,
        existingItems: Iterable<T>?,
        proposedItemName: String,
        showBlankError: Boolean,
        onValueChangedListener: (String) -> Unit,
        onClearPressedListener: () -> Unit,
        onDoneListener: () -> Unit,
        modifier: Modifier = Modifier,
        itemBeingEdited: T? = null,
) {
    val isDuplicate = proposedItemName.isDuplicate(existingItems, itemBeingEdited)
    val errorMessage = when {
        isDuplicate -> "A person with already exists"
        showBlankError && proposedItemName.isBlank() -> "Cannot be empty"
        else -> null
    }
    val label: @Composable () -> Unit = { Text("Add $typeContentDescription") }

    Column(
            verticalArrangement = Arrangement.spacedBy(3.dp),
            modifier = modifier
    ) {
        OutlinedTextField(
                value = proposedItemName,
                onValueChange = onValueChangedListener,
                label = label.takeIf { itemBeingEdited == null },
                placeholder = { Text("John Doe") },
                trailingIcon = {
                    IconButton(onClick = onClearPressedListener) {
                        Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear"
                        )
                    }
                },
                isError = isDuplicate,
                keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Words,
                ),
                keyboardActions = KeyboardActions(onDone = { onDoneListener() }),
        )
        errorMessage?.let {
            Text(
                    text = errorMessage,
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
    val editName = rememberSaveable(editDialogOpenFor) { mutableStateOf(editDialogOpenFor?.name ?: "") }
    val fieldTouched = rememberSaveable(editDialogOpenFor) { mutableStateOf(false) }
    val isDuplicate = editName.value.isDuplicate(items, editDialogOpenFor)
    val okListener = { itemEditedListener(editDialogOpenFor!!, editName.value) }

    ClavaDialog(
            isShown = editDialogOpenFor != null,
            title = "Edit ${editDialogOpenFor?.name}",
            okButtonText = "Edit",
            okButtonEnabled = !isDuplicate && editName.value.isNotBlank(),
            onCancelListener = itemEditCancelledListener,
            onOkListener = okListener
    ) {
        ListItemNameTextField(
                typeContentDescription = typeContentDescription,
                existingItems = items,
                proposedItemName = editName.value,
                onValueChangedListener = {
                    fieldTouched.value = true
                    editName.value = it
                },
                onClearPressedListener = {
                    fieldTouched.value = false
                    editName.value = ""
                },
                showBlankError = fieldTouched.value,
                onDoneListener = okListener,
                itemBeingEdited = editDialogOpenFor,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SetupListScreen_Preview() {
    val playersToGenerate = 15
    val currentTime = Calendar.getInstance(Locale.getDefault())
    val players = generatePlayers(playersToGenerate)
    val courts = generateCourts(6)
    val states = listOf(
            MatchState.OnCourt(currentTime.apply { add(Calendar.HOUR_OF_DAY, -1) }, courts[0]), // Disabled
            MatchState.OnCourt(currentTime.apply { add(Calendar.HOUR_OF_DAY, -1) }, courts[1]),
            null,
            null, // Disabled
            MatchState.OnCourt(currentTime.apply { add(Calendar.MINUTE, 1) }, courts[2]),
            MatchState.Paused(5, currentTime)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        SetupListScreen(
                typeContentDescription = "player",
                currentTime = currentTime,
                addItemName = "",
                addItemNameChangedListener = {},
                addItemNameClearPressedListener = {},
                showAddItemBlankError = false,
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
                selectedTab = SetupListTabSwitcherItem.PLAYERS,
                onTabSelectedListener = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExtraInfo_SetupListScreen_Preview() {
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
            selectedTab = SetupListTabSwitcherItem.PLAYERS,
            onTabSelectedListener = {},
    )
}

@Preview(showBackground = true)
@Composable
fun Dialog_SetupListScreen_Preview() {
    val players = generatePlayers(20)
    Box(modifier = Modifier.fillMaxSize()) {
        SetupListScreen(
                currentTime = Calendar.getInstance(Locale.getDefault()),
                typeContentDescription = "player",
                addItemName = "",
                addItemNameChangedListener = {},
                addItemNameClearPressedListener = {},
                showAddItemBlankError = false,
                items = players,
                getMatchState = { null },
                editDialogOpenFor = players[2],
                itemNameEditedListener = { _, _ -> },
                itemNameEditCancelledListener = {},
                itemClickedListener = {},
                itemAddedListener = {},
                itemNameEditStartedListener = {},
                itemDeletedListener = {},
                selectedTab = SetupListTabSwitcherItem.PLAYERS,
                onTabSelectedListener = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Error_SetupListScreen_Preview() {
    val generatePlayers = generatePlayers(5)
    Box(modifier = Modifier.fillMaxSize()) {
        SetupListScreen(
                currentTime = Calendar.getInstance(Locale.getDefault()),
                typeContentDescription = "player",
                addItemName = generatePlayers.first().name,
                addItemNameChangedListener = {},
                addItemNameClearPressedListener = {},
                showAddItemBlankError = false,
                items = generatePlayers,
                getMatchState = { null },
                editDialogOpenFor = null,
                itemNameEditedListener = { _, _ -> },
                itemNameEditCancelledListener = {},
                itemClickedListener = {},
                itemAddedListener = {},
                itemNameEditStartedListener = {},
                itemDeletedListener = {},
                selectedTab = SetupListTabSwitcherItem.PLAYERS,
                onTabSelectedListener = {},
        )
    }
}