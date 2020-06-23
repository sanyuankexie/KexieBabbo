package com.visualdust.kexiebabbo.agent

import com.visualdust.kexiebabbo.data.Resources
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class HttpAgent private constructor() {
    private val client = OkHttpClient()

    public fun post(url: String, body: String): Response? = client.newCall(
        Request.Builder().url(url).post(body.toRequestBody(Resources.JsonType)).build()
    ).execute()

    public fun get(url: String): Response? = client.newCall(
        Request.Builder().url(url).get().build()
    ).execute()

    companion object {
        private val agent = HttpAgent()
        fun getAgent() = agent
    }
}