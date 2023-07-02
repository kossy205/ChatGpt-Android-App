package com.example.chatgptapp1

import com.example.chatgptapp1.models.Answer
import com.example.chatgptapp1.models.CompletionParameters
import com.example.chatgptapp1.models.CompletionParametersForTitle
import retrofit2.Call
import retrofit2.http.*

interface ChatGptApi {
    @Headers("Content-Type: application/json")
    @POST("chat/completions")
    fun getAnswer(
        @Header("Authorization") apiKey: String,
        @Body request: CompletionParameters
    ): Call<Answer>

}
interface ChatGptApiForTitle {
    @Headers("Content-Type: application/json")
    @POST("chat/completions")
    fun getAnswer(
        @Header("Authorization") apiKey: String,
        @Body request: CompletionParametersForTitle
    ): Call<Answer>

}