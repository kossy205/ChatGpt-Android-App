package com.example.chatgptapp1.activities

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatgptapp1.ChatGptApi
import com.example.chatgptapp1.R
import com.example.chatgptapp1.ServiceBuilder
import com.example.chatgptapp1.models.Answer
import com.example.chatgptapp1.models.CompletionParameters
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private var etPrompt: TextInputEditText? = null
    private var tvAnswer: TextView? = null
    private var tvQuestion: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etPrompt = findViewById(R.id.et_prompt)
        tvAnswer = findViewById(R.id.answer_text)
        tvQuestion = findViewById(R.id.idTVQuestion)



        etPrompt?.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                // setting response tv on below line.
                tvQuestion?.text = etPrompt?.text.toString()
                tvAnswer?.text = "Please wait..."



                //
                networkAvailability()

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
                    tvAnswer?.text = textFromChoices

                }else{
                    val rc = response.code()
                    when(rc){
                        400 ->{
                            tvAnswer?.text = "Error: 400"
                        }
                        404 ->{
                            tvAnswer?.text = "Error: 404"
                        }else ->{
                        tvAnswer?.text = "Error: ${response.code()}"
                    }
                    }

                }
            }

            override fun onFailure(call: Call<Answer>, t: Throwable) {

                tvAnswer?.text = "error:$t"
                Log.i("watin sup", "${t.toString()}")
                Toast.makeText(this@MainActivity, "error:${t.toString()}", Toast.LENGTH_LONG).show()
            }

        })

    }

}