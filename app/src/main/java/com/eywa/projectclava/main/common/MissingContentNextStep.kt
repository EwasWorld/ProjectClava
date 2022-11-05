package com.eywa.projectclava.main.common

import com.eywa.projectclava.main.mainActivity.MainNavRoute
import com.eywa.projectclava.main.mainActivity.NavRoute

/**
 * When a screen is blank, this indicates what the user should do next to add content
 */
enum class MissingContentNextStep(
        val nextStepsText: String,
        val buttonRoute: NavRoute,
        val isMatchStep: Boolean = false,
) {
    ADD_PLAYERS(
            nextStepsText = "First we need to add some players",
            buttonRoute = MainNavRoute.ADD_PLAYER,
    ),
    ENABLE_PLAYERS(
            nextStepsText = "We need to mark some players as present",
            buttonRoute = MainNavRoute.ADD_PLAYER,
    ),
    SETUP_A_MATCH(
            nextStepsText = "We need to set up a match",
            buttonRoute = MainNavRoute.CREATE_MATCH,
            isMatchStep = true,
    ),
    ADD_COURTS(
            nextStepsText = "We need to add some courts",
            buttonRoute = MainNavRoute.ADD_COURT,
    ),
    ENABLE_COURTS(
            nextStepsText = "We need to mark some courts as available",
            buttonRoute = MainNavRoute.ADD_COURT,
    ),
    START_A_MATCH(
            nextStepsText = "We need to start a match",
            buttonRoute = MainNavRoute.MATCH_QUEUE,
            isMatchStep = true,
    ),
    COMPLETE_A_MATCH(
            nextStepsText = "We need to mark a match as completed",
            buttonRoute = MainNavRoute.ONGOING_MATCHES,
            isMatchStep = true,
    )
    ;

    companion object {
        fun Iterable<MissingContentNextStep>?.getFirstStep(): MissingContentNextStep? {
            if (this == null || none()) return null
            if (count() == 1) return first()

            return values().toSet()
                    // Everything that is present (not missing)
                    .minus(this)
                    .filter { state ->
                        state.isMatchStep
                                // If we're on the queued matches screen,
                                // it doesn't matter if there are completed matches
                                && state.ordinal < this.maxOf { it.ordinal }
                    }
                    // If there are matches, take the first missing state after the highest present state
                    // E.g. if START_A_MATCH is done, return COMPLETE_A_MATCH
                    .maxByOrNull { it.ordinal }
                    ?.let { maxCompleted -> filter { it.ordinal > maxCompleted.ordinal }.minOf { it } }
            // Otherwise just the first step
                    ?: minByOrNull { it.ordinal }
        }
    }
}