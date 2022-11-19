package com.eywa.projectclava.main.features.screens.manage.setupCourt

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
import com.eywa.projectclava.main.features.screens.manage.SetupListScreen
import com.eywa.projectclava.main.features.screens.manage.SetupListState
import com.eywa.projectclava.main.features.screens.manage.helperClasses.SetupListSettings
import com.eywa.projectclava.main.model.*
import java.util.*


@Composable
fun SetupCourtsScreen(
        state: SetupListState<Court>,
        databaseState: ModelState,
        isSoftKeyboardOpen: Boolean,
        prependCourt: Boolean = true,
        getTimeRemaining: Match.() -> TimeRemaining?,
        listener: (SetupCourtIntent) -> Unit,
) {
    SetupListScreen(
            setupListSettings = SetupListSettings.COURTS,
            // TODO_HACKY Not sure if I like this useTextPlaceholderAlt switcharoo...
            state = state.copy(useTextPlaceholderAlt = prependCourt),
            items = databaseState.courts,
            isSoftKeyboardOpen = isSoftKeyboardOpen,
            nameIsDuplicate = { newName, nameOfItemBeingEdited ->
                if (newName == nameOfItemBeingEdited) return@SetupListScreen true

                val checkName = Court.formatName(newName, prependCourt)
                databaseState.courts.any { it.name == checkName }
            },
            getMatch = { databaseState.matches.getLatestMatchForCourt(it) },
            getTimeRemaining = getTimeRemaining,
            hasExtraContent = { databaseState.matches.getLatestMatchForCourt(it) != null },
            isDeleteItemEnabled = { databaseState.matches.getLatestMatchForCourt(it) == null },
            extraContent = {
                ExtraContent(
                        match = databaseState.matches.getLatestMatchForCourt(it)!!,
                        getTimeRemaining = getTimeRemaining
                )
            },
            listener = { listener(it.toSetupCourtIntent()) },
    )
}

@Composable
fun RowScope.ExtraContent(match: Match, getTimeRemaining: Match.() -> TimeRemaining?) {
    if (match.court == null) return

    Text(
            text = match.playerNameString(),
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
            databaseState = ModelState(
                    courts = generateCourts(10),
                    matches = generateMatches(5, currentTime),
            ),
            getTimeRemaining = { state.getTimeLeft(currentTime) },
            listener = {},
            isSoftKeyboardOpen = false,
    )
}