package com.eywa.projectclava.main.features.screens.manage.setupPlayer

import androidx.compose.runtime.Composable
import com.eywa.projectclava.main.features.screens.manage.SetupListScreen
import com.eywa.projectclava.main.features.screens.manage.SetupListState
import com.eywa.projectclava.main.features.screens.manage.helperClasses.SetupListSettings
import com.eywa.projectclava.main.model.*


@Composable
fun SetupPlayersScreen(
        state: SetupListState<Player>,
        databaseState: ModelState,
        isSoftKeyboardOpen: Boolean,
        getTimeRemaining: Match.() -> TimeRemaining?,
        listener: (SetupPlayerIntent) -> Unit,
) {
    SetupListScreen(
            setupListSettings = SetupListSettings.PLAYERS,
            state = state,
            isSoftKeyboardOpen = isSoftKeyboardOpen,
            items = databaseState.players.filter { !it.isArchived },
            getMatch = { player ->
                databaseState.matches
                        .filter { match -> match.players.any { player.name == it.name } }
                        .getPlayerColouringMatch()
            },
            nameIsDuplicate = { newName, nameOfItemBeingEdited ->
                newName != nameOfItemBeingEdited && databaseState.players.any { it.name == newName.trim() }
            },
            nameIsArchived = { newName, nameOfItemBeingEdited ->
                if (newName == nameOfItemBeingEdited) return@SetupListScreen null
                databaseState.players.find { it.name == newName.trim() && it.isArchived }
            },
            getTimeRemaining = getTimeRemaining,
            listener = { listener(it.toSetupPlayerIntent()) },
    )
}
