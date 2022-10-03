package com.eywa.projectclava.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.eywa.projectclava.main.common.generateCourt
import com.eywa.projectclava.main.common.generateCourts
import com.eywa.projectclava.main.common.generateMatches
import com.eywa.projectclava.main.common.generatePlayers
import com.eywa.projectclava.main.model.MainState
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.MatchState
import com.eywa.projectclava.main.ui.mainScreens.CurrentMatchesScreen
import com.eywa.projectclava.main.ui.mainScreens.CurrentMatchesScreenPreviewParam
import com.eywa.projectclava.ui.theme.ProjectClavaTheme
import java.util.*

/*
 * Time spent: 16 hrs
 */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectClavaTheme {
                var state by remember { mutableStateOf(MainState()) }

                val currentTime = Calendar.getInstance()
                val newAmount = (currentTime.clone() as Calendar).apply { add(Calendar.SECOND, 5) }

                val m = Match(generatePlayers(2), MatchState.InProgressOrComplete(newAmount, generateCourt(1)))

                val params = CurrentMatchesScreenPreviewParam()
                val matches = generateMatches(params.matchCount, currentTime)
                CurrentMatchesScreen(
                        currentTime = currentTime,
                        courts = generateCourts(params.matchCount + params.availableCourtsCount),
                        matches = matches,
                        selectedMatch = params.selectedIndex?.let { index ->
                            matches.filter { it.isCurrent(currentTime) }.sortedBy { it.state }[index]
                        },
                        selectedMatchListener = {},
                        addTimeListener = {},
                        completeMatchListener = {},
                        changeCourtListener = {},
                        pauseListener = {},
                        unPauseListener = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ProjectClavaTheme {
    }
}