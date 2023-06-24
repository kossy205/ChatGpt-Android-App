package com.example.chatgptapp1

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatgptapp1.models.Chat

class ChatAdapter(
    private val context: Context,
    private var chatList: ArrayList<Chat>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var onClickListener: OnClickListener? = null

    private class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val chatTitleTv: TextView = itemView.findViewById(R.id.chat_title_tv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
            R.layout.rv_chat_item,
            parent,
            false
        ))
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chat = chatList[position]

        if(holder is MyViewHolder){
            holder.chatTitleTv.text = chat.chatTitleTxt
        }
        holder.itemView.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick(position, chat)
            }
        }
    }

    /**
     * A function for OnClickListener where the Interface is the expected parameter..
     */
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    /**
     * An interface for onclick items.
     */
    interface OnClickListener {
        fun onClick(position: Int, chat: Chat)
    }


}