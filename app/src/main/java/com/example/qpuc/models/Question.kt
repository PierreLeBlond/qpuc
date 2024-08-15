package com.example.qpuc.models

data class Question(
    var points: Int = 1,
    var state: String = "wait",
    var answeringPlayerName: String? = null
)