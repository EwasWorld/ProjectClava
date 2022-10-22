package com.eywa.projectclava.main.ui.sharedUi

import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.main.common.generateCourts
import com.eywa.projectclava.main.common.generateMatches
import com.eywa.projectclava.main.common.generatePlayers
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.model.*
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

@Composable
fun <T : SetupListItem> SetupListScreen(
        typeContentDescription: String,
        textPlaceholder: String,
        items: Iterable<T>?,
        getMatch: (T) -> Match?,
        getTimeRemaining: Match.() -> TimeRemaining?,
        nameIsDuplicate: (newName: String, editItemName: String?) -> Boolean,
        itemAddedListener: (String) -> Unit,
        itemNameEditedListener: (T, String) -> Unit,
        itemDeletedListener: (T) -> Unit,
        itemClickedListener: (T) -> Unit,
        hasExtraContent: (T) -> Boolean = { false },
        selectedTab: SetupListTabSwitcherItem,
        onTabSelectedListener: (SetupListTabSwitcherItem) -> Unit,
        extraContent: @Composable RowScope.(T) -> Unit = {},
        missingContentNextStep: MissingContentNextStep?,
        navigateListener: (NavRoute) -> Unit,
) {
    val newItemName = rememberSaveable { mutableStateOf("") }
    var editDialogOpenFor: T? by remember { mutableStateOf(null) }
    val addFieldTouched = rememberSaveable { mutableStateOf(false) }

    SetupListScreen(
            typeContentDescription = typeContentDescription,
            textPlaceholder = textPlaceholder,
            items = items,
            getMatch = getMatch,
            getTimeRemaining = getTimeRemaining,
            addItemName = newItemName.value,
            nameIsDuplicate = nameIsDuplicate,
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
            missingContentNextStep = missingContentNextStep,
            navigateListener = navigateListener,
    )
}

@Composable
fun <T : SetupListItem> SetupListScreen(
        typeContentDescription: String,
        textPlaceholder: String,
        items: Iterable<T>?,
        getMatch: (T) -> Match?,
        getTimeRemaining: Match.() -> TimeRemaining?,
        addItemName: String,
        nameIsDuplicate: (newName: String, editItemName: String?) -> Boolean,
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
        missingContentNextStep: MissingContentNextStep?,
        navigateListener: (NavRoute) -> Unit,
) {
    // TODO Add an are you sure to deletion
    val focusManager = LocalFocusManager.current
    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchText: String? by remember { mutableStateOf(null) }
    val noContentString = if (searchText == null) {
        "No ${typeContentDescription}s yet,\n\nType a name into the box below\nthen press enter!"
    }
    else {
        "No matches found for '$searchText'"
    }

    val itemsToShow = searchText
            .takeIf { !it.isNullOrBlank() }
            ?.let { searchTxt -> items?.filter { it.name.contains(searchTxt, ignoreCase = true) } }
            ?: items

    EditDialog(
            typeContentDescription = typeContentDescription,
            textPlaceholder = textPlaceholder,
            nameIsDuplicate = nameIsDuplicate,
            editDialogOpenFor = editDialogOpenFor,
            itemEditedListener = itemNameEditedListener,
            itemEditCancelledListener = itemNameEditCancelledListener,
    )

    ClavaScreen(
            noContentText = noContentString,
            missingContentNextStep = missingContentNextStep?.let { setOf(it) },
            showMissingContentNextStep = false,
            navigateListener = navigateListener,
            fabs = { modifier ->
                SearchFab(
                        isExpanded = isSearchExpanded,
                        textPlaceholder = textPlaceholder,
                        typeContentDescription = typeContentDescription,
                        toggleExpanded = {
                            isSearchExpanded = !isSearchExpanded
                            if (!isSearchExpanded) {
                                searchText = null
                            }
                        },
                        searchText = searchText ?: "",
                        onValueChangedListener = { searchText = it },
                        modifier = modifier
                )
            },
            footerIsVisible = !isSearchExpanded,
            footerContent = {
                SetupListScreenFooter<T>(
                        typeContentDescription = typeContentDescription,
                        textPlaceholder = textPlaceholder,
                        nameIsDuplicate = nameIsDuplicate,
                        addItemName = addItemName,
                        showAddItemBlankError = showAddItemBlankError,
                        addItemNameClearPressedListener = addItemNameClearPressedListener,
                        addItemNameChangedListener = addItemNameChangedListener,
                        itemAddedListener = itemAddedListener
                )
            },
            headerContent = {
                TabSwitcher(
                        items = SetupListTabSwitcherItem.values().toList(),
                        selectedItem = selectedTab,
                        onItemClicked = onTabSelectedListener,
                )
            }
    ) {
        if (itemsToShow?.any() != true) {
            // No search results
            item {
                Text(
                        text = "No matches found for '$searchText'",
                        style = Typography.h4,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                )
            }
        }
        else {
            items(itemsToShow.sortedBy { it.name }) { item ->
                val match = getMatch(item)
                SelectableListItem(
                        enabled = item.enabled,
                        matchState = match?.state,
                        timeRemaining = { match?.getTimeRemaining() },
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
}

@Composable
private fun <T : SetupListItem> SetupListScreenFooter(
        typeContentDescription: String,
        textPlaceholder: String,
        nameIsDuplicate: (newName: String, editItemName: String?) -> Boolean,
        addItemName: String,
        showAddItemBlankError: Boolean,
        addItemNameClearPressedListener: () -> Unit,
        addItemNameChangedListener: (String) -> Unit,
        itemAddedListener: (String) -> Unit,
) {
    val onAddPressed = { itemAddedListener(addItemName.trim()) }

    ListItemNameTextField<T>(
            typeContentDescription = typeContentDescription,
            textPlaceholder = textPlaceholder,
            nameIsDuplicate = nameIsDuplicate,
            proposedItemName = addItemName,
            showBlankError = showAddItemBlankError,
            onValueChangedListener = addItemNameChangedListener,
            onClearPressedListener = addItemNameClearPressedListener,
            onDoneListener = onAddPressed,
            textFieldModifier = Modifier.fillMaxWidth(),
            modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .padding(bottom = 5.dp)
    )
}

@Composable
fun <T : SetupListItem> ListItemNameTextField(
        typeContentDescription: String,
        textPlaceholder: String,
        nameIsDuplicate: (newName: String, editItemName: String?) -> Boolean,
        proposedItemName: String,
        showBlankError: Boolean,
        onValueChangedListener: (String) -> Unit,
        onClearPressedListener: () -> Unit,
        onDoneListener: () -> Unit,
        modifier: Modifier = Modifier,
        textFieldModifier: Modifier = Modifier,
        itemBeingEdited: T? = null,
) {
    val isDuplicate = nameIsDuplicate(proposedItemName, itemBeingEdited?.name)
    val errorMessage = when {
        isDuplicate -> "A $typeContentDescription with already exists"
        showBlankError && proposedItemName.isBlank() -> "Cannot be empty"
        else -> null
    }
    val label: @Composable () -> Unit = { Text("Add $typeContentDescription") }
    val onDone = {
        if (errorMessage == null && proposedItemName.isNotBlank()) {
            onDoneListener()
        }
        else {
            // Force dirty state
            onValueChangedListener(proposedItemName)
        }
    }

    Column(
            verticalArrangement = Arrangement.spacedBy(3.dp),
            modifier = modifier
    ) {
        // TODO Use a basic text field to get around the weird top padding?
        OutlinedTextField(
                value = proposedItemName,
                onValueChange = onValueChangedListener,
                label = label.takeIf { itemBeingEdited == null },
                placeholder = { Text(textPlaceholder) },
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
                keyboardActions = KeyboardActions(onDone = { onDone() }),
                modifier = textFieldModifier.onKeyEvent {
                    if (it.nativeKeyEvent.keyCode != KeyEvent.KEYCODE_ENTER) return@onKeyEvent false

                    onDone()
                    true
                }
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
        textPlaceholder: String,
        nameIsDuplicate: (newName: String, editItemName: String?) -> Boolean,
        editDialogOpenFor: T?,
        itemEditedListener: (T, String) -> Unit,
        itemEditCancelledListener: () -> Unit,
) {
    val editName = rememberSaveable(editDialogOpenFor) { mutableStateOf(editDialogOpenFor?.name ?: "") }
    val fieldTouched = rememberSaveable(editDialogOpenFor) { mutableStateOf(false) }
    val isDuplicate = nameIsDuplicate(editName.value, editDialogOpenFor?.name)
    val okListener = { itemEditedListener(editDialogOpenFor!!, editName.value.trim()) }

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
                textPlaceholder = textPlaceholder,
                nameIsDuplicate = nameIsDuplicate,
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

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Composable
private fun SearchFab(
        isExpanded: Boolean,
        typeContentDescription: String,
        textPlaceholder: String,
        toggleExpanded: () -> Unit,
        searchText: String,
        onValueChangedListener: (String) -> Unit,
        modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(key1 = isExpanded) {
        if (isExpanded) {
            focusRequester.requestFocus()
        }
    }

    Surface(
            color = ClavaColor.FabBackground,
            shape = RoundedCornerShape(100, 0, 0, 100),
            contentColor = ClavaColor.FabIcon,
            modifier = modifier
    ) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandIn(),
                    exit = shrinkOut(),
            ) {
                BasicTextField(
                        value = searchText,
                        onValueChange = onValueChangedListener,
                        interactionSource = interactionSource,
                        textStyle = Typography.body1.copy(color = ClavaColor.FabIcon),
                        singleLine = true,
                        visualTransformation = VisualTransformation.None,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                        modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp)
                                .focusRequester(focusRequester)
                ) { innerTextField ->
                    TextFieldDefaults.TextFieldDecorationBox(
                            value = searchText,
                            innerTextField = innerTextField,
                            contentPadding = PaddingValues(10.dp),
                            interactionSource = interactionSource,
                            enabled = true,
                            singleLine = true,
                            visualTransformation = VisualTransformation.None,
                            label = {
                                Text(
                                        text = "Search ${typeContentDescription}s",
                                        color = ClavaColor.FabIcon,
                                )
                            },
                            placeholder = {
                                Text(
                                        text = textPlaceholder,
                                        color = ClavaColor.FabIcon.copy(alpha = 0.6f),
                                )
                            },
                            leadingIcon = {
                                Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = ClavaColor.FabIcon,
                                )
                            },
                    )
                }
            }
            Crossfade(targetState = isExpanded) { expanded ->
                IconButton(
                        onClick = toggleExpanded,
                        modifier = Modifier
                                .defaultMinSize(minWidth = 56.dp, minHeight = 56.dp)
                ) {
                    Icon(
                            imageVector = if (expanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (expanded) "Close search" else "Search $typeContentDescription",
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SetupListScreen_Preview() {
    val playersToGenerate = 15
    val currentTime = Calendar.getInstance(Locale.getDefault())
    val players = generatePlayers(playersToGenerate)
    val courts = generateCourts(6)
    val matches = listOf(
            MatchState.OnCourt(currentTime.apply { add(Calendar.HOUR_OF_DAY, -1) }, courts[0]), // Disabled
            MatchState.OnCourt(currentTime.apply { add(Calendar.HOUR_OF_DAY, -1) }, courts[1]),
            null,
            null, // Disabled
            MatchState.OnCourt(currentTime.apply { add(Calendar.MINUTE, 1) }, courts[2]),
            MatchState.Paused(5, currentTime)
    ).mapIndexed { index, matchState ->
        matchState?.let { Match(index, listOf(), matchState) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SetupListScreen(
                typeContentDescription = "player",
                textPlaceholder = "John Doe",
                addItemName = "",
                nameIsDuplicate = { name, _ -> players.any { it.name == name } },
                addItemNameChangedListener = {},
                addItemNameClearPressedListener = {},
                showAddItemBlankError = false,
                items = players.sortedBy { it.name },
                getMatch = { player: Player -> matches[players.sortedBy { it.name }.indexOf(player) % matches.size] },
                getTimeRemaining = { state.getTimeLeft(currentTime) },
                editDialogOpenFor = null,
                itemNameEditedListener = { _, _ -> },
                itemNameEditCancelledListener = {},
                itemClickedListener = {},
                itemAddedListener = {},
                itemNameEditStartedListener = {},
                itemDeletedListener = {},
                selectedTab = SetupListTabSwitcherItem.PLAYERS,
                onTabSelectedListener = {},
                missingContentNextStep = null,
                navigateListener = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExtraInfo_SetupListScreen_Preview() {
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

@Preview(showBackground = true)
@Composable
fun Dialog_SetupListScreen_Preview() {
    val players = generatePlayers(20)
    Box(modifier = Modifier.fillMaxSize()) {
        SetupListScreen(
                typeContentDescription = "player",
                textPlaceholder = "John Doe",
                addItemName = "",
                addItemNameChangedListener = {},
                nameIsDuplicate = { name, _ -> players.any { it.name == name } },
                addItemNameClearPressedListener = {},
                showAddItemBlankError = false,
                items = players,
                getMatch = { null },
                getTimeRemaining = { null },
                editDialogOpenFor = players[2],
                itemNameEditedListener = { _, _ -> },
                itemNameEditCancelledListener = {},
                itemClickedListener = {},
                itemAddedListener = {},
                itemNameEditStartedListener = {},
                itemDeletedListener = {},
                selectedTab = SetupListTabSwitcherItem.PLAYERS,
                onTabSelectedListener = {},
                missingContentNextStep = null,
                navigateListener = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Error_SetupListScreen_Preview() {
    val players = generatePlayers(5)
    Box(modifier = Modifier.fillMaxSize()) {
        SetupListScreen(
                typeContentDescription = "player",
                textPlaceholder = "John Doe",
                addItemName = players.first().name,
                addItemNameChangedListener = {},
                nameIsDuplicate = { name, _ -> players.any { it.name == name } },
                addItemNameClearPressedListener = {},
                showAddItemBlankError = false,
                items = players,
                getMatch = { null },
                getTimeRemaining = { null },
                editDialogOpenFor = null,
                itemNameEditedListener = { _, _ -> },
                itemNameEditCancelledListener = {},
                itemClickedListener = {},
                itemAddedListener = {},
                itemNameEditStartedListener = {},
                itemDeletedListener = {},
                selectedTab = SetupListTabSwitcherItem.PLAYERS,
                onTabSelectedListener = {},
                missingContentNextStep = null,
                navigateListener = {},
        )
    }
}