package com.example.chatgptapp1.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message-table")
data class Message(

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    var message: String,

    var sender: String
)
