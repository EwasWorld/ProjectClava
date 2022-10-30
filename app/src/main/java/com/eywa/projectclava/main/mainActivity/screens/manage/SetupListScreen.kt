package com.eywa.projectclava.main.mainActivity.screens.manage

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.main.common.generateCourts
import com.eywa.projectclava.main.common.generateMatches
import com.eywa.projectclava.main.common.generatePlayers
import com.eywa.projectclava.main.mainActivity.screens.manage.SetupListIntent.SetupListItemIntent
import com.eywa.projectclava.main.mainActivity.screens.manage.SetupListIntent.SetupListStateIntent
import com.eywa.projectclava.main.model.*
import com.eywa.projectclava.main.ui.sharedUi.*
import com.eywa.projectclava.ui.theme.ClavaColor
import com.eywa.projectclava.ui.theme.Typography
import java.util.*


@Composable
fun <T : SetupListItem> SetupListScreen(
        setupListSettings: SetupListSettings,
        state: SetupListState<T>,
        items: Iterable<T>,
        getMatch: (T) -> Match?,
        getTimeRemaining: Match.() -> TimeRemaining?,
        nameIsDuplicate: (newName: String, nameOfItemBeingEdited: String?) -> Boolean,
        hasExtraContent: (T) -> Boolean = { false },
        extraContent: @Composable RowScope.(T) -> Unit = {},
        listener: (SetupListIntent) -> Unit,
) {
    // TODO Add an are you sure to deletion
    val focusManager = LocalFocusManager.current

    val itemsToShow = state.searchText
            .takeIf { !it.isNullOrBlank() }
            ?.let { searchTxt -> items.filter { it.name.contains(searchTxt, ignoreCase = true) } }
            ?.takeIf { it.isNotEmpty() }

    EditNameDialog(
            typeContentDescription = setupListSettings.typeContentDescription,
            textPlaceholder = setupListSettings.getTextPlaceholder(state.useTextPlaceholderAlt),
            nameIsDuplicate = nameIsDuplicate,
            editItemState = state,
            listener = { listener(it.toSetupListIntent()) },
    )

    ClavaScreen(
            noContentText = "No ${setupListSettings.typeContentDescription}s yet," +
                    "\n\nType a name into the box below\nthen press enter!",
            missingContentNextStep = if (items.none()) listOf(MissingContentNextStep.ADD_PLAYERS) else null,
            showMissingContentNextStep = false,
            navigateListener = { listener(SetupListStateIntent.Navigate(it)) },
            fabs = { modifier ->
                SearchFab(
                        isExpanded = state.isSearchExpanded,
                        textPlaceholder = setupListSettings.getTextPlaceholder(state.useTextPlaceholderAlt),
                        typeContentDescription = setupListSettings.typeContentDescription,
                        toggleExpanded = { listener(SetupListStateIntent.ToggleSearch) },
                        searchText = state.searchText ?: "",
                        onValueChangedListener = { listener(SetupListStateIntent.SearchTextChanged(it)) },
                        modifier = modifier
                )
            },
            footerIsVisible = !state.isSearchExpanded,
            footerContent = {
                NamedItemTextField<T>(
                        typeContentDescription = setupListSettings.typeContentDescription,
                        textPlaceholder = setupListSettings.getTextPlaceholder(state.useTextPlaceholderAlt),
                        nameIsDuplicate = nameIsDuplicate,
                        proposedItemName = state.addItemName,
                        fieldIsDirty = state.addItemIsDirty,
                        onValueChangedListener = { listener(SetupListStateIntent.AddNameChanged(it)) },
                        onClearPressedListener = { listener(SetupListStateIntent.AddNameCleared) },
                        onDoneListener = { listener(SetupListItemIntent.AddItemSubmitted) },
                        textFieldModifier = Modifier.fillMaxWidth(),
                        modifier = Modifier
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                                .padding(bottom = 5.dp)
                )
            },
            headerContent = {
                TabSwitcher(
                        items = SetupListTabSwitcherItem.values().toList(),
                        selectedItem = setupListSettings.selectedTab,
                        navigateListener = { listener(SetupListStateIntent.Navigate(it)) },
                )
            }
    ) {
        if (state.searchText?.isBlank() == false && itemsToShow == null) {
            // No search results
            item {
                Text(
                        text = "No matches found for '${state.searchText}'",
                        style = Typography.h4,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                )
            }
        }
        else {
            items((itemsToShow ?: items).sortedBy { it.name }) { item ->
                val match = getMatch(item)
                SelectableListItem(
                        enabled = item.enabled,
                        matchState = match?.state,
                        timeRemaining = { match?.getTimeRemaining() },
                ) {
                    Column(
                            modifier = Modifier.clickable {
                                listener(SetupListItemIntent.ItemClicked(item))
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
                                        listener(
                                                EditDialogIntent.EditItemStateIntent.EditItemStarted(item)
                                                        .toSetupListIntent()
                                        )
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
                                        listener(SetupListItemIntent.ItemDeleted(item))
                                        focusManager.clearFocus()
                                    }
                            ) {
                                Icon(
                                        painter = setupListSettings.deleteIconInfo.asPainter(),
                                        contentDescription = setupListSettings.deleteIconInfo.contentDescription!! +
                                                " " + item.name
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
                setupListSettings = SetupListSettings.PLAYERS,
                state = SetupListState(),
                nameIsDuplicate = { name, _ -> players.any { it.name == name } },
                items = players.sortedBy { it.name },
                getMatch = { player: Player -> matches[players.sortedBy { it.name }.indexOf(player) % matches.size] },
                getTimeRemaining = { state.getTimeLeft(currentTime) },
                listener = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExtraInfo_SetupListScreen_Preview() {
    val currentTime = Calendar.getInstance(Locale.getDefault())
    SetupCourtsScreen(
            databaseState = DatabaseState(
                    courts = generateCourts(10),
                    matches = generateMatches(5, currentTime),
            ),
            state = SetupListState(),
            prependCourt = false,
            getTimeRemaining = { state.getTimeLeft(currentTime) },
            listener = {},
    )
}

@Preview(showBackground = true)
@Composable
fun Dialog_SetupListScreen_Preview() {
    val players = generatePlayers(20)
    Box(modifier = Modifier.fillMaxSize()) {
        SetupListScreen(
                setupListSettings = SetupListSettings.PLAYERS,
                state = SetupListState(
                        editDialogOpenFor = players[2],
                ),
                nameIsDuplicate = { name, _ -> players.any { it.name == name } },
                items = players,
                getMatch = { null },
                getTimeRemaining = { null },
                listener = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Error_SetupListScreen_Preview() {
    val players = generatePlayers(5)
    Box(modifier = Modifier.fillMaxSize()) {
        SetupListScreen(
                setupListSettings = SetupListSettings.PLAYERS,
                state = SetupListState(
                        addItemName = players.first().name,
                ),
                nameIsDuplicate = { name, _ -> players.any { it.name == name } },
                items = players,
                getMatch = { null },
                getTimeRemaining = { null },
                listener = {},
        )
    }
}