package com.example.chatgptapp1.models

import java.io.Serializable

data class Answer(
    val id: String,
    val `object`: String,
    val created : Long,
    val model: String,
    val choices : List<Choices>,
    val usage: Usage
): Serializable


