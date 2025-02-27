package com.example.filetracking.utils

import android.content.Context
import android.content.SharedPreferences

class SharePref {
    private var sharedPreferences: SharedPreferences
    private var editor: SharedPreferences.Editor
    private var PRIVATE_MODE = 0

    private var instance: SharePref?= null

    fun get(): SharePref? {
        if (instance == null) {
            instance = SharePref()
        }
        return instance
    }


    fun clearAll() {
        editor.clear()
        editor.commit()
    }




    fun logOut() {
        val editor = sharedPreferences.edit()
        editor?.clear()
        editor?.apply()
    }

    fun saveData(key: String, value: String) {
        val prefsEditor: SharedPreferences.Editor = sharedPreferences.edit()
        with(prefsEditor) {
            this.putString(key, value)
            this.commit()
        }
    }


    fun getData(key: String): String? {
        return sharedPreferences.getString(key, null)

    }

//    fun <T>getData1(key: String?, defValue: T): T {
//        val returnValue = sharedPreferences.all[key] as T?
//        return returnValue ?: defValue
//
//    }

//    operator fun <T> get(key: String?, defValue: T): T {
//        val returnValue = sharedPreferences.all[key] as T?
//        return returnValue ?: defValue
//    }




    companion object {
        private const val PREF_NAME = "prefname"
        private var instance: SharePref? = null
        fun get(): SharePref {
            if (instance == null) {
                instance = SharePref()
            }
            return instance as SharePref
        }
    }

    init {
        sharedPreferences = App.get().getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = sharedPreferences.edit()
        editor.apply()
    }
}
