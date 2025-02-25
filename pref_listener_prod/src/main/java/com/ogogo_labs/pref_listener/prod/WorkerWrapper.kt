package com.ogogo_labs.pref_listener.prod

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

class WorkerWrapper(private val ctx: Context) :
    com.ogogo_labs.pref_listener.core.WorkerWrapper {

    override var isDebuggable: Boolean
        get() {
            return ctx.let {
                0 != it.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
            }
        }
        set(value) {
        }

    override fun addDatastoreSource(
        dataStoreAliasName: String, dataStore: DataStore<Preferences>
    ) {
        //---
    }

    override fun addSharedPreferencesSource(fileName: String) {
        //---
    }

    override fun connectFromReceiver(deviceID: String, ip: String?) {
        //---
    }
}