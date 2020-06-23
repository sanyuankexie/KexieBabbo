package com.visualdust.kexiebabbo.agent

import okhttp3.OkHttpClient
import okhttp3.Response

class InterveneAgent private constructor() {
    private val httpAgent = HttpAgent.getAgent()

    enum class InterveneStatus {
        Pending, Ignored, Resolved
    }

    class Intervene(
        var requestId: Long,
        var generator_id: Long,
        var content: String,
        var status: InterveneStatus = InterveneStatus.Pending
    ) {
        override fun toString(): String {
            return "[$requestId][$status] by [$generator_id] : \"$content\""
        }
    }

//    fun postInterveneRequest(id: Long, content: String):Response? {
//        try {
//            return httpAgent.post(
//
//            )
//        }
//    }
}