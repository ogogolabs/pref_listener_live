package com.ogogo_labs.pref_listener_live.utils

import android.util.Log
import com.ogogo_labs.pref_listener_live.PrefListener
import com.ogogo_labs.pref_listener_live.development

object Logger {

    private const val TAG = "PrefListener"

    fun logD(message: String) {
        if (PrefListener.isDebuggable && development) {
            Log.d(TAG, message)
        }
    }

    fun logP(message: String) {
        if (PrefListener.isDebuggable) {
            Log.d(TAG, message)
        }
    }

}
