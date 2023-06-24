package com.example.chatgptapp1

import android.app.Application

class ChatApp(): Application() {
    val db by lazy {
        ChatDataBase.getInstance(this)
    }
}