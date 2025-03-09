package com.ogogo_labs.pref_listener_live.utils

import android.util.Log
import com.ogogo_labs.pref_listener_live.development

object Logger {

    private const val TAG = "PrefListener"

    fun logD(message: String) {
        if (development) {
            Log.d(TAG, message)
        }
    }

}
