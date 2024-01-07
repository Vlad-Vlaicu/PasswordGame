package com.isi.passwordgame.entities

data class Player(
    var userId: String,
    var username: String,
    var playerTag: List<PlayerTag>,
    var playerPosition: Coordinates,
    var powerUps: List<PowerUp>,
    var isCaught: Boolean,
    var passwordPiece: PasswordPiece
) {
    // Empty constructor
    constructor() : this(
        userId = "",
        username = "",
        playerTag = mutableListOf(),
        playerPosition = Coordinates(0.0, 0.0),
        powerUps = mutableListOf(),
        isCaught = false,
        passwordPiece = PasswordPiece()
    )
}