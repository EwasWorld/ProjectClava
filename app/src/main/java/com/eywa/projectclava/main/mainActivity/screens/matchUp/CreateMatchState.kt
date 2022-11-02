package com.eywa.projectclava.main.mainActivity.screens.matchUp

import com.eywa.projectclava.main.mainActivity.screens.ScreenState
import com.eywa.projectclava.main.model.Player


data class CreateMatchState(
        /**
         * the players who will form the next match
         */
        val selectedPlayers: Iterable<Player> = setOf(),
) : ScreenState