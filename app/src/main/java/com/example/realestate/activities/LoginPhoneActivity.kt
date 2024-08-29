package com.example.realestate.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import com.example.realestate.MyUtils
import com.example.realestate.R
import com.example.realestate.databinding.ActivityLoginOptionsBinding
import com.example.realestate.databinding.ActivityLoginPhoneBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

class LoginPhoneActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginPhoneBinding

    private val TAG = "LOGIN_PHONE_TAG"

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    private var forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null

    private lateinit var mCallBacks: OnVerificationStateChangedCallbacks

    private  var mVerificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.phoneInputRl.visibility = View.VISIBLE
        binding.otpInputRl.visibility = View.GONE


        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        phoneLoginCallBack()

        binding.toolbarBackBtn.setOnClickListener {
            finish()
        }
        binding.sendOtpBtn.setOnClickListener {
             validateData()
        }
        binding.resendOtpTv.setOnClickListener {
            if(forceResendingToken != null){

                resendVerificationCode()
            }
            else{
                MyUtils.toast(this, "Can't resend OTP! Try again...!")
            }
        }
        binding.verifyOtpBtn.setOnClickListener {

            val otp =binding.otpEt.text.toString().trim()
            if(otp.isEmpty()){
                binding.otpEt.error = "Enter OTP"
                binding.otpEt.requestFocus()
            }
            else if(otp.length<6){
                binding.otpEt.error = "OTP length must be 6 characters"
                binding.otpEt.requestFocus()
            }
            else{
                verifyPhoneNumberWithCode(otp)
            }
        }
    }

    private var phoneCode = ""
    private var phoneNumber = ""
    private var phoneNumberWithCode = ""

    private fun validateData(){

        phoneCode = binding.phoneCodeEt.selectedCountryCodeWithPlus
        phoneNumber = binding.phoneNumberEt.text.toString().trim()
        phoneNumberWithCode = phoneCode + phoneNumber

        Log.d(TAG, "validateData: Phone Code: $phoneCode")
        Log.d(TAG, "validateData: Phone Number: $phoneNumber")
        Log.d(TAG, "validateData: Phone Number With Code: $phoneNumberWithCode")

        if(phoneNumber.isEmpty()){
            binding.phoneNumberEt.error = "Enter Phone Number"
            binding.phoneNumberEt.requestFocus()
        }
        else{

            startPhoneNumberVerification()
        }
    }
    private fun startPhoneNumberVerification(){

        progressDialog.setMessage("Sending OTP to $phoneNumberWithCode")
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumberWithCode)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallBacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resendVerificationCode(){

        progressDialog.setMessage("Sending OTP to $phoneNumberWithCode")
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumberWithCode)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setForceResendingToken(forceResendingToken!!)
            .setCallbacks(mCallBacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyPhoneNumberWithCode(otp: String){

        Log.d(TAG,"verifyPhoneNumberWithCode: OTP: $otp")

        progressDialog.setMessage("Verifying OTP...")
        progressDialog.show()

        val credential = PhoneAuthProvider.getCredential(mVerificationId!!,otp)
        signInWithPhoneAuthCredential(credential)

    }

    private fun phoneLoginCallBack(){

        mCallBacks = object:OnVerificationStateChangedCallbacks(){
            override fun onCodeSent(verificationId : String, token: PhoneAuthProvider.ForceResendingToken){

                Log.d(TAG, "onCodeSent: ")
                mVerificationId = verificationId
                forceResendingToken = token

                progressDialog.dismiss()

                binding.phoneInputRl.visibility = View.GONE
                binding.otpInputRl.visibility = View.VISIBLE

                MyUtils.toast(this@LoginPhoneActivity, "OTP sent to $phoneNumberWithCode")

                binding.loginPhoneLabelTv.text = "Please type verification code sent to $phoneNumberWithCode"

            }
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential)

            }

            override fun onVerificationFailed(e: FirebaseException) {
                progressDialog.dismiss()
                MyUtils.toast(this@LoginPhoneActivity, "Failed to verify due to ${e.message}")

            }

        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential){

        Log.d(TAG, "signInWithPhoneAuthCredential")

        progressDialog.setMessage("Logging In ...")
        progressDialog.show()

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {authResult->
                Log.d(TAG, "signInWithPhoneAuthCredential")

                if(authResult.additionalUserInfo!!.isNewUser){

                    Log.d(TAG, "signInWithPhoneAuthCredential: Account created")
                }
                else{
                    Log.d(TAG, "signInWithPhoneAuthCredential: Logged In")

                    startActivity(Intent(this,MainActivity::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener {e->
                Log.e(TAG,"signInWithPhoneAuthCredential")

                progressDialog.dismiss()
                MyUtils.toast(this, "Login failed due to ${e.message}")
            }
    }

    private fun updateUserInfo(){
        Log.d(TAG,"updateUserInfo: ")

        progressDialog.setMessage("Saving User Info")
        progressDialog.show()

        val timestamp = MyUtils.timestamp()
        val registerUserUid = firebaseAuth.uid

        val hashMap = HashMap<String, Any>()
        hashMap["uid"] = registerUserUid!!
        hashMap["email"] = ""
        hashMap["name"] = ""
        hashMap["timestamp"] = timestamp
        hashMap["phoneCode"] = phoneCode
        hashMap["phoneNumber"] = phoneNumber
        hashMap["profileImageUrl"] = ""
        hashMap["dob"] = ""
        hashMap["userType"] = MyUtils.USER_TYPE_PHONE
        hashMap["token"] = ""

        val ref =FirebaseDatabase.getInstance().getReference("Users")
        ref.child(registerUserUid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateUserInfo: User info saved...")
                progressDialog.dismiss()

                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener {e->
                Log.e(TAG, "updateUserInfo: ",e)
                progressDialog.dismiss()
                MyUtils.toast(this,"Failed due to ${e.message}")
            }
    }
}