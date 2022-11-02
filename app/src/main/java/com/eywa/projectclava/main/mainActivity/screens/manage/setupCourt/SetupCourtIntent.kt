package com.eywa.projectclava.main.mainActivity.screens.manage.setupCourt

import com.eywa.projectclava.main.database.DatabaseIntent
import com.eywa.projectclava.main.mainActivity.NavRoute
import com.eywa.projectclava.main.mainActivity.screens.ScreenIntent
import com.eywa.projectclava.main.mainActivity.screens.manage.SetupListIntent
import com.eywa.projectclava.main.mainActivity.screens.manage.SetupListState
import com.eywa.projectclava.main.mainActivity.viewModel.CoreIntent
import com.eywa.projectclava.main.model.Court


fun SetupListIntent.toSetupCourtIntent() = when (this) {
    is SetupListIntent.SetupListStateIntent -> SetupCourtIntent.ScreenIntent(this)
    SetupListIntent.SetupListItemIntent.AddItemSubmitted -> SetupCourtIntent.AddCourtSubmitted
    is SetupListIntent.SetupListItemIntent.ItemClicked<*> -> SetupCourtIntent.CourtClicked(value as Court)
    is SetupListIntent.SetupListItemIntent.ItemDeleted<*> -> SetupCourtIntent.CourtDeleted(value as Court)
}


sealed class SetupCourtIntent : ScreenIntent<SetupListState<Court>> {
    override val screen: NavRoute = NavRoute.ADD_COURT

    object AddCourtSubmitted : SetupCourtIntent()
    data class CourtDeleted(val court: Court) : SetupCourtIntent()
    data class CourtClicked(val court: Court) : SetupCourtIntent()

    data class ScreenIntent(val value: SetupListIntent.SetupListStateIntent) : SetupCourtIntent()

    override fun handle(
            currentState: SetupListState<Court>,
            handle: (CoreIntent) -> Unit,
            newStateListener: (SetupListState<Court>) -> Unit
    ) {
        when (this) {
            is AddCourtSubmitted -> {
                handle(DatabaseIntent.AddCourt(currentState.addItemName.trim()))
                SetupListIntent.SetupListStateIntent.AddNameCleared.handle(currentState, handle, newStateListener)
            }
            is CourtClicked -> handle(DatabaseIntent.UpdateCourt(court.copy(canBeUsed = !court.canBeUsed)))
            is CourtDeleted -> handle(DatabaseIntent.DeleteCourt(court))
            is ScreenIntent -> value.handle(currentState, handle, newStateListener) { editCourt, newName ->
                handle(DatabaseIntent.UpdateCourt(editCourt.copy(name = newName.trim())))
            }
        }
    }
}