package com.example.chatgptapp1.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar
import java.util.Date

@Entity(tableName = "chats-table")
data class Chat(

    @PrimaryKey(autoGenerate = true)
    val localDbId: Int? = 0,

    val userId: String? = "",
    val chatTitleTxt: String? = "",
    val messageList: ArrayList<Message>? = ArrayList(),
    var chatDocumentId: String? = "",
    val chatOwner: String? = "",
    val timestamp: Date? = Calendar.getInstance().time

): Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createTypedArrayList(Message)!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readSerializable() as? Date

    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(localDbId!!)
        parcel.writeString(userId)
        parcel.writeString(chatTitleTxt)
        parcel.writeTypedList(messageList)
        parcel.writeString(chatDocumentId)
        parcel.writeSerializable(timestamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Chat> {
        override fun createFromParcel(parcel: Parcel): Chat {
            return Chat(parcel)
        }

        override fun newArray(size: Int): Array<Chat?> {
            return arrayOfNulls(size)
        }
    }

}


/**class ListConverter {
    @TypeConverter
    fun fromListToJson(list:    MutableList<Message>?): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun jsonToList(json: String): MutableList<Message> {
        //val type = object : TypeToken<ArrayList<Message>>() {}.type
        return Gson().fromJson(json, Array<Message>::class.java).toMutableList()
    }
}*/

class TypeConverter {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }


    @TypeConverter
    fun fromArrayListToJson(list: ArrayList<Message>?): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun jsonToArrayList(json: String): ArrayList<Message> {
        val arrayType = object : TypeToken<ArrayList<Message>>() {}.type
        return Gson().fromJson(json, arrayType)
    }
}