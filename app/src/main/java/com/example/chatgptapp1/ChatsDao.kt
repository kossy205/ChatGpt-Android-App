package com.example.chatgptapp1

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.chatgptapp1.models.Chat
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatsDao {

    @Insert
    suspend fun insert(chat: Chat)

    @Delete
    suspend fun delete(chat: Chat)

    @Query("SELECT * from `chats-table`")
    fun fetchAllChats(): Flow<List<Chat>>

}