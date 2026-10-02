package com.fasting.time.data.session

import android.content.Context

class SharedPreferencesSessionLogStore(context: Context) : SessionLogStore {
    private val preferences = context.getSharedPreferences("sessions", Context.MODE_PRIVATE)

    override fun read(): String? = preferences.getString(Key, null)

    override fun write(log: String) {
        preferences.edit().putString(Key, log).apply()
    }

    private companion object {
        const val Key = "log"
    }
}
