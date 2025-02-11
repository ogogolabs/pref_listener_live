package com.ogogo_labs.pref_listener_live

import android.Manifest
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.ogogo_labs.pref_listener_live.datasource_processor.DataSourceProcessor
import com.ogogo_labs.pref_listener_live.datasource_processor.SharedPreferencesProcessor
import com.ogogo_labs.pref_listener_live.sql.DataUpdateEvent
import com.ogogo_labs.pref_listener_live.sql.PrefListenerDatabase
import com.ogogo_labs.pref_listener_live.sql.toDTO
import com.ogogo_labs.pref_listener_live.utils.Logger.logD
import com.ogogo_labs.pref_listener_live.utils.Logger.logP
import com.ogogo_labs.pref_listener_live.utils.NetworkMonitoringUtil
import com.ogogo_labs.pref_listener_live.utils.PrefUtil
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.Executors

const val PORT = 55690
const val development = true

object PrefListener {
    //--- manageSDK
    private var sdkInited = false
    private var deviceID: String = ""
    private var connectionIP: String = ""
    private val mDelay = if (development) {
        1500L
    } else {
        4L
    }

    //--- App vars
    private var appContext: Context? = null

    private var prefUtil: PrefUtil? = null

    private var networkStateHolder: NetworkMonitoringUtil? = null

    private var executor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val scope =
        CoroutineScope(SupervisorJob() + executor + CoroutineExceptionHandler { _, exception ->
            logD(exception.stackTraceToString())
        })


    val isDebuggable: Boolean
        get() {
            return appContext?.let {
                0 != it.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
            }?.run {
                false
            }!!
        }


    // database
    private var database: PrefListenerDatabase? = null

    fun init(
        ctx: Context
    ) {
        System.out.println("PrefListenerDEBUG init")
        appContext = ctx.applicationContext
        logD("Bla Bla Bla start init")
        if (sdkInited) {
            return
        }

        prefUtil = PrefUtil(ctx)

        database = PrefListenerDatabase.getDatabase(ctx)

        if (checkPermissions(ctx)) {
            networkStateHolder = NetworkMonitoringUtil(appContext!!).apply {
                registerNetworkCallbackEvents()
                checkNetworkState()
            }
        } else {
            logD("There is not permission for INTERNET")
        }

        scope.launch {
            networkStateHolder?.networkState?.collectLatest { isConnected ->
                logD("Network state $isConnected")
                if (isConnected) {
                    connectFromNetworkStateChange()
                }
            }
        }

        connectionIP = prefUtil?.ip.orEmpty()
        deviceID = prefUtil?.deviceId.orEmpty()

        SharedPreferencesProcessor.setWorkerScope(scope)
        DataSourceProcessor.setWorkerScope(scope)
        SharedPreferencesProcessor.setUpdateDataListener(PrefListener::saveNewEvent)
        DataSourceProcessor.setUpdateDataListener(PrefListener::saveNewEvent)

        scope.launch {

            database?.let { db ->
                combine(
                    db.dataDao().getLastEvent().distinctUntilChanged(), NativeSocket.socketState
                ) { dataList, networkState ->
                    dataList to networkState
                }.collect { data ->
                    logD("distinctUntilChanged:  $data")
                    data.first?.let { item ->
                        if (sendMessageNativeThread(item.toDTO())) {
                            deleteEventFromDB(item)
                        }
                    }
                }
            }
        }

        sdkInited = true
    }


    private fun deleteEventFromDB(event: DataUpdateEvent) {
        scope.launch {
            delay(mDelay)
            database?.dataDao()?.deleteEvent(event)
        }
    }

    fun connectFromReceiver(deviceID: String, ip: String? = "") {

//        PrefListener.deviceID = deviceID
        prefUtil?.deviceId = deviceID

        ip?.trim()!!.let {
            connectionIP = it
            prefUtil?.ip = it
        }

        runSocket()
    }

    private fun connectFromNetworkStateChange() {
        runSocket()
    }

    fun addSharedPreferencesSource(fileName: String) {
        appContext?.let { ctx ->
            SharedPreferencesProcessor.setSourceFileName(context = ctx, filename = fileName)
        } ?: run {
            logP("Before run this method, run PrefListener.init(context)")
        }
    }

    fun addDatastoreSource(dataStoreAliasName: String, dataStore: DataStore<Preferences>) {
        appContext?.let {
            DataSourceProcessor.setDataStore(
                datastore = dataStore, aliasSourceName = dataStoreAliasName
            )
        }?.run {
            logP("Before run this method, run PrefListener.init(context)")
        }
    }

    private fun runSocket() {

        if (deviceID.isEmpty()) {
            logD("connectFromNetworkStateChange deviceID is empty")
            return
        }

        if (connectionIP.isEmpty()) {
            logD("IP is empty, can't run socket")
            return
        }

        if (appContext != null && checkPermissions(appContext!!) && networkStateHolder?.checkNetworkState() == false) {
            logD("startSocket: networkStatus ${networkStateHolder?.networkState?.value} return")
            return
        }

        NativeSocket.createSocket(connectionIP, PORT) {
            logD("startSocket: Socket is starting")
        }
    }

    private fun sendMessageNativeThread(message: Builder.UpdatesObject): Boolean {
        if (NativeSocket.socketState.value != SocketState.CONNECTED) {
            logD("-----socketClient not connected -----")
            return false
        }

        val isSuccess = NativeSocket.sendMessage(
            Json.encodeToString(
                message.copy(
                    deviceId = deviceID, project = appContext?.packageName.orEmpty()
                )
            )
        )

        return isSuccess
    }

    private fun saveNewEvent(message: Builder.UpdatesObject) {

        if (message.data.isEmpty()) {
            return
        }

        scope.launch {
            database?.dataDao()?.insertEvent(
                DataUpdateEvent(
                    id = null,
                    sourceName = message.sourceName,
                    sourceType = message.source,
                    data = message.data.toString(),
                    timestamp = message.timestamp,
                    reConnection = false
                )
            )
        }
    }

    private fun checkPermissions(ctx: Context): Boolean {
        return ctx.checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED && ctx.checkSelfPermission(
            Manifest.permission.ACCESS_NETWORK_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }
}