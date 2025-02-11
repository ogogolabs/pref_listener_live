package com.ogogo_labs.pref_listener_live.datasource_processor

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ogogo_labs.pref_listener_live.Builder
import com.ogogo_labs.pref_listener_live.utils.Logger.logD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

const val prefLoaderDataStoreIdentifierPrefix = "PREF_LOADER_DATASTORE_IDENTIFIER_PREFIX_"

object DataSourceProcessor : DataHandlerProcessor {

    private var onDataUpdate: ((data: Builder.UpdatesObject) -> Unit)? = null


    private var coroutineScope: CoroutineScope? = null


    private val dataStoreNamesMap = mutableMapOf<DataStore<Preferences>, String>()
    private val dataStoreValuesMap = mutableMapOf<String, Map<Preferences.Key<*>, Any>>()


    fun setDataStore(datastore: DataStore<Preferences>, aliasSourceName: String) {

        if (aliasSourceName.isBlank()) {
            logD("AliasSourceName is empty")
            return
        }

        if (dataStoreNamesMap.containsValue(value = aliasSourceName)) {
            logD("Key: ${aliasSourceName} used, skip adding")
            return
        }

        if (dataStoreNamesMap.containsKey(key = datastore)) {
            logD("Datastore: ${datastore} used, skip adding")
            return
        }

        dataStoreNamesMap[datastore] = aliasSourceName
        logD("Datastore: Alias ${prefLoaderDataStoreIdentifierPrefix + aliasSourceName}")
        dataStoreValuesMap[prefLoaderDataStoreIdentifierPrefix + aliasSourceName] = mutableMapOf()

        coroutineScope?.launch {
            datastore.data.map { it }.collect { pref ->
                if (pref.asMap().isEmpty()) {
                    logD("map Is Empty")
                }

                buildAndSendPrefData(pref, false)

                cancel()
            }
        }

        coroutineScope?.launch {
            datastore.edit {
                it[stringPreferencesKey(prefLoaderDataStoreIdentifierPrefix)] =
                    prefLoaderDataStoreIdentifierPrefix + aliasSourceName
            }
        }

        coroutineScope?.launch {
            datastore.data.map { it }.collect { pref ->
                buildAndSendPrefData(pref, false)
            }
        }
    }

    override fun setUpdateDataListener(listener: (data: Builder.UpdatesObject) -> Unit) {
        onDataUpdate = listener
    }

    override fun setWorkerScope(scope: CoroutineScope) {
        coroutineScope = scope
    }

    private fun buildAndSendPrefData(
        pref: Preferences,
        isReconnection: Boolean = false // isReconnection - don't touch this value
    ) {
        val sourceName = pref.asMap().entries.firstOrNull { element ->
            element.key == stringPreferencesKey(prefLoaderDataStoreIdentifierPrefix)
        }?.value.toString()

        if (sourceName == "null") {
            return
        }

        // calculation


        val prevMap = dataStoreValuesMap[sourceName]!!.toMutableMap()
        val currentMap = pref.asMap()

        val keys = prevMap.keys + currentMap.keys
        val deleted = mutableMapOf<Preferences.Key<*>, Any?>()
        val edited = mutableMapOf<Preferences.Key<*>, Any?>()
        val added = mutableMapOf<Preferences.Key<*>, Any?>()

        keys.forEach { key ->
            if (prevMap.containsKey(key) && !currentMap.containsKey(key)) {
                deleted[key] = null
            } else if (!prevMap.containsKey(key) && currentMap.containsKey(key)) {
                added[key] = currentMap[key]
            } else if (prevMap[key] != currentMap[key]) {
                edited[key] = currentMap[key]
            } else {
                logD("Diff ___ unexpected: ${prevMap[key] == currentMap[key]}")
            }
        }

        dataStoreValuesMap[sourceName] = pref.asMap().toMutableMap()


        val message1 = Builder(
            source = Builder.SOURCE.DATASTORE,
            sourceName = sourceName,
            reConnection = isReconnection
        ).also { builder ->

            (added + edited + deleted).let {
                it.forEach { item ->
                    builder.putValue(item.key.name, item.value)
                }
            }

        }.build()

        onDataUpdate?.invoke(message1)

    }
}
