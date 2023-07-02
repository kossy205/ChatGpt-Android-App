package com.example.chatgptapp1.activities

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatgptapp1.ChatAdapter
import com.example.chatgptapp1.Constants
import com.example.chatgptapp1.FirebaseClass
import com.example.chatgptapp1.R
import com.example.chatgptapp1.models.Chat
import com.example.chatgptapp1.models.Message
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

class ChatActivity : AppCompatActivity() {

    var newChatBtn: LinearLayout? = null
    //private lateinit var mProgressDialog: Dialog
    private var rvChats: RecyclerView? = null
    private var chatAdapter: ChatAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        newChatBtn = findViewById(R.id.ll_new_chat)
        rvChats = findViewById(R.id.rv_chats)


        FirebaseClass().getChatList(this)


        newChatBtn?.setOnClickListener{
            //FirebaseAuth.getInstance().signOut()
            createChat()
        }

        if(FirebaseAuth.getInstance().currentUser == null){

            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    override fun onResume() {
        super.onResume()
        FirebaseClass().getChatList(this)
    }

    private fun createChat(){
        val chatTitle = Constants.NEWLY_CREATED_CHAT
        val userId = FirebaseClass().getCurrentUid()
        val messageList: ArrayList<Message> = ArrayList()
        val timestamp = Calendar.getInstance().time

        val chat = Chat(
            localDbId = 0,
            userId = userId,
            chatTitleTxt = chatTitle,
            messageList = messageList,
            chatOwner = userId,
            timestamp = timestamp
        )
        //val intent = Intent(this@ChatActivity, MainActivity::class.java)
        //intent.putExtra(Constants.NAME, mUserName)
        //intent.putExtra(Constants.DOCUMENT_ID, chat.chatDocumentId)
        //startActivity(intent)
        FirebaseClass().createChat(this, chat)
    }



    fun populateChatsOnRV(chatList: ArrayList<Chat>){

        rvChats?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        chatAdapter = ChatAdapter(this, chatList)
        rvChats?.adapter = chatAdapter
        rvChats?.scrollToPosition(chatAdapter!!.itemCount - 1)

        chatAdapter!!.setOnClickListener(object: ChatAdapter.OnClickListener{
            override fun onClick(position: Int, chat: Chat) {
                //TO DO("Not yet implemented")
                val intent = Intent(this@ChatActivity, MainActivity::class.java)
                intent.putExtra(Constants.DOCUMENT_ID, chat.chatDocumentId)
                startActivity(intent)
            }
        })

    }


    fun chatCreatedSuccessfully(){

        FirebaseClass()
            .getChatDetailsForNewChat_latest_chatDocumentId(this@ChatActivity){latestChatId ->

                val intent = Intent(this@ChatActivity, MainActivity::class.java)
                intent.putExtra(Constants.NEW_CHAT_DOCUMENT_ID, latestChatId)
                startActivity(intent)
        }
        //hideProgressDialog()

        setResult(Activity.RESULT_OK)
    }

    /**fun getNewChatLatestId(newId: String){
        newChatId = newId
        //return newChatId
        Log.i("3newChatId", "$newChatId")

        //FirebaseClass().getChatDetails(this@MainActivity, newChatId)
        //FirebaseClass().upDateChatDocumentId(this@MainActivity, newChatId)
    }*/

    /**fun showProgressDialog(text: String) {
        mProgressDialog = Dialog(this)

        /*Set the screen content from a layout resource.
        The resource will be inflated, adding all top-level views to the screen.*/
        mProgressDialog.setContentView(R.layout.dialog_progress)

        //mProgressDialog.tv_progress_text.text = text

        //Start the dialog and display it on screen.
        mProgressDialog.show()
    }*/

    fun hideProgressDialog() {
        //mProgressDialog.dismiss()
    }

}