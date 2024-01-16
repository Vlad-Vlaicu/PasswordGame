package com.isi.passwordgame.entities

enum class PowerUpType(val powerUpName: String) {
    EXTEND_TIME("Time Extend"),
    SHORTEN_TIME("Time Shortened"),
    RESET_PASSWORD("Reset Password"),
    GUESS_PASSWORD("Guess Password"),
    SATELLITE_SCAN("Satellite Scan")
}