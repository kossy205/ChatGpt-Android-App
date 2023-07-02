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
import com.example.chatgptapp1.ChatGptApiForTitle
import com.example.chatgptapp1.Constants
import com.example.chatgptapp1.FirebaseClass
import com.example.chatgptapp1.MessageAdapter
import com.example.chatgptapp1.R
import com.example.chatgptapp1.ServiceBuilder
import com.example.chatgptapp1.models.Answer
import com.example.chatgptapp1.models.Chat
import com.example.chatgptapp1.models.CompletionParameters
import com.example.chatgptapp1.models.CompletionParametersForTitle
import com.example.chatgptapp1.models.Message
import com.example.chatgptapp1.models.Title
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
    val mFirestore = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        tilSendIcon = findViewById(R.id.TIL_prompt)
        etPrompt = findViewById(R.id.et_prompt)
        rvMessage = findViewById(R.id.recycler_view_chat)
        toolBar = findViewById(R.id.toolbar_main_activity)
        drawerLayout = findViewById(R.id.drawer_layout_main)
        _chatDocumentId = ""
        Log.i("1_chatDocumentId", "$_chatDocumentId")
        //newChatId = ""
        //Log.i("1newChatId", "$newChatId")



        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            _chatDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
            Log.i("2_chatDocumentId", "$_chatDocumentId")
            mChatDetails = Chat(chatDocumentId = _chatDocumentId)
        }

        if (intent.hasExtra(Constants.NEW_CHAT_DOCUMENT_ID)) {
            newChatId = intent.getStringExtra(Constants.NEW_CHAT_DOCUMENT_ID)!!
            mChatDetails = Chat(chatDocumentId = newChatId)
            Log.i("1newChatId", "$newChatId")
        }





        if(_chatDocumentId.isNotEmpty()) {
            FirebaseClass().getChatDetails(this@MainActivity, _chatDocumentId)
            Log.i("3_chatDocumentId", "$_chatDocumentId")
            //upDateChatTitle(mChatDetails)

        }else{
            getNewChatLatestId(newChatId)
            //FirebaseClass().getChatDetailsForNewChat_latest_chatDocumentId(this@MainActivity)
            //
            Log.i("2newChatId", "$newChatId")
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
    fun getNewChatLatestId(newId: String){
        newChatId = newId
        //return newChatId
        Log.i("3newChatId", "$newChatId")

        //FirebaseClass().getChatDetails(this@MainActivity, newChatId)
        //FirebaseClass().upDateChatDocumentId(this@MainActivity, newChatId)
    }

    fun getChatMessagesDetails(chat: Chat){
        mChatDetails = chat
        setUpActionBar()
        //upDateChatTitle(mChatDetails)
        // best place to call the setUpMessageAdapter(). Immediately after it sets up actionBar, it sets up adapter too
        setUpMessageAdapter()
        messageList = mChatDetails.messageList!!
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
        mChatDetails.messageList?.add(Message(content = textFromUser, role = "user"))
        messageAdapter?.notifyDataSetChanged()

        FirebaseClass().createOrAddMessagesToMessageList(this@MainActivity, mChatDetails!!)
    }

    fun txtBot_createOrAddMessageListFirebase(textFromBot: String){
        mChatDetails.messageList?.add(Message(content = textFromBot, role = "assistant"))
        messageAdapter?.notifyDataSetChanged()

        FirebaseClass().createOrAddMessagesToMessageList(this@MainActivity, mChatDetails!!)
    }


    fun newChat_txtUser_createOrAddMessageListFirebase(textFromUser: String){
        mChatDetails.messageList?.add(Message(content = textFromUser, role = "user"))
        messageAdapter?.notifyDataSetChanged()

        FirebaseClass().newChat_createOrAddMessagesToMessageList(this@MainActivity, mChatDetails!!, newChatId)
        Log.i("4newChatId", "$newChatId")
    }

    fun newChat_txtBot_createOrAddMessageListFirebase(textFromBot: String){
        mChatDetails.messageList?.add(Message(content = textFromBot, role = "assistant"))
        messageAdapter?.notifyDataSetChanged()

        FirebaseClass().newChat_createOrAddMessagesToMessageList(this@MainActivity, mChatDetails!!, newChatId)
        Log.i("5newChatId", "$newChatId")
        Log.i("2text_answer", "${mChatDetails.messageList?.get(1)}")
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
                Log.i("4_chatDocumentId", "$_chatDocumentId")

            }else{
                newChat_txtUser_createOrAddMessageListFirebase(etPrompt?.text.toString())
                //upDateChatTitleForNewChat()
                //Toast.makeText(this, "A new chat created",Toast.LENGTH_LONG).show()
            }
            retrofitApiCall()
            //upDateChatTitle()
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
        //val prompTv = etPrompt?.text

        val completionParameters = CompletionParameters(
            "gpt-3.5-turbo",
            messageList,
            200,
            0.9,
            1,
            1,
            false,
            //"\n"
        )

        Log.i("aanswerlist", "$messageList")

        val requestCall = chatGptApi.getAnswer(apiKey, completionParameters)

        requestCall.enqueue(object: Callback<Answer>{
            override fun onResponse(call: Call<Answer>, response: Response<Answer>) {
                if (response.isSuccessful){
                    val answer: Answer = response.body()!!
                    val choices = answer.choices
                    val textFromChoices = choices[0].message.content
                    val answerText = textFromChoices.trim()

                    Log.i("aanswer", "$choices")

                    if(_chatDocumentId.isNotEmpty()) {
                        txtBot_createOrAddMessageListFirebase(answerText)
                        Log.i("5_chatDocumentId", "$_chatDocumentId")
                        upDateChatTitle()
                        Log.i("11text_answer", "$answer")
                    }else{
                        newChat_txtBot_createOrAddMessageListFirebase(answerText)
                        upDateChatTitle()
                        Log.i("22text_answer", "$answerText")
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

            val chatTitle = mChatDetails.messageList?.get(1)?.content.toString()
            Log.i("here chatTitle", "$chatTitle")

            val titlePrompt = "what is the main point of the following text: $chatTitle \n summarize in not more than 35 characters"

            val title: ArrayList<Title> = ArrayList()
            title.add(0, Title("system", titlePrompt ))
            //title.add(1, Title("user", chatTitle))

        if(_chatDocumentId.isNotEmpty()){
            if (mChatDetails.chatTitleTxt == Constants.NEWLY_CREATED_CHAT) {
                titleSummaryRetrofitApiCall(title)
            }
        }else{
            titleSummaryRetrofitApiCall(title)
        }
    }


    private fun titleSummaryRetrofitApiCall(prompt: ArrayList<Title>){

        val apiKey = ServiceBuilder.API_KEY
        val chatGptApiForTitle: ChatGptApiForTitle = ServiceBuilder.buildService(ChatGptApiForTitle::class.java)

        val completionParameters = CompletionParametersForTitle(
            "gpt-3.5-turbo",
            prompt,
            200,
            0.9,
            1,
            1,
            false,
            //"\n"
        )
        Log.i("title_answerlist", "$prompt")

        val requestCall = chatGptApiForTitle.getAnswer(apiKey, completionParameters)

        requestCall.enqueue(object: Callback<Answer>{
            override fun onResponse(call: Call<Answer>, response: Response<Answer>) {
                if (response.isSuccessful){
                    val answer: Answer = response.body()!!
                    val choices = answer.choices
                    val textFromChoices = choices[0].message.content

                    Log.i("222 na here chatTitle", "$textFromChoices")

                    val chat = Chat(chatTitleTxt = textFromChoices)
                    val summarizedText = textFromChoices.trim()
                    Log.i("main_chatTitle", "$summarizedText")

                    Log.i("title_answer", "$choices")

                    if(_chatDocumentId.isNotEmpty()){
                        FirebaseClass().upDateChatTitle(this@MainActivity, mChatDetails, summarizedText)
                        Log.i("122chatTitle", "$summarizedText")
                        Log.i("1_chatDocumentId", "$_chatDocumentId")
                    }else{
                        FirebaseClass().upDateChatTitleNewChat(this@MainActivity, summarizedText, newChatId)
                        Log.i("112chatTitle", "$summarizedText")
                        Log.i("112newChatId", "$newChatId")
                    }

                    //this enables the recycler view to autoscroll to the lastest item
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