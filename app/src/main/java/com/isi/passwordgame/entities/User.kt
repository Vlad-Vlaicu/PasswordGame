package com.isi.passwordgame.entities

data class User(
    val userID: String,
    val userName: String,
    val isInGame: Boolean,
    val currentGameId: String,
    val history: List<Game>
)