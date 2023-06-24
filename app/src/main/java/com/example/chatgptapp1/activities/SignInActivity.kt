package com.example.chatgptapp1.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.example.chatgptapp1.R
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit


class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var toolBarSignIn: androidx.appcompat.widget.Toolbar? = null
    private var btnSignIn: Button? = null
    private var etPhoneSignIn: EditText? = null
    private var progressBarSignIn: ProgressBar? = null
    private var numberEnteredSignIn: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()
        toolBarSignIn = findViewById(R.id.toolbar_sign_in_activity)
        btnSignIn = findViewById(R.id.btnSignIn)
        etPhoneSignIn = findViewById(R.id.et_phone_signIn)
        progressBarSignIn = findViewById(R.id.progressBarSignUp)
        progressBarSignIn?.visibility = View.GONE


        //calling functions
        setActionBar()


        //button linking to other activity
        btnSignIn?.setOnClickListener {

            numberEnteredSignIn = etPhoneSignIn?.text?.trim().toString()

            if(numberEnteredSignIn!!.isNotEmpty()){

                if (numberEnteredSignIn?.length == 10){
                    numberEnteredSignIn = "+234$numberEnteredSignIn"

                    btnSignIn?.visibility = View.GONE
                    progressBarSignIn?.visibility = View.VISIBLE

                    val options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(numberEnteredSignIn!!)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)
                }else{
                    Toast.makeText(this, "please enter correct phone number ", Toast.LENGTH_LONG).show()
                }

            }else{
                Toast.makeText(this, "please enter phone number", Toast.LENGTH_LONG).show()
            }

        }

    }


    private fun setActionBar(){
        setSupportActionBar(toolBarSignIn)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        toolBarSignIn?.setNavigationOnClickListener { onBackPressed() }
    }


    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            //Log.d(TAG, "onVerificationCompleted:$credential")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            //Log.w(TAG, "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            //Log.d(TAG, "onCodeSent:$verificationId")
            // Save verification ID and resending token so we can use them later

            val intent = Intent(this@SignInActivity, OTPSignInActivity::class.java)
            intent.putExtra("OTPSignIn" , verificationId)
            intent.putExtra("resendTokenSignIn" , token)
            intent.putExtra("phoneNumberSignIn" , numberEnteredSignIn)
            startActivity(intent)
            finish()
            progressBarSignIn?.visibility = View.GONE
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    //Log.d(TAG, "signInWithCredential:success")
                    //val user = task.result?.user
                    Toast.makeText(this, "authentication successful", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, ChatActivity::class.java))

                } else {
                    // Sign in failed, display a message and update the UI
                    //Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }

    override fun onStart() {
        super.onStart()
        //checks if the current user is already authenticated
        if(auth.currentUser != null){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }
}