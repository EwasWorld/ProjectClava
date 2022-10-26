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
import androidx.compose.runtime.*
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
import com.eywa.projectclava.R
import com.eywa.projectclava.main.common.generateCourts
import com.eywa.projectclava.main.common.generateMatches
import com.eywa.projectclava.main.common.generatePlayers
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.model.*
import com.eywa.projectclava.main.ui.sharedUi.*
import com.eywa.projectclava.ui.theme.ClavaColor
import com.eywa.projectclava.ui.theme.Typography
import java.util.*

interface SetupListItem : NamedItem {
    val enabled: Boolean
}

enum class SetupListTabSwitcherItem(
        override val label: String,
        override val destination: NavRoute,
) : TabSwitcherItem {
    PLAYERS("Players", NavRoute.ADD_PLAYER),
    COURTS("Courts", NavRoute.ADD_COURT),
}

data class SetupListState<T>(
        val addItemName: String = "",
        val addItemIsDirty: Boolean = false,
        val editDialogOpenFor: T? = null,
        val useTextPlaceholderAlt: Boolean = false,
)

/**
 * Properties that are different between screens but not dynamic like state
 */
enum class SetupListSettings(
        val typeContentDescription: String,
        private val textPlaceholder: String,
        private val textPlaceholderAlt: String?,
        val deleteIconInfo: ClavaIconInfo = ClavaIconInfo.VectorIcon(Icons.Default.Close, "Delete"),
        val selectedTab: SetupListTabSwitcherItem,
) {
    PLAYERS(
            typeContentDescription = "player",
            deleteIconInfo = ClavaIconInfo.PainterIcon(R.drawable.baseline_archive_24, "Archive"),
            selectedTab = SetupListTabSwitcherItem.PLAYERS,
            textPlaceholder = "John Doe",
            textPlaceholderAlt = null,
    ),
    COURTS(
            typeContentDescription = "court",
            selectedTab = SetupListTabSwitcherItem.COURTS,
            textPlaceholder = "Court 1",
            textPlaceholderAlt = "1",
    )
    ;

    fun getTextPlaceholder(useAlt: Boolean) =
            if (useAlt && textPlaceholderAlt != null) textPlaceholderAlt else textPlaceholder
}

sealed class SetupListIntent {

}

@Composable
fun <T : SetupListItem> SetupListScreen(
        setupListSettings: SetupListSettings,
        setupListState: SetupListState<T>,
        items: Iterable<T>,
        getMatch: (T) -> Match?,
        getTimeRemaining: Match.() -> TimeRemaining?,
        nameIsDuplicate: (newName: String, editItemName: String?) -> Boolean,
        addItemNameClearPressedListener: () -> Unit,
        addItemNameChangedListener: (String) -> Unit,
        itemAddedListener: (String) -> Unit,
        itemNameEditedListener: (T, String) -> Unit,
        itemNameEditCancelledListener: () -> Unit,
        itemNameEditStartedListener: (T) -> Unit,
        itemDeletedListener: (T) -> Unit,
        itemClickedListener: (T) -> Unit,
        hasExtraContent: (T) -> Boolean = { false },
        onTabSelectedListener: (SetupListTabSwitcherItem) -> Unit,
        extraContent: @Composable RowScope.(T) -> Unit = {},
        navigateListener: (NavRoute) -> Unit,
) {
    // TODO Add an are you sure to deletion
    val focusManager = LocalFocusManager.current
    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchText: String? by remember { mutableStateOf(null) }

    val itemsToShow = searchText
            .takeIf { !it.isNullOrBlank() }
            ?.let { searchTxt -> items.filter { it.name.contains(searchTxt, ignoreCase = true) } }
            ?.takeIf { it.isNotEmpty() }

    EditNameDialog(
            typeContentDescription = setupListSettings.typeContentDescription,
            textPlaceholder = setupListSettings.getTextPlaceholder(setupListState.useTextPlaceholderAlt),
            nameIsDuplicate = nameIsDuplicate,
            editDialogOpenFor = setupListState.editDialogOpenFor,
            itemEditedListener = itemNameEditedListener,
            itemEditCancelledListener = itemNameEditCancelledListener,
    )

    ClavaScreen(
            noContentText = "No ${setupListSettings.typeContentDescription}s yet," +
                    "\n\nType a name into the box below\nthen press enter!",
            missingContentNextStep = if (items.none()) listOf(MissingContentNextStep.ADD_PLAYERS) else null,
            showMissingContentNextStep = false,
            navigateListener = navigateListener,
            fabs = { modifier ->
                SearchFab(
                        isExpanded = isSearchExpanded,
                        textPlaceholder = setupListSettings.getTextPlaceholder(setupListState.useTextPlaceholderAlt),
                        typeContentDescription = setupListSettings.typeContentDescription,
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
                NamedItemTextField<T>(
                        typeContentDescription = setupListSettings.typeContentDescription,
                        textPlaceholder = setupListSettings.getTextPlaceholder(setupListState.useTextPlaceholderAlt),
                        nameIsDuplicate = nameIsDuplicate,
                        proposedItemName = setupListState.addItemName,
                        showBlankError = setupListState.addItemIsDirty,
                        onValueChangedListener = addItemNameChangedListener,
                        onClearPressedListener = addItemNameClearPressedListener,
                        onDoneListener = { itemAddedListener(setupListState.addItemName.trim()) },
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
                        onItemClicked = onTabSelectedListener,
                )
            }
    ) {
        if (!searchText.isNullOrBlank() && itemsToShow == null) {
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
            items((itemsToShow ?: items).sortedBy { it.name }) { item ->
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
                setupListState = SetupListState(),
                nameIsDuplicate = { name, _ -> players.any { it.name == name } },
                addItemNameChangedListener = {},
                addItemNameClearPressedListener = {},
                items = players.sortedBy { it.name },
                getMatch = { player: Player -> matches[players.sortedBy { it.name }.indexOf(player) % matches.size] },
                getTimeRemaining = { state.getTimeLeft(currentTime) },
                itemNameEditedListener = { _, _ -> },
                itemNameEditCancelledListener = {},
                itemClickedListener = {},
                itemAddedListener = {},
                itemNameEditStartedListener = {},
                itemDeletedListener = {},
                onTabSelectedListener = {},
                navigateListener = {},
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

@Preview(showBackground = true)
@Composable
fun Dialog_SetupListScreen_Preview() {
    val players = generatePlayers(20)
    Box(modifier = Modifier.fillMaxSize()) {
        SetupListScreen(
                setupListSettings = SetupListSettings.PLAYERS,
                setupListState = SetupListState(
                        editDialogOpenFor = players[2],
                ),
                addItemNameChangedListener = {},
                nameIsDuplicate = { name, _ -> players.any { it.name == name } },
                addItemNameClearPressedListener = {},
                items = players,
                getMatch = { null },
                getTimeRemaining = { null },
                itemNameEditedListener = { _, _ -> },
                itemNameEditCancelledListener = {},
                itemClickedListener = {},
                itemAddedListener = {},
                itemNameEditStartedListener = {},
                itemDeletedListener = {},
                onTabSelectedListener = {},
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
                setupListSettings = SetupListSettings.PLAYERS,
                setupListState = SetupListState(
                        addItemName = players.first().name,
                ),
                addItemNameChangedListener = {},
                nameIsDuplicate = { name, _ -> players.any { it.name == name } },
                addItemNameClearPressedListener = {},
                items = players,
                getMatch = { null },
                getTimeRemaining = { null },
                itemNameEditedListener = { _, _ -> },
                itemNameEditCancelledListener = {},
                itemClickedListener = {},
                itemAddedListener = {},
                itemNameEditStartedListener = {},
                itemDeletedListener = {},
                onTabSelectedListener = {},
                navigateListener = {},
        )
    }
}