package com.example.realestate.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.example.realestate.MyUtils
import com.example.realestate.R
import com.example.realestate.databinding.ActivityLoginEmailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginEmailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginEmailBinding

    private val TAG = "LOGIN_EMAIL_TAG"

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.toolbarBackBtn.setOnClickListener {
            finish()
        }

        binding.loginBtn.setOnClickListener {
            validateData()
        }

        binding.noAccountTv.setOnClickListener{
            startActivity(Intent(this,RegisterEmailActivity::class.java))
        }
        binding.forgotPasswordTv.setOnClickListener {
            startActivity(Intent(this,ForgotPasswordActivity::class.java))
        }
    }

    private var email=""
    private var password=""

    private fun validateData(){

        email=binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString()

        Log.d(TAG, "validateData: Email: $email")
        Log.d(TAG, "validateData: Password: $password")

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

            binding.emailEt.error = "Invalid Email"
            binding.emailEt.requestFocus()
        }
        else if(password.isEmpty()) {
            binding.passwordEt.error  = "Enter Password"
            binding.passwordEt.requestFocus()
        }
        else{
               loginUser()
        }
    }
    private fun loginUser(){
        progressDialog.setMessage("Logging In...")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener{
                Log.d(TAG,"loginUser: Logged In...")
                progressDialog.dismiss()

                startActivity(Intent(this,MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener{e->
                Log.e(TAG,"loginUser ")
                progressDialog.dismiss()
                MyUtils.toast(this, "Failed due to ${e.message}")
            }
    }

}