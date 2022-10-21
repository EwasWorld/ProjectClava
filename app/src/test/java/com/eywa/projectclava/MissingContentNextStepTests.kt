package com.eywa.projectclava

import com.eywa.projectclava.main.model.MissingContentNextStep
import com.eywa.projectclava.main.model.MissingContentNextStep.Companion.getFirstStep
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class MissingContentNextStepTests {
    @Test
    fun testMissingContentOrder_Simple() {
        assertEquals(null, null.getFirstStep())
        assertEquals(null, listOf<MissingContentNextStep>().getFirstStep())
        assertEquals(
                MissingContentNextStep.ADD_PLAYERS,
                MissingContentNextStep.values().toSet().getFirstStep()
        )
    }

    @Test
    fun testMissingContentOrder_CompletedSteps() {
        assertEquals(
                MissingContentNextStep.COMPLETE_A_MATCH,
                MissingContentNextStep.values().toSet().minus(MissingContentNextStep.START_A_MATCH).getFirstStep()
        )
    }

    @Test
    fun testMissingContentOrder_NoMatches() {
        assertEquals(
                MissingContentNextStep.ADD_PLAYERS,
                MissingContentNextStep.values().toSet().filter { !it.isMatchStep }.getFirstStep()
        )
    }
}