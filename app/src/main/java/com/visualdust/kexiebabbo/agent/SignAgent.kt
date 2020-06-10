package com.visualdust.kexiebabbo.agent

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.lang.Exception

import com.visualdust.kexiebabbo.data.Resources as R

class SignAgent private constructor() {
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

    class timeList(
        var id: Long,
        var alltime: Double = 0.0
    ) {
        override fun toString(): String {
            return "[userId]$id,[alltime]$alltime"
        }
    }

    enum class UserStatus {
        ONLINE, OFFLINE, NETFAILURE
    }

    private fun post(url: String, body: String): Response? = client.newCall(
        Request.Builder().url(url).post(body.toRequestBody(R.JsonType)).build()
    ).execute()

    private fun get(url: String): Response? = client.newCall(
        Request.Builder().url(url).get().build()
    ).execute()

    fun postSignIn(id: Long): Response? {
        try {
            return post(R.serviceAddress + R.div + R.signInAPIName, "{\"userId\":\"$id\"}")
        } catch (ioe: IOException) {
            throw ioe
        } catch (e: Exception) {
            throw e
        }
    }


    fun signIn(id: Long): Boolean? {
        try {
            return postSignIn(id)!!.body!!.string().contains("成功")
        } catch (ioe: IOException) {
            throw ioe
        } catch (e: Exception) {
            throw e
        }
    }

    fun postSignOut(id: Long): Response? {
        try {
            return post(R.serviceAddress + R.div + R.signOutAPIName, "{\"userId\":\"$id\"}")
        } catch (ioe: IOException) {
            throw ioe
        } catch (e: Exception) {
            throw e
        }
    }


    fun signOut(id: Long): Boolean? {
        try {
            return postSignOut(id)!!.body!!.string().contains("成功")
        } catch (ioe: IOException) {
            throw ioe
        } catch (e: Exception) {
            throw e
        }
    }

    fun postComplaint(id: Long): Response? {
        try {
            return post(R.serviceAddress + R.div + R.complaintAPIName, "{\"userId\":\"$id\"}")
        } catch (ioe: IOException) {
            throw ioe
        } catch (e: Exception) {
            throw e
        }
    }


    fun complaint(id: Long): Boolean {
        try {
            return postComplaint(id)!!.body!!.string().contains("成功")
        } catch (ioe: IOException) {
            throw ioe
        } catch (e: Exception) {
            throw e
        }
    }

    private fun getAttendancesResponse(): Response? {
        try {
            return get(R.serviceAddress + R.div + R.attendancesListAPIName)
        } catch (ioe: IOException) {
            throw ioe
        } catch (e: Exception) {
            throw e
        }
    }

    private fun getTopFiveAttendancesResponse(): Response? {
        try {
            return get(R.serviceAddress + R.div + R.rankTopFiveAttendanceAPIName)
        } catch (ioe: IOException) {
            throw ioe
        } catch (e: Exception) {
            throw e
        }
    }

    private fun getTimeResponse(id: Long): Response? {
        try {
            return get(R.serviceAddress + R.div + R.timeAPIName + "?userId=${id}")
        } catch (ioe: IOException) {
            throw ioe
        } catch (e: Exception) {
            throw e
        }
    }

    fun getAttendanceList(): ArrayList<Attendance>? {
        try {
            val response = getAttendancesResponse()!!.body!!.string()
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

                attendanceList.add(
                    Attendance(
                        dept,
                        location,
                        userid.toLong(),
                        username,
                        getTime(userid.toLong()) ?: 0.0
                    )
                )
            }
            return attendanceList
        } catch (e: Exception) {
            return ArrayList()
        }
    }

    fun getTopFiveAttendanceList(): ArrayList<Attendance>? {
        try {
            val response = getTopFiveAttendancesResponse()!!.body!!.string()
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
        } catch (e: Exception) {
            return ArrayList()
        }
    }

    fun getStatus(id: Long): UserStatus {
        try {
            if (getAttendancesResponse()!!.body!!.string().contains(id.toString()))
                return UserStatus.ONLINE
            return UserStatus.OFFLINE
        } catch (ioe: IOException) {
            return UserStatus.NETFAILURE
        } catch (e: Exception) {
            return UserStatus.NETFAILURE
        }
    }

    fun getTime(id: Long): Double? {
        return try {
            val response = getTimeResponse(id)!!.body!!.string()
            var pos_start = 0
            var pos_end = 0
            pos_start = response.indexOf("alltime\":") + 9
            pos_end = response.indexOf("},", pos_start)
            val alltime = response.substring(pos_start, pos_end).toDouble() * 60
            alltime
        } catch (ioe: IOException) {
            throw ioe
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private val signMachine = SignAgent()

        @JvmStatic
        fun getAgent() = signMachine
    }
}

fun main() {
    val agent = SignAgent.getAgent()
    println(agent.signIn(1900301236))
    println(agent.getTime(1900301236))
//    println(agent.signIn("1900420217"))
//    println(agent.getAttendanceList()[0])
//    println(agent.getTopFiveAttendanceList()[1])
//    println(agent.postComplaint(1900420217).body!!.string())
}