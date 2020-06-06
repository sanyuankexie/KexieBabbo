package com.visualdust.kexiebabbo.agent

import okhttp3.Response
import java.util.function.Consumer

class NonblockingSignAgent {

    fun handleSignIn(id: Long, consumer: Consumer<Boolean>) = Thread {
        consumer.accept(agent.signIn(id))
    }.start()

    fun handleSignInResponse(id: Long, consumer: Consumer<Response>) = Thread {
        consumer.accept(agent.postSignIn(id))
    }.start()

    fun handleSignOut(id: Long, consumer: Consumer<Boolean>) = Thread {
        consumer.accept(agent.signOut(id))
    }.start()

    fun handleSignOutResponse(id: Long, consumer: Consumer<Response>) = Thread {
        consumer.accept(agent.postSignOut(id))
    }.start()

    fun handleAttendance(consumer: Consumer<ArrayList<SignAgent.Attendance>>) = Thread {
        consumer.accept(agent.getAttendanceList())
    }.start()

    fun handleTopFives(consumer: Consumer<ArrayList<SignAgent.Attendance>>) = Thread {
        consumer.accept(agent.getTopFiveAttendanceList())
    }.start()

    private constructor()

    companion object {
        private val agent = SignAgent.getAgent()
        private val nbAgent = NonblockingSignAgent()
        fun getAgent() = nbAgent
    }
}