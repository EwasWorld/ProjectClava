package com.eywa.projectclava.main.ui.sharedUi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.eywa.projectclava.main.model.Court
import com.eywa.projectclava.main.model.Match
import com.eywa.projectclava.main.model.getCourtsInUse
import java.util.*

@Composable
fun SelectCourtRadioButtons(
        courts: Iterable<Court>?,
        matches: Iterable<Match>?,
        currentTime: Calendar,
        selectedCourt: Court?,
        onCourtSelected: (Court) -> Unit,
) {
    SelectCourtRadioButtons(
            availableCourts = courts?.minus((matches?.getCourtsInUse(currentTime) ?: listOf()).toSet()),
            selectedCourt = selectedCourt,
            onCourtSelected = onCourtSelected
    )
}

@Composable
fun SelectCourtRadioButtons(
        availableCourts: Iterable<Court>?,
        selectedCourt: Court?,
        onCourtSelected: (Court) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(availableCourts?.sortedBy { it.name } ?: listOf()) { court ->
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onCourtSelected(court) }
            ) {
                RadioButton(
                        selected = selectedCourt == court,
                        onClick = { onCourtSelected(court) }
                )
                Text(
                        text = court.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
    if (availableCourts?.any() != true) {
        Text(
                text = "No courts available"
        )
    }
}
