package com.isi.passwordgame.entities

data class Coordinates(
    var xCoordinate: Double,
    var yCoordinate: Double
) {
    constructor() : this(
        0.0,0.0
    )
}

