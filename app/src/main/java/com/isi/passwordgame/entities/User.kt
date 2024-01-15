package com.isi.passwordgame.entities

data class User(
    var userID: String,
    var userName: String,
    var isInGame: Boolean,
    var currentGameId: String,
    var history: List<Game>
) {
    constructor() : this(
        "",
        "",
        false,
        "",
        mutableListOf()
    )
}