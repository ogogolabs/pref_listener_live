package com.ogogo_labs.pref_listener_live.datasource_processor

import com.ogogo_labs.pref_listener_live.Builder
import kotlinx.coroutines.CoroutineScope

interface DataHandlerProcessor {

    fun setUpdateDataListener(listener: (data: Builder.UpdatesObject) -> Unit)

    fun setWorkerScope(scope: CoroutineScope)

}
