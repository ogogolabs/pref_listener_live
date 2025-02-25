package com.ogogo_labs.pref_listener.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

interface WorkerWrapper {
    var isDebuggable: Boolean
    fun addDatastoreSource(dataStoreAliasName: String, dataStore: DataStore<Preferences>)
    fun addSharedPreferencesSource(fileName: String)
    fun connectFromReceiver(deviceID: String, ip: String? = "")
}