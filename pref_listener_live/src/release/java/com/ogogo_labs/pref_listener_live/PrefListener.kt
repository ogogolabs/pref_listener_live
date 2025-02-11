package com.ogogo_labs.pref_listener_live

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

object PrefListener {

    fun init(
        ctx: Context
    ) {
        System.out.println("PrefListenerLIVE init")
    }

    fun addSharedPreferencesSource(fileName: String) {
        // ---
    }

    fun addDatastoreSource(dataStoreAliasName: String, dataStore: DataStore<Preferences>) {
        // ---
    }
}