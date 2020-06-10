package com.visualdust.kexiebabbo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.visualdust.kexiebabbo.agent.NonblockingSignAgent
import com.visualdust.kexiebabbo.agent.SignAgent
import java.lang.Exception
import java.util.function.Consumer
import com.visualdust.kexiebabbo.data.Resources as VDR

class MainActivity : AppCompatActivity() {

    val nbAgent = NonblockingSignAgent.getAgent()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button = findViewById<Button>(R.id.startTrial_button)
        val userIDEditText = findViewById<EditText>(R.id.userID_editText)
        var networkConnected = false

        val pref = getSharedPreferences("${VDR.appName}.userID", Context.MODE_PRIVATE)
        val userID = pref.getLong("userID", 0)
        if (userID != 0L)
            userIDEditText.setText(userID.toString())

        button.setOnClickListener {
            try {
                nbAgent.handleStatus(0L, Consumer {
                    if (SignAgent.UserStatus.NETFAILURE.equals(it))
                        runOnUiThread {
                            Toast.makeText(
                                applicationContext,
                                "我好像暂时连不上服务器(っ °Д °;)っ",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    else {
                        val text = userIDEditText.text.toString()
                        if (text.length == 10) {
                            VDR.userID = text.toLong()
                            val editor =
                                getSharedPreferences(
                                    "${VDR.appName}.userID",
                                    Context.MODE_PRIVATE
                                ).edit()
                            editor.putLong("userID", VDR.userID)
                            editor.apply()
                            val intent = Intent(this, ActivityAttendance::class.java)
                            startActivity(intent)
                        } else runOnUiThread {
                            Toast.makeText(applicationContext, "你应该输入一个十位的学号", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                })
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "我好像暂时连不上服务器(っ °Д °;)っ", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }
}
