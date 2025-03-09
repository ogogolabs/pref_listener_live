package com.ogogo_labs.pref_listener_live

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

object PrefListener {

    private var worker: WorkerWrapper? = null
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
            System.err.println("buildTypeRequested $buildTypeRequested")
            if(isDebuggable) {
                // allow work in debug mode
            } else {
                if (buildTypeRequested != BuildType.DEBUG) {
                    throw Error("""Do you relly wont to use this library in release version? 
                                   Use com.ogogo_labs.pref_listener.build_type as a configuration parameter in "manifestPlaceholder"!""".trimIndent())
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

//        worker =  try {
//            className.let {
//                val clazz = Class.forName(it)
//                val constructor = clazz.getConstructor(Context::class.java)
//                constructor.newInstance(ctx) as com.ogogo_labs.pref_listener.core.WorkerWrapper
//            }
//        } catch (e: Exception) {
//            println(e.printStackTrace())
//            null
//        }
        worker = WorkerWrapper(ctx)

    }

    private fun getBuildTypeRequested(ctx: Context):BuildType{
        val buildTypeRequested = try {
            val appContext = ctx.applicationContext
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appContext.packageManager.getApplicationInfo(
                    appContext.packageName,
                    PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                appContext.packageManager.getApplicationInfo(
                    appContext.packageName,
                    PackageManager.GET_META_DATA
                )
            }
            if(appInfo.metaData == null){
                println("appInfo.metaData == null")
            }
            appInfo.metaData?.getString("com.ogogo_labs.pref_listener.build_type") ?: "undefined"

        } catch (e: Exception) {
            println(e.printStackTrace())
            "undefined"
        }


        System.err.println("com.ogogo_labs.pref_listener.build_type: ${buildTypeRequested}")

        return when (buildTypeRequested) {
            "debug", "undefined" -> BuildType.DEBUG
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