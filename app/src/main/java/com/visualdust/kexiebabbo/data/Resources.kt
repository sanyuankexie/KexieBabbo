package com.visualdust.kexiebabbo.data

import okhttp3.MediaType.Companion.toMediaType

class Resources {
    companion object {
        const val serviceIP = "123.56.2.196"
        const val servicePort = 8080
        const val serviceProtocol = "http"
        const val serviceProtocolPrefix = "http://"
        const val div = "/"
        const val projectSpace = "kexie"
        const val appName = "kexieBabbo"
        const val serviceAddress =
            "$serviceProtocolPrefix$serviceIP:$servicePort$div$projectSpace"

        val JsonType = "application/json; charset=utf-8".toMediaType()

        const val signInAPIName = "signIn"
        const val signOutAPIName = "signOut"
        const val complaintAPIName = "Complaint"
        const val attendancesListAPIName = ""
        const val rankTopFiveAttendanceAPIName = "TopFive"
        const val getTimeAPIName = "getTime"

        var userID :Long = -266555
    }
}