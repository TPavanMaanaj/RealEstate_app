package com.example.realestate.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.ProgressBar
import com.example.realestate.MyUtils
import com.example.realestate.R
import com.example.realestate.databinding.ActivityRegisterEmailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterEmailBinding

    private val TAG = "REGISTER_EMAIL_TAG"

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.toolbarBackBtn.setOnClickListener{
            finish()
        }

        binding.haveAccountTv.setOnClickListener {
            finish()
        }

        binding.registerBtn.setOnClickListener {
            validateData()
        }
    }

    private var email = ""
    private var password = ""
    private var cPassword = ""

    private fun validateData(){
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString()
        cPassword = binding.cPasswordEt.text.toString()

        Log.d(TAG,"validatData: Email: $email")
        Log.d(TAG,"validatData: Password: $password")
        Log.d(TAG,"validatData: Confirm Password: $cPassword")

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEt.error ="Invalid Email Pattern"
            binding.emailEt.requestFocus()
        }
        else if(password.isEmpty()){

            binding.passwordEt.error = "Enter Password"
            binding.passwordEt.requestFocus()
        }
        else if(password!=cPassword){
            binding.cPasswordEt.error = "Password doesn't match"
            binding.cPasswordEt.requestFocus()
        }
        else{
            registerUser()
        }
    }
    private fun registerUser(){
        progressDialog.setMessage("Creating Account")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.d(TAG,"registerUser: Register sucess")
                updateUserInfo()
            }
            .addOnFailureListener {e->

                Log.e(TAG, "registerUser", e)
                MyUtils.toast(this,"Failed due to ${e.message}")
                progressDialog.dismiss()
            }
    }
    private fun updateUserInfo(){

        progressDialog.setMessage("Saving User Info...")
        val timestamp = MyUtils.timestamp()
        val registeredUserEmail = firebaseAuth.currentUser!!.email
        val registeredUserUid = firebaseAuth.uid

        val hashMap = HashMap<String, Any>()
        hashMap["uid"] ="$registeredUserUid"
        hashMap["email"] ="$registeredUserEmail"
        hashMap["name"] =""
        hashMap["timestamp"] =timestamp
        hashMap["phoneCode"] =""
        hashMap["phoneNumber"] =""
        hashMap["user"] =MyUtils.USER_TYPE_EMAIL
        hashMap["token"] =""

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("$registeredUserUid")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG,"updateUserInfo: Info saved")
                progressDialog.dismiss()

                startActivity(Intent(this,MainActivity::class.java))
                    finishAffinity()
            }
            .addOnFailureListener { e->
                Log.e(TAG,"updateUserInfo:",e)
                MyUtils.toast(this,"Failed to save due to ${e.message}")
                progressDialog.dismiss()
            }
    }
}