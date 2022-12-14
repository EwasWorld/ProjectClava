package com.eywa.projectclava.main.features.ui

import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.eywa.projectclava.R
import com.eywa.projectclava.main.common.asTimeString
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.MatchState
import com.eywa.projectclava.main.model.TimeRemaining
import com.eywa.projectclava.main.theme.Typography

@Composable
fun MatchTimeRemainingText(match: Match?, getTimeRemaining: Match.() -> TimeRemaining? = { null }) {
    Text(
            text = when (match?.state) {
                null,
                is MatchState.NotStarted -> "Not played"
                is MatchState.Paused,
                is MatchState.OnCourt -> match.getTimeRemaining()!!.asTimeString()
                is MatchState.Completed -> match.getTime().asTimeString()
            },
            style = Typography.body1,
    )
    if (match?.isPaused == true) {
        Icon(
                painter = painterResource(id = R.drawable.baseline_pause_24),
                contentDescription = "Match paused"
        )
    }
}