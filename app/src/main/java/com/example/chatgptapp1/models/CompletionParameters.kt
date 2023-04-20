package com.example.chatgptapp1.models

import java.io.Serializable

data class CompletionParameters(
    val model: String,
    val prompt: String,
    val max_tokens: Int,
    val temperature: Double,
    val top_p: Int = 1,
    val n: Int = 1,
    val stream: Boolean,
    //val logprobs: Null = null,
    //val stop: String = "\n"
): Serializable
