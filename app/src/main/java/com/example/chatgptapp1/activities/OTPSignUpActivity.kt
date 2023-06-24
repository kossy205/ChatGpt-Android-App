package com.example.chatgptapp1.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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

class OTPSignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var tvResendOtp: TextView? = null
    private var btnVerify: Button? = null
    private var etOTP1: EditText? = null
    private var etOTP2: EditText? = null
    private var etOTP3: EditText? = null
    private var etOTP4: EditText? = null
    private var etOTP5: EditText? = null
    private var etOTP6: EditText? = null
    private lateinit var progressBar: ProgressBar



    private lateinit var OTP: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var phoneNumber: String
    private lateinit var name: String
    private lateinit var email: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otpsign_up)

        auth = FirebaseAuth.getInstance()

        auth = FirebaseAuth.getInstance()
        progressBar = findViewById(R.id.progressBarOTP)
        btnVerify = findViewById(R.id.btn_verify)
        tvResendOtp = findViewById(R.id.tv_resend_otp)
        etOTP1 = findViewById(R.id.et_otp1)
        etOTP2 = findViewById(R.id.et_otp2)
        etOTP3 = findViewById(R.id.et_otp3)
        etOTP4 = findViewById(R.id.et_otp4)
        etOTP5 = findViewById(R.id.et_otp5)
        etOTP6 = findViewById(R.id.et_otp6)

        OTP = intent.getStringExtra("OTP").toString()
        resendToken = intent.getParcelableExtra("resendToken")!!
        phoneNumber = intent.getStringExtra("phoneNumber").toString()
        name = intent.getStringExtra("name").toString()
        email = intent.getStringExtra("email").toString()


        addTextChangeListener()
        resendOTPVisibility()

        btnVerify?.setOnClickListener {
            val typedOTP = etOTP1?.text.toString() + etOTP2?.text.toString() + etOTP3?.text.toString() + etOTP4?.text.toString() +etOTP5?.text.toString() + etOTP6?.text.toString()

            if(typedOTP.isNotEmpty()) {
                if (typedOTP.length == 6) {
                    progressBar.visibility = View.VISIBLE
                    btnVerify?.visibility = View.GONE
                    val credentials: PhoneAuthCredential = PhoneAuthProvider.getCredential(OTP, typedOTP)
                    signInWithPhoneAuthCredential(credentials)
                } else {
                    Toast.makeText(this, "pls, enter correct otp", Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(this, "enter otp", Toast.LENGTH_LONG).show()
            }
        }

        tvResendOtp?.setOnClickListener {
            resendVerificationCode()
            resendOTPVisibility()
        }
    }

    private fun addTextChangeListener(){
        etOTP1?.addTextChangedListener(EditTextWatcher(etOTP1!!))
        etOTP2?.addTextChangedListener(EditTextWatcher(etOTP2!!))
        etOTP3?.addTextChangedListener(EditTextWatcher(etOTP3!!))
        etOTP4?.addTextChangedListener(EditTextWatcher(etOTP4!!))
        etOTP5?.addTextChangedListener(EditTextWatcher(etOTP5!!))
        etOTP6?.addTextChangedListener(EditTextWatcher(etOTP6!!))
    }

    // to move the cursor one a number is entered in a box
    inner class EditTextWatcher(private val view: View): TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun afterTextChanged(p0: Editable?) {
            val text = p0.toString()
            when(view.id){
                R.id.et_otp1 -> if (text.length == 1) etOTP2?.requestFocus()
                R.id.et_otp2 -> if (text.length == 1) etOTP3?.requestFocus()
                R.id.et_otp3 -> if (text.length == 1) etOTP4?.requestFocus()
                R.id.et_otp4 -> if (text.length == 1) etOTP5?.requestFocus()
                R.id.et_otp5 -> if (text.length == 1) etOTP6?.requestFocus()
                R.id.et_otp6 -> if (text.length == 1) etOTP6?.requestFocus()
            }
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

                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    var registeredPhoneNumber = firebaseUser.phoneNumber?.toLong()
                    val user = User(firebaseUser.uid, name, email, registeredPhoneNumber!!)
                    FirebaseClass().registerUser(this@OTPSignUpActivity, user)

                    startActivity(Intent(this, ChatActivity::class.java))
                    finish()

                } else {
                    // Sign in failed, display a message and update the UI
                    //Log.w(TAG, "signInWithCredential:failure", task.exception)

                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this, "verification failed, code was invalid", Toast.LENGTH_LONG).show()
                    }else{
                        Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                    }
                    // Update UI
                }
            }
    }

    private fun resendVerificationCode(){
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .setForceResendingToken(resendToken)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
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

            OTP = verificationId
            resendToken = token
            btnVerify?.visibility = View.VISIBLE
            progressBar.visibility = View.GONE

        }
    }

    override fun onStart() {
        super.onStart()
        progressBar.visibility = View.GONE
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, SignUpActivity::class.java))
        finish()
    }

    fun resendOTPVisibility(){
        etOTP1?.setText("")
        etOTP2?.setText("")
        etOTP3?.setText("")
        etOTP4?.setText("")
        etOTP5?.setText("")
        etOTP6?.setText("")

        tvResendOtp?.visibility = View.GONE
        tvResendOtp?.isEnabled = false

        Handler(Looper.myLooper()!!).postDelayed({
            tvResendOtp?.visibility = View.VISIBLE
            tvResendOtp?.isEnabled = true
        }, 60000)
    }

    fun userRegisteredSuccessfully(){
        Toast.makeText(this, "authentication successful", Toast.LENGTH_LONG).show()
    }
    fun userRegisteredFailure(message: String){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

}