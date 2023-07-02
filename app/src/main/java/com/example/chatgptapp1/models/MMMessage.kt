package com.example.chatgptapp1.models

import java.io.Serializable

data class MMMessage(
    val role: String,
    val content: String
): Serializable
