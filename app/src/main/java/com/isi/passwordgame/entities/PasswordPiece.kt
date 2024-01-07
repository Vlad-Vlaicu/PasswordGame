package com.isi.passwordgame.entities

data class PasswordPiece(
    var passwordPiece: String,
    var passwordPiecePlace: Int
) {
    constructor() : this(
        passwordPiece = "",
        passwordPiecePlace = 0
    )
}