package com.example.chatgptapp1

import android.app.Application

class MessageApp(): Application() {
    val db by lazy {
        MessageDatabase.getInstance(this)
    }
}