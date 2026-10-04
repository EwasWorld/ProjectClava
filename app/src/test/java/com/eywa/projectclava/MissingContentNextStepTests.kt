package com.eywa.projectclava

import com.eywa.projectclava.main.common.MissingContentNextStep
import com.eywa.projectclava.main.common.MissingContentNextStep.Companion.getFirstStep
import org.junit.Assert.assertEquals
import org.junit.Test

class MissingContentNextStepTests {
    @Test
    fun testMissingContentOrder_Simple() {
        assertEquals(null, null.getFirstStep())
        assertEquals(null, listOf<MissingContentNextStep>().getFirstStep())
        assertEquals(
                MissingContentNextStep.ADD_PLAYERS,
                MissingContentNextStep.entries.toSet().getFirstStep()
        )
    }

    @Test
    fun testMissingContentOrder_CompletedSteps() {
        assertEquals(
                MissingContentNextStep.COMPLETE_A_MATCH,
                MissingContentNextStep.entries.toSet().minus(MissingContentNextStep.START_A_MATCH).getFirstStep()
        )
    }

    @Test
    fun testMissingContentOrder_NoMatches() {
        // Note this doesn't return ADD_PLAYERS because it's impossible to set up a match without any players existing,
        // therefore the next step after "SETUP_A_MATCH" is "ADD_COURTS"
        assertEquals(
                MissingContentNextStep.ADD_COURTS,
                MissingContentNextStep.entries.toSet().filter { !it.isMatchStep }.getFirstStep()
        )
    }
}