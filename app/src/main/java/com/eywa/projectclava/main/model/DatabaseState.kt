package com.eywa.projectclava.main.model

data class DatabaseState(
        val courts: Iterable<Court> = listOf(),
        val matches: Iterable<Match> = listOf(),
        val players: Iterable<Player> = listOf(),
)