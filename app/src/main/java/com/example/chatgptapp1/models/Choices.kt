package com.example.chatgptapp1.models

import java.io.Serializable

data class Choices(
    val text: String,
    val index: Int,
    val logprobs: Any?,
    val finish_reason: String
): Serializable
