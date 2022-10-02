package com.eywa.projectclava.main.model

data class MainState(
        val people: Iterable<Player> = listOf(),
        val previousMatches: Iterable<Match> = listOf(),
        val ongoingMatches: Iterable<Match> = listOf(),
        val upcomingMatches: List<Player> = listOf(),
)