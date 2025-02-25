package com.ogogo_labs.pref_listener_live

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

object PrefListener {

    private var worker: com.ogogo_labs.pref_listener.core.WorkerWrapper? = null
    private var appContext: Context? = null
    private var sdkInitialized = false

    private val isDebuggable: Boolean
        get() {
            return appContext?.let {
                0 != it.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
            } ?: kotlin.run {
                false
            }
        }

    fun init(
        ctx: Context
    ) {
        synchronized(this) {

            if (sdkInitialized) {
                return
            }

            appContext = ctx

            val buildTypeRequested = getBuildTypeRequested(ctx)
            if(isDebuggable) {
                // allow work in debug mode
            } else {
                if (buildTypeRequested != BuildType.DEBUG) {
                    throw Error("Do you relly wont to use this library in release version? Use BUILD_TYPE_IMPLEMENTATION as a configuration parameter!")
                }
            }

            getWorker(ctx)

            sdkInitialized = true
        }
    }

    private fun getWorker(ctx: Context) {
        val buildTypeRequested = getBuildTypeRequested(ctx)

        val className = if (buildTypeRequested == BuildType.DEBUG) {
            "com.ogogo_labs.pref_listener.debug.WorkerWrapper"
        } else {
            "com.ogogo_labs.pref_listener.prod.WorkerWrapper"
        }

        worker =  try {
            className.let {
                val clazz = Class.forName(it)
                val constructor = clazz.getConstructor(Context::class.java)
                constructor.newInstance(ctx) as com.ogogo_labs.pref_listener.core.WorkerWrapper
            }
        } catch (e: Exception) {
            println(e.printStackTrace())
            null
        }

    }

    private fun getBuildTypeRequested(ctx: Context):BuildType{
        val buildTypeRequested = try {
            val appContext = ctx.applicationContext
            val appInfo = appContext.packageManager.getApplicationInfo(
                appContext.packageName,
                PackageManager.GET_META_DATA
            )
            appInfo.metaData?.getString("BUILD_TYPE_IMPLEMENTATION") ?: "undefined"
        } catch (e: Exception) {
            println(e.printStackTrace())
            "undefined"
        }

        return when (buildTypeRequested) {
            "debug" -> BuildType.DEBUG
            "release" -> BuildType.RELEASE
            else -> {
                if (isDebuggable) {
                    BuildType.DEBUG
                } else {
                    BuildType.RELEASE
                }
            }
        }
    }

    fun connectFromReceiver(deviceID: String, ip: String? = "") {
        if (sdkInitialized) {
            worker?.connectFromReceiver(deviceID, ip)
            worker?.let {
                println("${it::class.java}")
            }
            println("PrefListener Initialized")
        } else {
            println("PrefListener NOT Initialized")
        }
    }

    fun addSharedPreferencesSource(fileName: String) {
        if (sdkInitialized) {
            worker?.addSharedPreferencesSource(fileName)
        } else {
            println("PrefListener NOT Initialized addSharedPreferencesSource")
        }
    }

    fun addDatastoreSource(dataStoreAliasName: String, dataStore: DataStore<Preferences>) {
        if (sdkInitialized) {
            worker?.addDatastoreSource(dataStoreAliasName, dataStore)
        } else {
            println("PrefListener NOT Initialized addDatastoreSource")
        }
    }

}

private enum class BuildType {
    DEBUG, RELEASE
}