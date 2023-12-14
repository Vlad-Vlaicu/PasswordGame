package com.isi.passwordgame.entities

enum class PlayerTag(val tagName: String) {
    HACKER("Hacker Agent"),
    FBI("FBI Agent"),
    HACKER_LEADER("Hacker Leader"),
    FBI_LEADER("FBI Leader"),
    CAPTURED("Captured"),
    GAME_MASTER("Game Master")
}