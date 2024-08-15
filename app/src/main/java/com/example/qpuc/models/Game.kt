package com.example.qpuc.models

data class Game(
    val numberOfPlayers: Int = 0,
    val players: Map<String, Player> = emptyMap()
)
