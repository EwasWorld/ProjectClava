package com.eywa.projectclava.main.model

data class MainState(
        val people: Iterable<Player> = listOf(),
        val courts: Iterable<Court> = listOf(),
        val matches: Iterable<Match> = listOf(),
)