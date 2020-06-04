package com.visualdust.kexiebabbo

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response


class SignMachine {
    private val client = OkHttpClient()
    private var lastPostResponse: Response? = null

    private fun post(url: String, json: String): String {
        val request: Request = Request.Builder()
            .url(url)
            .post(json.toRequestBody(JSON))
            .build()
        client.newCall(request).execute().use {
            lastPostResponse = it
            return it.body!!.string()
        }
    }

    fun logout(id: String): Boolean =
        post("http://123.56.2.196:8080/kexie/signOut", "{\"userId\":\"$id\"}").contains("成功")

    fun login(id: String): Boolean =
        post("http://123.56.2.196:8080/kexie/signIn", "{\"userId\":\"$id\"}").contains("成功")

    companion object {
        val JSON = "application/json; charset=utf-8".toMediaType()
    }
}

fun main() {
//    println(SignMachine().logout("1900420217"))
//    println(com.visualdust.kexiebabo.SignMachine().login("1900420217"))
}