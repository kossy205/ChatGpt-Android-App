package com.example.chatgptapp1.activities

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatgptapp1.ChatGptApi
import com.example.chatgptapp1.MessageAdapter
import com.example.chatgptapp1.R
import com.example.chatgptapp1.ServiceBuilder
import com.example.chatgptapp1.models.Answer
import com.example.chatgptapp1.models.CompletionParameters
import com.example.chatgptapp1.models.Message
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private var etPrompt: TextInputEditText? = null
    private var tvError: TextView? = null
    private var tvQuestion: TextView? = null
    private var messageAdapter: MessageAdapter? = null
    private var rvMessage: RecyclerView? = null
    private lateinit var messageList: ArrayList<Message>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etPrompt = findViewById(R.id.et_prompt)
        //tvError = findViewById(R.id.error_text)
        //tvQuestion = findViewById(R.id.idTVQuestion)
        rvMessage = findViewById(R.id.recycler_view_chat)
        messageList = ArrayList()


        setUpMessageAdapter()


        etPrompt?.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND){

                messageList.add(Message(etPrompt?.text.toString(), "user"))
                messageAdapter?.notifyDataSetChanged()
                networkAvailability()
                setUpMessageAdapter()
                // setting response tv on below line.
                //tvQuestion?.text = etPrompt?.text.toString()
                //tvAnswer?.text = "Please wait..."

                return@OnEditorActionListener true
            }
            false
        })

    }

    fun networkAvailability(){
        if(ServiceBuilder.isNetworkAvailable(this)){
            retrofitApiCall()
        }else{
            Toast.makeText(this, "no internet, pls turn on your internet or check ur data sub",
                Toast.LENGTH_LONG).show()
        }
    }

    private fun setUpMessageAdapter(){
        rvMessage?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        messageAdapter = MessageAdapter(messageList)
        rvMessage?.adapter = messageAdapter
        rvMessage?.scrollToPosition(messageAdapter!!.itemCount - 1)
    }

    private fun retrofitApiCall(){

        val apiKey = ServiceBuilder.API_KEY

        val chatGptApi: ChatGptApi = ServiceBuilder.buildService(ChatGptApi::class.java)
        val prompTv = etPrompt?.text

        val completionParameters = CompletionParameters(
            "text-davinci-003",
            "$prompTv",
            200,
            0.4,
            1,
            1,
            false,
            //"\n"
        )

        //makes editText empty after each question asked
        etPrompt?.setText("")

        val requestCall = chatGptApi.getAnswer(apiKey, completionParameters)

        requestCall.enqueue(object: Callback<Answer>{
            override fun onResponse(call: Call<Answer>, response: Response<Answer>) {
                if (response.isSuccessful){
                    val answer: Answer = response.body()!!
                    val choices = answer.choices
                    val textFromChoices = choices[0].text

                    messageList.add(Message(textFromChoices, "bot"))
                    messageAdapter?.notifyDataSetChanged()
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

}