package com.example.realestate.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.realestate.MyUtils
import com.example.realestate.R
import com.example.realestate.databinding.ActivityProfileEditBinding
import com.example.realestate.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileEditBinding

    private val TAG = "PROFILE_EDIT_TAG"

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    private var mUserType = ""

    private var imageUri: Uri?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadMyInfo()

        binding.toolbarBackBtn.setOnClickListener {
            finish()
        }
        binding.profileImagePickFab.setOnClickListener{
            imagePickDialog()
        }
    }

    private fun imagePickDialog(){
        val popupMenu = PopupMenu(this,binding.profileImagePickFab)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Camera")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Gallery")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {item->

            val itemId = item.itemId

            if(itemId == 1){
              /**  Log.d(TAG, "imagePickDialog: Camera Clicked")
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    requestCameraPermission.launch(arrayOf(Manifest.permission.CAMERA))
                }else{
                    requestCameraPermission.launch(arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ))
                }**/
            }else if(itemId == 2){
                Log.d(TAG,"imagePickDialog: Gallery Clicked ")
                pickImageGallery()
            }

            true
        }
    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){result->
        Log.d(TAG, "requestCameraPermissions: result $result")

        var areAllGranted = true

        for (isGranted in result.values){

            areAllGranted = areAllGranted && isGranted
        }
        if (areAllGranted) {
                Log.d(TAG,"requestCameraPermissions: All Permissions granted")
                 pickImageCamera()
        }
        else{

            Log.d(TAG,"requestCameraPermissions: All or either one permission denied...!")
            MyUtils.toast(this,"Camera or Storage or both permissions denied...!")
        }
    }
    private fun pickImageCamera(){
        Log.d(TAG,"pickImageCamera:")

        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "temp image")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"temp image description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)

    }

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){result ->

        if(result.resultCode == Activity.RESULT_OK)
        {
            Log.d(TAG,"cameraActivityResultLauncher: Image Captured: $imageUri")

            try{
                Glide.with(this)
                    .load(imageUri)
                    .placeholder(R.drawable.person_black)
                    .into(binding.profileIv)
            }catch (e:Exception){
                Log.e(TAG,"cameraActivityResultLauncher:",e)
            }
        }
        else{
            Log.d(TAG,"cameraActivityResultLauncher: Cancelled")
            MyUtils.toast(this,"Cancelled...!")
        }
    }

    private fun pickImageGallery(){
        Log.d(TAG,"pickImageGallery: ")
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("image/*")
        galleryActivityResultLauncher.launch(intent)
    }
    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

        } else {
        }
    }
    private fun loadMyInfo(){
        Log.d(TAG, "loadMyInfo: ")
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val dob = "${snapshot.child("dob").value}"
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val phoneCode = "${snapshot.child("phoneCode").value}"
                    val phoneNumber = "${snapshot.child("phoneNumber").value}"
                    val profileImageUrl = "${snapshot.child("profileImageUrl").value}"
                    var timestamp = "${snapshot.child("timestamp").value}"
                    mUserType = "${snapshot.child("userType").value}"

                    if(mUserType == MyUtils.USER_TYPE_EMAIL || mUserType == MyUtils.USER_TYPE_GOOGLE){
                        binding.emailTil.isEnabled = false
                        binding.emailEt.isEnabled = false
                    }
                    else{
                        binding.phoneNumberTil.isEnabled = false
                        binding.phoneNumberEt.isEnabled = false
                        binding.countryCodePicker.isEnabled = false
                    }

                    binding.fullNameTv.text = name
                    binding.nameEt.setText(name)
                    binding.emailEt.setText(email)
                    binding.dobEt.setText(dob)
                    binding.phoneNumberEt.setText(phoneNumber)

                    try{
                        val phoneCodeInt = phoneCode.replace("+","").toInt()
                        binding.countryCodePicker.setCountryForPhoneCode(phoneCodeInt)
                    }catch (e: Exception){
                        Log.e(TAG, "onDataChange: ",e)
                    }
                    try {
                        Glide.with(this@ProfileEditActivity)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.person_black)
                            .into(binding.profileIv)
                    }catch (e: Exception){
                        Log.e(TAG, "onDataChange: ",e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}