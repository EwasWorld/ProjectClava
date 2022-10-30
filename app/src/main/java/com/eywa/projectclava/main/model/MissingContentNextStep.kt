package com.eywa.projectclava.main.model

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
            buttonRoute = NavRoute.ADD_PLAYER,
    ),
    ENABLE_PLAYERS(
            nextStepsText = "We need to mark some players as present",
            buttonRoute = NavRoute.ADD_PLAYER,
    ),
    SETUP_A_MATCH(
            nextStepsText = "We need to set up a match",
            buttonRoute = NavRoute.CREATE_MATCH,
            isMatchStep = true,
    ),
    ADD_COURTS(
            nextStepsText = "We need to add some courts",
            buttonRoute = NavRoute.ADD_COURT,
    ),
    ENABLE_COURTS(
            nextStepsText = "We need to mark some courts as available",
            buttonRoute = NavRoute.ADD_COURT,
    ),
    START_A_MATCH(
            nextStepsText = "We need to start a match",
            buttonRoute = NavRoute.UPCOMING_MATCHES,
            isMatchStep = true,
    ),
    COMPLETE_A_MATCH(
            nextStepsText = "We need to mark a match as completed",
            buttonRoute = NavRoute.ONGOING_MATCHES,
            isMatchStep = true,
    )
    ;

    companion object {
        @Deprecated(
                message = "Moved to databaseState",
                replaceWith = ReplaceWith("databaseState.getMissingContent()")
        )
        fun getMissingContent(
                databaseState: DatabaseState
        ): Set<MissingContentNextStep> {
            val state = mutableSetOf<MissingContentNextStep>()

            if (databaseState.players.none()) state.add(ADD_PLAYERS)
            else if (databaseState.players.none { it.enabled }) state.add(ENABLE_PLAYERS)

            if (databaseState.courts.none()) state.add(ADD_COURTS)
            else if (databaseState.courts.none { it.enabled }) state.add(ENABLE_COURTS)

            if (databaseState.matches.none()) {
                state.addAll(values().filter { it.isMatchStep })
                return state
            }
            if (databaseState.matches.none { it.isFinished }) state.add(COMPLETE_A_MATCH)
            if (databaseState.matches.none { it.isCurrent }) state.add(START_A_MATCH)
            if (databaseState.matches.none { it.isNotStarted }) state.add(SETUP_A_MATCH)
            return state
        }

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