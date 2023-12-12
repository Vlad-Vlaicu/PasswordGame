package com.isi.passwordgame.entities

data class Player(
    val userId: String,
    val playerTag: List<PlayerTag>,
    val playerPosition: Coordinates,
    val powerUps: List<PowerUp>,
    val isCaught: Boolean,
    val passwordPiece: String
    )