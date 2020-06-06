package com.visualdust.kexiebabbo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.visualdust.kexiebabbo.data.Resources

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button = findViewById<Button>(R.id.startTrial_button)
        button.setOnClickListener {
            var text = findViewById<EditText>(R.id.userID_editText).text.toString()
            if (text.length == 10)
            {
                Resources.userID = text.toLong()
                val intent = Intent(this, ActivityAttendance::class.java)
                startActivity(intent)
            }
            else Toast.makeText(applicationContext, "你应该输入一个十位的学号", Toast.LENGTH_LONG).show()
        }
    }
}
