package com.visualdust.kexiebabbo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.visualdust.kexiebabbo.data.Resources as VDR

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button = findViewById<Button>(R.id.startTrial_button)
        val userIDEditText = findViewById<EditText>(R.id.userID_editText)

        val pref = getSharedPreferences("${VDR.appName}.userID", Context.MODE_PRIVATE)
        val userID = pref.getLong("userID", 0)
        if (userID != 0L)
            userIDEditText.setText(userID.toString())

        button.setOnClickListener {
            val text = userIDEditText.text.toString()
            val editor = getSharedPreferences("${VDR.appName}.userID", Context.MODE_PRIVATE).edit()
            editor.putLong("${VDR.appName}.userID", text.toLong())
            editor.apply()
            if (text.length == 10) {
                VDR.userID = text.toLong()
                editor.putLong("userID", text.toLong())
                editor.apply()
                val intent = Intent(this, ActivityAttendance::class.java)
                startActivity(intent)
            } else Toast.makeText(applicationContext, "你应该输入一个十位的学号", Toast.LENGTH_LONG).show()
        }
    }
}
