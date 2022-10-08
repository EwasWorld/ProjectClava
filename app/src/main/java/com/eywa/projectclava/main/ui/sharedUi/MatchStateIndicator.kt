package com.eywa.projectclava.main.ui.sharedUi

import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.eywa.projectclava.R
import com.eywa.projectclava.main.common.asString
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.MatchState
import com.eywa.projectclava.ui.theme.Typography
import java.util.*

@Composable
fun MatchStateIndicator(match: Match?, currentTime: Calendar) {
    Text(
            text = when (match?.state) {
                null,
                is MatchState.NotStarted -> "Not played"
                is MatchState.Paused,
                is MatchState.OnCourt -> match.state.getTimeLeft(currentTime)!!.asString()
                is MatchState.Completed -> match.state.getFinishedTime()!!.asString()
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