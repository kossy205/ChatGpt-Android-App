package com.example.chatgptapp1.models

import android.os.Parcel
import android.os.Parcelable

//@Entity(tableName = "message-table")
data class Message(

    //@PrimaryKey(autoGenerate = true)
    //var id: Int = 0,

    //var userId: String? = null,

    // this role means "sendBy"
    var role: String? = null,

    var content: String? = null

): Parcelable{
    constructor(parcel: Parcel) : this(
        //parcel.readInt(),
        //parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        //parcel.writeInt(id)
        //parcel.writeString(userId)
        parcel.writeString(content)
        parcel.writeString(role)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Message> {
        override fun createFromParcel(parcel: Parcel): Message {
            return Message(parcel)
        }

        override fun newArray(size: Int): Array<Message?> {
            return arrayOfNulls(size)
        }
    }

}
