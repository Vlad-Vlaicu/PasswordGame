package com.isi.passwordgame.entities

data class Game (
    val uuid: String,
    val players: List<Player>,
    val startTime: String,
    val allocatedTime: String,
    val captureDiameter: Double,
    val captureTime: Int,
    val mapDiameter: Double,
    val mapCenter: Coordinates,
    val isJoinEligible: Boolean,
    val isFinished: Boolean,
    val events: List<String>,
    val password: String
)