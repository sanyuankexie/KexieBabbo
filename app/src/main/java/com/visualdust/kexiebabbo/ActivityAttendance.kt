package com.visualdust.kexiebabbo

import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.visualdust.kexiebabbo.agent.NonblockingSignAgent

class ActivityAttendance : AppCompatActivity() {
    val agent = NonblockingSignAgent.getAgent()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)
        
    }
}
