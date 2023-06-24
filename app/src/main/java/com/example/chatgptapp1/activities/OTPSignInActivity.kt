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
import com.example.chatgptapp1.R
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class OTPSignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var tvResendOtpSignIn: TextView? = null
    private var btnVerifySignIn: Button? = null
    private var etOTP1SignIn: EditText? = null
    private var etOTP2SignIn: EditText? = null
    private var etOTP3SignIn: EditText? = null
    private var etOTP4SignIn: EditText? = null
    private var etOTP5SignIn: EditText? = null
    private var etOTP6SignIn: EditText? = null
    private lateinit var progressBarSignIn: ProgressBar

    private lateinit var OTPSignIn: String
    private lateinit var resendTokenSignIn: PhoneAuthProvider.ForceResendingToken
    private lateinit var phoneNumberSignIn: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otpsign_in)


        auth = FirebaseAuth.getInstance()
        progressBarSignIn = findViewById(R.id.progressBarOTP_signIn)
        btnVerifySignIn = findViewById(R.id.btn_verify_signIn)
        tvResendOtpSignIn = findViewById(R.id.tv_resend_otp_signIn)
        etOTP1SignIn = findViewById(R.id.et_otp1_signIn)
        etOTP2SignIn = findViewById(R.id.et_otp2_signIn)
        etOTP3SignIn = findViewById(R.id.et_otp3_signIn)
        etOTP4SignIn = findViewById(R.id.et_otp4_signIn)
        etOTP5SignIn = findViewById(R.id.et_otp5_signIn)
        etOTP6SignIn = findViewById(R.id.et_otp6_signIn)


        OTPSignIn = intent.getStringExtra("OTPSignIn").toString()
        resendTokenSignIn = intent.getParcelableExtra("resendTokenSignIn")!!
        phoneNumberSignIn = intent.getStringExtra("phoneNumberSignIn").toString()


        resendOTPVisibility()
        addTextChangeListener()


        btnVerifySignIn?.setOnClickListener {
            val typedOTPSignIn = etOTP1SignIn?.text.toString() + etOTP2SignIn?.text.toString() + etOTP3SignIn?.text.toString() + etOTP4SignIn?.text.toString() +etOTP5SignIn?.text.toString() + etOTP6SignIn?.text.toString()

            if(typedOTPSignIn.isNotEmpty()) {
                if (typedOTPSignIn.length == 6) {
                    progressBarSignIn.visibility = View.VISIBLE
                    btnVerifySignIn?.visibility = View.GONE

                    val credentials: PhoneAuthCredential = PhoneAuthProvider.getCredential(OTPSignIn, typedOTPSignIn)
                    signInWithPhoneAuthCredential(credentials)
                } else {
                    Toast.makeText(this, "pls, enter correct otp", Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(this, "enter otp", Toast.LENGTH_LONG).show()
            }
        }

        tvResendOtpSignIn?.setOnClickListener {
            resendVerificationCode()
            resendOTPVisibility()
        }


    }

    private fun addTextChangeListener(){
        etOTP1SignIn?.addTextChangedListener(EditTextWatcher(etOTP1SignIn!!))
        etOTP2SignIn?.addTextChangedListener(EditTextWatcher(etOTP2SignIn!!))
        etOTP3SignIn?.addTextChangedListener(EditTextWatcher(etOTP3SignIn!!))
        etOTP4SignIn?.addTextChangedListener(EditTextWatcher(etOTP4SignIn!!))
        etOTP5SignIn?.addTextChangedListener(EditTextWatcher(etOTP5SignIn!!))
        etOTP6SignIn?.addTextChangedListener(EditTextWatcher(etOTP6SignIn!!))
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
                R.id.et_otp1_signIn -> if (text.length == 1) etOTP2SignIn?.requestFocus()
                R.id.et_otp2_signIn -> if (text.length == 1) etOTP3SignIn?.requestFocus() else if (text.isEmpty()) etOTP1SignIn?.requestFocus()
                R.id.et_otp3_signIn -> if (text.length == 1) etOTP4SignIn?.requestFocus() else if (text.isEmpty()) etOTP2SignIn?.requestFocus()
                R.id.et_otp4_signIn -> if (text.length == 1) etOTP5SignIn?.requestFocus() else if (text.isEmpty()) etOTP3SignIn?.requestFocus()
                R.id.et_otp5_signIn -> if (text.length == 1) etOTP6SignIn?.requestFocus() else if (text.isEmpty()) etOTP4SignIn?.requestFocus()
                R.id.et_otp6_signIn -> if (text.isEmpty()) etOTP5SignIn?.requestFocus()
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
                    Toast.makeText(this, "suthentication successful", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, ChatActivity::class.java))
                    finish()

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

    private fun resendVerificationCode(){
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumberSignIn)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .setForceResendingToken(resendTokenSignIn)
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

            progressBarSignIn.visibility = View.GONE
            btnVerifySignIn?.visibility = View.VISIBLE
            Toast.makeText(this@OTPSignInActivity, "crosscheck the otp or click on RESEND OTP below", Toast.LENGTH_LONG).show()
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

            OTPSignIn = verificationId
            resendTokenSignIn = token
            btnVerifySignIn?.visibility = View.VISIBLE
            progressBarSignIn.visibility = View.GONE

        }
    }

    override fun onStart() {
        super.onStart()
        progressBarSignIn.visibility = View.GONE
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, SignUpActivity::class.java))
        finish()
    }

    fun resendOTPVisibility(){
        etOTP1SignIn?.setText("")
        etOTP2SignIn?.setText("")
        etOTP3SignIn?.setText("")
        etOTP4SignIn?.setText("")
        etOTP5SignIn?.setText("")
        etOTP6SignIn?.setText("")

        tvResendOtpSignIn?.visibility = View.GONE
        tvResendOtpSignIn?.isEnabled = false

        Handler(Looper.myLooper()!!).postDelayed({
            tvResendOtpSignIn?.visibility = View.VISIBLE
            tvResendOtpSignIn?.isEnabled = true
        }, 60000)
    }

}