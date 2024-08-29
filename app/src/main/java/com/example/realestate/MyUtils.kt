package com.example.realestate

import android.content.Context
import android.widget.Toast
import android.text.format.DateFormat
import java.util.Calendar

object MyUtils {

    const val USER_TYPE_GOOGLE = "Google"
    const val USER_TYPE_EMAIL = "Email"
    const val USER_TYPE_PHONE = "Phone"

    fun toast(context: Context, message: String) {

        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


    fun timestamp(): Long{
        return System.currentTimeMillis()
    }

    fun formatTimestampDate(timestamp: Long): String{
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp

        return DateFormat.format("dd/MM/yyyy", calendar).toString()
    }
}