package com.example.myapplication

import android.app.Application
import com.ogogo_labs.pref_listener_live.PrefListener

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PrefListener.init(this@MyApplication)
    }
}