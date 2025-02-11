package com.ogogo_labs.pref_listener_live.datasource_processor

import android.content.Context
import android.content.SharedPreferences
import com.ogogo_labs.pref_listener_live.Builder
import com.ogogo_labs.pref_listener_live.utils.Logger.logD
import kotlinx.coroutines.CoroutineScope

object SharedPreferencesProcessor : DataHandlerProcessor {

    private var onDataUpdate: ((data: Builder.UpdatesObject) -> Unit)? = null
    private val mapSharedPreferences = mutableMapOf<String, SharedPreferences>()
    private var listListener = mutableListOf<SharedPreferencesListener>()

    fun setSourceFileName(context: Context, filename: String) {

        if (filename.isBlank()) {
            logD("Filename can't be empty")
            return
        }

        if (mapSharedPreferences.contains(filename)) {
            logD("Always listened")
            return
        }

        val prefListener = SharedPreferencesListener(filename)
        listListener.add(prefListener)

        context.getSharedPreferences(filename, Context.MODE_PRIVATE)?.let {
            it.registerOnSharedPreferenceChangeListener(
                prefListener
            )
            logD("register listener for filename: $filename")
            mapSharedPreferences[filename] = it
        }
    }

    override fun setUpdateDataListener(listener: (data: Builder.UpdatesObject) -> Unit) {
        onDataUpdate = listener
    }

    override fun setWorkerScope(scope: CoroutineScope) {
        // no need implementation
    }

    private fun buildAndSendPrefData(
        pref: SharedPreferences, key: String?, sourceName: String, isReconnection: Boolean = false // don't touch this val
    ) {
        val message = Builder(
            source = Builder.SOURCE.SHARED_PREFERENCES,
            sourceName = sourceName,
            reConnection = isReconnection
        ).also { builder ->
            if (isReconnection) {
                pref.all.forEach { entry -> builder.putValue(entry.key, entry.value) }
            } else {
                key?.let {
                    if (pref.contains(key)) {
                        builder.putValue(key, pref.all[key])
                    } else {
                        builder.putValue(key, null)
                    }
                }
            }
        }.build()
        onDataUpdate?.invoke(message)
    }

    private class SharedPreferencesListener(private val sourceName: String) :
        SharedPreferences.OnSharedPreferenceChangeListener {

        override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences, key: String?
        ) {
            logD("PrefLoaderSharedPreferencesListener: onSharedPreferenceChanged")
            buildAndSendPrefData(
                pref = sharedPreferences, key = key, sourceName = sourceName, false
            )
        }
    }
}
