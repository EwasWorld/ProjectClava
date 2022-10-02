package com.eywa.projectclava.main.ui.sharedUi

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
import com.eywa.projectclava.main.common.asString
import com.eywa.projectclava.main.common.filterAvailable
import com.eywa.projectclava.main.common.generateCourts
import com.eywa.projectclava.main.model.Court
import com.eywa.projectclava.ui.theme.Typography
import java.util.*

@Composable
fun AvailableCourtsHeader(
        currentTime: Calendar,
        courts: Iterable<Court>?,
) {
    val availableCourtsString = courts
            .filterAvailable(currentTime)
            ?.joinToString { it.number.toString() }
            ?.let { "Available courts: $it" }
    val nextAvailableCourt = courts
            ?.associateWith { it.currentMatch?.state?.getTimeLeft(currentTime) }
            ?.filter { it.key.canBeUsed && it.value != null }
            ?.minByOrNull { it.value!! }
            ?.let { "Next available court: " + it.value.asString() }

    Text(
            text = availableCourtsString ?: nextAvailableCourt ?: "No courts found",
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = Typography.h4,
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun AvailableCourtsHeader_Preview(
        @PreviewParameter(AvailableCourtsHeaderPreviewParamProvider::class) params: AvailableCourtsHeaderPreviewParam
) {
    AvailableCourtsHeader(
            currentTime = Calendar.getInstance(),
            courts = generateCourts(params.matchCount, params.availableCourtsCount),
    )
}


data class AvailableCourtsHeaderPreviewParam(
        val matchCount: Int = 4,
        val availableCourtsCount: Int = 4,
)

private class AvailableCourtsHeaderPreviewParamProvider :
        CollectionPreviewParameterProvider<AvailableCourtsHeaderPreviewParam>(
                listOf(
                        AvailableCourtsHeaderPreviewParam(),
                        AvailableCourtsHeaderPreviewParam(
                                matchCount = 1,
                                availableCourtsCount = 0,
                        ),
                        AvailableCourtsHeaderPreviewParam(
                                matchCount = 0,
                                availableCourtsCount = 1,
                        ),
                        AvailableCourtsHeaderPreviewParam(
                                matchCount = 0,
                                availableCourtsCount = 0,
                        ),
                )
        )