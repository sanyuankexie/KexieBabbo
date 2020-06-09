package com.visualdust.kexiebabbo.agent

import okhttp3.Response
import java.io.IOException
import java.lang.Exception
import java.util.function.Consumer

class NonblockingSignAgent {

    fun handleSignIn(id: Long, consumer: Consumer<Boolean?>) = Thread {
        consumer.accept(agent.signIn(id))
    }.start()

    fun handleSignInResponse(id: Long, consumer: Consumer<Response?>) = Thread {
        consumer.accept(agent.postSignIn(id))
    }.start()

    fun handleSignOut(id: Long, consumer: Consumer<Boolean?>) = Thread {
        consumer.accept(agent.signOut(id))
    }.start()

    fun handleComplaintResponse(id: Long, consumer: Consumer<Response?>) = Thread {
        consumer.accept(agent.postComplaint(id))
    }.start()

    fun handleComplaint(id: Long, consumer: Consumer<Boolean?>) = Thread {
        consumer.accept(agent.complaint(id))
    }.start()

    fun handleSignOutResponse(id: Long, consumer: Consumer<Response?>) = Thread {
        consumer.accept(agent.postSignOut(id))
    }.start()

    fun handleAttendance(consumer: Consumer<ArrayList<SignAgent.Attendance>?>) = Thread {
        consumer.accept(agent.getAttendanceList())
    }.start()

    fun handleStatus(id: Long, consumer: Consumer<SignAgent.UserStatus>) = Thread {
        consumer.accept(agent.getStatus(id))
    }.start()

    fun handleTopFives(consumer: Consumer<ArrayList<SignAgent.Attendance>?>) = Thread {
        consumer.accept(agent.getTopFiveAttendanceList())
    }.start()

    fun handleTime(id: Long, consumer: Consumer<Double>) = Thread{
        consumer.accept(agent.getTime(id))
    }.start()

    private constructor()

    companion object {
        private val agent = SignAgent.getAgent()
        private val nbAgent = NonblockingSignAgent()
        fun getAgent() = nbAgent
    }
}