package com.visualdust.kexiebabbo.agent

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import com.visualdust.kexiebabbo.data.Resources as R

class SignAgent {
    private val client = OkHttpClient()

    class Attendance(
        var dept: String,
        var location: String,
        var id: Long,
        var name: String,
        var time: Double = 0.0
    ) {
        override fun toString(): String {
            return "[dept]$dept,[location]$location,[userid]$id,[username]$name,[time]$time"
        }
    }

    enum class UserStatus {
        ONLINE, OFFLINE
    }

    private fun post(url: String, body: String): Response = client.newCall(
        Request.Builder().url(url).post(body.toRequestBody(R.JsonType)).build()
    ).execute()

    private fun get(url: String): Response = client.newCall(
        Request.Builder().url(url).get().build()
    ).execute()

    fun postSignIn(id: Long) =
        post(R.serviceAddress + R.div + R.signInAPIName, "{\"userId\":\"$id\"}")

    fun signIn(id: Long): Boolean = postSignIn(id).body!!.string().contains("成功")

    fun postSignOut(id: Long) =
        post(R.serviceAddress + R.div + R.signOutAPIName, "{\"userId\":\"$id\"}")

    fun signOut(id: Long): Boolean = postSignOut(id).body!!.string().contains("成功")

    fun postComplaint(id: Long) =
        post(R.serviceAddress + R.div + R.complaintAPIName, "{\"userId\":\"$id\"}")

    fun complaint(id: Long) = postComplaint(id).body!!.string().contains("成功")

    private fun getAttendancesResponse() = get(R.serviceAddress + R.div + R.attendancesListAPIName)

    private fun getTopFiveAttendancesResponse() =
        get(R.serviceAddress + R.div + R.rankTopFiveAttendanceAPIName)

    fun getAttendanceList(): ArrayList<Attendance> {
        val response = getAttendancesResponse().body!!.string()
        val attendanceList = ArrayList<Attendance>()
        var pos_start = 0
        var pos_end = 0
        while (true) {
            pos_start = response.indexOf("dept\":\"", pos_end) + 7
            if (pos_start <= pos_end || pos_start == -1) break
            pos_end = response.indexOf("\",\"", pos_start)
            val dept = response.substring(pos_start, pos_end)

            pos_start = response.indexOf("location\":\"", pos_end) + 11
            pos_end = response.indexOf("\",\"", pos_start)
            val location = response.substring(pos_start, pos_end)

            pos_start = response.indexOf("userid\":", pos_end) + 8
            pos_end = response.indexOf(",\"", pos_start)
            val userid = response.substring(pos_start, pos_end)

            pos_start = response.indexOf("username\":\"", pos_end) + 11
            pos_end = response.indexOf("\"}", pos_start)
            val username = response.substring(pos_start, pos_end)

            attendanceList.add(Attendance(dept, location, userid.toLong(), username))
        }
        return attendanceList
    }

    fun getTopFiveAttendanceList(): ArrayList<Attendance> {
        val response = getTopFiveAttendancesResponse().body!!.string()
        val topFiveAttendanceList = ArrayList<Attendance>()
        var pos_start = 0
        var pos_end = 0
        while (true) {
            pos_start = response.indexOf("userid\":", pos_end) + 8
            if (pos_start <= pos_end || pos_start == -1) break
            pos_end = response.indexOf(",\"", pos_start)
            val userid = response.substring(pos_start, pos_end)

            pos_start = response.indexOf("username\":\"", pos_end) + 11
            pos_end = response.indexOf("\",", pos_start)
            val username = response.substring(pos_start, pos_end)

            pos_start = response.indexOf("dept\":\"", pos_end) + 7
            pos_end = response.indexOf("\",\"", pos_start)
            val dept = response.substring(pos_start, pos_end)

            pos_start = response.indexOf("location\":\"", pos_end) + 11
            pos_end = response.indexOf("\",\"", pos_start)
            val location = response.substring(pos_start, pos_end)

            pos_start = response.indexOf("allTime\":", pos_end) + 9
            pos_end = response.indexOf(",\"", pos_start)
            val allTime = response.substring(pos_start, pos_end).toDouble() * 60

            topFiveAttendanceList.add(
                Attendance(
                    dept,
                    location,
                    userid.toLong(),
                    username,
                    allTime
                )
            )
        }
        return topFiveAttendanceList
    }

    fun getStatus(id: Long): UserStatus {
        if (getAttendancesResponse().body!!.string().contains(id.toString()))
            return UserStatus.ONLINE
        return UserStatus.OFFLINE
    }

    private constructor()

    companion object {
        private val signMachine = SignAgent()

        @JvmStatic
        fun getAgent() = signMachine
    }
}

fun main() {
    val agent = SignAgent.getAgent()
    println(agent.signIn(1900420217))
//    println(agent.signIn("1900420217"))
//    println(agent.getAttendanceList()[0])
//    println(agent.getTopFiveAttendanceList()[1])
//    println(agent.postComplaint(1900420217).body!!.string())
}