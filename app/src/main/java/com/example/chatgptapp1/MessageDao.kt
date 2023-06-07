package com.example.chatgptapp1

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.chatgptapp1.models.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert
    suspend fun insert(message: Message)

    @Query("SELECT * from `message-table`")
    fun fetchAllMessages(): Flow<List<Message>>
}