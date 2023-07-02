package com.example.chatgptapp1

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.chatgptapp1.activities.ChatActivity
import com.example.chatgptapp1.activities.MainActivity
import com.example.chatgptapp1.activities.OTPSignUpActivity
import com.example.chatgptapp1.activities.SignUpActivity
import com.example.chatgptapp1.models.Chat
import com.example.chatgptapp1.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Query

class FirebaseClass {

    val mFirestore = FirebaseFirestore.getInstance()

    fun registerUser(activity: Activity, userInfo: User){
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUid())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                if(activity is SignUpActivity) {
                    activity.userRegisteredSuccessfully()
                }else if(activity is OTPSignUpActivity){
                    activity.userRegisteredSuccessfully()
                }

            }
            .addOnFailureListener {e ->
                Log.e(
                    activity.javaClass.simpleName,
                    "Error writing document",
                    e
                )
            }
    }

    fun createChat(activity: ChatActivity, chat: Chat){
        mFirestore.collection(Constants.CHAT)
            .document()
            .set(chat, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "chat created successfully.")

                Toast.makeText(activity, "chats created successfully.", Toast.LENGTH_SHORT).show()

                activity.chatCreatedSuccessfully()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a chat.",
                    e
                )
            }
    }

    fun getChatList(activity: Activity){
        mFirestore.collection(Constants.CHAT)
            .orderBy(Constants.TIMESTAMP, Query.Direction.ASCENDING)
            .whereEqualTo(Constants.USER_ID, getCurrentUid())
            .get()
            .addOnSuccessListener {querySnapshot ->

                Log.e(activity.javaClass.simpleName, querySnapshot.documents.toString())

                val chatsList: ArrayList<Chat> = ArrayList()
                for(document in querySnapshot.documents){
                    val chat = document.toObject(Chat::class.java)
                    chat?.chatDocumentId = document.id
                    chatsList.add(chat!!)

                    Log.i("ddocument Id", "${chat.chatDocumentId}")
                }

                when(activity){
                    is ChatActivity ->{
                        activity.populateChatsOnRV(chatsList)
                    }
                    is MainActivity ->{
                        // Do something
                    }
                }
            //activity.populateChatsOnRV(chatsList)
            }
            .addOnFailureListener { e ->

                when(activity){
                    is ChatActivity ->{
                        activity.hideProgressDialog()
                    }
                    is MainActivity ->{
                        // Do something
                    }
                }

                //activity.hideProgressDialog()
                Log.i(activity.javaClass.simpleName, "Error while getting chatList.", e)
            }
    }


    /**
     * A function to get chat details using the id of the chat clicked
     */
    fun getChatDetails(activity: MainActivity, documentId: String){
        mFirestore.collection(Constants.CHAT)
            .document(documentId)
            .get()
            .addOnSuccessListener {document ->

                val chat = document.toObject(Chat::class.java)
                if (chat != null) {
                    chat.chatDocumentId = document.id
                }

                if (chat != null) {
                    activity.getChatMessagesDetails(chat)
                }
            }
    }

    /**
     * If a new chat is created and right inside the chat, it seems not to have the chatDetails, which means...
     * ...the documentId at this point cant be accessed...
     * ...that is why this function was created to get newly created chatDetails
     *
     * A function to get chat details using the id of the chat clicked
     */

    fun getChatDetailsForNewChat_latest_chatDocumentId(activity: Activity, callback: (String) -> Unit){
        mFirestore.collection(Constants.CHAT)
            .orderBy(Constants.TIMESTAMP, Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val latestChatDocument = querySnapshot.documents[0]
                    val latestChatDocumentId = latestChatDocument.id
                    // Do something with the latestChatDocumentId
                    upDateChatDocumentId(latestChatDocumentId, latestChatDocumentId)
                    if(activity is MainActivity){
                        activity.getNewChatLatestId(latestChatDocumentId)
                    }
                    if(activity is ChatActivity){
                        //activity.getNewChatLatestId(latestChatDocumentId)
                    }

                    Log.i("latestChatDocumentId", "$latestChatDocumentId")
                    callback(latestChatDocumentId)
                } else {
                    if(activity is MainActivity){
                        activity.chatDocumentIsEmpty()
                    }
                    //Toast.makeText(MainActivity, "document is empty")
                }
            }
            .addOnFailureListener {

            }
    }



    /**
     * A function to create messages when they are being typed and sent to chatgpt, in the chat details.
     * It also acts as updating message to message list of the Chat
     */
    fun createOrAddMessagesToMessageList(activity: MainActivity, chat: Chat){

        val messageListHashMap = HashMap<String, Any>()
        messageListHashMap[Constants.MESSAGE_LIST] = chat.messageList!!

        mFirestore.collection(Constants.CHAT)
            .document(chat.chatDocumentId!!)
            .update(messageListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "MessageList updated successfully.")
                //activity.addMessageListSuccess()
            }
            .addOnFailureListener {e ->
                Log.e(activity.javaClass.simpleName, "Error while creating a updating message list.", e)
            }

    }



    fun upDateChatTitle(activity: MainActivity, chat: Chat, chatTitle: String){

        val chatTitleTxt = hashMapOf<String, Any>(
            Constants.CHAT_TITLE to chatTitle
        )

        mFirestore.collection(Constants.CHAT)
            .document(chat.chatDocumentId!!)
            .update(chatTitleTxt)
            .addOnSuccessListener {

                //activity.updataChatTitleSuccess()

            }
            .addOnFailureListener {e ->

            }

    }

    fun upDateChatTitleNewChat(activity: MainActivity, chatTitle: String, documentId: String){

        val chatTitleTxt = hashMapOf<String, Any>(
            Constants.CHAT_TITLE to chatTitle
        )

        mFirestore.collection(Constants.CHAT)
            .document(documentId)
            .update(chatTitleTxt)
            .addOnSuccessListener {
                Log.i("updateChatTitleSuccess", "$chatTitle")
                //activity.updateChatTitleSuccess()

            }
            .addOnFailureListener {e ->

            }

    }



    /**
     * If a new chat is created and right inside the chat, it seems not to have chatDetails, that is why this function...
     * ... was created to add newly created chat messages to the message list
     *
     * A function to add or create new messages to MessageList when they are being typed and sent to chatgpt, in the chat details.
     * It also acts as updating message to message list of the Chat
     *
     */
    fun newChat_createOrAddMessagesToMessageList(activity: MainActivity, chat: Chat, documentId: String){

        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.MESSAGE_LIST] = chat.messageList!!

        mFirestore.collection(Constants.CHAT)
            .document(documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "(New Chat)MessageList updated successfully.")
                Log.i("newCChatId", "$documentId")
                //activity.addMessageListSuccess()
            }
            .addOnFailureListener {e ->
                Log.e(activity.javaClass.simpleName, "Error while creating a (New Chat)MessageList.", e)
            }

    }

    fun upDateChatDocumentId(chatDocumentId: String, documentId: String){

        val chatDocumentIdd = hashMapOf<String, Any>(
            Constants.DOCUMENT_ID to chatDocumentId
        )

        mFirestore.collection(Constants.CHAT)
            .document(documentId)
            .update(chatDocumentIdd)
            .addOnSuccessListener {
                Log.i("updateChatTitleSuccess", "$chatDocumentId")
                Log.i("documentIdSuccess", "$documentId")
                //activity.updateChatTitleSuccess()

            }
            .addOnFailureListener {e ->

            }

    }


    fun getCurrentUid(): String{
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserId = ""
        if(currentUser != null){
            currentUserId = currentUser.uid
        }
        return currentUserId
    }

}