package com.eywa.projectclava.main.ui.sharedUi

import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.eywa.projectclava.R
import com.eywa.projectclava.main.common.asTimeString
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.MatchState
import com.eywa.projectclava.ui.theme.Typography
import java.util.*

/**
 * @param currentTime required if match is [MatchState.OnCourt] || [MatchState.Paused]
 */
@Composable
fun MatchStateIndicator(match: Match?, currentTime: Calendar?) {
    Text(
            text = when (match?.state) {
                null,
                is MatchState.NotStarted -> "Not played"
                is MatchState.Paused,
                is MatchState.OnCourt -> match.state.getTimeLeft(currentTime!!)!!.asTimeString()
                is MatchState.Completed -> match.state.getFinishedTime()!!.asTimeString()
                is MatchState.InProgressOrComplete -> throw NotImplementedError()
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