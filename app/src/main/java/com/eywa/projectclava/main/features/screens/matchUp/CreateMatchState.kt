package com.eywa.projectclava.main.features.screens.matchUp

import com.eywa.projectclava.main.features.screens.ScreenState
import com.eywa.projectclava.main.model.Player


data class CreateMatchState(
        /**
         * the players who will form the next match
         */
        val selectedPlayers: Iterable<Player> = setOf(),
) : ScreenState