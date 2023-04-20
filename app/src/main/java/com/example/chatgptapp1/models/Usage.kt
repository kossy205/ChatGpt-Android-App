package com.example.chatgptapp1.models

import java.io.Serializable

data class Usage(
    val prompt_token: Int,
    val completion_tokens: Int,
    val total_tokens: Int
): Serializable
