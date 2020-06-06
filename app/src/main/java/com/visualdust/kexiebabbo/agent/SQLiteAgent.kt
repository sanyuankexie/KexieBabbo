package com.visualdust.kexiebabbo.agent

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.visualdust.kexiebabbo.data.Resources as R

class SQLiteAgent(context: Context) :
    SQLiteOpenHelper(context, DBName, null, DBVersion) {

    override fun onCreate(db: SQLiteDatabase?) {
        val queue =
            "create table if not exists $DBName (userid text)"
        db!!.execSQL(queue)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val sql = "DROP TABLE IF EXISTS $DBName"
        db!!.execSQL(sql)
        onCreate(db)
    }

    companion object {
        const val DBName = "${R.projectSpace}.${R.appName}"
        const val DBVersion = 1
    }
}