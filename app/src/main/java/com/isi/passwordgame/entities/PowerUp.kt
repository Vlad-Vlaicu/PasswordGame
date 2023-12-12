package com.isi.passwordgame.entities

data class PowerUp(
    val powerUpType: PowerUpType,
    val spawnTime: String,
    val coordinates: Coordinates
)