package com.example.chatgptapp1.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatgptapp1.ChatGptApi
import com.example.chatgptapp1.Constants
import com.example.chatgptapp1.FirebaseClass
import com.example.chatgptapp1.MessageAdapter
import com.example.chatgptapp1.R
import com.example.chatgptapp1.ServiceBuilder
import com.example.chatgptapp1.models.Answer
import com.example.chatgptapp1.models.Chat
import com.example.chatgptapp1.models.CompletionParameters
import com.example.chatgptapp1.models.Message
import com.example.chatgptapp1.models.User
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var etPrompt: TextInputEditText? = null
    private var tvError: TextView? = null
    private var tvQuestion: TextView? = null
    private var messageAdapter: MessageAdapter? = null
    private var rvMessage: RecyclerView? = null
    //private lateinit var messageList: ArrayList<Message>
    private var toolBar: Toolbar? = null
    private var drawerLayout: DrawerLayout? = null
    lateinit var mChatDetails: Chat
    lateinit var _chatDocumentId: String
    lateinit var newChatId: String
    var tilSendIcon: TextInputLayout? = null
    lateinit var messageList: ArrayList<Message>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        tilSendIcon = findViewById(R.id.TIL_prompt)
        etPrompt = findViewById(R.id.et_prompt)
        rvMessage = findViewById(R.id.recycler_view_chat)
        toolBar = findViewById(R.id.toolbar_main_activity)
        drawerLayout = findViewById(R.id.drawer_layout_main)
        newChatId = ""



        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            _chatDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }

        mChatDetails = Chat(chatDocumentId = _chatDocumentId)

        if(_chatDocumentId.isNotEmpty()) {
            FirebaseClass().getChatDetails(this@MainActivity, _chatDocumentId)
            //upDateChatTitle(mChatDetails)

        }else{
            FirebaseClass().getChatDetailsForNewChat(this@MainActivity, newChatId)
            getNewChatLatestId(newChatId)
            setUpActionBar()
            messageList = mChatDetails.messageList!!


            Toast.makeText(this, "A new chat created",Toast.LENGTH_LONG).show()
        }



        // room localDB fetch messages
        /**lifecycleScope.launch {
            val fetchedData = messageDao.fetchAllMessages().collect{
                val messageList = ArrayList(it)
                setUpMessageAdapter(messageList, messageDao)

                Log.i("fetched-Data", "")
            }
        }*/


        tilSendIcon?.setEndIconOnClickListener {
            networkAvailability()
            //this enables the recycler view to autoscroll to the lastest item
            // very important place tot call it
            setUpMessageAdapter()
           etPrompt?.setText("")
        }

        etPrompt?.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND){

                networkAvailability()

                //this enables the recycler view to autoscroll to the lastest item
                // very important place tot call it
                setUpMessageAdapter()
                etPrompt?.setText("")

                return@OnEditorActionListener true
            }
            false
        })

    }

    fun getChatMessagesDetails(chat: Chat){
        mChatDetails = chat
        setUpActionBar()
        //upDateChatTitle(mChatDetails)
        // best place to call the setUpMessageAdapter(). Immediately after it sets up actionBar, it sets up adapter too
        setUpMessageAdapter()
        messageList = mChatDetails.messageList!!
    }

    fun getNewChatLatestId(newId: String){
        newChatId = newId

    }

    fun chatDocumentIsEmpty(){
        Toast.makeText(this, "chat document Id is still empty", Toast.LENGTH_LONG).show()
    }

    private fun setUpActionBar(){
        setSupportActionBar(toolBar!!)
        toolBar?.title = mChatDetails.chatTitleTxt
        toolBar?.setNavigationIcon(R.drawable.ic_menu_24)

        // this specifies what to do when the toolbar icon is clicked
        toolBar?.setOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer(){
        if (drawerLayout!!.isDrawerOpen(GravityCompat.START)){
            drawerLayout!!.closeDrawer(GravityCompat.START)
        }else{
            drawerLayout!!.openDrawer(GravityCompat.START)
        }
    }

    override fun onStart() {
        super.onStart()
        checkUserRegOrSignInStatus()
    }

    fun txtUser_createOrAddMessageListFirebase(textFromUser: String){
        mChatDetails.messageList?.add(Message(message = textFromUser, sender = "user"))
        messageAdapter?.notifyDataSetChanged()

        FirebaseClass().createOrAddMessagesToMessageList(this@MainActivity, mChatDetails!!)
    }

    fun txtBot_createOrAddMessageListFirebase(textFromBot: String){
        mChatDetails.messageList?.add(Message(message = textFromBot, sender = "bot"))
        messageAdapter?.notifyDataSetChanged()

        FirebaseClass().createOrAddMessagesToMessageList(this@MainActivity, mChatDetails!!)
    }


    fun newChat_txtUser_createOrAddMessageListFirebase(textFromUser: String){
        mChatDetails.messageList?.add(Message(message = textFromUser, sender = "user"))
        messageAdapter?.notifyDataSetChanged()

        FirebaseClass().newChat_createOrAddMessagesToMessageList(this@MainActivity, mChatDetails!!, newChatId)
    }

    fun newChat_txtBot_createOrAddMessageListFirebase(textFromBot: String){
        mChatDetails.messageList?.add(Message(message = textFromBot, sender = "bot"))
        messageAdapter?.notifyDataSetChanged()

        FirebaseClass().newChat_createOrAddMessagesToMessageList(this@MainActivity, mChatDetails!!, newChatId)
    }

    fun checkUserRegOrSignInStatus(){
        if(auth.currentUser == null){

            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }



    /**fun storeMessageFromUserInLocalDB(messageDao: MessageDao){

        val etPrompt = etPrompt?.text.toString()

        //var messageList = messageList.add(Message(message = etPrompt, sender = "user"))

        lifecycleScope.launch{
            if (etPrompt.isNotEmpty()){
                messageDao.insert(Message(message = etPrompt, sender = "user"))
            }else{
                Toast.makeText(applicationContext, "prompt cannot be empty", Toast.LENGTH_LONG).show()
            }
        }
    }*/

    /**fun storeMessageFromBotInLocalDB(choiceTxt: String, messageDao: MessageDao){

        //val choiceTxt = etPrompt?.text.toString()

        //var messageList = messageList.add(Message(message = etPrompt, sender = "user"))

        lifecycleScope.launch{
                messageDao.insert(Message(message = choiceTxt, sender = "bot"))

                Toast.makeText(applicationContext, "prompt cannot be empty", Toast.LENGTH_LONG).show()
        }
    }*/

    fun networkAvailability(){
        if(ServiceBuilder.isNetworkAvailable(this)){

            if(_chatDocumentId.isNotEmpty()) {
                txtUser_createOrAddMessageListFirebase(etPrompt?.text.toString())
            }else{
                newChat_txtUser_createOrAddMessageListFirebase(etPrompt?.text.toString())
                //Toast.makeText(this, "A new chat created",Toast.LENGTH_LONG).show()
            }

            retrofitApiCall()
        }else{
            Toast.makeText(this, "no internet, pls turn on your internet or check ur data sub",
                Toast.LENGTH_LONG).show()
        }
    }


    private fun setUpMessageAdapter(){

        rvMessage?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        messageAdapter = MessageAdapter(this, mChatDetails.messageList!!)
        rvMessage?.adapter = messageAdapter
        rvMessage?.scrollToPosition(messageAdapter!!.itemCount - 1)
    }

    private fun retrofitApiCall(){

        val apiKey = ServiceBuilder.API_KEY

        val chatGptApi: ChatGptApi = ServiceBuilder.buildService(ChatGptApi::class.java)
        val prompTv = etPrompt?.text

        val completionParameters = CompletionParameters(
            "text-davinci-003",
            "The following is a conversation with an AI assistant called Kosi. The assistant is helpful, creative, clever, and very friendly. It was created by Kosi.\n$prompTv",
            200,
            0.9,
            1,
            1,
            false,
            //"\n"
        )

        val requestCall = chatGptApi.getAnswer(apiKey, completionParameters)

        requestCall.enqueue(object: Callback<Answer>{
            override fun onResponse(call: Call<Answer>, response: Response<Answer>) {
                if (response.isSuccessful){
                    val answer: Answer = response.body()!!
                    val choices = answer.choices
                    val textFromChoices = choices[0].text
                    val answerText = textFromChoices.trim()

                    if(_chatDocumentId.isNotEmpty()) {
                        txtBot_createOrAddMessageListFirebase(answerText)
                        upDateChatTitle()
                    }else{
                        newChat_txtBot_createOrAddMessageListFirebase(answerText)
                        //Toast.makeText(this, "A new chat created",Toast.LENGTH_LONG).show()
                    }

                    //this enables the recycycler view to autoscroll to the lastest item
                    // very important place tot call it
                    setUpMessageAdapter()

                    tvError?.text = textFromChoices
                    Log.i("textFromChoices", textFromChoices)

                }else{
                    val rc = response.code()
                    when(rc){
                        400 ->{
                            tvError?.text = "Error: 400"
                        }
                        404 ->{
                            tvError?.text = "Error: 404"
                        }else ->{
                        tvError?.text = "Error: ${response.code()}"
                        Log.i("responce code", "${response.code()}")
                    }
                    }
                }
            }

            override fun onFailure(call: Call<Answer>, t: Throwable) {

                tvError?.text = "error:$t"
                Log.i("watin sup", "${t.toString()}")
                Toast.makeText(this@MainActivity, "error:${t.toString()}", Toast.LENGTH_LONG).show()
            }

        })

    }


    fun upDateChatTitle(){
        val chatTitle = mChatDetails.messageList?.get(1)?.message.toString()

        Log.i("1 na here chatTitle", "$chatTitle")

        var titlePrompt = "In not more than 35 characters, what is the main point of the following text? : $chatTitle"


        if(mChatDetails.chatTitleTxt == ""){
            titleSummaryRetrofitApiCall(titlePrompt)
        }

    }


    private fun titleSummaryRetrofitApiCall(promptTv: String){

        val apiKey = ServiceBuilder.API_KEY
        val chatGptApi: ChatGptApi = ServiceBuilder.buildService(ChatGptApi::class.java)

        val completionParameters = CompletionParameters(
            "text-davinci-003",
            "$promptTv",
            200,
            0.9,
            1,
            1,
            false,
            //"\n"
        )

        val requestCall = chatGptApi.getAnswer(apiKey, completionParameters)

        requestCall.enqueue(object: Callback<Answer>{
            override fun onResponse(call: Call<Answer>, response: Response<Answer>) {
                if (response.isSuccessful){
                    val answer: Answer = response.body()!!
                    val choices = answer.choices
                    val textFromChoices = choices[0].text

                    Log.i("222 na here chatTitle", "$textFromChoices")


                    val chat = Chat(chatTitleTxt = textFromChoices)
                    val summarizedText = textFromChoices.trim()
                    FirebaseClass().upDateChatTitle(this@MainActivity, mChatDetails, summarizedText)


                    //this enables the recycycler view to autoscroll to the lastest item
                    // very important place tot call it
                    //setUpMessageAdapter()

                    tvError?.text = textFromChoices
                    Log.i("textFromChoices", textFromChoices)

                }else{
                    val rc = response.code()
                    when(rc){
                        400 ->{
                            tvError?.text = "Error: 400"
                        }
                        404 ->{
                            tvError?.text = "Error: 404"
                        }else ->{
                        tvError?.text = "Error: ${response.code()}"
                        Log.i("responce code", "${response.code()}")
                    }
                    }
                }
            }

            override fun onFailure(call: Call<Answer>, t: Throwable) {

                tvError?.text = "error:$t"
                Log.i("watin sup", "${t.toString()}")
                Toast.makeText(this@MainActivity, "error:${t.toString()}", Toast.LENGTH_LONG).show()
            }

        })

    }
}