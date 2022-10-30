package com.eywa.projectclava.main.mainActivity.screens.manage

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.eywa.projectclava.R
import com.eywa.projectclava.main.common.asTimeString
import com.eywa.projectclava.main.common.generateCourts
import com.eywa.projectclava.main.common.generateMatches
import com.eywa.projectclava.main.mainActivity.CoreIntent
import com.eywa.projectclava.main.mainActivity.DatabaseIntent
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.mainActivity.screens.ScreenIntent
import com.eywa.projectclava.main.model.*
import java.util.*

sealed class AddCourtIntent : ScreenIntent<SetupListState<Court>> {
    override val screen: NavRoute = NavRoute.ADD_COURT

    // TODO_HACKY Get the prependedness from the state
    data class AddCourtSubmit(val prependCourt: Boolean) : AddCourtIntent()
    object EditCourtSubmit : AddCourtIntent()
    data class CourtDeleted(val court: Court) : AddCourtIntent()
    data class CourtClicked(val court: Court) : AddCourtIntent()

    data class ScreenIntent(val value: SetupListIntent.SetupListStateIntent) : AddCourtIntent()

    override fun handle(
            currentState: SetupListState<Court>,
            handle: (CoreIntent) -> Unit,
            newStateListener: (SetupListState<Court>) -> Unit
    ) {
        when (this) {
            is AddCourtSubmit -> {
                val prefix = if (prependCourt) "Court " else ""
                handle(DatabaseIntent.AddCourt(prefix + currentState.addItemName.trim()))
                SetupListIntent.SetupListStateIntent.AddItemClear.handle(currentState, handle, newStateListener)
            }
            EditCourtSubmit -> {
                val courtToEdit = currentState.editDialogOpenFor!!
                handle(DatabaseIntent.UpdateCourt(courtToEdit.copy(name = currentState.editItemName.trim())))
                SetupListIntent.SetupListStateIntent.EditNameCleared.handle(currentState, handle, newStateListener)
            }
            is CourtClicked -> handle(DatabaseIntent.UpdateCourt(court.copy(canBeUsed = !court.canBeUsed)))
            is CourtDeleted -> handle(DatabaseIntent.DeleteCourt(court))
            is ScreenIntent -> value.handle(currentState, handle, newStateListener)
        }
    }
}

private fun SetupListIntent.toAddCourtIntent(prependCourt: Boolean) = when (this) {
    is SetupListIntent.SetupListStateIntent -> AddCourtIntent.ScreenIntent(this)
    SetupListIntent.SetupListItemIntent.AddItemSubmit -> AddCourtIntent.AddCourtSubmit(prependCourt)
    SetupListIntent.SetupListItemIntent.EditItemSubmit -> AddCourtIntent.EditCourtSubmit
    is SetupListIntent.SetupListItemIntent.ItemClicked<*> -> AddCourtIntent.CourtClicked(value as Court)
    is SetupListIntent.SetupListItemIntent.ItemDeleted<*> -> AddCourtIntent.CourtDeleted(value as Court)
}


// TODO Sort by number (plus on court-picking dialogs)
// TODO Court picking dialogs: no courts available
@Composable
fun SetupCourtsScreen(
        state: SetupListState<Court>,
        databaseState: DatabaseState,
        prependCourt: Boolean = true,
        getTimeRemaining: Match.() -> TimeRemaining?,
        listener: (AddCourtIntent) -> Unit,
) {
    SetupListScreen(
            setupListSettings = SetupListSettings.COURTS,
            // TODO_HACKY Not sure if I like this useTextPlaceholderAlt switcharoo...
            state = state.copy(useTextPlaceholderAlt = prependCourt),
            items = databaseState.courts,
            nameIsDuplicate = { newName, editItemName ->
                if (newName == editItemName) return@SetupListScreen true

                val checkName = if (prependCourt) "Court $newName" else newName
                databaseState.courts.any { it.name == checkName.trim() }
            },
            getMatch = { databaseState.matches.getLatestMatchForCourt(it) },
            getTimeRemaining = getTimeRemaining,
            hasExtraContent = { databaseState.matches.getLatestMatchForCourt(it) != null },
            extraContent = {
                ExtraContent(
                        match = databaseState.matches.getLatestMatchForCourt(it)!!,
                        getTimeRemaining = getTimeRemaining
                )
            },
            listener = { listener(it.toAddCourtIntent(prependCourt)) },
    )
}

@Composable
fun RowScope.ExtraContent(match: Match, getTimeRemaining: Match.() -> TimeRemaining?) {
    if (match.court == null) return

    Text(
            text = match.players.joinToString { it.name },
            modifier = Modifier.weight(1f)
    )
    Text(
            text = match.getTimeRemaining().asTimeString()
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
            state = SetupListState(),
            databaseState = DatabaseState(
                    courts = generateCourts(10),
                    matches = generateMatches(5, currentTime),
            ),
            getTimeRemaining = { state.getTimeLeft(currentTime) },
            listener = {},
    )
}