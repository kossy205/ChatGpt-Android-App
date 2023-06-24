package com.example.chatgptapp1

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.chatgptapp1.models.Chat
import com.example.chatgptapp1.models.TypeConverter

@Database(entities = [Chat::class], version = 1)
@TypeConverters(TypeConverter::class) // Add this line

abstract class ChatDataBase: RoomDatabase() {

    //connects database to our dao
    abstract fun chatsDao(): ChatsDao

    companion object {

        @Volatile
        private var INSTANCE: ChatDataBase? = null


        fun getInstance(context: Context): ChatDataBase {
            // Multiple threads can ask for the database at the same time, ensure we only initialize
            // it once by using synchronized. Only one thread may enter a synchronized block at a
            // time.
            synchronized(this) {

                // Copy the current value of INSTANCE to a local variable so Kotlin can smart cast.
                // Smart cast is only available to local variables.
                var instance = INSTANCE

                // If instance is `null` make a new database instance.
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ChatDataBase::class.java,
                        "chat_database"
                    )
                        // Wipes and rebuilds instead of migrating if no Migration object.
                        // Migration is not part of this lesson. You can learn more about
                        // migration with Room in this blog post:
                        // https://medium.com/androiddevelopers/understanding-migrations-with-room-f01e04b07929
                        .fallbackToDestructiveMigration()
                        .build()
                    // Assign INSTANCE to the newly created database.
                    INSTANCE = instance
                }

                // Return instance; smart cast to be non-null.
                return instance
            }
        }
    }
}