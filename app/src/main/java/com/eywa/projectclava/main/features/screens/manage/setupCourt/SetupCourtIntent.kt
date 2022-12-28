package com.eywa.projectclava.main.features.screens.manage.setupCourt

import com.eywa.projectclava.main.database.DatabaseIntent
import com.eywa.projectclava.main.features.screens.ScreenIntent
import com.eywa.projectclava.main.features.screens.manage.SetupListIntent
import com.eywa.projectclava.main.features.screens.manage.SetupListIntent.SetupListItemIntent.*
import com.eywa.projectclava.main.features.screens.manage.SetupListIntent.SetupListStateIntent.AddNameCleared
import com.eywa.projectclava.main.features.screens.manage.SetupListState
import com.eywa.projectclava.main.mainActivity.MainNavRoute
import com.eywa.projectclava.main.mainActivity.viewModel.CoreIntent
import com.eywa.projectclava.main.model.Court


fun SetupListIntent.toSetupCourtIntent() = when (this) {
    is SetupListIntent.SetupListStateIntent -> SetupCourtIntent.ScreenIntent(this)
    AddItemSubmitted -> SetupCourtIntent.AddCourtSubmitted
    is UnarchiveItemSubmitted<*> -> throw IllegalStateException("Courts cannot be archived")
    is ItemClicked<*> -> SetupCourtIntent.CourtClicked(item as Court)
    is ItemDeleted<*> -> SetupCourtIntent.CourtDeleted(item as Court)
    is ItemNameUpdated<*> -> SetupCourtIntent.CourtNameUpdated(item as Court, newName)
}


sealed class SetupCourtIntent : ScreenIntent<SetupListState<Court>> {
    override val screen = MainNavRoute.ADD_COURT

    object AddCourtSubmitted : SetupCourtIntent()
    data class CourtDeleted(val court: Court) : SetupCourtIntent()
    data class CourtClicked(val court: Court) : SetupCourtIntent()
    data class CourtNameUpdated(val court: Court, val newName: String) : SetupCourtIntent()

    data class ScreenIntent(val value: SetupListIntent.SetupListStateIntent) : SetupCourtIntent()

    override fun handle(
            currentState: SetupListState<Court>,
            handle: (CoreIntent) -> Unit,
            newStateListener: (SetupListState<Court>) -> Unit
    ) {
        when (this) {
            is AddCourtSubmitted -> {
                handle(DatabaseIntent.AddCourt(currentState.addItemName.trim()))
                ScreenIntent(AddNameCleared).handle(currentState, handle, newStateListener)
            }
            is CourtClicked -> handle(DatabaseIntent.UpdateCourt(court.copy(canBeUsed = !court.canBeUsed)))
            is CourtDeleted -> handle(DatabaseIntent.DeleteCourt(court))
            is ScreenIntent -> value.handle(
                    currentState = currentState,
                    handleCore = handle,
                    handleFollowOn = { it.toSetupCourtIntent().handle(currentState, handle, newStateListener) },
                    newStateListener = newStateListener,
            )
            is CourtNameUpdated -> handle(DatabaseIntent.UpdateCourt(court.copy(name = newName.trim())))
        }
    }
}