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
import com.eywa.projectclava.R
import com.eywa.projectclava.main.common.generateCourts
import com.eywa.projectclava.main.common.generateMatches
import com.eywa.projectclava.main.common.generatePlayers
import com.eywa.projectclava.main.mainActivity.CoreIntent
import com.eywa.projectclava.main.mainActivity.MainEffect
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.mainActivity.screens.ScreenState
import com.eywa.projectclava.main.mainActivity.screens.manage.SetupListIntent.SetupListItemIntent
import com.eywa.projectclava.main.mainActivity.screens.manage.SetupListIntent.SetupListStateIntent
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

data class SetupListState<T : SetupListItem>(
        val addItemName: String = "",
        val addItemIsDirty: Boolean = false,
        override val editItemName: String = "",
        override val editNameIsDirty: Boolean = false,
        override val editDialogOpenFor: T? = null,
        val useTextPlaceholderAlt: Boolean = false,
        val searchText: String? = null,
) : ScreenState, EditItemState<T> {
    val isSearchExpanded = searchText != null
}

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
    sealed class SetupListItemIntent<T : SetupListItem> : SetupListIntent() {
        object AddItemSubmit : SetupListItemIntent<SetupListItem>()
        object EditItemSubmit : SetupListItemIntent<SetupListItem>()
        data class ItemDeleted<T : SetupListItem>(val value: T) : SetupListItemIntent<T>()
        data class ItemClicked<T : SetupListItem>(val value: T) : SetupListItemIntent<T>()
    }

    /**
     * Actions that will behave the same no matter the type of T
     * (usually due to affecting only the [SetupListState] or things like navigation)
     */
    sealed class SetupListStateIntent : SetupListIntent() {
        object ToggleUseAltPlaceholderText : SetupListStateIntent()

        object AddItemClear : SetupListStateIntent()
        data class AddItemNameChanged(val value: String) : SetupListStateIntent()

        data class EditItemStarted(val value: SetupListItem) : SetupListStateIntent()
        data class EditItemNameChanged(val value: String) : SetupListStateIntent()
        object EditItemCancelled : SetupListStateIntent()
        object EditNameCleared : SetupListStateIntent()

        object ToggleSearch : SetupListStateIntent()
        data class SearchTextChanged(val value: String) : SetupListStateIntent()

        data class Navigate(val value: NavRoute) : SetupListStateIntent()

        fun <T : SetupListItem> handle(
                currentState: SetupListState<T>,
                handle: (CoreIntent) -> Unit,
                newStateListener: (SetupListState<T>) -> Unit
        ) {
            @Suppress("UNCHECKED_CAST")
            when (this) {
                AddItemClear -> newStateListener(currentState.copy(addItemName = "", addItemIsDirty = false))
                is AddItemNameChanged -> newStateListener(currentState.copy(addItemName = value, addItemIsDirty = true))
                EditItemCancelled -> newStateListener(currentState.copy(editDialogOpenFor = null))
                EditNameCleared -> newStateListener(currentState.copy(editItemName = "", editNameIsDirty = false))
                is EditItemNameChanged -> newStateListener(
                        currentState.copy(
                                editItemName = value,
                                editNameIsDirty = true
                        )
                )
                is EditItemStarted -> newStateListener(
                        currentState.copy(
                                editItemName = value.name,
                                editNameIsDirty = false,
                                editDialogOpenFor = value as T
                        )
                )
                is Navigate -> handle(MainEffect.Navigate(value))
                is SearchTextChanged -> newStateListener(currentState.copy(searchText = value))
                ToggleSearch -> {
                    val newSearchText = if (currentState.searchText == null) "" else null
                    newStateListener(currentState.copy(searchText = newSearchText))
                }
                ToggleUseAltPlaceholderText -> newStateListener(
                        currentState.copy(useTextPlaceholderAlt = !currentState.useTextPlaceholderAlt)
                )
            }
        }
    }
}

fun EditDialogListener.toSetupListIntent() = when (this) {
    EditDialogListener.EditItemCancelled -> SetupListStateIntent.EditItemCancelled
    EditDialogListener.EditNameCleared -> SetupListStateIntent.EditNameCleared
    is EditDialogListener.EditItemNameChanged -> SetupListStateIntent.EditItemNameChanged(value)
    is EditDialogListener.EditItemStarted<*> -> SetupListStateIntent.EditItemStarted(value as SetupListItem)
    EditDialogListener.EditItemSubmit -> SetupListItemIntent.EditItemSubmit
}

@Composable
fun <T : SetupListItem> SetupListScreen(
        setupListSettings: SetupListSettings,
        state: SetupListState<T>,
        items: Iterable<T>,
        getMatch: (T) -> Match?,
        getTimeRemaining: Match.() -> TimeRemaining?,
        nameIsDuplicate: (newName: String, editItemName: String?) -> Boolean,
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
                        onValueChangedListener = { listener(SetupListStateIntent.AddItemNameChanged(it)) },
                        onClearPressedListener = { listener(SetupListStateIntent.AddItemClear) },
                        onDoneListener = { listener(SetupListItemIntent.AddItemSubmit) },
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
                                        listener(SetupListStateIntent.EditItemStarted(item))
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