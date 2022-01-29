package com.example.todoapp.utilService

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

class SharedPreferenceClass(context: Context) {
    private val USER_PREF = "user_todo"
    private val appShared: SharedPreferences
    private val prefsEditor: SharedPreferences.Editor

    init {
        appShared = context.getSharedPreferences(USER_PREF, Activity.MODE_PRIVATE)
        this.prefsEditor = appShared.edit()
    }

    //Integer
    fun getValueInt(key: String): Int{
        return appShared.getInt(key, 0)
    }

    fun setValueInt(key: String, value: Int) {
        prefsEditor.putInt(key, value)
    }

    // string
    fun getValueString(key: String?): String? {
        return appShared.getString(key, "")
    }

    fun setValueString(key: String?, value: String?) {
        prefsEditor.putString(key, value).commit()
    }


    // boolean
    fun getValueBoolean(key: String?): Boolean {
        return appShared.getBoolean(key, false)
    }

    fun setValueBoolean(key: String?, value: Boolean) {
        prefsEditor.putBoolean(key, value).commit()
    }

    fun clear() {
        prefsEditor.clear().commit()
    }
}