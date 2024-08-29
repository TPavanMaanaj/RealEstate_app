package com.example.realestate.activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import com.example.realestate.MyUtils
import com.example.realestate.R
import com.example.realestate.databinding.ActivityForgotPasswordBinding
import com.example.realestate.databinding.ActivityLoginPhoneBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    private val TAG ="FORGOT_PASSWORD_TAG"

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.toolbarBackBtn.setOnClickListener {
            finish()
        }
        binding.submitBtn.setOnClickListener {

            validateData()
        }
    }
    private var email = ""
    private fun validateData(){
        email = binding.emailEt.text.toString().trim()
        Log.d(TAG, "validateData: Email: $email")

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            binding.emailEt.error = "Invalid Email Pattern!"
            binding.emailEt.requestFocus()
        }
        else{

            sendPasswordRecoveryInstructions()
        }
    }
    private fun sendPasswordRecoveryInstructions(){
        Log.d(TAG,"setPasswordRecoveryInstructions: ")

        progressDialog.setMessage("Sending password recovery instructions to $email")
        progressDialog.show()

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener{
                Log.d(TAG, "sendPasswordRecoveryInstructions: Instructions sent to $email")
                progressDialog.dismiss()
                MyUtils.toast(this,"Instructions to reset password sent to $email")
        }
            .addOnFailureListener {e->
                Log.e(TAG,"sendPasswordRecoveryInstructions: ",e)
                progressDialog.dismiss()
                MyUtils.toast(this,"Failed to send due to ${e.message}")
            }
    }
}