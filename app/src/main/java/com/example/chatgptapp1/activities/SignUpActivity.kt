package com.example.chatgptapp1.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.chatgptapp1.FirebaseClass
import com.example.chatgptapp1.R
import com.example.chatgptapp1.models.User
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private var toolBarSignUp: Toolbar? = null
    private var etName: EditText? = null
    private var etPhone: EditText? = null
    private var etEmail: EditText? = null
    private var btnSignUp: Button? = null
    private var numberEntered: String? = null
    private var progressBarSignUp: ProgressBar? = null
    private var signInTv: TextView? = null

    private lateinit var name: String
    private lateinit var email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        //firebase auth
        auth = FirebaseAuth.getInstance()

        toolBarSignUp = findViewById(R.id.toolbar_sign_up_activity)
        etName = findViewById(R.id.et_name)
        etPhone = findViewById(R.id.et_phone)
        etEmail = findViewById(R.id.et_email)
        btnSignUp = findViewById(R.id.btn_SignUp)
        progressBarSignUp = findViewById(R.id.progressBarSignUp)
        progressBarSignUp?.visibility = View.GONE
        signInTv = findViewById(R.id.signIn_tv)


        //calling funtionns
        setActionBar()

        signInTv?.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        btnSignUp?.setOnClickListener {
            numberEntered = etPhone?.text?.trim().toString()

            if (numberEntered?.isNotEmpty()!!) {
                if (numberEntered?.length == 10) {

                    numberEntered = "+234$numberEntered"
                    btnSignUp?.visibility = View.GONE
                    progressBarSignUp?.visibility = View.VISIBLE

                    //(firebase)
                    val options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(numberEntered!!)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)
                } else {
                    Toast.makeText(this, "please enter phone number correctly", Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(this, "please enter phone number", Toast.LENGTH_LONG).show()
            }
        }
    }



    private fun setActionBar(){
        setSupportActionBar(toolBarSignUp)
        setSupportActionBar(toolBarSignUp)
        //toolBarSignUp?.title = mChatDetails.chatTitleTxt
        toolBarSignUp?.setNavigationIcon(R.drawable.ic_black_color_back_24dp)

        toolBarSignUp?.setNavigationOnClickListener { onBackPressed() }
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

            val nameSignUp: String = etName?.text.toString().trim()
            val emailSignUp: String = etEmail?.text.toString().trim()

            val intent = Intent(this@SignUpActivity, OTPSignUpActivity::class.java)
            intent.putExtra("OTP" , verificationId)
            intent.putExtra("resendToken" , token)
            intent.putExtra("phoneNumber" , numberEntered)
            intent.putExtra("name", nameSignUp)
            intent.putExtra("email", emailSignUp)
            startActivity(intent)
            finish()
            progressBarSignUp?.visibility = View.GONE
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

                    name = etName?.text.toString()
                    email = etEmail?.text.toString()

                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    var registeredPhoneNumber = firebaseUser.phoneNumber?.toLong()
                    val user = User(firebaseUser.uid, name, email, registeredPhoneNumber!!)
                    FirebaseClass().registerUser(this, user)

                    startActivity(Intent(this, MainActivity::class.java))

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

    fun userRegisteredSuccessfully(){
        Toast.makeText(this, "authentication successful", Toast.LENGTH_LONG).show()
    }
    fun userRegisteredFailure(){
        Toast.makeText(this, "registration failed", Toast.LENGTH_LONG).show()
    }


}