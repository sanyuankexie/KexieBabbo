package com.visualdust.kexiebabbo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.visualdust.kexiebabbo.data.Resources

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button = findViewById<Button>(R.id.startTrial_button)
        val ed_school_number = findViewById<EditText>(R.id.userID_editText)

        val pref = getSharedPreferences("school_number", Context.MODE_PRIVATE)
        val school_number = pref.getInt("school_number", 0)
        if (school_number != 0){
            ed_school_number.setText(school_number)
        }

        button.setOnClickListener {
            val text = ed_school_number.text.toString()
            val editor = getSharedPreferences("school_number", Context.MODE_PRIVATE).edit()
            val str_school_number = ed_school_number.text.toString()
            val school_number = str_school_number.toInt()
            editor.putInt("school_number", school_number)
            editor.apply()
            if (text.length == 10)
            {

                Resources.userID = text.toLong()
                editor.putLong("school_number", text.toLong())
                editor.apply()
                val intent = Intent(this, ActivityAttendance::class.java)
                startActivity(intent)
            }
            else Toast.makeText(applicationContext, "你应该输入一个十位的学号", Toast.LENGTH_LONG).show()
        }
    }
}
