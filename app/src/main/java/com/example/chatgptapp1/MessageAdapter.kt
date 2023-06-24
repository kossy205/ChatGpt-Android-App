package com.example.chatgptapp1

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatgptapp1.models.Message
import java.util.ArrayList

    class MessageAdapter(private val context: Context, private val messageList: ArrayList<Message>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class UserMessageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val userMsgTv: TextView = itemView.findViewById(R.id.user_tv)
    }

    class BotMessageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val botMsgTv: TextView = itemView.findViewById(R.id.chatgpt_tv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View

        return if (viewType ==0){
            view = LayoutInflater.from(context).inflate(R.layout.rv_user_item,parent,false)
            UserMessageViewHolder(view)
        }else{
            view = LayoutInflater.from(context).inflate(R.layout.rv_bot_item,parent,false)
            BotMessageViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    // used to set the data for a specific view, eg the text of a textview to display
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val sender = messageList[position].sender
        when (sender){
            "user" -> (holder as UserMessageViewHolder).userMsgTv.text = messageList[position].message

            "bot" -> (holder as BotMessageViewHolder).botMsgTv.text = messageList[position].message
        }
    }


    override fun getItemViewType(position: Int): Int {
        return when (messageList[position].sender){
            "user" -> 0
            "bot" -> 1
            else -> 1
        }
    }
}