package com.isi.passwordgame.entities

data class Game (
    var uuid: String,
    var players: List<Player>,
    var startTime: String,
    var allocatedTime: String,
    var captureDiameter: Double,
    var captureTime: Int,
    var mapDiameter: Double,
    var mapCenter: Coordinates,
    var isJoinEligible: Boolean,
    var isFinished: Boolean,
    var events: List<String>,
    var password: String
) {

    // Empty constructor
    constructor() : this(
        "",
        mutableListOf(),
        "",
        "",
        0.0,
        0,
        0.0,
        Coordinates(0.0, 0.0), // You may need to provide default values for nested data classes or objects
        false,
        false,
        mutableListOf(),
        ""
    )
    fun build(): Game {
        return Game(
            uuid,
            players,
            startTime,
            allocatedTime,
            captureDiameter,
            captureTime,
            mapDiameter,
            mapCenter,
            isJoinEligible,
            isFinished,
            events,
            password
        )
    }
}
