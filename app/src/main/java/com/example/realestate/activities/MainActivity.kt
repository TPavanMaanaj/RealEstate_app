package com.example.realestate.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.realestate.MyUtils
import com.example.realestate.R
import com.example.realestate.databinding.ActivityMainBinding
import com.example.realestate.fragments.ChatsListFragment
import com.example.realestate.fragments.HomeFragment
import com.example.realestate.fragments.ProfileFragment
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root) // Remove the second call to setContentView

        firebaseAuth = FirebaseAuth.getInstance()

        if (firebaseAuth.currentUser == null) {
            startLoginOptionsActivity()
        } else {
            showHomeFragment()
        }

        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->

            val itemId = menuItem.itemId

            when (itemId) {
                R.id.item_home -> {
                    showHomeFragment()
                    true
                }
                R.id.item_chats -> {
                    if (firebaseAuth.currentUser == null) {
                        MyUtils.toast(this, "Login Required...")
                        false
                    } else {
                        showChatsListFragment()
                        true
                    }
                }
                R.id.item_favorite -> {
                    if (firebaseAuth.currentUser == null) {
                        MyUtils.toast(this, "Login Required...")
                        false
                    } else {
                        showFavouriteListFragment()
                        true
                    }
                }
                R.id.item_profile -> {
                    if (firebaseAuth.currentUser == null) {
                        MyUtils.toast(this, "Login Required...")
                        false
                    } else {
                        showProfileFragment()
                        true
                    }
                }
                else -> false
            }
        }
    }

    private fun showHomeFragment() {
        binding.toolbarTitleTv.text = "Home"

        val homeFragment = HomeFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentFl.id, homeFragment, "HomeFragment")
        fragmentTransaction.commit()
    }

    private fun showChatsListFragment() {
        binding.toolbarTitleTv.text = "Chats"

        val chatsListFragment = ChatsListFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentFl.id, chatsListFragment, "ChatListFragment")
        fragmentTransaction.commit()
    }

    private fun showFavouriteListFragment() {
        binding.toolbarTitleTv.text = "Favourite"

        val favouriteListFragment = ChatsListFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentFl.id, favouriteListFragment, "FavouriteListFragment")
        fragmentTransaction.commit()
    }

    private fun showProfileFragment() {
        binding.toolbarTitleTv.text = "Profile"

        val profileFragment = ProfileFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentFl.id, profileFragment, "ProfileFragment")
        fragmentTransaction.commit()
    }

    private fun startLoginOptionsActivity() {
        startActivity(Intent(this, LoginOptionsActivity::class.java))
    }
}
