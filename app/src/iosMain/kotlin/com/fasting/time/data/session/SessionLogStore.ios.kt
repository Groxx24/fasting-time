package com.fasting.time.data.session

import platform.Foundation.NSUserDefaults

class UserDefaultsSessionLogStore : SessionLogStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun read(): String? = defaults.stringForKey(Key)

    override fun write(log: String) {
        defaults.setObject(log, forKey = Key)
    }

    private companion object {
        const val Key = "session_log"
    }
}
