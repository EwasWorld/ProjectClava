package com.eywa.projectclava.main.features.screens.matchUp

import com.eywa.projectclava.main.features.screens.ScreenState


data class CreateMatchState(
        /**
         * ids of the players who will form the next match
         */
        val selectedPlayers: Iterable<Int> = setOf(),
) : ScreenState