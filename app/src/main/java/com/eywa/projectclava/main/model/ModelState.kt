package com.eywa.projectclava.main.model

import com.eywa.projectclava.main.common.MissingContentNextStep

data class ModelState(
        val courts: Iterable<Court> = listOf(),
        val matches: Iterable<Match> = listOf(),
        val players: Iterable<Player> = listOf(),
) {
    fun getMissingContent(maxStep: MissingContentNextStep? = null): Set<MissingContentNextStep> {
        val state = mutableSetOf<MissingContentNextStep>()

        if (players.none() || players.all { it.isArchived }) state.add(MissingContentNextStep.ADD_PLAYERS)
        else if (players.none { it.enabled }) state.add(MissingContentNextStep.ENABLE_PLAYERS)

        if (courts.none()) state.add(MissingContentNextStep.ADD_COURTS)
        else if (courts.none { it.enabled }) state.add(MissingContentNextStep.ENABLE_COURTS)

        if (matches.none()) {
            state.addAll(
                    MissingContentNextStep.values()
                            .filter { it.isMatchStep && (maxStep == null || it.ordinal <= maxStep.ordinal) }
            )
            return state
        }
        if (matches.none { it.isFinished }) state.add(MissingContentNextStep.COMPLETE_A_MATCH)
        if (matches.none { it.isCurrent }) state.add(MissingContentNextStep.START_A_MATCH)
        if (matches.none { it.isNotStarted }) state.add(MissingContentNextStep.SETUP_A_MATCH)
        return state.filter { maxStep == null || it.ordinal <= maxStep.ordinal }.toSet()
    }
}