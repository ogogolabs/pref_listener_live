package com.ogogo_labs.pref_listener.debug.datasource_processor

import kotlinx.coroutines.CoroutineScope

interface DataHandlerProcessor {

    fun setUpdateDataListener(listener: (data: Builder.UpdatesObject) -> Unit)

    fun setWorkerScope(scope: CoroutineScope)

}
