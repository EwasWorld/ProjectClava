package com.eywa.projectclava.main.ui.sharedUi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.eywa.projectclava.main.common.GeneratableMatchState
import com.eywa.projectclava.main.common.asString
import com.eywa.projectclava.main.common.generateCourts
import com.eywa.projectclava.main.common.generateMatches
import com.eywa.projectclava.main.model.Court
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.getCourtsInUse
import com.eywa.projectclava.ui.theme.ClavaColor
import com.eywa.projectclava.ui.theme.Typography
import java.util.*

@Composable
fun AvailableCourtsHeader(
        currentTime: Calendar,
        courts: Iterable<Court>?,
        matches: Iterable<Match>?,
) {
    val availableCourtsString = courts
            ?.minus((matches?.getCourtsInUse(currentTime) ?: listOf()).toSet())
            ?.takeIf { it.isNotEmpty() }
            ?.joinToString { it.number.toString() }
            ?.let { "Available courts: $it" }
    val nextAvailableCourt = matches?.filter { it.isInProgress(currentTime) }
            ?.minByOrNull { it.state }
            ?.let { "Next available court: " + it.state.getTimeLeft(currentTime).asString() }

    Text(
            text = availableCourtsString ?: nextAvailableCourt ?: "No courts found",
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = Typography.h4,
            modifier = Modifier
                    .background(ClavaColor.HeaderFooterBackground)
                    .fillMaxWidth()
                    .padding(10.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun AvailableCourtsHeader_Preview(
        @PreviewParameter(AvailableCourtsHeaderPreviewParamProvider::class) params: AvailableCourtsHeaderPreviewParam
) {
    val currentTime = Calendar.getInstance(Locale.getDefault())
    AvailableCourtsHeader(
            currentTime = currentTime,
            courts = generateCourts(params.courtCount),
            matches = if (params.matchesCount > 0)
                generateMatches(params.matchesCount, currentTime, forceState = params.matchesType)
            else
                null
    )
}

data class AvailableCourtsHeaderPreviewParam(
        val courtCount: Int = 4,
        val matchesCount: Int = 2,
        val matchesType: GeneratableMatchState? = GeneratableMatchState.IN_PROGRESS,
)

private class AvailableCourtsHeaderPreviewParamProvider :
        CollectionPreviewParameterProvider<AvailableCourtsHeaderPreviewParam>(
                listOf(
                        AvailableCourtsHeaderPreviewParam(),
                        AvailableCourtsHeaderPreviewParam(
                                matchesCount = 0,
                        ),
                        AvailableCourtsHeaderPreviewParam(
                                matchesCount = 4,
                        ),
                        AvailableCourtsHeaderPreviewParam(
                                matchesCount = 4,
                                matchesType = GeneratableMatchState.NOT_STARTED,
                        ),
                        AvailableCourtsHeaderPreviewParam(
                                courtCount = 0,
                                matchesCount = 0,
                        ),
                )
        )