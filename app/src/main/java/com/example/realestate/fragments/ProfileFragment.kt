package com.example.realestate.fragments

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.realestate.MyUtils
import com.example.realestate.R
import com.example.realestate.activities.MainActivity
import com.example.realestate.activities.ProfileEditActivity
import com.example.realestate.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    private val TAG ="PROFILE_TAG"

    private lateinit var mContext: Context

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

     override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
         binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = ProgressDialog(mContext)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        loadMyInfo()

        binding.logoutCv.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(mContext, MainActivity::class.java))
            activity?.finishAffinity()
        }
        binding.editProfileCv.setOnClickListener {
            startActivity(Intent(mContext, ProfileEditActivity::class.java))
        }
    }

    private fun loadMyInfo(){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object: ValueEventListener{
                override fun  onDataChange(snapshot: DataSnapshot){

                    Log.d(TAG,"onDataChange: ")

                    val dob = "${snapshot.child("dob").value}"
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val phoneCode = "${snapshot.child("phoneCode").value}"
                    val phoneNumber = "${snapshot.child("phoneNumber").value}"
                    val profileImageUrl = "${snapshot.child("profileImageUrl").value}"
                    var timestamp = "${snapshot.child("timestamp").value}"
                    val userType = "${snapshot.child("userType").value}"

                    val phone = phoneCode+phoneNumber

                    if(timestamp == "null"){
                        timestamp = "0"
                    }
                     val formattedDate = MyUtils.formatTimestampDate(timestamp.toLong())

                    binding.emailTv.text = email
                    binding.fullNameTv.text = name
                    binding.dobTv.text = dob
                    binding.phoneTv.text = phone
                    binding.memberSinceTv.text = formattedDate

                    if (userType == MyUtils.USER_TYPE_EMAIL){
                        val isVerified = firebaseAuth.currentUser!!.isEmailVerified
                        Log.d(TAG,"onDataChange: isVerified: $isVerified")
                        if (isVerified){
                            binding.verifyAccountCv.visibility = View.GONE
                            binding.verificationTv.text = "Verified"
                        }
                        else{
                            binding.verifyAccountCv.visibility =  View.VISIBLE
                            binding.verificationTv.text = "Not Verified"
                        }
                    }
                    else{
                        binding.verifyAccountCv.visibility = View.GONE
                        binding.verificationTv.text = "Verified"
                    }

                      try{
                          Glide.with(mContext)
                              .load(profileImageUrl)
                              .placeholder(R.drawable.person_black)
                              .into(binding.profileIv)
                      }
                      catch(e: Exception){
                          Log.e(TAG,"onDataChnage: ",e)
                      }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}