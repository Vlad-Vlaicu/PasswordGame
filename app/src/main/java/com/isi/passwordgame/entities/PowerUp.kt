package com.isi.passwordgame.entities

data class PowerUp(
    var powerUpType: PowerUpType,
    var spawnTime: String,
    var coordinates: Coordinates
)