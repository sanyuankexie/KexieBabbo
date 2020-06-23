package com.visualdust.kexiebabbo.shortcuts

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.visualdust.kexiebabbo.ActivityAttendance
import com.visualdust.kexiebabbo.MainActivity
import com.visualdust.kexiebabbo.R
import com.visualdust.kexiebabbo.data.Resources as VDR

class SCRequestSignout : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_s_c_request_signout)
        if (VDR.userID <= 0L) {
            val pref = getSharedPreferences("${VDR.appName}.userID", Context.MODE_PRIVATE)
            val userID = pref.getLong("userID", -266555L)
            if (userID > 0L)
                VDR.userID = userID
        }
        if (VDR.userID > 0L) {
//            val bundle = Bundle()
//            bundle.putBoolean(VDR.signOutRequestBundleKey, true)
//            bundle.putBoolean(VDR.signInRequestBundleKey, false)
            val intent = Intent(this, ActivityAttendance::class.java)
            intent.putExtra(VDR.signOutRequestBundleKey, true)
            intent.putExtra(VDR.signInRequestBundleKey, false)
            startActivity(intent)
        } else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        finish()
    }
}
